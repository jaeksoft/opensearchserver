/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008-2013 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.crawler.web.database.pattern;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import com.jaeksoft.searchlib.Logging;
import com.jaeksoft.searchlib.util.LinkUtils;
import com.jaeksoft.searchlib.util.StringUtils;

public class PatternItem {

	public enum Status {
		UNDEFINED("Undefined"), INJECTED("Injected"), ALREADY(
				"Already injected"), ERROR("Unknown Error");

		private String name;

		private Status(String name) {
			this.name = name;
		}

		@Override
		public String toString() {
			return name;
		}
	}

	private Status status;

	private PatternMatcher matcher;

	public PatternItem() {
		status = Status.UNDEFINED;
		matcher = null;
	}

	public PatternItem(URL url) {
		this();
		setPattern(url.toExternalForm());
	}

	public PatternItem(String sPattern) {
		this();
		setPattern(sPattern);
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status v) {
		status = v;
	}

	public void setPattern(String s) {
		if (s == null)
			return;
		matcher = new PatternMatcher(s);
	}

	public PatternMatcher getMatcher() {
		return matcher;
	}

	/**
	 * Return NULL if no valid URL can be extracted
	 * 
	 * @return
	 */
	public final URL tryExtractURL() {
		if (matcher == null || matcher.sPattern == null)
			return null;
		try {
			return LinkUtils.newEncodedURL(StringUtils.replace(
					matcher.sPattern, "*", ""));
		} catch (MalformedURLException e) {
			Logging.warn("Unable to extract URL from " + matcher.sPattern);
			return null;
		} catch (URISyntaxException e) {
			Logging.warn("Unable to extract URL from " + matcher.sPattern);
			return null;
		}
	}

	public String getPattern() {
		if (matcher == null)
			return null;
		return matcher.sPattern;
	}

	public final static void main(String[] args) throws MalformedURLException {
		PatternItem item = new PatternItem("http://*.open-search-server.com/*");
		System.out.println(item.matcher.topPrivateDomain);
		System.out.println(item.matcher
				.match("http://www.open-search-server.com/download"));
		System.out.println(!item.matcher
				.match("http://open-search-server.com/download"));
		System.out.println(!item.matcher
				.match("https://www.open-search-server.com/download"));
		System.out.println(!item.matcher
				.match("https://www.open-search-server.com/download"));
	}
}
