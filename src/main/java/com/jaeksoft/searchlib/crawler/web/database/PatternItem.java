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

package com.jaeksoft.searchlib.crawler.web.database;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.regex.Pattern;

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

	protected String sPattern;

	private Pattern pattern;

	public PatternItem() {
		status = Status.UNDEFINED;
		sPattern = null;
		pattern = null;
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

	public boolean match(String sUrl) {
		if (pattern == null)
			return sUrl.equals(sPattern);
		return pattern.matcher(sUrl).matches();
	}

	public void setPattern(String s) {
		sPattern = s.trim();
		pattern = StringUtils.wildcardPattern(s);
	}

	public final String getPatternWithoutWildcard() {
		return StringUtils.replace(sPattern, "*", "");
	}

	/**
	 * Return NULL if no valid URL can be extracted
	 * 
	 * @return
	 */
	public final URL tryExtractURL() {
		try {
			return LinkUtils.newEncodedURL(getPatternWithoutWildcard());
		} catch (MalformedURLException e) {
			Logging.warn("Unable to extract URL from " + sPattern);
			return null;
		} catch (URISyntaxException e) {
			Logging.warn("Unable to extract URL from " + sPattern);
			return null;
		}
	}

	public String getHost() throws MalformedURLException {
		return new URL(getPatternWithoutWildcard()).getHost();
	}

	public String getPattern() {
		return sPattern;
	}

}
