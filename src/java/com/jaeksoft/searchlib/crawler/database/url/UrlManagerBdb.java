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
import com.jaeksoft.searchlib.util.PartialList;
import com.jaeksoft.searchlib.util.bdb.BdbJoin;
import com.jaeksoft.searchlib.util.bdb.BdbUtil;
import com.jaeksoft.searchlib.util.bdb.BdbUtil.BdbFilter;
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
		try {
			tupleBinding.setKey(url, key);
			urlDb.delete(null, key);
		} catch (DatabaseException e) {
			throw new CrawlDatabaseException(e);
		} catch (UnsupportedEncodingException e) {
			throw new CrawlDatabaseException(e);
		}
	}

	private class HostToFetchFilter implements BdbFilter<UrlItem> {

		private Set<String> hostSet;
		private List<HostItem> hostList;
		private int max;
		private CrawlStatistics stats;

		public HostToFetchFilter(Set<String> hostSet, List<HostItem> hostList,
				int max, CrawlStatistics stats) {
			this.hostSet = hostSet;
			this.hostList = hostList;
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
			synchronized (hostList) {
				hostList.add(new HostItem(host));
			}
			stats.incHostCount();
			return hostSet.size() < max;
		}

	}

	private void getUnfetchedHostToFetch(Transaction txn,
			HostToFetchFilter filter) throws CrawlDatabaseException {

		BdbJoin join = null;
		try {
			join = new BdbJoin();

			DatabaseEntry key = new DatabaseEntry();
			IntegerBinding.intToEntry(FetchStatus.UN_FETCHED.value, key);
			if (!join.add(txn, null, key, urlFetchStatusDb))
				return;

			tupleBinding.getFilter(join.getJoinCursor(urlDb), filter, null);

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
			HostToFetchFilter filter, CrawlStatistics stats)
			throws CrawlDatabaseException {
		SecondaryCursor cursor = null;
		try {

			cursor = urlWhenDb.openSecondaryCursor(txn, null);

			DatabaseEntry key = new DatabaseEntry();
			DatabaseEntry data = new DatabaseEntry();
			data.setPartial(0, 0, true);
			LongBinding.longToEntry(timestamp, key);

			OperationStatus os = cursor.getSearchKeyRange(key, data, null);

			if (os == OperationStatus.NOTFOUND) {
				if (cursor.getPrevNoDup(key, data, null) != OperationStatus.SUCCESS)
					return;
			} else if (os != OperationStatus.SUCCESS)
				return;

			// Find the first valid result by descending secondary key
			while (LongBinding.entryToLong(key) > timestamp) {
				if (cursor.getPrevDup(key, data, null) != OperationStatus.SUCCESS)
					if (cursor.getPrev(key, data, null) != OperationStatus.SUCCESS)
						return;
				stats.incEvaluatedCount();
			}

			data = new DatabaseEntry();
			while (filter.addHost(tupleBinding.entryToObject(data))) {
				if (cursor.getPrevDup(key, data, null) != OperationStatus.SUCCESS)
					if (cursor.getPrev(key, data, null) != OperationStatus.SUCCESS)
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
	public void getHostToFetch(int fetchInterval, int limit,
			CrawlStatistics stats, List<HostItem> hostList)
			throws CrawlDatabaseException {

		HashSet<String> hostSet = new HashSet<String>();

		HostToFetchFilter filter = new HostToFetchFilter(hostSet, hostList,
				limit, stats);
		getUnfetchedHostToFetch(null, filter);
		getExpiredHostToFetch(null, getNewTimestamp(fetchInterval).getTime(),
				filter, stats);
	}

	@Override
	public List<UrlItem> getUrlToFetch(HostItem host, int fetchInterval,
			long limit) throws CrawlDatabaseException {

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
			join = new BdbJoin();
			List<UrlItem> list = new ArrayList<UrlItem>();
			DatabaseEntry key = new DatabaseEntry();
			StringBinding.stringToEntry(host.getHost(), key);
			if (!join.add(null, null, key, urlHostDb))
				return list;
			tupleBinding.getFilter(join.getJoinCursor(urlDb), list, limit,
					new Filter(), null);
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
		}
	}

	@Override
	public void inject(List<InjectUrlItem> list) throws CrawlDatabaseException {

		try {
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
					OperationStatus result = urlDb.putNoOverwrite(null,
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
		} catch (DatabaseException e) {
			throw new CrawlDatabaseException(e);
		} finally {
			crawlDatabase.flush();
		}
	}

	private void discoverLinks(LinkList links) throws IllegalArgumentException,
			DatabaseException, UnsupportedEncodingException,
			CrawlDatabaseException {
		PatternUrlManager patternManager = crawlDatabase.getPatternUrlManager();
		for (Link link : links.values()) {
			if (link.getFollow()) {
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
		}
		crawlDatabase.flush();
	}

	@Override
	public void update(Crawl crawl) throws CrawlDatabaseException,
			MalformedURLException {
		UrlItem urlItem = crawl.getUrlItem();
		try {
			urlItem.setWhenNow();
			urlDb.put(null, tupleBinding.getKey(urlItem), tupleBinding
					.getData(urlItem));
			crawlDatabase.flush();
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

	private void getUrls(Transaction txn, String host, FetchStatus fetchStatus,
			ParserStatus parserStatus, IndexStatus indexStatus, Date startDate,
			Date endDate, long start, long rows,
			PartialList<UrlItem> partialList) throws CrawlDatabaseException {

		BdbJoin join = null;

		try {

			join = new BdbJoin();

			if (host.length() > 0) {
				DatabaseEntry key = new DatabaseEntry();
				StringBinding.stringToEntry(host, key);
				if (!join.add(null, null, key, urlHostDb))
					return;
			}
			if (fetchStatus != FetchStatus.ALL) {
				DatabaseEntry key = new DatabaseEntry();
				IntegerBinding.intToEntry(fetchStatus.value, key);
				if (!join.add(null, null, key, urlFetchStatusDb))
					return;
			}
			if (parserStatus != ParserStatus.ALL) {
				DatabaseEntry key = new DatabaseEntry();
				IntegerBinding.intToEntry(parserStatus.value, key);
				if (!join.add(null, null, key, urlParserStatusDb))
					return;
			}
			if (indexStatus != IndexStatus.ALL) {
				DatabaseEntry key = new DatabaseEntry();
				IntegerBinding.intToEntry(indexStatus.value, key);
				if (!join.add(null, null, key, urlIndexStatusDb))
					return;
			}

			tupleBinding.getCursor(join.getJoinCursor(urlDb), partialList)
					.getLimit(start, rows, null);

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
	public void getUrls(String like, String host, FetchStatus fetchStatus,
			ParserStatus parserStatus, IndexStatus indexStatus, Date startDate,
			Date endDate, Field orderBy, long start, long rows, UrlList urlList)
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

		Cursor cursor = null;
		try {

			PartialList<UrlItem> partialList = urlList.getPartialList();
			if (like.length() == 0 && host.length() == 0
					&& fetchStatus == FetchStatus.ALL
					&& parserStatus == ParserStatus.ALL
					&& indexStatus == IndexStatus.ALL && startDate == null
					&& endDate == null) {
				cursor = urlDb.openCursor(null, null);
				tupleBinding.getCursor(cursor, partialList).getLimit(start,
						rows, null);
			} else {
				getUrls(null, host, fetchStatus, parserStatus, indexStatus,
						startDate, endDate, start, rows, partialList);
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
}
