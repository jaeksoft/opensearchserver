/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2008-2009 Emmanuel Keller / Jaeksoft
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

	private PropertyItem<Integer> fetchInterval;
	private PropertyItem<Integer> maxThreadNumber;
	private PropertyItem<Integer> maxUrlPerHost;
	private PropertyItem<Integer> maxUrlPerSession;
	private PropertyItem<String> userAgent;
	private PropertyItem<Boolean> publishAfterSession;
	private PropertyItem<Boolean> debug;

	public WebPropertyManager(File file) throws IOException {
		super(file);
		fetchInterval = newIntegerProperty("fetchInterval", 30);
		maxThreadNumber = newIntegerProperty("maxThreadNumber", 50);
		maxUrlPerHost = newIntegerProperty("maxUrlPerHost", 100);
		maxUrlPerSession = newIntegerProperty("maxUrlPerSession", 10000);
		userAgent = newStringProperty("userAgent", "OpenSearchServer_Bot");
		publishAfterSession = newBooleanProperty("PublishAfterSession", false);
		debug = newBooleanProperty("debug", false);
	}

	public PropertyItem<Integer> getFetchInterval() {
		return fetchInterval;
	}

	public PropertyItem<Integer> getMaxThreadNumber() {
		return maxThreadNumber;
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

	public PropertyItem<Boolean> getPublishAfterSession() {
		return publishAfterSession;
	}

	public PropertyItem<Boolean> getDebug() {
		return debug;
	}

}
