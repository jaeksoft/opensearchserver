/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2013 Emmanuel Keller / Jaeksoft
 * 
 * http://www.open-search-server.com
 * 
 * This file is part of OpenSearchServer.
 *
 * OpenSearchServer is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * OpenSearchServer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with OpenSearchServer. 
 *  If not, see <http://www.gnu.org/licenses/>.
 **/

package com.jaeksoft.searchlib.script;

import java.io.Closeable;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.apache.commons.io.IOUtils;

import com.jaeksoft.pojodbc.Query;
import com.jaeksoft.pojodbc.Transaction;
import com.jaeksoft.pojodbc.connection.JDBCConnection;
import com.jaeksoft.searchlib.Logging;
import com.jaeksoft.searchlib.config.Config;
import com.jaeksoft.searchlib.crawler.database.DatabaseCrawlSql.SqlUpdateMode;
import com.jaeksoft.searchlib.crawler.database.IsolationLevelEnum;
import com.jaeksoft.searchlib.scheduler.TaskLog;
import com.jaeksoft.searchlib.util.DatabaseUtils;

public class DatabaseScript implements Closeable {

	private final static String COLUMN_ID = "id";
	private final static String COLUMN_COMMAND = "command";
	private final static String COLUMN_PARAM = "param";

	private final JDBCConnection connectionManager;

	private final IsolationLevelEnum isolationLevel;

	private final String sqlVariable;

	private final String varColumnName;

	private final String varColumnValue;

	private final String sqlSelect;

	private final String sqlUpdate;

	private final SqlUpdateMode sqlUpdateMode;

	private final ScriptCommandContext scriptCommandContext;

	private Transaction transaction;

	private HashMap<String, String> variablesMap;

	public DatabaseScript(Config config, String driverClass, String jdbcURL,
			String username, String password,
			IsolationLevelEnum isolationLevel, String sqlVariable,
			String varColumnName, String varColumnValue, String sqlSelect,
			String sqlUpdate, SqlUpdateMode sqlUpdateMode, TaskLog taskLog)
			throws InstantiationException, IllegalAccessException,
			ClassNotFoundException {
		connectionManager = new JDBCConnection();
		connectionManager.setDriver(driverClass);
		connectionManager.setUrl(jdbcURL);
		connectionManager.setUsername(username);
		connectionManager.setPassword(password);
		this.isolationLevel = isolationLevel;
		this.sqlVariable = sqlVariable;
		this.varColumnName = varColumnName;
		this.varColumnValue = varColumnValue;
		this.sqlSelect = sqlSelect;
		this.sqlUpdate = sqlUpdate;
		this.sqlUpdateMode = sqlUpdateMode;
		scriptCommandContext = new ScriptCommandContext(config, taskLog);
		transaction = null;
		variablesMap = null;
	}

	private final String doVariableReplacement(String text) {
		if (variablesMap == null || text == null)
			return text;
		for (Map.Entry<String, String> entry : variablesMap.entrySet())
			text = text.replace(entry.getKey(), entry.getValue());
		return text;
	}

	public void run() throws SQLException, ScriptException {
		try {
			transaction = connectionManager.getNewTransaction(false,
					isolationLevel.value);

			// Load variables
			if (sqlVariable != null && sqlVariable.length() > 0) {
				variablesMap = new HashMap<String, String>();
				Query query = transaction.prepare(sqlVariable);
				ResultSet resultSet = query.getResultSet();
				while (resultSet.next()) {
					String name = resultSet.getString(varColumnName);
					String value = resultSet.getString(varColumnValue);
					if (name == null || value == null)
						continue;
					StringBuffer sb = new StringBuffer();
					sb.append('{');
					sb.append(name);
					sb.append('}');
					variablesMap.put(sb.toString(), value);
				}
			}

			String sqlU = doVariableReplacement(sqlUpdate);
			String sqlS = doVariableReplacement(sqlSelect);
			Query query = transaction.prepare(sqlS);
			ResultSet resultSet = query.getResultSet();

			ResultSetMetaData metaData = resultSet.getMetaData();
			TreeSet<String> columns = new TreeSet<String>();
			int paramCount = 0;
			int columnCount = metaData.getColumnCount();
			for (int i = 1; i <= columnCount; i++)
				columns.add(metaData.getColumnLabel(i));
			for (int i = 1; i <= columnCount; i++)
				if (columns.contains(COLUMN_PARAM + i))
					paramCount = i;
			List<String> pkList = sqlUpdateMode == SqlUpdateMode.PRIMARY_KEY_CHAR_LIST
					|| sqlUpdateMode == SqlUpdateMode.PRIMARY_KEY_CHAR_LIST ? new ArrayList<String>(
					0) : null;
			CommandEnum commandFinder = null;
			while (resultSet.next()) {
				String id = resultSet.getString(COLUMN_ID);
				if (pkList != null)
					pkList.add(id);
				String command = resultSet.getString(COLUMN_COMMAND);
				String[] parameters = null;
				if (paramCount > 0) {
					parameters = new String[paramCount];
					for (int i = 0; i < paramCount; i++) {
						Object o = resultSet.getObject(COLUMN_PARAM + (i + 1));
						if (o == null)
							continue;
						parameters[i] = doVariableReplacement(o.toString());
					}
				}
				CommandEnum commandEnum = CommandEnum.find(command);
				if (commandFinder != null) {
					if (commandFinder != commandEnum)
						continue;
					commandFinder = null;
				}
				CommandAbstract commandAbstract = commandEnum.getNewInstance();
				try {
					commandAbstract.run(scriptCommandContext, id, parameters);
				} catch (ScriptException e) {
					switch (scriptCommandContext.getOnError()) {
					case FAILURE:
						throw e;
					case RESUME:
						Logging.warn(e);
						break;
					case NEXT_COMMAND:
						Logging.warn(e);
						commandFinder = scriptCommandContext
								.getOnErrorNextCommand();
						break;
					}
				}
				if (sqlUpdateMode == SqlUpdateMode.ONE_CALL_PER_PRIMARY_KEY)
					DatabaseUtils.update(transaction, id, sqlUpdateMode, sqlU);
			}
			if (sqlUpdateMode != SqlUpdateMode.ONE_CALL_PER_PRIMARY_KEY
					&& sqlUpdateMode != SqlUpdateMode.NO_CALL)
				DatabaseUtils.update(transaction, pkList, sqlUpdateMode, sqlU);
		} finally {
			IOUtils.closeQuietly(this);
		}
	}

	@Override
	public void close() throws IOException {
		if (transaction != null) {
			transaction.close();
			transaction = null;
		}
		if (scriptCommandContext != null)
			scriptCommandContext.close();
	}
}
