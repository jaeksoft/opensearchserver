/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2008 Emmanuel Keller / Jaeksoft
 * 
 * http://www.jaeksoft.com
 * 
 * This file is part of Jaeksoft OpenSearchServer.
 *
 * Jaeksoft OpenSearchServer is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * Jaeksoft OpenSearchServer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Jaeksoft OpenSearchServer. 
 *  If not, see <http://www.gnu.org/licenses/>.
 **/

package com.jaeksoft.searchlib.crawler.web.database;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class PropertyManager {

	protected enum Property {

		FETCH_INTERVAL("fetchInterval"), MAX_THREAD_NUMBER("maxThreadNumber"), MAX_URL_PER_HOST(
				"maxUrlPerHost"), MAX_URL_PER_SESSION("maxUrlPerSession"), USER_AGENT(
				"userAgent"), DELAY_BETWEEN_ACCESSES("delayBetweenAccesses"), CRAWL_ENABLED(
				"crawlEnabled"), OPTIMIZE_AFTER_SESSION("optimizeAfterSession"), PUBLISH_AFTER_SESSION(
				"PublishAfterSession"), DRY_RUN("dryRun"), DEBUG("debug"), INDEX_DOCUMENT_BUFFER_SIZE(
				"indexDocumentBufferSize");

		private String name;

		private Property(String name) {
			this.name = name;
		}

		@Override
		public String toString() {
			return name;
		}

		public String getName() {
			return name;
		}
	}

	private Properties properties;

	private File propFile;

	public PropertyManager(File file) throws IOException {
		propFile = file;
		properties = new Properties();
		if (propFile.exists()) {
			FileInputStream inputStream = null;
			try {
				inputStream = new FileInputStream(file);
				properties.loadFromXML(inputStream);
			} catch (IOException e) {
				throw e;
			} finally {
				if (inputStream != null)
					inputStream.close();
			}
		}
	}

	protected String getPropertyString(Property prop) {
		return properties.getProperty(prop.name);
	}

	protected void setProperty(PropertyItem property) throws IOException {
		if (property.getValue().equals(
				properties.getProperty(property.getName())))
			return;
		properties.setProperty(property.getName(), property.getValue());
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(propFile);
			properties.storeToXML(fos, "");
		} catch (IOException e) {
			throw e;
		} finally {
			if (fos != null)
				fos.close();
		}
	}

	protected Boolean getPropertyBoolean(Property prop) {
		String v = getPropertyString(prop);
		if (v == null)
			return null;
		return "1".equals(v) || "true".equalsIgnoreCase(v)
				|| "yes".equalsIgnoreCase(v);
	}

	protected Integer getPropertyInteger(Property prop) {
		String v = getPropertyString(prop);
		if (v == null)
			return null;
		return Integer.parseInt(v);
	}

	private Boolean crawlEnabled = null;

	public boolean isCrawlEnabled() {
		if (crawlEnabled == null)
			crawlEnabled = getPropertyBoolean(Property.CRAWL_ENABLED);
		if (crawlEnabled == null)
			crawlEnabled = false;
		return crawlEnabled;
	}

	public void setCrawlEnabled(boolean b) throws IOException {
		if (crawlEnabled != null)
			if (crawlEnabled.booleanValue() == b)
				return;
		crawlEnabled = b;
		setProperty(new PropertyItem(Property.CRAWL_ENABLED.name, crawlEnabled));
	}

	private Boolean dryRun = null;

	public boolean isDryRun() {
		if (dryRun == null)
			dryRun = getPropertyBoolean(Property.DRY_RUN);
		if (dryRun == null)
			dryRun = false;
		return dryRun;
	}

	public void setDryRun(boolean b) throws IOException {
		if (dryRun != null)
			if (dryRun.booleanValue() == b)
				return;
		dryRun = b;
		setProperty(new PropertyItem(Property.DRY_RUN.name, dryRun));
	}

	private Boolean debug = null;

	public boolean isDebug() {
		if (debug == null)
			debug = getPropertyBoolean(Property.DEBUG);
		if (debug == null)
			debug = false;
		return debug;
	}

	public void setDebug(boolean b) throws IOException {
		if (debug != null)
			if (debug.booleanValue() == b)
				return;
		debug = b;
		setProperty(new PropertyItem(Property.DEBUG.name, debug));
	}

	private Integer fetchInterval = null;

	public int getFetchInterval() {
		if (fetchInterval == null)
			fetchInterval = getPropertyInteger(Property.FETCH_INTERVAL);
		if (fetchInterval == null)
			fetchInterval = 30;
		return fetchInterval;
	}

	public void setFetchInterval(int v) throws IOException {
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

	public void setMaxUrlPerSession(int v) throws IOException {
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

	public void setMaxUrlPerHost(int v) throws IOException {
		if (maxUrlPerHost != null)
			if (maxUrlPerHost.intValue() == v)
				return;
		maxUrlPerHost = v;
		setProperty(new PropertyItem(Property.MAX_URL_PER_HOST.name,
				maxUrlPerHost));
	}

	private Integer indexDocumentBufferSize = null;

	public int getIndexDocumentBufferSize() {
		if (indexDocumentBufferSize == null)
			indexDocumentBufferSize = getPropertyInteger(Property.INDEX_DOCUMENT_BUFFER_SIZE);
		if (indexDocumentBufferSize == null)
			indexDocumentBufferSize = 10000;
		return indexDocumentBufferSize;
	}

	public void setIndexDocumentBufferSize(int v) throws IOException {
		if (indexDocumentBufferSize != null)
			if (indexDocumentBufferSize.intValue() == v)
				return;
		indexDocumentBufferSize = v;
		setProperty(new PropertyItem(Property.INDEX_DOCUMENT_BUFFER_SIZE.name,
				indexDocumentBufferSize));
	}

	private Integer delayBetweenAccesses = null;

	public int getDelayBetweenAccesses() {
		if (delayBetweenAccesses == null)
			delayBetweenAccesses = getPropertyInteger(Property.DELAY_BETWEEN_ACCESSES);
		if (delayBetweenAccesses == null)
			delayBetweenAccesses = 10;
		return delayBetweenAccesses;
	}

	public void setDelayBetweenAccesses(int v) throws IOException {
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

	public void setUserAgent(String v) throws IOException {
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

	public void setMaxThreadNumber(int v) throws IOException {
		if (maxThreadNumber != null)
			if (maxThreadNumber.equals(v))
				return;
		maxThreadNumber = v;
		setProperty(new PropertyItem(Property.MAX_THREAD_NUMBER.name,
				maxThreadNumber));
	}

	private Boolean optimizeAfterSession = null;

	public boolean isOptimizeAfterSession() {
		if (optimizeAfterSession == null)
			optimizeAfterSession = getPropertyBoolean(Property.OPTIMIZE_AFTER_SESSION);
		if (optimizeAfterSession == null)
			optimizeAfterSession = true;
		return optimizeAfterSession;
	}

	public void setOptimizeAfterSession(boolean v) throws IOException {
		if (optimizeAfterSession != null)
			if (optimizeAfterSession == v)
				return;
		optimizeAfterSession = v;
		setProperty(new PropertyItem(Property.OPTIMIZE_AFTER_SESSION.name,
				optimizeAfterSession));
	}

	private Boolean publishAfterSession = null;

	public boolean isPublishAfterSession() {
		if (publishAfterSession == null)
			publishAfterSession = getPropertyBoolean(Property.PUBLISH_AFTER_SESSION);
		if (publishAfterSession == null)
			publishAfterSession = true;
		return publishAfterSession;
	}

	public void setPublishAfterSession(boolean v) throws IOException {
		if (publishAfterSession != null)
			if (publishAfterSession == v)
				return;
		publishAfterSession = v;
		setProperty(new PropertyItem(Property.PUBLISH_AFTER_SESSION.name,
				publishAfterSession));
	}

}
