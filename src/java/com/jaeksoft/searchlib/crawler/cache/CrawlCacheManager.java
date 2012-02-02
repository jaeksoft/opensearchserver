/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2012 Emmanuel Keller / Jaeksoft
 * 
 * http://www.open-search-server.com
 * 
 * This file is part of OpenSearchServer.
 *
 * OpenSearchServer is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * OpenSearchServer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with OpenSearchServer. 
 *  If not, see <http://www.gnu.org/licenses/>.
 **/

package com.jaeksoft.searchlib.crawler.cache;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.InvalidPropertiesFormatException;
import java.util.Properties;

import org.json.JSONException;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.crawler.web.spider.DownloadItem;
import com.jaeksoft.searchlib.util.PropertiesUtils;
import com.jaeksoft.searchlib.util.ReadWriteLock;
import com.jaeksoft.searchlib.web.StartStopListener;

public class CrawlCacheManager implements Closeable {

	private final ReadWriteLock rwl = new ReadWriteLock();

	private final static String CRAWLCACHE_PROPERTY_FILE = "crawlCache.xml";

	private final static String CRAWLCACHE_PROPERTY_ENABLED = "enabled";

	private final static String CRAWLCACHE_PROPERTY_EXPIRATION_VALUE = "expirationValue";

	private final static String CRAWCACHE_PROPERTY_EXPIRATION_UNIT = "expirationUnit";

	private final static String CRAWCACHE_PROPERTY_PROVIDER_TYPE = "provider";

	private final static String CRAWCACHE_PROPERTY_CONFIGURATION = "configuration";

	private CrawlCacheProvider crawlCache;

	private CrawlCacheProviderEnum crawlCacheProvider;

	private boolean enabled;

	private int expirationValue;

	private String expirationUnit;

	private String configuration;

	private File propFile;

	public CrawlCacheManager(File dataDir)
			throws InvalidPropertiesFormatException, IOException,
			InstantiationException, IllegalAccessException {
		crawlCache = null;
		propFile = new File(StartStopListener.OPENSEARCHSERVER_DATA_FILE,
				CRAWLCACHE_PROPERTY_FILE);
		Properties properties = PropertiesUtils.loadFromXml(propFile);
		enabled = "true".equals(properties.getProperty(
				CRAWLCACHE_PROPERTY_ENABLED, "false"));
		expirationValue = Integer.parseInt(properties.getProperty(
				CRAWLCACHE_PROPERTY_EXPIRATION_VALUE, "0"));
		expirationUnit = properties.getProperty(
				CRAWCACHE_PROPERTY_EXPIRATION_UNIT, "days");
		crawlCacheProvider = CrawlCacheProviderEnum.find(properties
				.getProperty(CRAWCACHE_PROPERTY_PROVIDER_TYPE));
		configuration = properties
				.getProperty(CRAWCACHE_PROPERTY_CONFIGURATION);
		crawlCache = crawlCacheProvider.getNewInstance();
		setEnabled(enabled);
	}

	private void save() throws IOException {
		Properties properties = new Properties();
		properties.setProperty(CRAWLCACHE_PROPERTY_ENABLED,
				Boolean.toString(enabled));
		properties.setProperty(CRAWLCACHE_PROPERTY_EXPIRATION_VALUE,
				Integer.toString(expirationValue));
		properties.setProperty(CRAWCACHE_PROPERTY_EXPIRATION_UNIT,
				expirationUnit);
		properties.setProperty(CRAWCACHE_PROPERTY_PROVIDER_TYPE,
				crawlCacheProvider.name());
		if (configuration != null)
			properties.setProperty(CRAWCACHE_PROPERTY_CONFIGURATION,
					configuration);
		PropertiesUtils.storeToXml(properties, propFile);
	}

	@Override
	public void close() {
		rwl.w.lock();
		try {
			if (crawlCache != null)
				crawlCache.close();
		} finally {
			rwl.w.unlock();
		}
	}

	public String getInfos() throws IOException {
		rwl.r.lock();
		try {
			return crawlCache.getInfos();
		} finally {
			rwl.r.unlock();
		}
	}

	public String getConfigurationInformation() throws IOException {
		rwl.r.lock();
		try {
			return crawlCache.getConfigurationInformation();
		} finally {
			rwl.r.unlock();
		}
	}

	public InputStream storeCache(DownloadItem downloadItem)
			throws IOException, JSONException {
		rwl.r.lock();
		try {
			if (!enabled)
				return downloadItem.getContentInputStream();
			else
				return crawlCache.store(downloadItem);
		} finally {
			rwl.r.unlock();
		}
	}

	private long getExpirationDate() {
		if (expirationValue == 0)
			return 0;
		long l;
		if ("hours".equalsIgnoreCase(expirationUnit))
			l = expirationValue * 1000 * 3600;
		else if ("minutes".equalsIgnoreCase(expirationUnit))
			l = expirationValue * 1000 * 60;
		else
			// Default is days
			l = expirationValue * 1000 * 86400;
		return System.currentTimeMillis() - l;
	}

	public DownloadItem loadCache(URI uri) throws IOException, JSONException,
			URISyntaxException {
		rwl.r.lock();
		try {
			if (!enabled)
				return null;
			return crawlCache.load(uri, getExpirationDate());
		} finally {
			rwl.r.unlock();
		}
	}

	public long flushCache(boolean expiration) throws IOException {
		rwl.r.lock();
		try {
			long exp = expiration ? getExpirationDate() : System
					.currentTimeMillis();
			return crawlCache.flush(exp);
		} finally {
			rwl.r.unlock();
		}
	}

	/**
	 * @return the enabled
	 */
	public boolean isEnabled() {
		rwl.r.lock();
		try {
			return enabled;
		} finally {
			rwl.r.unlock();
		}
	}

	public boolean isDisabled() {
		return !isEnabled();
	}

	/**
	 * @param enabled
	 *            the enabled to set
	 * @throws IOException
	 */
	public void setEnabled(boolean enabled) throws IOException {
		rwl.w.lock();
		try {
			if (!enabled)
				crawlCache.close();
			else
				crawlCache.init(configuration);
			this.enabled = enabled;
			save();
		} finally {
			rwl.w.unlock();
		}
	}

	/**
	 * @return the expirationValue
	 */
	public int getExpirationValue() {
		rwl.r.lock();
		try {
			return expirationValue;
		} finally {
			rwl.r.unlock();
		}
	}

	public boolean isExpiration() {
		rwl.r.lock();
		try {
			return getExpirationDate() != 0;
		} finally {
			rwl.r.unlock();
		}
	}

	/**
	 * @param expirationValue
	 *            the expirationValue to set
	 * @throws IOException
	 */
	public void setExpirationValue(int expirationValue) throws IOException {
		rwl.w.lock();
		try {
			this.expirationValue = expirationValue;
			save();
		} finally {
			rwl.w.unlock();
		}
	}

	private final static String[] expirationUnitValues = { "days", "hours",
			"minutes" };

	public String[] getExpirationUnitValues() {
		return expirationUnitValues;
	}

	/**
	 * @return the expirationUnit
	 */
	public String getExpirationUnit() {
		rwl.r.lock();
		try {
			return expirationUnit;
		} finally {
			rwl.r.unlock();
		}
	}

	/**
	 * @param expirationUnit
	 *            the expirationUnit to set
	 * @throws IOException
	 */
	public void setExpirationUnit(String expirationUnit) throws IOException {
		rwl.w.lock();
		try {
			this.expirationUnit = expirationUnit;
			save();
		} finally {
			rwl.w.unlock();
		}
	}

	/**
	 * @return the crawlCacheProvider
	 */
	public CrawlCacheProviderEnum getCrawlCacheProvider() {
		return crawlCacheProvider;
	}

	/**
	 * @param crawlCacheProvider
	 *            the crawlCacheProvider to set
	 * @throws SearchLibException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws IOException
	 */
	public void setCrawlCacheProvider(CrawlCacheProviderEnum crawlCacheProvider)
			throws SearchLibException, InstantiationException,
			IllegalAccessException, IOException {
		rwl.w.lock();
		try {
			if (enabled)
				throw new SearchLibException("Crawl cache is running");
			if (this.crawlCacheProvider == crawlCacheProvider)
				return;
			this.crawlCacheProvider = crawlCacheProvider;
			this.crawlCache = crawlCacheProvider.getNewInstance();
			save();
		} finally {
			rwl.w.unlock();
		}
	}

	/**
	 * @return the configuration
	 */
	public String getConfiguration() {
		rwl.r.lock();
		try {
			return configuration;
		} finally {
			rwl.r.unlock();
		}
	}

	/**
	 * @param configuration
	 *            the configuration to set
	 * @throws SearchLibException
	 * @throws IOException
	 */
	public void setConfiguration(String configuration)
			throws SearchLibException, IOException {
		rwl.w.lock();
		try {
			if (enabled)
				throw new SearchLibException("Crawl crawl is running");
			this.configuration = configuration;
			save();
		} finally {
			rwl.w.unlock();
		}
	}

}
