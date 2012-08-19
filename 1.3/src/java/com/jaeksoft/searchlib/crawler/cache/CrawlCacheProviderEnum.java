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

public enum CrawlCacheProviderEnum {

	LOCAL_FILE("Local file system", LocalFileCrawlCache.class),

	HADOOP("Hadoop file system (HDFS)", HadoopCrawlCache.class);

	private Class<? extends CrawlCacheProvider> providerClass;

	private String label;

	private CrawlCacheProviderEnum(String label,
			Class<? extends CrawlCacheProvider> providerClass) {
		this.providerClass = providerClass;
		this.label = label;
	}

	public CrawlCacheProvider getNewInstance() throws InstantiationException,
			IllegalAccessException {
		return providerClass.newInstance();
	}

	public static CrawlCacheProviderEnum find(String property) {
		for (CrawlCacheProviderEnum crawlCacheProviderEnum : CrawlCacheProviderEnum
				.values())
			if (crawlCacheProviderEnum.name().equals(property))
				return crawlCacheProviderEnum;
		return LOCAL_FILE;
	}

	@Override
	public String toString() {
		return label;
	}
}
