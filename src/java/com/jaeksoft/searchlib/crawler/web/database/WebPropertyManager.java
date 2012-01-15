/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008-2012 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.crawler.web.database;

import java.io.File;
import java.io.IOException;

import com.jaeksoft.searchlib.ClientFactory;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.crawler.common.database.PropertyItem;
import com.jaeksoft.searchlib.crawler.common.database.PropertyItemListener;
import com.jaeksoft.searchlib.crawler.common.database.PropertyManager;
import com.jaeksoft.searchlib.crawler.web.spider.ProxyHandler;

public class WebPropertyManager extends PropertyManager implements
		PropertyItemListener {

	private PropertyItem<Integer> delayBetweenAccesses;
	private PropertyItem<Integer> fetchInterval;
	private PropertyItem<String> fetchIntervalUnit;
	private PropertyItem<Integer> maxUrlPerHost;
	private PropertyItem<Integer> maxUrlPerSession;
	private PropertyItem<String> userAgent;
	private PropertyItem<Boolean> replicationAfterSession;
	private PropertyItem<String> replicationTarget;
	private PropertyItem<Boolean> exclusionEnabled;
	private PropertyItem<Boolean> inclusionEnabled;
	private PropertyItem<Boolean> robotsTxtEnabled;
	private PropertyItem<String> screenshotMethod;
	private PropertyItem<Integer> screenshotCaptureWidth;
	private PropertyItem<Integer> screenshotCaptureHeight;
	private PropertyItem<Integer> screenshotResizeWidth;
	private PropertyItem<Integer> screenshotResizeHeight;
	private PropertyItem<String> proxyHost;
	private PropertyItem<Integer> proxyPort;
	private PropertyItem<String> proxyExclusion;
	private PropertyItem<Boolean> proxyEnabled;

	private ProxyHandler proxyHandler = null;

	public WebPropertyManager(File file) throws IOException {
		super(file);
		delayBetweenAccesses = newIntegerProperty("delayBetweenAccesses", 10,
				ClientFactory.INSTANCE.properties.getMinCrawlerDelay(), null);
		fetchInterval = newIntegerProperty("fetchInterval", 30, 1, null);
		fetchIntervalUnit = newStringProperty("fechIntervalUnit", "days");
		maxUrlPerHost = newIntegerProperty("maxUrlPerHost", 100, 1, null);
		maxUrlPerSession = newIntegerProperty("maxUrlPerSession", 10000, 1,
				null);
		userAgent = newStringProperty("userAgent", "OpenSearchServer_Bot");
		replicationAfterSession = newBooleanProperty("replicationAfterSession",
				false);
		replicationTarget = newStringProperty("replicationTarget", "");
		exclusionEnabled = newBooleanProperty("exclusionEnabled", true);
		inclusionEnabled = newBooleanProperty("inclusionEnabled", true);
		robotsTxtEnabled = newBooleanProperty("robotsTxtEnabled", true);
		screenshotMethod = newStringProperty("screenshotMethod", "");
		screenshotCaptureWidth = newIntegerProperty("screenshotCaptureWidth",
				1024, 1, null);
		screenshotCaptureHeight = newIntegerProperty("screenshotCaptureHeight",
				768, 1, null);
		screenshotResizeWidth = newIntegerProperty("screenshotResizeWidth",
				240, 1, null);
		screenshotResizeHeight = newIntegerProperty("screenshotResizeHeight",
				180, 1, null);
		proxyHost = newStringProperty("proxyHost", "");
		proxyPort = newIntegerProperty("proxyPort", 8080, null, null);
		proxyExclusion = newStringProperty("proxyExclusion", "");
		proxyEnabled = newBooleanProperty("proxyEnabled", false);
		proxyHost.addListener(this);
		proxyPort.addListener(this);
		proxyExclusion.addListener(this);
	}

	public PropertyItem<String> getProxyHost() {
		return proxyHost;
	}

	public PropertyItem<String> getProxyExclusion() {
		return proxyExclusion;
	}

	public PropertyItem<Boolean> getProxyEnabled() {
		return proxyEnabled;
	}

	public PropertyItem<Integer> getProxyPort() {
		return proxyPort;
	}

	public PropertyItem<Integer> getFetchInterval() {
		return fetchInterval;
	}

	public PropertyItem<Integer> getMaxUrlPerHost() {
		return maxUrlPerHost;
	}

	public PropertyItem<Integer> getMaxUrlPerSession() {
		return maxUrlPerSession;
	}

	public PropertyItem<String> getUserAgent() {
		return userAgent;
	}

	public PropertyItem<String> getFetchIntervalUnit() {
		return fetchIntervalUnit;
	}

	public PropertyItem<Boolean> getReplicationAfterSession() {
		return replicationAfterSession;
	}

	public PropertyItem<String> getReplicationTarget() {
		return replicationTarget;
	}

	public PropertyItem<Boolean> getInclusionEnabled() {
		return inclusionEnabled;
	}

	public PropertyItem<Boolean> getExclusionEnabled() {
		return exclusionEnabled;
	}

	public PropertyItem<Integer> getDelayBetweenAccesses() {
		return delayBetweenAccesses;
	}

	public PropertyItem<Boolean> getRobotsTxtEnabled() {
		return robotsTxtEnabled;
	}

	public PropertyItem<String> getScreenshotMethod() {
		return screenshotMethod;
	}

	public PropertyItem<Integer> getScreenshotCaptureWidth() {
		return screenshotCaptureWidth;
	}

	public PropertyItem<Integer> getScreenshotCaptureHeight() {
		return screenshotCaptureHeight;
	}

	public PropertyItem<Integer> getScreenshotResizeWidth() {
		return screenshotResizeWidth;
	}

	public PropertyItem<Integer> getScreenshotResizeHeight() {
		return screenshotResizeHeight;
	}

	@Override
	public void hasBeenSet(PropertyItem<?> prop) throws SearchLibException {
		synchronized (this) {
			if (prop == proxyHost || prop == proxyPort
					|| prop == proxyExclusion)
				proxyHandler = null;
		}
	}

	public ProxyHandler getProxyHandler() throws SearchLibException {
		synchronized (this) {
			if (proxyHandler != null)
				return proxyHandler;
			proxyHandler = new ProxyHandler(this);
			return proxyHandler;
		}
	}
}
