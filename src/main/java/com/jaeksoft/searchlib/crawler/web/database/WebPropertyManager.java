/**
 * License Agreement for OpenSearchServer
 * <p/>
 * Copyright (C) 2008-2016 Emmanuel Keller / Jaeksoft
 * <p/>
 * http://www.open-search-server.com
 * <p/>
 * This file is part of OpenSearchServer.
 * <p/>
 * OpenSearchServer is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p/>
 * OpenSearchServer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU General Public License
 * along with OpenSearchServer.
 * If not, see <http://www.gnu.org/licenses/>.
 **/

package com.jaeksoft.searchlib.crawler.web.database;

import com.jaeksoft.searchlib.ClientFactory;
import com.jaeksoft.searchlib.crawler.common.database.AbstractPropertyManager;
import com.jaeksoft.searchlib.crawler.web.spider.ProxyHandler;
import com.jaeksoft.searchlib.util.properties.PropertyItem;
import com.jaeksoft.searchlib.util.properties.PropertyItemListener;

import java.io.File;
import java.io.IOException;

public class WebPropertyManager extends AbstractPropertyManager implements PropertyItemListener {

	final private PropertyItem<Integer> delayBetweenAccesses;
	final private PropertyItem<Integer> fetchInterval;
	final private PropertyItem<String> fetchIntervalUnit;
	final private PropertyItem<Integer> maxUrlPerHost;
	final private PropertyItem<Integer> maxUrlPerSession;
	final private PropertyItem<String> userAgent;
	final private PropertyItem<Integer> connectionTimeOut;
	final private PropertyItem<Integer> maxDepth;
	final private PropertyItem<Boolean> exclusionEnabled;
	final private PropertyItem<Boolean> inclusionEnabled;
	final private PropertyItem<Boolean> robotsTxtEnabled;
	final private PropertyItem<Boolean> linkDetectionEnabled;
	final private PropertyItem<String> screenshotBrowser;
	final private PropertyItem<String> screenshotMethod;
	final private PropertyItem<Integer> screenshotCaptureWidth;
	final private PropertyItem<Integer> screenshotCaptureHeight;
	final private PropertyItem<Integer> screenshotResizeWidth;
	final private PropertyItem<Integer> screenshotResizeHeight;
	final private PropertyItem<String> proxyHost;
	final private PropertyItem<Integer> proxyPort;
	final private PropertyItem<String> proxyExclusion;
	final private PropertyItem<Boolean> proxyEnabled;
	final private PropertyItem<String> proxyUsername;
	final private PropertyItem<String> proxyPassword;

	private ProxyHandler proxyHandler = null;

	public WebPropertyManager(File file) throws IOException {
		super(file, 1000);
		delayBetweenAccesses = newIntegerProperty("delayBetweenAccesses", 10,
				ClientFactory.INSTANCE.properties.getMinCrawlerDelay(), null);
		fetchInterval = newIntegerProperty("fetchInterval", 30, 1, null);
		fetchIntervalUnit = newStringProperty("fechIntervalUnit", "days");
		maxUrlPerHost = newIntegerProperty("maxUrlPerHost", 100, 1, null);
		maxUrlPerSession = newIntegerProperty("maxUrlPerSession", 10000, 1, null);
		userAgent = newStringProperty("userAgent", "OpenSearchServer_Bot");
		connectionTimeOut = newIntegerProperty("connectionTimeOut", 600, 0, null);
		exclusionEnabled = newBooleanProperty("exclusionEnabled", true);
		maxDepth = newIntegerProperty("maxDepth", null, null, null);
		inclusionEnabled = newBooleanProperty("inclusionEnabled", true);
		robotsTxtEnabled = newBooleanProperty("robotsTxtEnabled", true);
		linkDetectionEnabled = newBooleanProperty("linkDetectionEnabled", true);
		screenshotMethod = newStringProperty("screenshotMethod", "");
		screenshotBrowser = newStringProperty("screenshotBrowser", "");
		screenshotCaptureWidth = newIntegerProperty("screenshotCaptureWidth", 1024, 1, null);
		screenshotCaptureHeight = newIntegerProperty("screenshotCaptureHeight", 768, 1, null);
		screenshotResizeWidth = newIntegerProperty("screenshotResizeWidth", 240, 1, null);
		screenshotResizeHeight = newIntegerProperty("screenshotResizeHeight", 180, 1, null);
		proxyHost = newStringProperty("proxyHost", "");
		proxyPort = newIntegerProperty("proxyPort", 8080, null, null);
		proxyExclusion = newStringProperty("proxyExclusion", "");
		proxyEnabled = newBooleanProperty("proxyEnabled", false);
		proxyUsername = newStringProperty("proxyUsername", "");
		proxyPassword = newStringProperty("proxyPassword", "");
		proxyHost.addListener(this);
		proxyPort.addListener(this);
		proxyExclusion.addListener(this);
		proxyEnabled.addListener(this);
		proxyUsername.addListener(this);
		proxyPassword.addListener(this);
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

	public PropertyItem<String> getProxyUsername() {
		return proxyUsername;
	}

	public PropertyItem<String> getProxyPassword() {
		return proxyPassword;
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

	public PropertyItem<Integer> getConnectionTimeOut() {
		return connectionTimeOut;
	}

	public PropertyItem<Integer> getMaxDepth() {
		return maxDepth;
	}

	public PropertyItem<String> getFetchIntervalUnit() {
		return fetchIntervalUnit;
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

	public PropertyItem<Boolean> getLinkDetectionEnabled() {
		return linkDetectionEnabled;
	}

	public PropertyItem<String> getScreenshotMethod() {
		return screenshotMethod;
	}

	public PropertyItem<String> getScreenshotBrowser() {
		return screenshotBrowser;
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
	public void hasBeenSet(PropertyItem<?> prop) {
		synchronized (this) {
			if (prop == proxyHost || prop == proxyPort || prop == proxyExclusion || prop == proxyUsername
					|| prop == proxyPassword)
				proxyHandler = null;
		}
	}

	public ProxyHandler getProxyHandler() throws IOException {
		synchronized (this) {
			if (!proxyEnabled.getValue())
				return null;
			if (proxyHandler != null)
				return proxyHandler;
			proxyHandler = new ProxyHandler(this);
			return proxyHandler;
		}
	}

}
