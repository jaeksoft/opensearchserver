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
import java.util.TreeSet;

import org.apache.commons.io.IOUtils;

import com.jaeksoft.pojodbc.Query;
import com.jaeksoft.pojodbc.Transaction;
import com.jaeksoft.pojodbc.connection.JDBCConnection;
import com.jaeksoft.searchlib.config.Config;
import com.jaeksoft.searchlib.crawler.database.DatabaseCrawlSql.SqlUpdateMode;
import com.jaeksoft.searchlib.crawler.database.IsolationLevelEnum;
import com.jaeksoft.searchlib.scheduler.TaskLog;

public class DatabaseScript implements Closeable {

	private final static String COLUMN_ID = "id";
	private final static String COLUMN_COMMAND = "command";
	private final static String COLUMN_PARAM = "param";

	private final JDBCConnection connectionManager;

	private final IsolationLevelEnum isolationLevel;

	private final String sqlSelect;

	private final String sqlUpdate;

	private final SqlUpdateMode sqlUpdateMode;

	private final ScriptCommandContext scriptCommandContext;

	private Transaction transaction;

	public DatabaseScript(Config config, String driverClass, String jdbcURL,
			String username, String password,
			IsolationLevelEnum isolationLevel, String sqlSelect,
			String sqlUpdate, SqlUpdateMode sqlUpdateMode, TaskLog taskLog)
			throws InstantiationException, IllegalAccessException,
			ClassNotFoundException {
		connectionManager = new JDBCConnection();
		connectionManager.setDriver(driverClass);
		connectionManager.setUrl(jdbcURL);
		connectionManager.setUsername(username);
		connectionManager.setPassword(password);
		this.isolationLevel = isolationLevel;
		this.sqlSelect = sqlSelect;
		this.sqlUpdate = sqlUpdate;
		this.sqlUpdateMode = sqlUpdateMode;
		scriptCommandContext = new ScriptCommandContext(config, taskLog);
		transaction = null;
	}

	public void run() throws SQLException, ScriptException {
		try {
			transaction = connectionManager.getNewTransaction(false,
					isolationLevel.value);
			Query query = transaction.prepare(sqlSelect);
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

			while (resultSet.next()) {
				String id = resultSet.getString(COLUMN_ID);
				String command = resultSet.getString(COLUMN_COMMAND);
				Object[] parameters = null;
				if (paramCount > 0) {
					parameters = new Object[paramCount];
					for (int i = 0; i < paramCount; i++)
						parameters[i] = resultSet.getObject(COLUMN_PARAM
								+ (i + 1));
				}
				CommandEnum.execute(scriptCommandContext, id, command,
						parameters);
			}

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
