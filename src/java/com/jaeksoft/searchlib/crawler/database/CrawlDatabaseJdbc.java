/**   
 * License Agreement for Jaeksoft SearchLib Community
 *
 * Copyright (C) 2008 Emmanuel Keller / Jaeksoft
 * 
 * http://www.jaeksoft.com
 * 
 * This file is part of Jaeksoft SearchLib Community.
 *
 * Jaeksoft SearchLib Community is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * Jaeksoft SearchLib Community is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Jaeksoft SearchLib Community. 
 *  If not, see <http://www.gnu.org/licenses/>.
 **/

package com.jaeksoft.searchlib.crawler.database;

import java.sql.Connection;
import java.sql.SQLException;

import com.jaeksoft.pojojdbc.Transaction;
import com.jaeksoft.pojojdbc.connection.JDBCConnection;
import com.jaeksoft.searchlib.crawler.database.pattern.PatternUrlManagerJdbc;
import com.jaeksoft.searchlib.crawler.database.property.PropertyManagerJdbc;
import com.jaeksoft.searchlib.crawler.database.url.UrlManagerJdbc;

public class CrawlDatabaseJdbc extends CrawlDatabase {

	private JDBCConnection database = null;

	private PatternUrlManagerJdbc patternUrlManager = null;

	private UrlManagerJdbc urlManager = null;

	private PropertyManagerJdbc propertyManager = null;

	protected CrawlDatabaseJdbc(String databaseDriver, String databaseUrl)
			throws CrawlDatabaseException {
		try {
			database = new JDBCConnection(databaseDriver, databaseUrl);
			getTransaction(true).close();
		} catch (SQLException e) {
			throw new CrawlDatabaseException(e);
		}
	}

	public Transaction getTransaction(boolean autoCommit) throws SQLException {
		try {
			return database.getNewTransaction(autoCommit,
					Connection.TRANSACTION_READ_UNCOMMITTED);
		} catch (SQLException e) {
			if (e.getMessage().endsWith("not found.")
					&& e.getMessage().startsWith("Database"))
				create();
			return database.getNewTransaction(autoCommit,
					Connection.TRANSACTION_READ_UNCOMMITTED);
		}
	}

	private void create() throws SQLException {
		Transaction transaction = null;
		try {
			transaction = database.getNewTransaction(false,
					Connection.TRANSACTION_READ_UNCOMMITTED, ";create=true");

			transaction
					.update("CREATE TABLE pattern(pattern VARCHAR(2048), PRIMARY KEY(pattern))");
			transaction
					.update("CREATE TABLE property(name VARCHAR(128), value VARCHAR(2048), PRIMARY KEY(name))");
			transaction
					.update("CREATE TABLE url(url VARCHAR(2048), host VARCHAR(2048), "
							+ "fetchStatus SMALLINT, parserStatus SMALLINT, indexStatus SMALLINT, "
							+ "when TIMESTAMP, retry SMALLINT, indexed TIMESTAMP, "
							+ "PRIMARY KEY (url))");
			transaction.update("CREATE INDEX url_host ON url(host)");
			transaction
					.update("CREATE INDEX url_fetch_status ON url(fetchStatus)");
			transaction
					.update("CREATE INDEX url_parser_status ON url(parserStatus)");
			transaction
					.update("CREATE INDEX url_index_status ON url(indexStatus)");
			transaction.update("CREATE INDEX url_when ON url(when)");
			transaction.update("CREATE INDEX url_retry ON url(retry)");
			transaction.update("CREATE INDEX url_indexed ON url(indexed)");
			transaction.commit();
		} catch (SQLException e) {
			throw e;
		} finally {
			if (transaction != null)
				transaction.close();
		}
	}

	public UrlManagerJdbc getUrlManager() throws CrawlDatabaseException {
		synchronized (this) {
			if (urlManager != null)
				return urlManager;
			urlManager = new UrlManagerJdbc(this);
			return urlManager;
		}
	}

	public PatternUrlManagerJdbc getPatternUrlManager()
			throws CrawlDatabaseException {
		synchronized (this) {
			if (patternUrlManager != null)
				return patternUrlManager;
			patternUrlManager = new PatternUrlManagerJdbc(this);
			return patternUrlManager;
		}
	}

	public PropertyManagerJdbc getPropertyManager()
			throws CrawlDatabaseException {
		synchronized (this) {
			if (propertyManager != null)
				return propertyManager;
			propertyManager = new PropertyManagerJdbc(this);
			return propertyManager;
		}

	}

}
