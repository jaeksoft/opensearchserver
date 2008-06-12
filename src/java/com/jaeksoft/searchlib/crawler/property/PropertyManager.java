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
		st.setString(1, property.getValue());
		st.setString(2, property.getName());
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
			transaction = config.getDatabaseTransaction(true);
			Query query = transaction.prepare("SELECT value FROM property "
					+ "WHERE name=?");
			query.getStatement().setString(1, prop.name);
			List<PropertyItem> propertyList = (List<PropertyItem>) query
					.getResultList(PropertyItem.class);
			if (propertyList == null || propertyList.size() == 0)
				return null;
			return propertyList.get(0).getValue();
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
			transaction = config.getDatabaseTransaction(false);
			insertOrUpdate(transaction, property, false);
			transaction.commit();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		} finally {
			if (transaction != null)
				transaction.close();
		}
	}

	private Boolean crawlEnabled = null;

	public boolean isCrawlEnabled() {
		if (crawlEnabled == null)
			crawlEnabled = getPropertyBoolean(Property.CRAWL_ENABLED);
		return crawlEnabled;
	}

	public void setCrawlEnabled(boolean b) {
		if (crawlEnabled != null)
			if (crawlEnabled.booleanValue() == b)
				return;
		crawlEnabled = b;
		setProperty(new PropertyItem(Property.CRAWL_ENABLED.name, crawlEnabled));
	}

	private Integer fetchInterval = null;

	public int getFetchInterval() {
		if (fetchInterval == null)
			fetchInterval = getPropertyInteger(Property.FETCH_INTERVAL);
		if (fetchInterval == null)
			fetchInterval = 30;
		return fetchInterval;
	}

	public void setFetchInterval(int v) {
		if (fetchInterval != null)
			if (fetchInterval.intValue() == v)
				return;
		fetchInterval = v;
		setProperty(new PropertyItem(Property.FETCH_INTERVAL.name,
				fetchInterval));
	}

	private Integer maxUrlPerSession = null;

	public int getMaxUrlPerSession() {
		if (maxUrlPerSession == null)
			maxUrlPerSession = getPropertyInteger(Property.MAX_URL_PER_SESSION);
		if (maxUrlPerSession == null)
			maxUrlPerSession = 10000;
		return maxUrlPerSession;
	}

	public void setMaxUrlPerSession(int v) {
		if (maxUrlPerSession != null)
			if (maxUrlPerSession.intValue() == v)
				return;
		maxUrlPerSession = v;
		setProperty(new PropertyItem(Property.MAX_URL_PER_SESSION.name,
				maxUrlPerSession));
	}

	private Integer maxUrlPerHost = null;

	public int getMaxUrlPerHost() {
		if (maxUrlPerHost == null)
			maxUrlPerHost = getPropertyInteger(Property.MAX_URL_PER_HOST);
		if (maxUrlPerHost == null)
			maxUrlPerHost = 1000;
		return maxUrlPerHost;
	}

	public void setMaxUrlPerHost(int v) {
		if (maxUrlPerHost != null)
			if (maxUrlPerHost.intValue() == v)
				return;
		maxUrlPerHost = v;
		setProperty(new PropertyItem(Property.MAX_URL_PER_HOST.name,
				maxUrlPerHost));
	}

	private Integer delayBetweenAccesses = null;

	public int getDelayBetweenAccesses() {
		if (delayBetweenAccesses == null)
			delayBetweenAccesses = getPropertyInteger(Property.DELAY_BETWEEN_ACCESSES);
		if (delayBetweenAccesses == null)
			delayBetweenAccesses = 10;
		return delayBetweenAccesses;
	}

	public void setDelayBetweenAccesses(int v) {
		if (delayBetweenAccesses != null)
			if (delayBetweenAccesses.intValue() == v)
				return;
		delayBetweenAccesses = v;
		setProperty(new PropertyItem(Property.DELAY_BETWEEN_ACCESSES.name,
				delayBetweenAccesses));
	}

	private String userAgent = null;

	public String getUserAgent() {
		if (userAgent == null)
			userAgent = getPropertyString(Property.USER_AGENT);
		if (userAgent == null || userAgent.trim().length() == 0)
			userAgent = "JaeksoftWebSearchBot";
		return userAgent;
	}

	public void setUserAgent(String v) {
		if (userAgent != null)
			if (userAgent.equals(v))
				return;
		userAgent = v;
		setProperty(new PropertyItem(Property.USER_AGENT.name, userAgent));
	}

	private Integer maxThreadNumber = null;

	public int getMaxThreadNumber() {
		if (maxThreadNumber == null)
			maxThreadNumber = getPropertyInteger(Property.MAX_THREAD_NUMBER);
		if (maxThreadNumber == null)
			maxThreadNumber = 10;
		return maxThreadNumber;
	}

	public void setMaxThreadNumber(int v) {
		if (maxThreadNumber != null)
			if (maxThreadNumber.equals(v))
				return;
		maxThreadNumber = v;
		setProperty(new PropertyItem(Property.MAX_THREAD_NUMBER.name,
				maxThreadNumber));
	}

}
