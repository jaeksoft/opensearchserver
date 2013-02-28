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

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;

import com.jaeksoft.searchlib.Logging;
import com.jaeksoft.searchlib.crawler.web.database.UrlFilterItem;
import com.jaeksoft.searchlib.crawler.web.database.UrlFilterList;

public class LinkUtils {

	public final static URL getLink(URL currentURL, String href,
			UrlFilterItem[] urlFilterList, boolean removeFragment) {

		if (href == null)
			return null;
		href = href.trim();
		if (href.length() == 0)
			return null;

		String fragment = null;
		try {
			href = new URL(currentURL, href).toExternalForm();
			href = UrlFilterList.doReplace(href, urlFilterList);
			URI uri = URI.create(href);
			uri = uri.normalize();

			String p = uri.getPath();
			if (p != null)
				if (p.contains("/./") || p.contains("/../"))
					return null;

			if (!removeFragment)
				fragment = uri.getRawFragment();

			return new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost(),
					uri.getPort(), uri.getPath(), uri.getQuery(), fragment)
					.normalize().toURL();
		} catch (MalformedURLException e) {
			Logging.info(e.getMessage());
			return null;
		} catch (URISyntaxException e) {
			Logging.info(e.getMessage());
			return null;
		} catch (IllegalArgumentException e) {
			Logging.info(e.getMessage());
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

	public final static String lastPart(String path) {
		if (path == null)
			return null;
		String[] parts = StringUtils.split(path, '/');
		if (parts == null)
			return path;
		if (parts.length == 0)
			return path;
		return parts[parts.length - 1];
	}

	public final static String UTF8_URL_Encode(String s)
			throws UnsupportedEncodingException {
		return URLEncoder.encode(s, "UTF-8").replace("+", "%20");
	}

	public final static void main(String[] args) {
		System.out.println(lastPart("/my+folder/"));
		System.out.println(lastPart("my folder/"));
		System.out.println(lastPart("my folder/my+sub-folder/"));
		System.out.println(lastPart("/my+file.png"));
		System.out.println(lastPart("my+file.png"));
		System.out.println(lastPart("my+folder/my+sub-folder/my+file.png"));
	}

}
