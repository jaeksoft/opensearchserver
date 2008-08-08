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

package com.jaeksoft.searchlib.crawler.database.property;

import java.io.UnsupportedEncodingException;

import com.jaeksoft.searchlib.crawler.database.CrawlDatabaseBdb;
import com.jaeksoft.searchlib.crawler.database.CrawlDatabaseException;
import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;

public class PropertyManagerBdb extends PropertyManager {

	public class PropertyTupleBinding extends TupleBinding<PropertyItem> {

		@Override
		public PropertyItem entryToObject(TupleInput input) {
			return new PropertyItem(input.readString(), input.readString());
		}

		@Override
		public void objectToEntry(PropertyItem prop, TupleOutput output) {
			output.writeString(prop.getName());
			output.writeString(prop.getValue());
		}

		protected DatabaseEntry getKey(PropertyItem prop)
				throws UnsupportedEncodingException {
			DatabaseEntry key = new DatabaseEntry();
			key.setData(prop.getName().getBytes("UTF-8"));
			return key;
		}

		protected DatabaseEntry getData(PropertyItem prop) {
			DatabaseEntry data = new DatabaseEntry();
			objectToEntry(prop, data);
			return data;
		}
	}

	private CrawlDatabaseBdb crawlDatabase;
	private Database propertyDb = null;
	private PropertyTupleBinding tupleBinding;

	public PropertyManagerBdb(CrawlDatabaseBdb database)
			throws CrawlDatabaseException {
		crawlDatabase = database;
		DatabaseConfig dbConfig = new DatabaseConfig();
		dbConfig.setAllowCreate(true);
		dbConfig.setTransactional(true);
		Environment dbEnv = crawlDatabase.getEnv();
		try {
			propertyDb = dbEnv.openDatabase(null, "property", dbConfig);
		} catch (DatabaseException e) {
			throw new CrawlDatabaseException(e);
		}
		tupleBinding = new PropertyTupleBinding();
	}

	public void close() throws DatabaseException {
		propertyDb.close();
	}

	@Override
	protected String getPropertyString(Property prop)
			throws CrawlDatabaseException {
		DatabaseEntry data = new DatabaseEntry();
		try {
			if (propertyDb.get(null, tupleBinding.getKey(new PropertyItem(prop
					.getName())), data, LockMode.DEFAULT) == OperationStatus.SUCCESS)
				return tupleBinding.entryToObject(data).getValue();
			return null;
		} catch (UnsupportedEncodingException e) {
			throw new CrawlDatabaseException(e);
		} catch (DatabaseException e) {
			throw new CrawlDatabaseException(e);
		}
	}

	@Override
	protected void setProperty(PropertyItem prop) throws CrawlDatabaseException {
		try {
			propertyDb.put(null, tupleBinding.getKey(prop), tupleBinding
					.getData(prop));
		} catch (UnsupportedEncodingException e) {
			throw new CrawlDatabaseException(e);
		} catch (DatabaseException e) {
			throw new CrawlDatabaseException(e);
		}

	}

}
