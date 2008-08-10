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

import java.net.MalformedURLException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import com.jaeksoft.pojojdbc.Query;
import com.jaeksoft.pojojdbc.Transaction;
import com.jaeksoft.searchlib.crawler.database.CrawlDatabaseException;
import com.jaeksoft.searchlib.crawler.database.CrawlDatabaseJdbc;
import com.jaeksoft.searchlib.crawler.database.pattern.PatternUrlManager;
import com.jaeksoft.searchlib.crawler.spider.Crawl;
import com.jaeksoft.searchlib.crawler.spider.Link;
import com.jaeksoft.searchlib.crawler.spider.LinkList;
import com.jaeksoft.searchlib.crawler.spider.Parser;

public class UrlManagerJdbc extends UrlManager {

	final private static Logger logger = Logger.getLogger(UrlManagerJdbc.class);

	private CrawlDatabaseJdbc database;

	public UrlManagerJdbc(CrawlDatabaseJdbc database) {
		this.database = database;
	}

	private String getUrlWhereClause(String like, String host,
			FetchStatus fetchStatus, ParserStatus parserStatus,
			IndexStatus indexStatus, Date startDate, Date endDate) {

		StringBuffer where = new StringBuffer();

		if (like != null && like.length() > 0)
			where.append("url LIKE ?");

		if (host != null && host.length() > 0) {
			if (where.length() > 0)
				where.append(" AND ");
			where.append("host=?");
		}

		if (fetchStatus != null) {
			if (where.length() > 0)
				where.append(" AND ");
			where.append("fetchStatus=?");
		}

		if (parserStatus != null) {
			if (where.length() > 0)
				where.append(" AND ");
			where.append("parserStatus=?");
		}

		if (indexStatus != null) {
			if (where.length() > 0)
				where.append(" AND ");
			where.append("indexStatus=?");
		}

		if (startDate != null) {
			if (where.length() > 0)
				where.append(" AND ");
			where.append("when>=?");
		}

		if (endDate != null) {
			if (where.length() > 0)
				where.append(" AND ");
			where.append("when<?");
		}
		return where.toString();
	}

	private Query getUrls(Transaction transaction, String like, String host,
			FetchStatus fetchStatus, ParserStatus parserStatus,
			IndexStatus indexStatus, Date startDate, Date endDate,
			Field orderBy, boolean asc) throws SQLException {

		if (fetchStatus == FetchStatus.ALL)
			fetchStatus = null;
		if (parserStatus == ParserStatus.ALL)
			parserStatus = null;
		if (indexStatus == IndexStatus.ALL)
			indexStatus = null;

		if (orderBy == null)
			orderBy = Field.HOST;

		String select = "SELECT url,host,when,retry,"
				+ "fetchStatus as fetchStatusInt,"
				+ "parserStatus as parserStatusInt,"
				+ "indexStatus as indexStatusInt";

		// Build statement
		StringBuffer sql = new StringBuffer();
		sql.append(select);
		sql.append(" FROM url ");

		// Build the where clause
		String where = getUrlWhereClause(like, host, fetchStatus, parserStatus,
				indexStatus, startDate, endDate);
		if (where.length() > 0) {
			sql.append("WHERE ");
			sql.append(where);
		}

		sql.append(" ORDER BY ");
		sql.append(orderBy);
		if (!asc)
			sql.append(" DESC");

		Query query = transaction.prepare(sql.toString(),
				ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
		PreparedStatement stmt = query.getStatement();
		int i = 1;
		if (like != null && like.length() > 0)
			stmt.setString(i++, "%" + like + "%");
		if (host != null && host.length() > 0)
			stmt.setString(i++, host);
		if (fetchStatus != null)
			stmt.setInt(i++, fetchStatus.value);
		if (parserStatus != null)
			stmt.setInt(i++, parserStatus.value);
		if (indexStatus != null)
			stmt.setInt(i++, indexStatus.value);
		if (startDate != null)
			stmt.setDate(i++, new java.sql.Date(startDate.getTime()));
		if (endDate != null)
			stmt.setDate(i++, new java.sql.Date(endDate.getTime()));
		return query;
	}

	private void update(Transaction transaction, UrlItem urlItem)
			throws SQLException, MalformedURLException {
		Query query = transaction
				.prepare("UPDATE url "
						+ "SET host=?,retry=?,fetchStatus=?,parserStatus=?,indexStatus=?,"
						+ "when=CURRENT_TIMESTAMP WHERE url=?");
		PreparedStatement st = query.getStatement();
		st.setString(1, urlItem.getURL().getHost());
		st.setInt(2, urlItem.getRetry());
		st.setInt(3, urlItem.getFetchStatus().value);
		st.setInt(4, urlItem.getParserStatus().value);
		st.setInt(5, urlItem.getIndexStatus().value);
		st.setString(6, urlItem.getUrl());
		query.update();
	}

	private void insert(Transaction transaction, UrlItem urlItem)
			throws SQLException {
		Query query = transaction
				.prepare("INSERT INTO "
						+ "url(url,host,retry,fetchStatus,parserStatus,indexStatus,when) "
						+ "VALUES (?,?,?,?,?,?,CURRENT_TIMESTAMP)");
		PreparedStatement st = query.getStatement();
		st.setString(1, urlItem.getUrl());
		st.setString(2, urlItem.getHost());
		st.setInt(3, urlItem.getRetry());
		st.setInt(4, urlItem.getFetchStatus().value);
		st.setInt(5, urlItem.getParserStatus().value);
		st.setInt(6, urlItem.getIndexStatus().value);
		query.update();
	}

	private void insertOrUpdate(Transaction transaction, UrlItem urlItem,
			boolean bIgnoreDuplicate) throws SQLException,
			MalformedURLException {
		try {
			insert(transaction, urlItem);
		} catch (SQLException e) {
			// Duplicate Key
			if ("23505".equals(e.getSQLState())) {
				if (bIgnoreDuplicate)
					return;
				update(transaction, urlItem);
				return;
			}
			throw e;
		}
	}

	private void discoverLinks(Transaction transaction, LinkList links)
			throws CrawlDatabaseException, SQLException {
		PatternUrlManager patternManager = database.getPatternUrlManager();
		for (Link link : links.values())
			if (link.getFollow())
				if (patternManager.findPatternUrl(link.getUrl()) != null) {
					UrlItem urlItem = new UrlItem();
					urlItem.setUrl(link.getUrl().toExternalForm());
					try {
						insertOrUpdate(transaction, urlItem, true);
					} catch (MalformedURLException e) {
						logger.warn(link.getUrl() + " " + e.getMessage(), e);
					}
				}
	}

	private void deleteUrl(Transaction transaction, String sUrl)
			throws SQLException {
		Query query = transaction.prepare("DELETE FROM url WHERE url=?");
		query.getStatement().setString(1, sUrl);
		query.update();
	}

	public void delete(String sUrl) throws CrawlDatabaseException {
		Transaction transaction = null;
		try {
			transaction = database.getTransaction(false);
			deleteUrl(transaction, sUrl);
			transaction.commit();
		} catch (SQLException e) {
			throw new CrawlDatabaseException(e);
		} finally {
			if (transaction != null)
				transaction.close();
		}
	}

	public void update(Crawl crawl) throws MalformedURLException,
			CrawlDatabaseException {
		UrlItem urlItem = crawl.getUrlItem();
		Transaction transaction = null;
		try {
			transaction = database.getTransaction(false);
			insertOrUpdate(transaction, urlItem, false);
			Parser parser = crawl.getParser();
			if (parser != null && urlItem.isStatusFull()) {
				discoverLinks(transaction, parser.getInlinks());
				discoverLinks(transaction, parser.getOutlinks());
			}
			transaction.commit();
		} catch (SQLException e) {
			throw new CrawlDatabaseException(e);
		} catch (MalformedURLException e) {
			throw e;
		} finally {
			if (transaction != null)
				transaction.close();
		}
	}

	public void inject(List<InjectUrlItem> list) throws CrawlDatabaseException {
		Transaction transaction = null;
		try {
			transaction = database.getTransaction(false);
			Iterator<InjectUrlItem> it = list.iterator();
			while (it.hasNext()) {
				InjectUrlItem item = it.next();
				if (item.getStatus() != InjectUrlItem.Status.UNDEFINED)
					continue;
				try {
					UrlItem urlItem = new UrlItem();
					urlItem.setUrl(item.getUrl());
					urlItem.checkHost();
					insert(transaction, urlItem);
					item.setStatus(InjectUrlItem.Status.INJECTED);
				} catch (SQLException e) {
					if ("23505".equals(e.getSQLState()))
						item.setStatus(InjectUrlItem.Status.ALREADY);
					else {
						e.printStackTrace();
						item.setStatus(InjectUrlItem.Status.ERROR);
					}
				} catch (MalformedURLException e) {
					item.setStatus(InjectUrlItem.Status.MALFORMATED);
				}
			}
			transaction.commit();
		} catch (SQLException e) {
			throw new CrawlDatabaseException(e);
		} finally {
			if (transaction != null)
				transaction.close();
		}
	}

	@SuppressWarnings("unchecked")
	public List<HostItem> getHostToFetch(int fetchInterval, int limit)
			throws CrawlDatabaseException {
		Transaction transaction = null;
		try {
			transaction = database.getTransaction(true);
			Query query = transaction.prepare("SELECT host FROM url "
					+ "WHERE fetchStatus=? OR when<? " + "GROUP BY host");
			query.getStatement().setInt(1, FetchStatus.UN_FETCHED.value);
			query.getStatement()
					.setTimestamp(2, getNewTimestamp(fetchInterval));
			query.setMaxResults(limit);
			return (List<HostItem>) query.getResultList(HostItem.class);
		} catch (SQLException e) {
			throw new CrawlDatabaseException(e);
		} finally {
			if (transaction != null)
				transaction.close();
		}
	}

	@SuppressWarnings("unchecked")
	public List<UrlItem> getUrlToFetch(HostItem host, int fetchInterval,
			int limit) throws CrawlDatabaseException {
		Transaction transaction = null;
		try {
			transaction = database.getTransaction(true);
			Query query = transaction.prepare("SELECT url,host,when,retry,"
					+ "fetchStatus as fetchStatusInt,"
					+ "parserStatus as parserStatusInt,"
					+ "indexStatus as indexStatusInt"
					+ " FROM url WHERE host=? AND "
					+ "(fetchStatus=? OR when<?)" + "ORDER BY when ASC");
			query.getStatement().setString(1, host.host);
			query.getStatement().setInt(2, FetchStatus.UN_FETCHED.value);
			query.getStatement()
					.setTimestamp(3, getNewTimestamp(fetchInterval));
			query.setMaxResults(limit);
			return (ArrayList<UrlItem>) query.getResultList(UrlItem.class);
		} catch (SQLException e) {
			throw new CrawlDatabaseException(e);
		} finally {
			if (transaction != null)
				transaction.close();
		}

	}

	@SuppressWarnings("unchecked")
	@Override
	public List<UrlItem> getUrls(String like, String host,
			FetchStatus fetchStatus, ParserStatus parserStatus,
			IndexStatus indexStatus, Date startDate, Date endDate,
			Field orderBy, int start, int rows, UrlList urlList)
			throws CrawlDatabaseException {
		Transaction transaction = null;
		try {
			transaction = database.getTransaction(true);
			Query query = getUrls(transaction, like, host, fetchStatus,
					parserStatus, indexStatus, startDate, endDate, orderBy,
					true);
			query.setFirstResult(start);
			query.setMaxResults(rows);
			List<UrlItem> results = (List<UrlItem>) query
					.getResultList(UrlItem.class);
			urlList.setNewList(results, query.getResultCount());
			return results;
		} catch (SQLException e) {
			throw new CrawlDatabaseException(e);
		} finally {
			if (transaction != null)
				transaction.close();
		}
	}
}