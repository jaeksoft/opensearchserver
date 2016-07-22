/**
 * License Agreement for OpenSearchServer
 * <p/>
 * Copyright (C) 2012-2016 Emmanuel Keller / Jaeksoft
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
package com.jaeksoft.searchlib.analysis.filter;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.analysis.ClassPropertyEnum;
import com.jaeksoft.searchlib.analysis.FilterFactory;
import com.jaeksoft.searchlib.crawler.web.database.WebPropertyManager;
import com.jaeksoft.searchlib.crawler.web.spider.HttpDownloader;
import org.apache.lucene.analysis.TokenStream;

import java.io.IOException;

public class VimeoFilter extends FilterFactory {

	private int vimeoData;
	private boolean faultTolerant = true;

	@Override
	protected void initProperties() throws SearchLibException {
		super.initProperties();
		addProperty(ClassPropertyEnum.VIMEO_DATA, ClassPropertyEnum.VIMEO_DATA_LIST[0],
				ClassPropertyEnum.VIMEO_DATA_LIST, 0, 0);
		addProperty(ClassPropertyEnum.FAULT_TOLERANT, ClassPropertyEnum.BOOLEAN_LIST[0], ClassPropertyEnum.BOOLEAN_LIST,
				0, 0);
	}

	@Override
	protected void checkValue(ClassPropertyEnum prop, String value) throws SearchLibException {
		if (prop == ClassPropertyEnum.VIMEO_DATA) {
			vimeoData = 0;
			int i = 0;
			for (String v : ClassPropertyEnum.VIMEO_DATA_LIST) {
				if (value.equals(v)) {
					vimeoData = i;
					break;
				}
				i++;
			}
		} else if (prop == ClassPropertyEnum.FAULT_TOLERANT)
			faultTolerant = Boolean.parseBoolean(value);
	}

	@Override
	public TokenStream create(TokenStream tokenStream) {
		try {
			WebPropertyManager propertyManager = config.getWebPropertyManager();
			HttpDownloader httpDownloader = new HttpDownloader(propertyManager.getUserAgent().getValue(), false,
					propertyManager.getProxyHandler(), propertyManager.getConnectionTimeOut().getValue() * 1000);
			return new VimeoTokenFilter(tokenStream, vimeoData, httpDownloader, faultTolerant);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
