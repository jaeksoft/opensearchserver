/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2008-2009 Emmanuel Keller / Jaeksoft
 * 
 * http://www.jaeksoft.com
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

package com.jaeksoft.searchlib.util;

import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LinkUtils {

	private static String changePathUrl(URL url, String newPath,
			String additionnal) {
		StringBuffer newUri = new StringBuffer();
		newUri.append(url.getProtocol());
		newUri.append("://");
		newUri.append(url.getHost());
		if (url.getPort() != -1)
			newUri.append(url.getPort());
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

	public static URL getLink(URL currentURL, String uri, boolean follow,
			boolean allowRefAnchor, boolean resolveDotSlash,
			boolean removeSessionId) throws MalformedURLException {

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

		// Do we have to remove session id
		if (removeSessionId)
			uri = removeSessionId(uri);

		return new URL(uri);
	}

	private static final Pattern[] sessionIdPatterns = {
			Pattern.compile("^(.*)" + "([\\?|&]{1}"
					+ "PHPSESSID=[0-9a-zA-Z]{32}" + "&?)" + "(.*)$",
					Pattern.CASE_INSENSITIVE),
			Pattern.compile("^(.*)" + "([\\?|&]{1}"
					+ "jsessionid=[0-9a-zA-Z]{32}" + "&?)" + "(.*)$",
					Pattern.CASE_INSENSITIVE),
			Pattern.compile("^(.*)" + "([\\?|&]{1}" + "sid=[0-9a-zA-Z]{32}"
					+ "&?)" + "(.*)$", Pattern.CASE_INSENSITIVE),
			Pattern.compile("^(.*)" + "([\\?|&]{1}"
					+ "ASPSESSIONID[a-zA-Z]{8}=[a-zA-Z]{24}" + "&?)" + "(.*)$",
					Pattern.CASE_INSENSITIVE) };

	public static String removeSessionId(String url) {
		for (Pattern pattern : sessionIdPatterns) {
			Matcher matcher = pattern.matcher(url);
			if (matcher.matches()) {
				StringBuffer newUrl = new StringBuffer(matcher.group(1));
				if (matcher.group(3).length() > 0) {
					newUrl.append(matcher.group(2).charAt(0));
					newUrl.append(matcher.group(3));
				}
				url = newUrl.toString();
			}
		}
		return url;
	}

	public static void test(String url, String uri, PrintWriter pw)
			throws MalformedURLException {
		pw.println(getLink(new URL(url), uri, true, false, true, true));
		pw.flush();
	}

	public final static void main(String[] argv) {
		try {
			PrintWriter pw = new PrintWriter(System.out);
			test("http://www.jaeksoft.com", "////websearch.html", pw);
			test(
					"http://www.jaeksoft.com/archives/copieslocales/",
					"./../copieslocales/../copieslocales/./../copieslocales/../copieslocales/./tribunelibre/fr-x203.html",
					pw);
			// test("http://www.ledisez.net/blog/2007/03/20//archives",
			// "uriEmpty:", pw);
			test(
					"http://clx.anet.fr/spip/spip_login.php3?url=ecrire%2Findex.php",
					"http://clx.anet.fr/spip?test=5#m'enfin", pw);
			test("http://www.test.fr/directory/subdirectory/page.html", "#", pw);
			test("http://www.test.fr/directory/subdirectory/page.html",
					"?PHPSESSID=88cde663abb075261403c11751defc17", pw);
			test("http://www.test.fr/directory/subdirectory/page.html",
					"?test=2&PHPSESSID=88cde663abb075261403c11751defc17", pw);
			test("http://www.test.fr/directory/subdirectory/page.html",
					"?PHPSESSID=88cde663abb075261403c11751defc17&test=3", pw);
			test(
					"http://www.test.fr/directory/subdirectory/page.html",
					"?test=4&PHPSESSID=88cde663abb075261403c11751defc17&test=5",
					pw);
			test(
					"http://www.test.fr/directory/subdirectory/page.html",
					"?test=6&PHPSESSID=88cde663abb075261403c11751defc17&test=7&jsessionid=88cde663abb075261403c11751defc17&test=8",
					pw);
			pw.close();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}
}
