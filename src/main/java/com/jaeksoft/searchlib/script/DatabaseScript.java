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
import java.util.List;
import java.util.TreeSet;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.exception.ExceptionUtils;

import com.jaeksoft.pojodbc.Query;
import com.jaeksoft.pojodbc.Transaction;
import com.jaeksoft.pojodbc.connection.JDBCConnection;
import com.jaeksoft.searchlib.Logging;
import com.jaeksoft.searchlib.config.Config;
import com.jaeksoft.searchlib.crawler.database.DatabaseCrawlSql.SqlUpdateMode;
import com.jaeksoft.searchlib.crawler.database.IsolationLevelEnum;
import com.jaeksoft.searchlib.scheduler.TaskLog;
import com.jaeksoft.searchlib.util.DatabaseUtils;
import com.jaeksoft.searchlib.utils.Variables;

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

	private Variables variables;

	public DatabaseScript(Config config, String driverClass, String jdbcURL,
			String username, String password,
			IsolationLevelEnum isolationLevel, String sqlVariable,
			String varColumnName, String varColumnValue, String sqlSelect,
			String sqlUpdate, SqlUpdateMode sqlUpdateMode, Variables variables,
			TaskLog taskLog) throws InstantiationException,
			IllegalAccessException, ClassNotFoundException {
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
		this.variables = variables != null ? variables : new Variables();
	}

	public void run() throws SQLException, ScriptException {
		try {
			transaction = connectionManager.getNewTransaction(false,
					isolationLevel.value);

			// Load variables
			if (sqlVariable != null && sqlVariable.length() > 0) {
				Query query = transaction.prepare(sqlVariable);
				ResultSet resultSet = query.getResultSet();
				while (resultSet.next())
					variables.put(resultSet.getString(varColumnName),
							resultSet.getString(varColumnValue));
			}

			String sqlU = variables.replace(sqlUpdate);
			String sqlS = variables.replace(sqlSelect);
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
			CommandEnum[] commandFinder = null;
			String lastScriptError = null;
			while (resultSet.next()) {
				String currentScriptError = null;
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
						parameters[i] = variables.replace(o.toString());
					}
				}
				try {
					CommandEnum commandEnum = CommandEnum.find(command);
					if (commandFinder != null) {
						// On error next_command is active, looking for next
						// statement
						boolean bFind = false;
						for (CommandEnum cmd : commandFinder) {
							if (cmd == commandEnum) {
								bFind = true;
								break;
							}
						}
						if (!bFind)
							continue;
						commandFinder = null;
					}
					CommandAbstract commandAbstract = commandEnum
							.getNewInstance();
					commandAbstract.run(scriptCommandContext, id, parameters);
				} catch (Exception e) {
					Throwable t = ExceptionUtils.getRootCause(e);
					currentScriptError = t != null ? t.getClass().getName() : e
							.getClass().getName();
					lastScriptError = currentScriptError;
					switch (scriptCommandContext.getOnError()) {
					case FAILURE:
						throw new ScriptException(e);
					case RESUME:
						Logging.warn(e);
						break;
					case NEXT_COMMAND:
						Logging.warn(e);
						commandFinder = scriptCommandContext
								.getOnErrorNextCommands();
						break;
					}
				}
				if (sqlUpdateMode == SqlUpdateMode.ONE_CALL_PER_PRIMARY_KEY)
					DatabaseUtils.update(transaction, id, currentScriptError,
							sqlUpdateMode, sqlU);
			}
			if (sqlUpdateMode != SqlUpdateMode.ONE_CALL_PER_PRIMARY_KEY
					&& sqlUpdateMode != SqlUpdateMode.NO_CALL)
				DatabaseUtils.update(transaction, pkList, lastScriptError,
						sqlUpdateMode, sqlU);
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
