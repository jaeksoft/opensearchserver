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

package com.jaeksoft.searchlib.crawler.urldb;

import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import com.jaeksoft.pojojdbc.Query;
import com.jaeksoft.pojojdbc.Transaction;
import com.jaeksoft.searchlib.config.Config;
import com.jaeksoft.searchlib.crawler.filter.PatternUrlFilter;
import com.jaeksoft.searchlib.crawler.filter.PatternUrlItem;
import com.jaeksoft.searchlib.crawler.spider.Crawl;
import com.jaeksoft.searchlib.crawler.spider.Link;
import com.jaeksoft.searchlib.crawler.spider.LinkList;
import com.jaeksoft.searchlib.crawler.spider.Parser;

public class UrlDb {

	public enum Field {

		URL("url"), WHEN("when"), RETRY("retry"), STATUS("status"), HOST("host");

		private String name;

		private Field(String name) {
			this.name = name;
		}

		@Override
		public String toString() {
			return name;
		}
	}

	private Config config;

	public UrlDb(Config config) {
		this.config = config;
	}

	@SuppressWarnings("unchecked")
	public Query getUrl(Transaction transaction, String like, String host,
			UrlStatus status, Date startDate, Date endDate, Field orderBy,
			boolean asc) throws SQLException {
		// Build the where clause
		String where = null;

		if (like != null && like.length() > 0)
			where = "url LIKE ? ";

		if (host != null && host.length() > 0) {
			if (where != null)
				where += " AND ";
			else
				where = "";
			where += "host=?";
		}

		if (status != null) {
			if (where != null)
				where += " AND ";
			else
				where = "";
			where += "status=?";
		}

		if (startDate != null) {
			if (where != null)
				where += " AND ";
			else
				where = "";
			where += "when>=?";
		}

		if (endDate != null) {
			if (where != null)
				where += " AND ";
			else
				where = "";
			where += "when<?";
		}

		if (where == null)
			where = "";
		else
			where = "WHERE " + where;

		// Build statement
		if (orderBy == null)
			orderBy = Field.URL;
		String sql = "SELECT url,host,when,retry,status FROM url " + where
				+ " ORDER BY " + orderBy;
		if (!asc)
			sql += " DESC";

		Query query = transaction.prepare(sql);
		PreparedStatement stmt = query.getStatement();
		int i = 1;
		if (like != null && like.length() > 0)
			stmt.setString(i++, "%" + like + "%");
		if (host != null && host.length() > 0)
			stmt.setString(i++, host);
		if (status != null)
			stmt.setInt(i++, status.getValue());
		if (startDate != null)
			stmt.setDate(i++, new java.sql.Date(startDate.getTime()));
		if (endDate != null)
			stmt.setDate(i++, new java.sql.Date(endDate.getTime()));
		return query;
	}

	private void update(Transaction transaction, URL url, int retry,
			UrlStatus status) throws SQLException {
		Query query = transaction.prepare("UPDATE url "
				+ "SET host=?,retry=?,status=?,when=CURRENT_TIMESTAMP "
				+ "WHERE url=?");
		PreparedStatement st = query.getStatement();
		st.setString(1, url.getHost());
		st.setInt(2, retry);
		st.setInt(3, status.getValue());
		st.setString(4, url.toExternalForm());
		query.update();
	}

	private void insert(Transaction transaction, URL url, int retry,
			UrlStatus status) throws SQLException {
		Query query = transaction.prepare("INSERT INTO "
				+ "url(url,host,retry,status,when) "
				+ "VALUES (?,?,?,?,CURRENT_TIMESTAMP)");
		PreparedStatement st = query.getStatement();
		st.setString(1, url.toExternalForm());
		st.setString(2, url.getHost());
		st.setInt(3, retry);
		st.setInt(4, status.getValue());
		query.update();
	}

	private void insertOrUpdate(Transaction transaction, URL url, int retry,
			UrlStatus status, boolean bIgnoreDuplicate) throws SQLException {
		try {
			insert(transaction, url, retry, status);
		} catch (SQLException e) {
			// Duplicate Key
			if ("23505".equals(e.getSQLState())) {
				if (bIgnoreDuplicate)
					return;
				update(transaction, url, retry, status);
				return;
			}
			throw e;
		}
	}

	private void discoverLinks(Transaction transaction, LinkList links,
			PatternUrlFilter patternFilter) throws SQLException {
		for (Link link : links.values())
			if (link.getFollow())
				if (patternFilter.findPatternUrl(link.getUrl()) != null)
					insertOrUpdate(transaction, link.getUrl(), 0,
							UrlStatus.UN_FETCHED, true);
	}

	private void deleteUrl(Transaction transaction, String sUrl)
			throws SQLException {
		Query query = transaction.prepare("DELETE FROM url WHERE url=?");
		query.getStatement().setString(1, sUrl);
		query.update();
	}

	public void delete(String sUrl) throws SQLException {
		Transaction transaction = config.getDatabaseTransaction();
		try {
			deleteUrl(transaction, sUrl);
			transaction.commit();
		} catch (SQLException e) {
			throw e;
		} finally {
			transaction.close();
		}
	}

	public void update(Crawl crawl) throws SQLException {
		if (crawl == null)
			return;
		UrlStatus urlStatus = UrlStatus.UN_FETCHED;
		Crawl.Status crawlStatus = crawl.getStatus();
		if (crawlStatus == Crawl.Status.CRAWLED)
			urlStatus = UrlStatus.FETCHED;
		else if (crawlStatus == Crawl.Status.NOPARSER)
			urlStatus = UrlStatus.NOPARSER;
		else if (crawlStatus == Crawl.Status.REDIR_PERM)
			urlStatus = UrlStatus.REDIR_PERM;
		else if (crawlStatus == Crawl.Status.REDIR_TEMP)
			urlStatus = UrlStatus.REDIR_TEMP;
		else if (crawlStatus == Crawl.Status.HTTP_GONE)
			urlStatus = UrlStatus.GONE;
		else if (crawlStatus == Crawl.Status.HTTP_ERROR)
			urlStatus = UrlStatus.ERROR;
		Transaction transaction = config.getDatabaseTransaction();
		try {
			insertOrUpdate(transaction, crawl.getUrl(), 0, urlStatus, false);
			Parser parser = crawl.getParser();
			if (parser != null && urlStatus == UrlStatus.FETCHED) {
				PatternUrlFilter patternFilter = config.getPatternUrlFilter();
				discoverLinks(transaction, parser.getInlinks(), patternFilter);
				discoverLinks(transaction, parser.getOutlinks(), patternFilter);
			}
			transaction.commit();
		} catch (SQLException e) {
			throw e;
		} finally {
			transaction.close();
		}
	}

	public void inject(List<InjectUrlItem> list) {
		Transaction transaction = null;
		try {
			transaction = config.getDatabaseTransaction();
			Iterator<InjectUrlItem> it = list.iterator();
			while (it.hasNext()) {
				InjectUrlItem item = it.next();
				if (item.getStatus() != InjectUrlItem.Status.UNDEFINED)
					continue;
				try {
					insert(transaction, item.getURL(), 0, UrlStatus.UN_FETCHED);
					item.setStatus(InjectUrlItem.Status.INJECTED);
				} catch (SQLException e) {
					if ("23505".equals(e.getSQLState()))
						item.setStatus(InjectUrlItem.Status.ALREADY);
					else {
						e.printStackTrace();
						item.setStatus(InjectUrlItem.Status.ERROR);
					}
				}
			}
			transaction.commit();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (transaction != null)
				transaction.close();
		}
	}

	public void injectPrefix(List<PatternUrlItem> patternList) {
		Iterator<PatternUrlItem> it = patternList.iterator();
		List<InjectUrlItem> urlList = new ArrayList<InjectUrlItem>();
		while (it.hasNext()) {
			PatternUrlItem item = it.next();
			if (item.getStatus() == PatternUrlItem.Status.INJECTED)
				urlList.add(new InjectUrlItem(item));
		}
		inject(urlList);
	}

	@SuppressWarnings("unchecked")
	public List<HostCountItem> getHostToFetch(int fetchInterval, int limit)
			throws SQLException {
		Transaction transaction = null;
		try {
			transaction = config.getDatabaseTransaction();
			Query query = transaction
					.prepare("SELECT host,count(*) as count FROM url "
							+ "WHERE status=? OR when<? " + "GROUP BY host");
			query.getStatement().setInt(1, UrlStatus.UN_FETCHED.getValue());
			query.getStatement()
					.setTimestamp(2, getNewTimestamp(fetchInterval));
			query.setMaxResults(limit);
			return (List<HostCountItem>) query
					.getResultList(HostCountItem.class);
		} catch (SQLException e) {
			throw e;
		} finally {
			if (transaction != null)
				transaction.close();
		}
	}

	private Timestamp getNewTimestamp(long fetchInterval) {
		long t = System.currentTimeMillis() - fetchInterval * 1000 * 86400;
		return new Timestamp(t);
	}

	@SuppressWarnings("unchecked")
	public ArrayList<UrlItem> getUrlToFetch(HostCountItem host,
			int fetchInterval, int limit) throws SQLException {
		Transaction transaction = config.getDatabaseTransaction();
		try {
			Query query = transaction
					.prepare("SELECT url,host,when,retry,status FROM url "
							+ "WHERE host=? AND " + "(status=? OR when<?)"
							+ "ORDER BY when ASC");
			query.getStatement().setString(1, host.host);
			query.getStatement().setInt(2, UrlStatus.UN_FETCHED.getValue());
			query.getStatement()
					.setTimestamp(3, getNewTimestamp(fetchInterval));
			query.setMaxResults(limit);
			return (ArrayList<UrlItem>) query.getResultList(UrlItem.class);
		} catch (SQLException e) {
			throw e;
		} finally {
			transaction.close();
		}

	}
}