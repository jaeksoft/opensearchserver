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
package com.jaeksoft.searchlib.analysis.filter;

import org.apache.lucene.analysis.TokenStream;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.analysis.ClassPropertyEnum;
import com.jaeksoft.searchlib.analysis.FilterFactory;
import com.jaeksoft.searchlib.crawler.web.database.WebPropertyManager;
import com.jaeksoft.searchlib.crawler.web.spider.HttpDownloader;

public class YouTubeFilter extends FilterFactory {

	private int youtubeData = 0;
	private boolean faultTolerant = true;

	@Override
	protected void initProperties() throws SearchLibException {
		super.initProperties();
		addProperty(ClassPropertyEnum.YOUTUBE_DATA,
				ClassPropertyEnum.YOUTUBE_DATA_LIST[0],
				ClassPropertyEnum.YOUTUBE_DATA_LIST);
		addProperty(ClassPropertyEnum.FAULT_TOLERANT,
				ClassPropertyEnum.BOOLEAN_LIST[0],
				ClassPropertyEnum.BOOLEAN_LIST);
	}

	@Override
	protected void checkValue(ClassPropertyEnum prop, String value)
			throws SearchLibException {
		if (prop == ClassPropertyEnum.YOUTUBE_DATA) {
			int i = 0;
			for (String v : ClassPropertyEnum.YOUTUBE_DATA_LIST) {
				if (value.equals(v)) {
					youtubeData = i;
					break;
				}
				i++;
			}
		} else if (prop == ClassPropertyEnum.FAULT_TOLERANT)
			faultTolerant = Boolean.parseBoolean(value);
	}

	@Override
	public TokenStream create(TokenStream tokenStream)
			throws SearchLibException {
		WebPropertyManager propertyManager = config.getWebPropertyManager();
		HttpDownloader httpDownloader = new HttpDownloader(propertyManager
				.getUserAgent().getValue(), false,
				propertyManager.getProxyHandler());
		return new YouTubeTokenFilter(tokenStream, youtubeData, httpDownloader,
				faultTolerant);
	}
}
