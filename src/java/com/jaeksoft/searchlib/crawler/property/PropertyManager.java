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

package com.jaeksoft.searchlib.crawler.property;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import com.jaeksoft.pojojdbc.Query;
import com.jaeksoft.pojojdbc.Transaction;
import com.jaeksoft.searchlib.config.Config;

public class PropertyManager {

	private Config config;

	protected enum Property {

		FETCH_INTERVAL("fetchInterval"), MAX_THREAD_NUMBER("maxThreadNumber"), MAX_URL_PER_HOST(
				"maxUrlPerHost"), MAX_URL_PER_SESSION("maxUrlPerSession"), USER_AGENT(
				"userAgent"), DELAY_BETWEEN_ACCESSES("delayBetweenAccesses"), CRAWL_ENABLED(
				"crawlEnabled");

		private String name;

		private Property(String name) {
			this.name = name;
		}

		@Override
		public String toString() {
			return name;
		}
	}

	public PropertyManager(Config config) {
		this.config = config;
	}

	private void update(Transaction transaction, PropertyItem property)
			throws SQLException {
		Query query = transaction.prepare("UPDATE property "
				+ "SET value=? WHERE name=?");
		PreparedStatement st = query.getStatement();
		st.setString(1, property.getName());
		query.update();
	}

	private void insert(Transaction transaction, PropertyItem property)
			throws SQLException {
		Query query = transaction.prepare("INSERT INTO "
				+ "property(name,value) " + "VALUES (?,?)");
		PreparedStatement st = query.getStatement();
		st.setString(1, property.getName());
		st.setString(2, property.getValue());
		query.update();
	}

	private void insertOrUpdate(Transaction transaction, PropertyItem property,
			boolean bIgnoreDuplicate) throws SQLException {
		try {
			insert(transaction, property);
		} catch (SQLException e) {
			// Duplicate Key
			if ("23505".equals(e.getSQLState())) {
				if (bIgnoreDuplicate)
					return;
				update(transaction, property);
				return;
			}
			throw e;
		}
	}

	@SuppressWarnings("unchecked")
	private String getPropertyString(Property prop) {
		Transaction transaction = null;
		try {
			transaction = config.getDatabaseTransaction();
			Query query = transaction.prepare("SELECT value FROM property "
					+ "WHERE name=?");
			query.getStatement().setString(1, prop.name);
			List<PropertyItem> propertyList = (List<PropertyItem>) query
					.getResultList(PropertyItem.class);
			if (propertyList == null || propertyList.size() == 0)
				return null;
			return propertyList.get(0).getName();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		} finally {
			if (transaction != null)
				transaction.close();
		}
	}

	private boolean getPropertyBoolean(Property prop) {
		String v = getPropertyString(prop);
		if (v == null)
			return false;
		return "1".equals(v) || "true".equalsIgnoreCase(v)
				|| "yes".equalsIgnoreCase(v);
	}

	private Integer getPropertyInteger(Property prop) {
		String v = getPropertyString(prop);
		if (v == null)
			return null;
		return Integer.parseInt(v);
	}

	private void setProperty(PropertyItem property) {
		Transaction transaction = null;
		try {
			transaction = config.getDatabaseTransaction();
			insertOrUpdate(transaction, property, true);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		} finally {
			if (transaction != null)
				transaction.close();
		}
	}

	public boolean isCrawlEnabled() {
		return getPropertyBoolean(Property.CRAWL_ENABLED);
	}

	public int getFetchInterval() {
		Integer v = getPropertyInteger(Property.FETCH_INTERVAL);
		if (v == null)
			return 30;
		return v;
	}

	public void setFetchInterval(int v) {
		setProperty(new PropertyItem(Property.FETCH_INTERVAL.name, v));
	}

	public int getMaxUrlPerSession() {
		Integer v = getPropertyInteger(Property.MAX_URL_PER_SESSION);
		if (v == null)
			return 10000;
		return v;
	}

	public void setMaxUrlPerSession(int v) {
		setProperty(new PropertyItem(Property.MAX_URL_PER_SESSION.name, v));
	}

	public int getMaxUrlPerHost() {
		Integer v = getPropertyInteger(Property.MAX_URL_PER_HOST);
		if (v == null)
			return 1000;
		return v;
	}

	public void setMaxUrlPerHost(int v) {
		setProperty(new PropertyItem(Property.MAX_URL_PER_HOST.name, v));
	}

	public int getDelayBetweenAccesses() {
		Integer v = getPropertyInteger(Property.DELAY_BETWEEN_ACCESSES);
		if (v == null)
			return 10;
		return v;
	}

	public void setDelayBetweenAccesses(int v) {
		setProperty(new PropertyItem(Property.DELAY_BETWEEN_ACCESSES.name, v));
	}

	public String getUserAgent() {
		String v = getPropertyString(Property.USER_AGENT);
		if (v == null || v.trim().length() == 0)
			return "JaeksoftWebSearchBot";
		return v;
	}

	public void setUserAgent(String v) {
		setProperty(new PropertyItem(Property.USER_AGENT.name, v));
	}

	public int getMaxThreadNumber() {
		Integer v = getPropertyInteger(Property.MAX_THREAD_NUMBER);
		if (v == null)
			return 10;
		return v;
	}

	public void setMaxThreadNumber(int v) {
		setProperty(new PropertyItem(Property.MAX_THREAD_NUMBER.name, v));
	}

}
