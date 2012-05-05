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

package com.jaeksoft.searchlib.util;

import java.net.MalformedURLException;
import java.net.URL;

import com.jaeksoft.searchlib.Logging;
import com.jaeksoft.searchlib.crawler.web.database.UrlFilterItem;
import com.jaeksoft.searchlib.crawler.web.database.UrlFilterList;

public class LinkUtils {

	public final static URL getLink(URL currentURL, String href,
			UrlFilterItem[] urlFilterList) {

		if (href == null)
			return null;
		href = href.trim();
		if (href.length() == 0)
			return null;

		try {
			href = new URL(currentURL, href).toExternalForm();
			href = UrlFilterList.doReplace(href, urlFilterList);
			return new URL(href);
		} catch (MalformedURLException e) {
			Logging.warn(e.getMessage(), e);
			return null;
		}
	}

	public final static String concatPath(String path1, String path2) {
		StringBuffer sb = new StringBuffer(path1);
		if (!path1.endsWith("/") && !path2.startsWith("/"))
			sb.append('/');
		sb.append(path2);
		return sb.toString();
	}

}
