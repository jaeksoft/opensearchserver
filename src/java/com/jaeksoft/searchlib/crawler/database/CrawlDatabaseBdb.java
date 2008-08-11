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

import java.io.File;

import com.jaeksoft.searchlib.crawler.database.pattern.PatternUrlManagerBdb;
import com.jaeksoft.searchlib.crawler.database.property.PropertyManagerBdb;
import com.jaeksoft.searchlib.crawler.database.url.UrlManagerBdb;
import com.sleepycat.je.CursorConfig;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.je.Transaction;
import com.sleepycat.je.TransactionConfig;

public class CrawlDatabaseBdb extends CrawlDatabase {

	private Environment dbEnv = null;

	private PatternUrlManagerBdb patternUrlManager = null;

	private UrlManagerBdb urlManager = null;

	private PropertyManagerBdb propertyManager = null;

	protected CrawlDatabaseBdb(File location) throws CrawlDatabaseException {
		try {
			if (!location.exists())
				location.mkdir();
			EnvironmentConfig envConfig = new EnvironmentConfig();
			envConfig.setAllowCreate(true);
			envConfig.setTransactional(true);
			envConfig.setTxnTimeout(60 * 1000 * 1000);
			envConfig.setLockTimeout(60 * 1000 * 1000);
			dbEnv = new Environment(location, envConfig);
		} catch (DatabaseException e) {
			close();
			throw new CrawlDatabaseException(e);
		}
	}

	public Environment getEnv() {
		return dbEnv;
	}

	public Transaction beginTransaction() throws DatabaseException {
		return dbEnv.beginTransaction(null, getTransactionConfig());
	}

	public TransactionConfig getTransactionConfig() {
		TransactionConfig config = new TransactionConfig();
		config.setReadUncommitted(true);
		return config;
	}

	public CursorConfig getCursorConfig() {
		CursorConfig config = new CursorConfig();
		config.setReadUncommitted(true);
		return config;
	}

	private void close() {
		try {
			if (urlManager != null) {
				urlManager.close();
				urlManager = null;
			}
		} catch (DatabaseException e) {
			e.printStackTrace();
		}
		try {
			if (patternUrlManager != null) {
				patternUrlManager.close();
				patternUrlManager = null;
			}
		} catch (DatabaseException e) {
			e.printStackTrace();
		}
		try {
			if (propertyManager != null) {
				propertyManager.close();
				propertyManager = null;
			}
		} catch (DatabaseException e) {
			e.printStackTrace();
		}
		try {
			if (dbEnv != null) {
				dbEnv.cleanLog();
				dbEnv.close();
				dbEnv = null;
			}
		} catch (DatabaseException e) {
			e.printStackTrace();
		}
	}

	public UrlManagerBdb getUrlManager() throws CrawlDatabaseException {
		synchronized (this) {
			if (urlManager != null)
				return urlManager;
			urlManager = new UrlManagerBdb(this);
			return urlManager;
		}
	}

	public PatternUrlManagerBdb getPatternUrlManager()
			throws CrawlDatabaseException {
		synchronized (this) {
			if (patternUrlManager != null)
				return patternUrlManager;
			patternUrlManager = new PatternUrlManagerBdb(this);
			return patternUrlManager;
		}
	}

	public PropertyManagerBdb getPropertyManager()
			throws CrawlDatabaseException {
		synchronized (this) {
			if (propertyManager != null)
				return propertyManager;
			propertyManager = new PropertyManagerBdb(this);
			return propertyManager;
		}

	}

}
