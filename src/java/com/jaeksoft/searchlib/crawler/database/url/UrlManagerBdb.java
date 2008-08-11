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

package com.jaeksoft.searchlib.crawler.database.url;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.jaeksoft.searchlib.crawler.database.CrawlDatabaseBdb;
import com.jaeksoft.searchlib.crawler.database.CrawlDatabaseException;
import com.jaeksoft.searchlib.crawler.database.pattern.PatternUrlManager;
import com.jaeksoft.searchlib.crawler.process.CrawlStatistics;
import com.jaeksoft.searchlib.crawler.spider.Crawl;
import com.jaeksoft.searchlib.crawler.spider.Link;
import com.jaeksoft.searchlib.crawler.spider.LinkList;
import com.jaeksoft.searchlib.crawler.spider.Parser;
import com.jaeksoft.searchlib.util.BdbJoin;
import com.jaeksoft.searchlib.util.BdbUtil;
import com.jaeksoft.searchlib.util.BdbUtil.BdbFilter;
import com.sleepycat.bind.tuple.IntegerBinding;
import com.sleepycat.bind.tuple.LongBinding;
import com.sleepycat.bind.tuple.StringBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;
import com.sleepycat.je.Cursor;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;
import com.sleepycat.je.SecondaryConfig;
import com.sleepycat.je.SecondaryCursor;
import com.sleepycat.je.SecondaryDatabase;
import com.sleepycat.je.SecondaryKeyCreator;
import com.sleepycat.je.Transaction;

public class UrlManagerBdb extends UrlManager implements SecondaryKeyCreator {

	final private static Logger logger = Logger.getLogger(UrlManagerBdb.class);

	public class UrlItemTupleBinding extends BdbUtil<UrlItem> {

		@Override
		public UrlItem entryToObject(TupleInput input) {
			UrlItem urlItem = new UrlItem();
			urlItem.setUrl(input.readString());
			urlItem.setHost(input.readString());
			urlItem.setFetchStatusInt(input.readInt());
			urlItem.setParserStatusInt(input.readInt());
			urlItem.setIndexStatusInt(input.readInt());
			urlItem.setWhen(new Timestamp(input.readLong()));
			return urlItem;
		}

		@Override
		public void objectToEntry(UrlItem urlItem, TupleOutput output) {
			output.writeString(urlItem.getUrl());
			output.writeString(urlItem.getHost());
			output.writeInt(urlItem.getFetchStatus().value);
			output.writeInt(urlItem.getParserStatus().value);
			output.writeInt(urlItem.getIndexStatus().value);
			if (urlItem.getWhen() == null)
				output.writeLong(0);
			else
				output.writeLong(urlItem.getWhen().getTime());
			output.writeInt(urlItem.getRetry());
		}

		@Override
		public DatabaseEntry getKey(UrlItem item)
				throws UnsupportedEncodingException {
			DatabaseEntry key = new DatabaseEntry();
			setKey(item.getUrl(), key);
			return key;
		}

	}

	private CrawlDatabaseBdb crawlDatabase;
	private Database urlDb = null;
	private UrlItemTupleBinding tupleBinding = null;
	private SecondaryDatabase urlHostDb = null;
	private SecondaryDatabase urlFetchStatusDb = null;
	private SecondaryDatabase urlParserStatusDb = null;
	private SecondaryDatabase urlIndexStatusDb = null;
	private SecondaryDatabase urlWhenDb = null;
	private SecondaryDatabase urlRetryDb = null;

	public UrlManagerBdb(CrawlDatabaseBdb crawlDatabase)
			throws CrawlDatabaseException {
		this.crawlDatabase = crawlDatabase;
		DatabaseConfig dbConfig = new DatabaseConfig();
		dbConfig.setAllowCreate(true);
		dbConfig.setTransactional(true);
		try {
			Environment dbEnv = crawlDatabase.getEnv();
			urlDb = dbEnv.openDatabase(null, "url", dbConfig);
			tupleBinding = new UrlItemTupleBinding();
			urlHostDb = createSecondary(dbEnv, "url_host");
			urlFetchStatusDb = createSecondary(dbEnv, "url_fetch_status");
			urlParserStatusDb = createSecondary(dbEnv, "url_parser_status");
			urlIndexStatusDb = createSecondary(dbEnv, "url_index_status");
			urlWhenDb = createSecondary(dbEnv, "url_when");
			urlRetryDb = createSecondary(dbEnv, "url_retry");
		} catch (DatabaseException e) {
			throw new CrawlDatabaseException(e);
		}
	}

	private SecondaryDatabase createSecondary(Environment dbEnv, String name)
			throws DatabaseException {
		SecondaryConfig secConfig = new SecondaryConfig();
		secConfig.setAllowCreate(true);
		secConfig.setSortedDuplicates(true);
		secConfig.setAllowPopulate(true);
		secConfig.setKeyCreator(this);
		secConfig.setTransactional(true);
		return dbEnv.openSecondaryDatabase(null, name, urlDb, secConfig);
	}

	public void close() throws DatabaseException {
		if (urlDb != null) {
			urlDb.close();
			urlDb = null;
		}
	}

	@Override
	public void delete(String url) throws CrawlDatabaseException {
		DatabaseEntry key = new DatabaseEntry();
		Transaction txn = null;
		try {
			tupleBinding.setKey(url, key);
			txn = crawlDatabase.getEnv().beginTransaction(null, null);
			urlDb.delete(txn, key);
			txn.commit();
			txn = null;
		} catch (DatabaseException e) {
			throw new CrawlDatabaseException(e);
		} catch (UnsupportedEncodingException e) {
			throw new CrawlDatabaseException(e);
		} finally {
			if (txn != null)
				try {
					txn.abort();
				} catch (DatabaseException e) {
					logger.warn(e);
				}
		}
	}

	private class HostToFetchFilter implements BdbFilter<UrlItem> {

		private Set<String> hostSet;
		private int max;
		private CrawlStatistics stats;

		public HostToFetchFilter(Set<String> hostSet, int max,
				CrawlStatistics stats) {
			this.hostSet = hostSet;
			this.max = max;
			this.stats = stats;
		}

		public boolean accept(UrlItem item) {
			stats.incEvaluatedCount();
			if (item.getFetchStatus() != FetchStatus.UN_FETCHED)
				return true;
			return addHost(item);
		}

		protected boolean addHost(UrlItem item) {
			String host = item.getHost();
			if (hostSet.contains(host))
				return true;
			hostSet.add(host);
			stats.incHostCount();
			return hostSet.size() < max;
		}

	}

	private void getUnfetchedHostToFetch(Transaction txn, int limit,
			HashSet<String> hostSet, CrawlStatistics stats)
			throws CrawlDatabaseException {

		BdbJoin join = null;
		try {
			join = new BdbJoin();

			DatabaseEntry key = new DatabaseEntry();
			IntegerBinding.intToEntry(FetchStatus.UN_FETCHED.value, key);
			if (!join.add(txn, key, urlFetchStatusDb))
				return;

			HostToFetchFilter filter = new HostToFetchFilter(hostSet, limit,
					stats);
			tupleBinding.getFilter(join.getJoinCursor(urlDb), filter);

		} catch (DatabaseException e) {
			throw new CrawlDatabaseException(e);
		} finally {
			try {
				if (join != null)
					join.close();
			} catch (DatabaseException e) {
				logger.warn(e);
			}
		}
	}

	private void getExpiredHostToFetch(Transaction txn, long timestamp,
			int limit, HashSet<String> hostSet, CrawlStatistics stats)
			throws CrawlDatabaseException {
		SecondaryCursor cursor = null;
		try {

			cursor = urlWhenDb.openSecondaryCursor(txn, null);

			DatabaseEntry key = new DatabaseEntry();
			DatabaseEntry data = new DatabaseEntry();
			LongBinding.longToEntry(timestamp, key);

			OperationStatus os = cursor.getSearchKeyRange(key, data,
					LockMode.DEFAULT);

			if (os == OperationStatus.NOTFOUND) {
				if (cursor.getPrevNoDup(key, data, LockMode.DEFAULT) != OperationStatus.SUCCESS)
					return;
			} else if (os != OperationStatus.SUCCESS)
				return;

			// Find the first valid result by descending secondary key
			while (LongBinding.entryToLong(key) > timestamp) {
				if (cursor.getPrevDup(key, data, LockMode.DEFAULT) != OperationStatus.SUCCESS)
					if (cursor.getPrev(key, data, LockMode.DEFAULT) != OperationStatus.SUCCESS)
						return;
				stats.incEvaluatedCount();
			}

			// Iterate valid results by descending secondary key
			HostToFetchFilter filter = new HostToFetchFilter(hostSet, limit,
					stats);
			while (filter.addHost(tupleBinding.entryToObject(data))) {
				if (cursor.getPrevDup(key, data, LockMode.DEFAULT) != OperationStatus.SUCCESS)
					if (cursor.getPrev(key, data, LockMode.DEFAULT) != OperationStatus.SUCCESS)
						return;
			}

		} catch (DatabaseException e) {
			throw new CrawlDatabaseException(e);
		} finally {
			try {
				if (cursor != null)
					cursor.close();
			} catch (DatabaseException e) {
				logger.warn(e);
			}
		}

	}

	@Override
	public List<HostItem> getHostToFetch(int fetchInterval, int limit,
			CrawlStatistics stats) throws CrawlDatabaseException {
		Transaction txn = null;
		try {
			HashSet<String> hostSet = new HashSet<String>();
			txn = crawlDatabase.getEnv().beginTransaction(null, null);

			getUnfetchedHostToFetch(txn, limit, hostSet, stats);
			getExpiredHostToFetch(txn,
					getNewTimestamp(fetchInterval).getTime(), limit, hostSet,
					stats);

			List<HostItem> hostList = new ArrayList<HostItem>();
			Iterator<String> it = hostSet.iterator();
			while (it.hasNext())
				hostList.add(new HostItem(it.next()));
			return hostList;
		} catch (DatabaseException e) {
			throw new CrawlDatabaseException(e);
		} finally {
			try {
				if (txn != null)
					txn.abort();
			} catch (DatabaseException e) {
				logger.warn(e);
			}
		}
	}

	@Override
	public List<UrlItem> getUrlToFetch(HostItem host, int fetchInterval,
			long limit) throws CrawlDatabaseException {
		Transaction txn = null;
		BdbJoin join = null;

		final long timestamp = getNewTimestamp(fetchInterval).getTime();

		class Filter implements BdbFilter<UrlItem> {

			public boolean accept(UrlItem item) {
				if (item.getFetchStatus() == FetchStatus.UN_FETCHED)
					return true;
				if (item.getWhen().getTime() < timestamp)
					return true;
				return false;
			}
		}

		try {
			txn = crawlDatabase.getEnv().beginTransaction(null, null);
			join = new BdbJoin();
			List<UrlItem> list = new ArrayList<UrlItem>();
			DatabaseEntry key = new DatabaseEntry();
			StringBinding.stringToEntry(host.getHost(), key);
			if (!join.add(txn, key, urlHostDb))
				return list;
			tupleBinding.getFilter(join.getJoinCursor(urlDb), list, limit,
					new Filter());
			return list;
		} catch (DatabaseException e) {
			throw new CrawlDatabaseException(e);
		} finally {
			try {
				if (join != null)
					join.close();
			} catch (DatabaseException e) {
				e.printStackTrace();
			}
			try {
				if (txn != null)
					txn.abort();
			} catch (DatabaseException e) {
				logger.warn(e);
			}
		}
	}

	@Override
	public void inject(List<InjectUrlItem> list) throws CrawlDatabaseException {
		Transaction txn = null;
		try {
			txn = crawlDatabase.getEnv().beginTransaction(null, null);
			Iterator<InjectUrlItem> it = list.iterator();
			while (it.hasNext()) {
				InjectUrlItem item = it.next();
				if (item.getStatus() != InjectUrlItem.Status.UNDEFINED)
					continue;
				UrlItem urlItem = new UrlItem();
				urlItem.setUrl(item.getUrl());
				try {
					urlItem.setWhenNow();
					urlItem.checkHost();
					OperationStatus result = urlDb.putNoOverwrite(txn,
							tupleBinding.getKey(urlItem), tupleBinding
									.getData(urlItem));
					if (result == OperationStatus.SUCCESS)
						item.setStatus(InjectUrlItem.Status.INJECTED);
					else if (result == OperationStatus.KEYEXIST)
						item.setStatus(InjectUrlItem.Status.ALREADY);
				} catch (IllegalArgumentException e) {
					item.setStatus(InjectUrlItem.Status.ERROR);
				} catch (UnsupportedEncodingException e) {
					item.setStatus(InjectUrlItem.Status.ERROR);
				} catch (MalformedURLException e) {
					item.setStatus(InjectUrlItem.Status.MALFORMATED);
				}
			}
			txn.commit();
			txn = null;
		} catch (DatabaseException e) {
			throw new CrawlDatabaseException(e);
		} finally {
			if (txn != null)
				try {
					txn.abort();
				} catch (DatabaseException e) {
					logger.warn(e);
				}
		}
	}

	private void discoverLinks(LinkList links) throws IllegalArgumentException,
			DatabaseException, UnsupportedEncodingException,
			CrawlDatabaseException {
		PatternUrlManager patternManager = crawlDatabase.getPatternUrlManager();
		for (Link link : links.values())
			if (link.getFollow())
				if (patternManager.findPatternUrl(link.getUrl()) != null) {
					UrlItem urlItem = new UrlItem();
					urlItem.setUrl(link.getUrl().toExternalForm());
					try {
						urlItem.checkHost();
						urlItem.setWhenNow();
						urlDb.putNoOverwrite(null,
								tupleBinding.getKey(urlItem), tupleBinding
										.getData(urlItem));
					} catch (MalformedURLException e) {
						logger.warn(urlItem.getUrl(), e);
					}
				}
	}

	@Override
	public void update(Crawl crawl) throws CrawlDatabaseException,
			MalformedURLException {
		UrlItem urlItem = crawl.getUrlItem();
		try {
			urlItem.setWhenNow();
			urlDb.put(null, tupleBinding.getKey(urlItem), tupleBinding
					.getData(urlItem));
			Parser parser = crawl.getParser();
			if (parser != null && urlItem.isStatusFull()) {
				discoverLinks(parser.getInlinks());
				discoverLinks(parser.getOutlinks());
			}
		} catch (IllegalArgumentException e) {
			throw new CrawlDatabaseException(e);
		} catch (DatabaseException e) {
			throw new CrawlDatabaseException(e);
		} catch (UnsupportedEncodingException e) {
			throw new CrawlDatabaseException(e);
		}

	}

	public boolean createSecondaryKey(SecondaryDatabase secDb,
			DatabaseEntry key, DatabaseEntry data, DatabaseEntry result)
			throws DatabaseException {
		UrlItem urlItem = tupleBinding.entryToObject(data);
		if (secDb == urlHostDb) {
			StringBinding.stringToEntry(urlItem.getHost(), result);
		} else if (secDb == urlFetchStatusDb) {
			IntegerBinding.intToEntry(urlItem.getFetchStatus().value, result);
		} else if (secDb == urlParserStatusDb) {
			IntegerBinding.intToEntry(urlItem.getParserStatus().value, result);
		} else if (secDb == urlIndexStatusDb) {
			IntegerBinding.intToEntry((Integer) urlItem.getIndexStatus().value,
					result);
		} else if (secDb == urlWhenDb) {
			LongBinding.longToEntry(urlItem.getWhen().getTime(), result);
		} else if (secDb == urlRetryDb) {
			IntegerBinding.intToEntry(urlItem.getRetry(), result);
		}
		return true;
	}

	private int getUrls(Transaction txn, String host, FetchStatus fetchStatus,
			ParserStatus parserStatus, IndexStatus indexStatus, Date startDate,
			Date endDate, int start, int rows, List<UrlItem> list)
			throws CrawlDatabaseException {

		BdbJoin join = null;

		try {

			join = new BdbJoin();

			if (host.length() > 0) {
				DatabaseEntry key = new DatabaseEntry();
				StringBinding.stringToEntry(host, key);
				if (!join.add(txn, key, urlHostDb))
					return 0;
			}
			if (fetchStatus != FetchStatus.ALL) {
				DatabaseEntry key = new DatabaseEntry();
				IntegerBinding.intToEntry(fetchStatus.value, key);
				if (!join.add(txn, key, urlFetchStatusDb))
					return 0;
			}
			if (parserStatus != ParserStatus.ALL) {
				DatabaseEntry key = new DatabaseEntry();
				IntegerBinding.intToEntry(parserStatus.value, key);
				if (!join.add(txn, key, urlParserStatusDb))
					return 0;
			}
			if (indexStatus != IndexStatus.ALL) {
				DatabaseEntry key = new DatabaseEntry();
				IntegerBinding.intToEntry(indexStatus.value, key);
				if (!join.add(txn, key, urlIndexStatusDb))
					return 0;
			}

			return tupleBinding.getLimit(join.getJoinCursor(urlDb), start,
					rows, list);

		} catch (DatabaseException e) {
			throw new CrawlDatabaseException(e);
		} finally {
			try {
				if (join != null)
					join.close();
			} catch (DatabaseException e) {
				logger.warn(e);
			}
		}
	}

	@Override
	public List<UrlItem> getUrls(String like, String host,
			FetchStatus fetchStatus, ParserStatus parserStatus,
			IndexStatus indexStatus, Date startDate, Date endDate,
			Field orderBy, int start, int rows, UrlList urlList)
			throws CrawlDatabaseException {

		if (like == null)
			like = "";
		else
			like.trim();
		if (host == null)
			host = "";
		else
			host.trim();
		if (fetchStatus == null)
			fetchStatus = FetchStatus.ALL;
		if (parserStatus == null)
			parserStatus = ParserStatus.ALL;
		if (indexStatus == null)
			indexStatus = IndexStatus.ALL;

		Transaction txn = null;
		Cursor cursor = null;
		try {
			txn = crawlDatabase.getEnv().beginTransaction(null, null);

			List<UrlItem> list = new ArrayList<UrlItem>();

			if (like.length() == 0 && host.length() == 0
					&& fetchStatus == FetchStatus.ALL
					&& parserStatus == ParserStatus.ALL
					&& indexStatus == IndexStatus.ALL && startDate == null
					&& endDate == null) {
				cursor = urlDb.openCursor(txn, null);
				urlList.setNewList(list, tupleBinding.getLimit(cursor, start,
						rows, list));
			} else {
				urlList.setNewList(list, getUrls(txn, host, fetchStatus,
						parserStatus, indexStatus, startDate, endDate, start,
						rows, list));
			}

		} catch (DatabaseException e) {
			throw new CrawlDatabaseException(e);
		} finally {
			try {
				if (cursor != null)
					cursor.close();
			} catch (DatabaseException e) {
				logger.warn(e);
			}
			try {
				if (txn != null)
					txn.abort();
			} catch (DatabaseException e) {
				logger.warn(e);
			}
		}
		return null;
	}
}
