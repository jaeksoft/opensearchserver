/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2008-2009 Emmanuel Keller / Jaeksoft
 * 
 * http://www.open-search-server.com
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

package com.jaeksoft.searchlib.crawler.web.spider;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UrlHref {

	private StringBuffer url;

	private UrlHref() {
	}

	private void initWithRootBaseUrl(String baseUrl) throws URISyntaxException {
		url = new StringBuffer();
		URI baseUri = new URI(baseUrl);
		url.append(baseUri.getScheme());
		url.append("://");
		url.append(baseUri.getHost());
		if (baseUri.getPort() != -1)
			url.append(baseUri.getPort());
		url.append('/');
	}

	private void initWithDirectoryBaseUrl(String baseUrl)
			throws URISyntaxException {
		url = new StringBuffer();
		URI baseUri = new URI(baseUrl);
		url.append(baseUri.getScheme());
		url.append("://");
		url.append(baseUri.getHost());
		if (baseUri.getPort() != -1)
			url.append(baseUri.getPort());
		String path = baseUri.getPath();
		int i = path.lastIndexOf('/');
		if (i != -1)
			url.append(path.substring(0, i));
		url.append('/');
	}

	private void initWithUrl(String url) {
		this.url = new StringBuffer(url);
	}

	private void setRootFullHref(String baseUrl, String href)
			throws URISyntaxException {
		initWithRootBaseUrl(baseUrl);
		url.append(href);
	}

	private void setFullHref(String baseUrl, String href)
			throws URISyntaxException {
		initWithDirectoryBaseUrl(baseUrl);
		url.append(href);
	}

	private void setFragmentHref(String baseUrl, String href) {
		initWithUrl(baseUrl);
		url.append(href);
	}

	private String toUrl() {
		return url.toString();
	}

	protected static String getUrl(String baseUrl, String href)
			throws URISyntaxException {
		if (href == null)
			return null;
		href = href.trim();
		if (href.length() == 0)
			return null;
		UrlHref urlHref = new UrlHref();
		char startChar = href.charAt(0);
		switch (startChar) {
		// Relative HREF starting with slash
		case '/':
			urlHref.setRootFullHref(baseUrl, href);
			break;
		// Relative HREF is only a ref or fragment
		case '#':
		case '?':
			urlHref.setFragmentHref(baseUrl, href);
			break;
		default:
			// HREF is absolute
			if (href.contains(":"))
				urlHref.initWithUrl(href);
			else
				// or not
				urlHref.setFullHref(baseUrl, href);
			break;
		}
		urlHref.removeSessionId();
		urlHref.resoldeDotSlash();
		urlHref.removeAnchor();
		return urlHref.toUrl();
	}

	private static String resolveDotSlash(String path) {
		if (path == null)
			return "";
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

	private void resoldeDotSlash() throws URISyntaxException {
		URI uri = new URI(url.toString());
		url = new StringBuffer();
		url.append(uri.getScheme());
		url.append("://");
		url.append(uri.getHost());
		if (uri.getPort() != -1)
			url.append(uri.getPort());
		url.append(resolveDotSlash(uri.getPath()));
		if (uri.getQuery() != null) {
			url.append('?');
			url.append(uri.getQuery());
		}
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

	private void removeSessionId() {
		for (Pattern pattern : sessionIdPatterns) {
			Matcher matcher = pattern.matcher(url);
			if (matcher.matches()) {
				StringBuffer newUrl = new StringBuffer(matcher.group(1));
				if (matcher.group(3).length() > 0) {
					newUrl.append(matcher.group(2).charAt(0));
					newUrl.append(matcher.group(3));
				}
				url = newUrl;
			}
		}
	}

	private void removeAnchor() {
		int p = url.indexOf("#");
		if (p != -1)
			url.setLength(p);
	}

	public static void test(String baseUrl, String href)
			throws URISyntaxException {
		System.out.println(getUrl(baseUrl, href));
	}

	public final static void main(String[] argv) throws URISyntaxException {
		test("http://www.open-search-server.com", "////websearch.html");
		test(
				"http://www.open-search-server.com/archives/copieslocales/",
				"./../copieslocales/../copieslocales/./../copieslocales/../copieslocales/./tribunelibre/fr-x203.html");
		// test("http://www.ledisez.net/blog/2007/03/20//archives",
		// "uriEmpty:");
		test("http://clx.anet.fr/spip/spip_login.php3?url=ecrire%2Findex.php",
				"http://clx.anet.fr/spip?test=5#m'enfin");
		test("http://www.test.fr/directory/subdirectory/page.html", "#");
		test("http://www.test.fr/directory/subdirectory/page.html",
				"?PHPSESSID=88cde663abb075261403c11751defc17");
		test("http://www.test.fr/directory/subdirectory/page.html",
				"?test=2&PHPSESSID=88cde663abb075261403c11751defc17");
		test("http://www.test.fr/directory/subdirectory/page.html",
				"?PHPSESSID=88cde663abb075261403c11751defc17&test=3");
		test("http://www.test.fr/directory/subdirectory/page.html",
				"?test=4&PHPSESSID=88cde663abb075261403c11751defc17&test=5");
		test(
				"http://www.test.fr/directory/subdirectory/page.html",
				"?test=6&PHPSESSID=88cde663abb075261403c11751defc17&test=7&jsessionid=88cde663abb075261403c11751defc17&test=8");
	}
}
