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

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class PatternListMatcher {

	// For better performances, pattern are grouped by hostname in a map
	private final Map<String, List<PatternMatcher>> patternMap;

	public PatternListMatcher(Collection<String> patterns) {
		this.patternMap = new TreeMap<String, List<PatternMatcher>>();
		for (String pattern : patterns) {
			PatternMatcher matcher = new PatternMatcher(pattern);
			if (matcher.topPrivateDomain == null)
				continue;
			List<PatternMatcher> patternMatcherList = patternMap
					.get(matcher.topPrivateDomain);
			if (patternMatcherList == null) {
				patternMatcherList = new ArrayList<PatternMatcher>(1);
				patternMap.put(matcher.topPrivateDomain, patternMatcherList);
			}
			patternMatcherList.add(matcher);
		}
	}

	final public boolean matchPattern(final URL url, String sUrl) {
		if (url == null)
			return false;
		List<PatternMatcher> patternList = patternMap.get(PatternMatcher
				.getTopDomainOrHost(url.getHost()));
		if (patternList == null)
			return false;
		if (sUrl == null)
			sUrl = url.toExternalForm();
		for (PatternMatcher patternMatcher : patternList)
			if (patternMatcher.match(sUrl))
				return true;
		return false;
	}
}
