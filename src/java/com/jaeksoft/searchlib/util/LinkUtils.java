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

	private static String changePathUrl(URL url, String newPath,
			String additionnal) {
		StringBuffer newUri = new StringBuffer();
		newUri.append(url.getProtocol());
		newUri.append("://");
		newUri.append(url.getHost());
		if (url.getPort() != -1) {
			newUri.append(":");
			newUri.append(url.getPort());
		}
		if (newPath != null && newPath.length() > 0) {
			if (newPath.charAt(0) != '/')
				newUri.append('/');
			newUri.append(newPath);
		}
		if (additionnal != null && additionnal.length() > 0)
			newUri.append(additionnal);
		return newUri.toString();
	}

	private static String resolveDotSlash(String path) {
		// "/./" means nothing
		path = path.replaceAll("/\\./", "/");
		// "///" multiple slashes
		path = path.replaceAll("/{2,}", "/");
		// "/../" means one directory up
		String newPath = path;
		do {
			path = newPath;
			newPath = path.replaceFirst("/[^\\./]*/\\.\\./", "/");
		} while (!newPath.equals(path));
		return path;
	}

	public final static URL getLink(URL currentURL, String uri, boolean follow,
			boolean allowRefAnchor, boolean resolveDotSlash,
			UrlFilterItem[] urlFilterList) {

		if (uri == null)
			return null;
		uri = uri.trim();
		if (uri.length() == 0)
			return null;

		char startChar = uri.charAt(0);
		// Relative URI starting with slash
		if (startChar == '/') {
			if (resolveDotSlash)
				uri = resolveDotSlash(uri);
			uri = changePathUrl(currentURL, null, uri);
		} else if (startChar == '#' || startChar == '?')
			uri = changePathUrl(currentURL, currentURL.getPath(), uri);
		// Relative URI not starting with slash
		else if (!uri.contains(":")) {
			String path = currentURL.getPath();
			// Search the last slash
			int i = path.lastIndexOf('/');
			if (i != -1)
				path = path.substring(0, i + 1);
			if (resolveDotSlash)
				path = resolveDotSlash(path + uri);
			uri = changePathUrl(currentURL, path, null);
		}

		// Do we have to remove anchor ?
		if (!allowRefAnchor) {
			int p = uri.indexOf('#');
			if (p != -1)
				uri = uri.substring(0, p);
		}

		uri = UrlFilterList.doReplace(uri, urlFilterList);

		try {
			return new URL(uri);
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
