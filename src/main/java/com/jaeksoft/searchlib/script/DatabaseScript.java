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

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import com.jaeksoft.pojodbc.Query;
import com.jaeksoft.pojodbc.Transaction;
import com.jaeksoft.pojodbc.connection.JDBCConnection;
import com.jaeksoft.searchlib.config.Config;
import com.jaeksoft.searchlib.crawler.database.DatabaseCrawlSql.SqlUpdateMode;
import com.jaeksoft.searchlib.crawler.database.IsolationLevelEnum;
import com.jaeksoft.searchlib.scheduler.TaskLog;
import com.jaeksoft.searchlib.util.DatabaseUtils;
import com.jaeksoft.searchlib.util.StringUtils;
import com.jaeksoft.searchlib.utils.Variables;

public class DatabaseScript extends AbstractScriptRunner {

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

	private Transaction transaction;

	private ResultSet resultSet;

	private int paramCount;

	private List<String> pkList;

	private ResultSetMetaData metaData;

	private int columnCount;

	private final Variables scriptVariables;

	public DatabaseScript(Config config, String driverClass, String jdbcURL,
			String username, String password,
			IsolationLevelEnum isolationLevel, String sqlVariable,
			String varColumnName, String varColumnValue, String sqlSelect,
			String sqlUpdate, SqlUpdateMode sqlUpdateMode, Variables variables,
			TaskLog taskLog) throws InstantiationException,
			IllegalAccessException, ClassNotFoundException {
		super(config, variables, taskLog);
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
		transaction = null;
		resultSet = null;
		paramCount = 0;
		pkList = null;
		metaData = null;
		columnCount = 0;
		scriptVariables = new Variables();
	}

	private void doSqlUpdateOneCall(String sqlU, String id,
			String currentScriptError) throws SQLException {
		if (sqlUpdateMode == SqlUpdateMode.ONE_CALL_PER_PRIMARY_KEY)
			DatabaseUtils.update(transaction, id, currentScriptError,
					sqlUpdateMode, sqlU);
	}

	@Override
	protected void beforeRun(final ScriptCommandContext context)
			throws ScriptException {
		try {
			transaction = connectionManager.getNewTransaction(false,
					isolationLevel.value);
			context.setSql(transaction);
			context.addVariables(scriptVariables);
			// Load variables
			if (sqlVariable != null && sqlVariable.length() > 0) {
				Query query = transaction.prepare(sqlVariable);
				ResultSet resultSet = query.getResultSet();
				while (resultSet.next())
					scriptVariables.put(resultSet.getString(varColumnName),
							resultSet.getString(varColumnValue));
			}
			String sqlS = context.replaceVariables(sqlSelect);
			Query query = transaction.prepare(sqlS);
			resultSet = query.getResultSet();

			metaData = resultSet.getMetaData();
			TreeSet<String> columns = new TreeSet<String>();
			paramCount = 0;
			columnCount = metaData.getColumnCount();
			for (int i = 1; i <= columnCount; i++)
				columns.add(metaData.getColumnLabel(i));
			for (int i = 1; i <= columnCount; i++)
				if (columns.contains(COLUMN_PARAM + i))
					paramCount = i;
			pkList = sqlUpdateMode == SqlUpdateMode.PRIMARY_KEY_CHAR_LIST
					|| sqlUpdateMode == SqlUpdateMode.PRIMARY_KEY_CHAR_LIST ? new ArrayList<String>(
					0) : null;
		} catch (SQLException e) {
			throw new ScriptException(e);
		}
	}

	@Override
	protected ScriptLine nextScriptLine(final ScriptCommandContext context)
			throws ScriptException {
		try {
			if (!resultSet.next())
				return null;
			for (int i = 1; i <= columnCount; i++)
				scriptVariables.put(
						StringUtils.fastConcat("sql:",
								metaData.getColumnLabel(i)),
						resultSet.getString(i));
			String id = resultSet.getString(COLUMN_ID);
			if (pkList != null)
				pkList.add(id);
			String[] parameters = null;
			if (paramCount > 0) {
				parameters = new String[paramCount];
				for (int i = 0; i < paramCount; i++) {
					Object o = resultSet.getObject(COLUMN_PARAM + (i + 1));
					parameters[i] = o == null ? null : o.toString();
				}
			}
			return new ScriptLine(id, resultSet.getString(COLUMN_COMMAND),
					parameters);
		} catch (SQLException e) {
			throw new ScriptException(e);
		}
	}

	@Override
	protected void updateScriptLine(final ScriptCommandContext context,
			final ScriptLine scriptLine, String errorMsg)
			throws ScriptException {
		try {
			doSqlUpdateOneCall(context.replaceVariables(sqlUpdate),
					scriptLine.id, errorMsg);
		} catch (SQLException e) {
			throw new ScriptException(e);
		}
	}

	@Override
	public void afterRun(final ScriptCommandContext context,
			final String lastScriptError) throws ScriptException {
		if (sqlUpdateMode == SqlUpdateMode.ONE_CALL_PER_PRIMARY_KEY
				|| sqlUpdateMode == SqlUpdateMode.NO_CALL)
			return;
		try {
			DatabaseUtils.update(transaction, pkList, lastScriptError,
					sqlUpdateMode, context.replaceVariables(sqlUpdate));
		} catch (SQLException e) {
			throw new ScriptException(e);
		}
	}

	@Override
	public void close() {
		if (transaction != null) {
			transaction.close();
			transaction = null;
		}
		super.close();
	}

}
