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

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.jaeksoft.pojodbc.Transaction;

/**
 * 
 * The abstract class for all ConnectionManager.
 * 
 * @author Jaeksoft / Emmanuel Keller
 * 
 */
public abstract class ConnectionManager {

	/**
	 * Use com.jaeksoft.pojodbc.connection.ConnectionManager to manager the log
	 * level.
	 */
	static protected Logger logger = Logger.getLogger(ConnectionManager.class.getCanonicalName());

	/**
	 * Start a new transaction (or/and a new connection).
	 * 
	 * @param autoCommit
	 *            Enable or disable autocommit (if available)
	 * @param transactionIsolation
	 *            java.sql.Connection.TRANSACTION..., or null
	 * @return a new Transaction object
	 * @throws SQLException
	 *             if any jdbc error occurs
	 */
	public abstract Transaction getNewTransaction(boolean autoCommit, Integer transactionIsolation) throws SQLException;

	/**
	 * Start a new transaction (or/and a new connection)
	 * 
	 * @param autoCommit
	 *            Enable or disable autocommit (if available)
	 * @return a new Transaction object
	 * @throws SQLException
	 *             if any jdbc error occurs
	 */
	public Transaction getNewTransaction(boolean autoCommit) throws SQLException {
		return getNewTransaction(autoCommit, null);
	}

	/**
	 * Start a new transaction (or/and a new connection) with autoCommit set to
	 * true, and transactionIsolation set to null
	 * 
	 * @return a new Transaction object
	 * @throws SQLException
	 *             if any jdbc error occurs
	 */
	public Transaction getNewTransaction() throws SQLException {
		return getNewTransaction(true);
	}

	/**
	 * That static method try to close quietly each parameters. Null parameters
	 * are allowed. SQLException are catched and logged.
	 * 
	 * @param resultSet
	 *            A ResultSet to close
	 * @param stmt
	 *            A Statement to close
	 * @param cnx
	 *            A connection to close
	 */
	public static void close(ResultSet resultSet, Statement stmt, Connection cnx) {
		if (resultSet != null)
			try {
				resultSet.close();
			} catch (SQLException e) {
				logger.log(Level.SEVERE, e.getMessage(), e);
			}
		if (stmt != null)
			try {
				stmt.close();
			} catch (SQLException e) {
				logger.log(Level.SEVERE, e.getMessage(), e);
			}
		if (cnx != null) {
			try {
				if (logger.isLoggable(Level.FINEST))
					logger.finest("Close JDBC connection");
				cnx.close();
			} catch (SQLException e) {
				logger.log(Level.SEVERE, e.getMessage(), e);
			}
		}
	}
}
