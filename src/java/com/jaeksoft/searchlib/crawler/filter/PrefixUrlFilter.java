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

public class PrefixUrlFilter {

	private Hashtable<String, ArrayList<PrefixUrl>> prefixUrlList = null;

	private Config config;

	public PrefixUrlFilter(Config config) {
		prefixUrlList = null;
		this.config = config;
		updateCache();
	}

	public void addPrefixList(List<PrefixItem> prefixList) {
		Transaction transaction = null;
		try {
			transaction = config.getDatabaseTransaction();
			Query query = transaction
					.prepare("INSERT INTO prefixurl(url) VALUES (?)");
			Iterator<PrefixItem> it = prefixList.iterator();
			while (it.hasNext()) {
				PrefixItem item = it.next();
				if (item.getStatus() != PrefixItem.Status.UNDEFINED)
					continue;
				try {
					query.getStatement().setString(1, item.getPrefix());
					query.update();
					item.setStatus(PrefixItem.Status.INJECTED);
				} catch (SQLException e) {
					// Duplicate Key
					if ("23505".equals(e.getSQLState()))
						item.setStatus(PrefixItem.Status.ALREADY);
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

	public void delPrefix(PrefixItem item) {
		Transaction transaction = null;
		try {
			transaction = config.getDatabaseTransaction();
			Query query = transaction
					.prepare("DELETE FROM prefixurl WHERE url=?");
			query.getStatement().setString(1, item.getPrefix());
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
			Hashtable<String, ArrayList<PrefixUrl>> newPrefixUrlList = new Hashtable<String, ArrayList<PrefixUrl>>();
			Query query = transaction.prepare("SELECT url FROM prefixurl");
			List<PrefixUrl> result = (List<PrefixUrl>) query
					.getResultList(PrefixUrl.class);
			for (PrefixUrl prefixUrl : result) {
				if (prefixUrl.getUrl() == null)
					continue;
				if (prefixUrl.getUrl().length() == 0)
					continue;
				try {
					URL url = prefixUrl.normalize();
					ArrayList<PrefixUrl> prefixList = newPrefixUrlList.get(url
							.getHost());
					if (prefixList == null) {
						prefixList = new ArrayList<PrefixUrl>();
						newPrefixUrlList.put(url.getHost(), prefixList);
					}
					prefixList.add(prefixUrl);
				} catch (MalformedURLException e) {
					continue;
				}
			}
			synchronized (this) {
				prefixUrlList = newPrefixUrlList;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (transaction != null)
				transaction.close();
		}
	}

	public PrefixUrl findPrefixUrl(URL url) {
		ArrayList<PrefixUrl> prefixList = null;
		synchronized (this) {
			prefixList = prefixUrlList.get(url.getHost());
		}
		if (prefixList == null)
			return null;
		synchronized (prefixList) {
			String sUrl = url.toExternalForm();
			int lUrl = sUrl.length();
			for (PrefixUrl prefixUrl : prefixList)
				if (prefixUrl.lUrl <= lUrl)
					if (prefixUrl.startsWith(sUrl, lUrl))
						return prefixUrl;
			return null;
		}
	}

	public int getSize() {
		Transaction transaction = null;
		try {
			transaction = config.getDatabaseTransaction();
			Query query = transaction.prepare("SELECT count(*) FROM prefixurl");
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

	public Query getPrefix(Transaction transaction, String like, boolean asc)
			throws SQLException {
		String sql = "SELECT url AS prefix FROM prefixurl";
		if (like != null)
			sql += " WHERE url LIKE ? ";
		sql += " ORDER BY url";
		if (!asc)
			sql += " DESC";
		Query query = transaction.prepare(sql);
		if (like != null)
			query.getStatement().setString(1, "%" + like + "%");
		return query;
	}

}
