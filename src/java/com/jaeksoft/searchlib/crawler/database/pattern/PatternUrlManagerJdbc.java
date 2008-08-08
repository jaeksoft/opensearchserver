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

import java.net.MalformedURLException;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import com.jaeksoft.pojojdbc.Query;
import com.jaeksoft.pojojdbc.Transaction;
import com.jaeksoft.searchlib.crawler.database.CrawlDatabaseException;
import com.jaeksoft.searchlib.crawler.database.CrawlDatabaseJdbc;

public class PatternUrlManagerJdbc extends PatternUrlManager {

	private CrawlDatabaseJdbc database;

	final private static Logger logger = Logger
			.getLogger(PatternUrlManagerJdbc.class);

	public PatternUrlManagerJdbc(CrawlDatabaseJdbc database)
			throws CrawlDatabaseException {
		this.database = database;
		updateCache();
	}

	public void addList(List<PatternUrlItem> patternList)
			throws CrawlDatabaseException {
		Transaction transaction = null;
		try {
			transaction = database.getTransaction(false);
			Query query = transaction
					.prepare("INSERT INTO pattern(pattern) VALUES (?)");
			Iterator<PatternUrlItem> it = patternList.iterator();
			while (it.hasNext()) {
				PatternUrlItem item = it.next();
				if (item.getStatus() != PatternUrlItem.Status.UNDEFINED)
					continue;
				try {
					query.getStatement().setString(1, item.getPattern());
					query.update();
					item.setStatus(PatternUrlItem.Status.INJECTED);
				} catch (SQLException e) {
					// Duplicate Key
					if ("23505".equals(e.getSQLState()))
						item.setStatus(PatternUrlItem.Status.ALREADY);
					else
						throw e;
				}
			}
			transaction.commit();
			updateCache();
		} catch (SQLException e) {
			throw new CrawlDatabaseException(e);
		} finally {
			if (transaction != null)
				transaction.close();
		}
	}

	@Override
	public void delPattern(PatternUrlItem item) throws CrawlDatabaseException {
		Transaction transaction = null;
		try {
			transaction = database.getTransaction(false);
			Query query = transaction
					.prepare("DELETE FROM pattern WHERE pattern=?");
			query.getStatement().setString(1, item.getPattern());
			query.update();
			transaction.commit();
			updateCache();
		} catch (SQLException e) {
			throw new CrawlDatabaseException(e);
		} finally {
			if (transaction != null)
				transaction.close();
		}
	}

	@SuppressWarnings("unchecked")
	protected void updateCache() throws CrawlDatabaseException {
		Transaction transaction = null;
		try {
			transaction = database.getTransaction(false);
			Hashtable<String, ArrayList<PatternUrlItem>> newPatternMap = new Hashtable<String, ArrayList<PatternUrlItem>>();
			Query query = transaction.prepare("SELECT pattern FROM pattern");
			List<PatternUrlItem> result = (List<PatternUrlItem>) query
					.getResultList(PatternUrlItem.class);
			for (PatternUrlItem patternUrl : result) {
				if (patternUrl.getPattern() == null)
					continue;
				if (patternUrl.getPattern().length() == 0)
					continue;
				try {
					URL url = patternUrl.extractUrl(true);
					ArrayList<PatternUrlItem> patternList = newPatternMap
							.get(url.getHost());
					if (patternList == null) {
						patternList = new ArrayList<PatternUrlItem>();
						newPatternMap.put(url.getHost(), patternList);
					}
					patternList.add(patternUrl);
				} catch (MalformedURLException e) {
					logger.info(e.getMessage(), e);
					continue;
				}
			}
			synchronized (this) {
				patternUrlMap = newPatternMap;
			}
		} catch (SQLException e) {
			throw new CrawlDatabaseException(e);
		} finally {
			if (transaction != null)
				transaction.close();
		}
	}

	private Query getPatterns(Transaction transaction, String like, boolean asc)
			throws SQLException {
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT pattern FROM pattern");
		if (like != null)
			sql.append(" WHERE pattern LIKE ? ");
		sql.append(" ORDER BY pattern");
		if (!asc)
			sql.append(" DESC");
		Query query = transaction.prepare(sql.toString(),
				ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
		if (like != null)
			query.getStatement().setString(1, "%" + like + "%");
		return query;
	}

	@SuppressWarnings("unchecked")
	public List<PatternUrlItem> getPatterns(String like, boolean asc,
			int start, int rows, PatternUrlList urlList)
			throws CrawlDatabaseException {
		Transaction transaction = null;
		try {
			transaction = database.getTransaction(true);
			Query query = getPatterns(transaction, like, asc);
			query.setFirstResult(start);
			query.setMaxResults(rows);
			List<PatternUrlItem> results = (List<PatternUrlItem>) query
					.getResultList(PatternUrlItem.class);
			urlList.setSize(query.getResultCount());
			return results;
		} catch (SQLException e) {
			throw new CrawlDatabaseException(e);
		} finally {
			if (transaction != null)
				transaction.close();
		}
	}

}
