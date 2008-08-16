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

import org.apache.log4j.Logger;

import com.jaeksoft.searchlib.crawler.database.pattern.PatternUrlManagerBdb;
import com.jaeksoft.searchlib.crawler.database.property.PropertyManagerBdb;
import com.jaeksoft.searchlib.crawler.database.url.UrlManagerBdb;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;

public class CrawlDatabaseBdb extends CrawlDatabase {

	private Environment dbEnv = null;

	private PatternUrlManagerBdb patternUrlManager = null;

	private UrlManagerBdb urlManager = null;

	private PropertyManagerBdb propertyManager = null;

	final private static Logger logger = Logger
			.getLogger(CrawlDatabaseBdb.class);

	protected CrawlDatabaseBdb(File location) throws CrawlDatabaseException {
		try {
			if (!location.exists())
				location.mkdir();
			EnvironmentConfig envConfig = new EnvironmentConfig();
			envConfig.setAllowCreate(true);
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

	public void close() {
		logger.info("Closing database");
		if (urlManager != null) {
			urlManager.close();
			urlManager = null;
		}
		if (patternUrlManager != null) {
			patternUrlManager.close();
			patternUrlManager = null;
		}
		if (propertyManager != null) {
			propertyManager.close();
			propertyManager = null;
		}
		try {
			if (dbEnv != null) {
				dbEnv.close();
				dbEnv = null;
			}
		} catch (DatabaseException e) {
			logger.warn(e);
		}
		logger.info("Database closed");
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
