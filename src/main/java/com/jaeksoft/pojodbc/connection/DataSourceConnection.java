/**   
 * License Agreement for OpenSearchServer Pojodbc
 *
 * Copyright 2008-2013 Emmanuel Keller / Jaeksoft
 * Copyright 2014-2015 OpenSearchServer Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jaeksoft.pojodbc.connection;

import java.sql.SQLException;

import javax.sql.DataSource;

import com.jaeksoft.pojodbc.Transaction;

/**
 * A connection manager getting database connection from a javax.sql.DataSource.
 * <p>
 * That example show how to create an instance of a DataSourceConnection using
 * DataSource from JNDI.
 * </p>
 * 
 * <pre>
 * Context initContext = new InitialContext();
 * Context envContext = (Context) initContext.lookup(&quot;java:/comp/env&quot;);
 * DataSource ds = (DataSource) envContext.lookup(&quot;myDatabase&quot;);
 * DatabaseConnectionManager connectionManager = new DataSourceConnection(ds);
 * </pre>
 * 
 */
public class DataSourceConnection extends ConnectionManager {

	private DataSource dataSource;

	/**
	 * @param dataSource
	 *            The DataSource that connection manager will use to get new
	 *            database connection.
	 */
	public DataSourceConnection(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	@Override
	public Transaction getNewTransaction(boolean autoCommit, Integer transactionIsolation) throws SQLException {
		return new Transaction(dataSource.getConnection(), autoCommit, transactionIsolation);
	}

}
