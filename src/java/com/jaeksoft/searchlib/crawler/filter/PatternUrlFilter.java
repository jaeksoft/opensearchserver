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

package com.jaeksoft.searchlib.crawler.filter;

import java.net.MalformedURLException;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import com.jaeksoft.pojojdbc.Query;
import com.jaeksoft.pojojdbc.Transaction;
import com.jaeksoft.searchlib.config.Config;

public class PatternUrlFilter {

	private Hashtable<String, ArrayList<PatternUrlItem>> patternUrlMap = null;

	private Config config;

	public PatternUrlFilter(Config config) {
		patternUrlMap = null;
		this.config = config;
		updateCache();
	}

	public void addList(List<PatternUrlItem> patternList) {
		Transaction transaction = null;
		try {
			transaction = config.getDatabaseTransaction();
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
			e.printStackTrace();
		} finally {
			if (transaction != null)
				transaction.close();
		}
	}

	public void delPattern(PatternUrlItem item) {
		Transaction transaction = null;
		try {
			transaction = config.getDatabaseTransaction();
			Query query = transaction
					.prepare("DELETE FROM pattern WHERE pattern=?");
			query.getStatement().setString(1, item.getPattern());
			query.update();
			transaction.commit();
			updateCache();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (transaction != null)
				transaction.close();
		}
	}

	@SuppressWarnings("unchecked")
	private void updateCache() {
		Transaction transaction = null;
		try {
			transaction = config.getDatabaseTransaction();
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
					continue;
				}
			}
			synchronized (this) {
				patternUrlMap = newPatternMap;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (transaction != null)
				transaction.close();
		}
	}

	public PatternUrlItem findPatternUrl(URL url) {
		ArrayList<PatternUrlItem> patternList = null;
		synchronized (this) {
			patternList = patternUrlMap.get(url.getHost());
		}
		if (patternList == null)
			return null;
		synchronized (patternList) {
			String sUrl = url.toExternalForm();
			for (PatternUrlItem patternItem : patternList)
				if (patternItem.match(sUrl))
					return patternItem;
			return null;
		}
	}

	public int getSize() {
		Transaction transaction = null;
		try {
			transaction = config.getDatabaseTransaction();
			Query query = transaction.prepare("SELECT count(*) FROM pattern");
			ResultSet rs = query.getResultSet();
			if (rs.next())
				return rs.getInt(1);
			return 0;
		} catch (SQLException e) {
			e.printStackTrace();
			return 0;
		} finally {
			if (transaction != null)
				transaction.close();
		}
	}

	public Query getPattern(Transaction transaction, String like, boolean asc)
			throws SQLException {
		String sql = "SELECT pattern FROM pattern";
		if (like != null)
			sql += " WHERE pattern LIKE ? ";
		sql += " ORDER BY pattern";
		if (!asc)
			sql += " DESC";
		Query query = transaction.prepare(sql);
		if (like != null)
			query.getStatement().setString(1, "%" + like + "%");
		return query;
	}

}
