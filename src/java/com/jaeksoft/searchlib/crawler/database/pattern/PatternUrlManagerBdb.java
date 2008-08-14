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

package com.jaeksoft.searchlib.crawler.database.pattern;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import com.jaeksoft.searchlib.crawler.database.CrawlDatabaseBdb;
import com.jaeksoft.searchlib.crawler.database.CrawlDatabaseException;
import com.jaeksoft.searchlib.util.bdb.BdbUtil;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;
import com.sleepycat.je.Cursor;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.OperationStatus;

public class PatternUrlManagerBdb extends PatternUrlManager {

	final private static Logger logger = Logger
			.getLogger(PatternUrlManagerBdb.class);

	public class PatternUrlTupleBinding extends BdbUtil<PatternUrlItem> {

		@Override
		public PatternUrlItem entryToObject(TupleInput input) {
			PatternUrlItem patternUrlItem = new PatternUrlItem();
			patternUrlItem.setPattern(input.readString());
			return patternUrlItem;
		}

		@Override
		public void objectToEntry(PatternUrlItem patternUrlItem,
				TupleOutput output) {
			output.writeString(patternUrlItem.getPattern());
		}

		public DatabaseEntry getKey(PatternUrlItem patternUrlItem)
				throws UnsupportedEncodingException {
			DatabaseEntry key = new DatabaseEntry();
			setKey(patternUrlItem.getPattern(), key);
			return key;
		}

	}

	private CrawlDatabaseBdb crawlDatabase;
	private Database patternDb = null;
	private PatternUrlTupleBinding tupleBinding;

	public PatternUrlManagerBdb(CrawlDatabaseBdb database)
			throws CrawlDatabaseException {
		crawlDatabase = database;
		DatabaseConfig dbConfig = new DatabaseConfig();
		dbConfig.setAllowCreate(true);
		Environment dbEnv = crawlDatabase.getEnv();
		try {
			patternDb = dbEnv.openDatabase(null, "pattern", dbConfig);
		} catch (DatabaseException e) {
			throw new CrawlDatabaseException(e);
		}
		tupleBinding = new PatternUrlTupleBinding();
		updateCache();
	}

	public void close() throws DatabaseException {
		patternDb.close();
	}

	@Override
	public void addList(List<PatternUrlItem> patternList)
			throws CrawlDatabaseException {
		Iterator<PatternUrlItem> it = patternList.iterator();
		while (it.hasNext()) {
			PatternUrlItem item = it.next();
			if (item.getStatus() != PatternUrlItem.Status.UNDEFINED)
				continue;
			try {
				OperationStatus result = patternDb.putNoOverwrite(null,
						tupleBinding.getKey(item), tupleBinding.getData(item));
				if (result == OperationStatus.SUCCESS)
					item.setStatus(PatternUrlItem.Status.INJECTED);
				else if (result == OperationStatus.KEYEXIST)
					item.setStatus(PatternUrlItem.Status.ALREADY);
			} catch (IllegalArgumentException e) {
				item.setStatus(PatternUrlItem.Status.ERROR);
			} catch (UnsupportedEncodingException e) {
				item.setStatus(PatternUrlItem.Status.ERROR);
			} catch (DatabaseException e) {
				throw new CrawlDatabaseException(e);
			}
		}
		crawlDatabase.flush();
		updateCache();
	}

	@Override
	public void delPattern(PatternUrlItem item) throws CrawlDatabaseException {
		try {
			patternDb.delete(null, tupleBinding.getKey(item));
			updateCache();
		} catch (DatabaseException e) {
			throw new CrawlDatabaseException(e);
		} catch (UnsupportedEncodingException e) {
			throw new CrawlDatabaseException(e);
		}
	}

	@Override
	protected void updateCache() throws CrawlDatabaseException {
		Cursor cursor = null;
		try {
			Hashtable<String, ArrayList<PatternUrlItem>> newPatternMap = new Hashtable<String, ArrayList<PatternUrlItem>>();
			cursor = patternDb.openCursor(null, null);
			DatabaseEntry key = new DatabaseEntry();
			DatabaseEntry data = new DatabaseEntry();
			while (cursor.getNext(key, data, null) == OperationStatus.SUCCESS) {
				PatternUrlItem item = tupleBinding.entryToObject(data);
				try {
					URL url = item.extractUrl(true);
					ArrayList<PatternUrlItem> patternList = newPatternMap
							.get(url.getHost());
					if (patternList == null) {
						patternList = new ArrayList<PatternUrlItem>();
						newPatternMap.put(url.getHost(), patternList);
					}
					patternList.add(item);
				} catch (MalformedURLException e) {
					logger.info(e.getMessage(), e);
					continue;
				}
			}
			synchronized (this) {
				patternUrlMap = newPatternMap;
			}
		} catch (DatabaseException e) {
			throw new CrawlDatabaseException(e);
		} finally {
			try {
				if (cursor != null)
					cursor.close();
			} catch (DatabaseException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void getPatterns(String like, long start, long rows,
			PatternUrlList patternUrlList) throws CrawlDatabaseException {
		Cursor cursor = null;
		try {
			cursor = patternDb.openCursor(null, null);

			if (like == null || like.length() == 0) {
				tupleBinding.getCursor(cursor, patternUrlList.getPartialList())
						.getLimit(start, rows, null);
			} else
				tupleBinding.getStartsWith(cursor, like, start, rows,
						patternUrlList.getPartialList());
		} catch (DatabaseException e) {
			throw new CrawlDatabaseException(e);
		} catch (UnsupportedEncodingException e) {
			throw new CrawlDatabaseException(e);
		} finally {
			try {
				if (cursor != null)
					cursor.close();
			} catch (DatabaseException e) {
				e.printStackTrace();
			}
		}
	}
}
