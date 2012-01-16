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

package com.jaeksoft.searchlib.crawler.web.spider;

import org.json.simple.JSONObject;

import com.jaeksoft.searchlib.Logging;
import com.jaeksoft.searchlib.SearchLibException;

public abstract class DownloaderAbstract implements DownloaderInterface {

	@SuppressWarnings("unchecked")
	private final void put(JSONObject json, Object key, Object value) {
		if (value == null)
			return;
		json.put(key, value);
	}

	protected final static String KEY_REDIRECT_LOCATION = "KEY_REDIRECT_LOCATION";
	protected final static String KEY_CONTENT_DISPOSITION_FILENAME = "KEY_CONTENT_DISPOSITION_FILENAME";
	protected final static String KEY_CONTENT_LENGTH = "KEY_CONTENT_LENGTH";
	protected final static String KEY_CONTENT_BASE_TYPE = "KEY_CONTENT_BASE_TYPE";
	protected final static String KEY_CONTENT_TYPE_CHARSET = "KEY_CONTENT_TYPE_CHARSET";
	protected final static String KEY_CONTENT_ENCODING = "KEY_CONTENT_ENCODING";
	protected final static String KEY_STATUS_CODE = "KEY_STATUS_CODE";

	@Override
	public String getMetaAsJson() {
		JSONObject json = new JSONObject();
		try {
			put(json, KEY_REDIRECT_LOCATION, getRedirectLocation());
		} catch (SearchLibException e) {
			Logging.warn(e);
		}
		put(json, KEY_CONTENT_LENGTH, getContentLength());
		put(json, KEY_CONTENT_DISPOSITION_FILENAME,
				getContentDispositionFilename());
		put(json, KEY_CONTENT_BASE_TYPE, getContentBaseType());
		put(json, KEY_CONTENT_TYPE_CHARSET, getContentTypeCharset());
		put(json, KEY_CONTENT_ENCODING, getContentEncoding());
		put(json, KEY_CONTENT_ENCODING, getStatusCode());
		return json.toString();
	}
}
