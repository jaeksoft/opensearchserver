/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2014 Emmanuel Keller / Jaeksoft
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
import java.net.URL;
import java.util.regex.Pattern;

import com.jaeksoft.searchlib.Logging;
import com.jaeksoft.searchlib.util.StringUtils;

public class PatternMatcher {

	final String sPattern;

	final Pattern pattern;

	final String topPrivateDomain;

	PatternMatcher(final String patternString) {
		sPattern = patternString.trim();
		pattern = StringUtils.wildcardPattern(sPattern);
		String tpn;
		try {
			String host = new URL(StringUtils.replace(sPattern, "*", "a"))
					.getHost();
			tpn = getTopDomainOrHost(host);
		} catch (MalformedURLException e) {
			tpn = null;
			Logging.info(e);
		}
		topPrivateDomain = tpn;
	}

	final boolean match(final String sUrl) {
		if (pattern == null)
			return sUrl.equals(sPattern);
		return pattern.matcher(sUrl).matches();
	}

	public final static String getTopDomainOrHost(final String host) {
		String[] part = StringUtils.split(host, '.');
		return part == null || part.length == 0 ? host : part[part.length - 1];
	}

}
