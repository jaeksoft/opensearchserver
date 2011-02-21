/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2008-2011 Emmanuel Keller / Jaeksoft
 * 
 * http://www.open-search-server.com
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
import java.io.IOException;

import com.jaeksoft.searchlib.crawler.common.database.PropertyItem;
import com.jaeksoft.searchlib.crawler.common.database.PropertyManager;

public class WebPropertyManager extends PropertyManager {

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

	public WebPropertyManager(File file) throws IOException {
		super(file);
		delayBetweenAccesses = newIntegerProperty("delayBetweenAccesses", 10);
		fetchInterval = newIntegerProperty("fetchInterval", 30);
		fetchIntervalUnit = newStringProperty("fechIntervalUnit", "days");
		maxUrlPerHost = newIntegerProperty("maxUrlPerHost", 100);
		maxUrlPerSession = newIntegerProperty("maxUrlPerSession", 10000);
		userAgent = newStringProperty("userAgent", "OpenSearchServer_Bot");
		replicationAfterSession = newBooleanProperty("replicationAfterSession",
				false);
		replicationTarget = newStringProperty("replicationTarget", "");
		exclusionEnabled = newBooleanProperty("exclusionEnabled", true);
		inclusionEnabled = newBooleanProperty("inclusionEnabled", true);
		robotsTxtEnabled = newBooleanProperty("robotsTxtEnabled", true);
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

}
