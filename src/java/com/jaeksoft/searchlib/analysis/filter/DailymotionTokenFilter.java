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

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.lucene.analysis.TokenStream;
import org.json.JSONException;
import org.openqa.selenium.remote.JsonException;

import com.google.gdata.util.ServiceException;
import com.jaeksoft.searchlib.crawler.web.spider.HttpDownloader;
import com.jaeksoft.searchlib.util.video.Dailymotion;
import com.jaeksoft.searchlib.util.video.DailymotionItem;

public class DailymotionTokenFilter extends AbstractTermFilter {

	private int dailymotionData;
	private HttpDownloader httpDownloader = null;

	protected DailymotionTokenFilter(TokenStream input, int dailymotionData,
			HttpDownloader httpDownloader) {
		super(input);
		this.dailymotionData = dailymotionData;
		this.httpDownloader = httpDownloader;
	}

	@Override
	public final boolean incrementToken() throws IOException {
		current = captureState();
		for (;;) {
			if (!input.incrementToken())
				return false;
			String term = getTerm();
			try {
				URL url = new URL(term);
				DailymotionItem dailymotionItem = Dailymotion.getInfo(url,
						httpDownloader);
				switch (dailymotionData) {
				case 0:
					term = dailymotionItem.getTitle();
					break;
				case 2:
					term = dailymotionItem.toJson(url);
					break;
				default:
					term = null;
					break;
				}
				if (term == null || term.length() == 0)
					return false;
				createToken(term);
				return true;
			} catch (ServiceException e) {
				throw new IOException(e);
			} catch (URISyntaxException e) {
				throw new IOException(e);
			} catch (JSONException e) {
				throw new JsonException(e);
			}
		}
	}
}
