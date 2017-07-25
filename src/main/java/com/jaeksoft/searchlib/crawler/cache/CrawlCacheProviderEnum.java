/*
 * License Agreement for OpenSearchServer
 * <p>
 * Copyright (C) 2012-2017 Emmanuel Keller / Jaeksoft
 * <p>
 * http://www.open-search-server.com
 * <p>
 * This file is part of OpenSearchServer.
 * <p>
 * OpenSearchServer is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * OpenSearchServer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with OpenSearchServer.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package com.jaeksoft.searchlib.crawler.cache;

public enum CrawlCacheProviderEnum {

	LOCAL_FILE("Local file system", LocalFileCrawlCache.class), MONGODB("MongoDB", MongoDbCrawlCache.class);

	private Class<? extends CrawlCacheProvider> providerClass;

	private String label;

	CrawlCacheProviderEnum(String label, Class<? extends CrawlCacheProvider> providerClass) {
		this.providerClass = providerClass;
		this.label = label;
	}

	public CrawlCacheProvider getNewInstance() throws InstantiationException, IllegalAccessException {
		return providerClass.newInstance();
	}

	public static CrawlCacheProviderEnum find(String property) {
		for (CrawlCacheProviderEnum crawlCacheProviderEnum : CrawlCacheProviderEnum.values())
			if (crawlCacheProviderEnum.name().equals(property))
				return crawlCacheProviderEnum;
		return LOCAL_FILE;
	}

	@Override
	public String toString() {
		return label;
	}

	public String getName() {
		return label;
	}
}
