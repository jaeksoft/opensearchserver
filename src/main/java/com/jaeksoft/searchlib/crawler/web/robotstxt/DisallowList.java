/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008-2015 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.crawler.web.robotstxt;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;

import com.jaeksoft.searchlib.analysis.LanguageEnum;
import com.jaeksoft.searchlib.parser.Parser;
import com.jaeksoft.searchlib.streamlimiter.StreamLimiter;

/**
 * Contains the list of clauses of a "robots.txt" file *
 */
public class DisallowList extends Parser {

	private Map<String, DisallowSet> list;

	public DisallowList() {
		super(null, false);
		list = null;
	}

	public Map<String, DisallowSet> getMap() {
		return list;
	}

	/**
	 * @param userAgent
	 * @return the right DisallowSet for the passed user-agent
	 */
	protected DisallowSet get(String userAgent) {
		synchronized (this) {
			if (list == null)
				return null;
			return list.get(userAgent);
		}
	}

	/**
	 * @param userAgent
	 * @return the disallowset which is related to the useragent.
	 */
	protected DisallowSet getOrCreate(String userAgent) {
		synchronized (this) {
			if (list == null)
				list = new TreeMap<String, DisallowSet>();
			DisallowSet disallowSet = list.get(userAgent);
			if (disallowSet == null) {
				disallowSet = new DisallowSet(userAgent);
				list.put(userAgent, disallowSet);
			}
			return disallowSet;
		}
	}

	/**
	 * Parse a robots.txt file
	 * 
	 * @param br
	 * @throws IOException
	 */
	@Override
	public void parseContent(StreamLimiter streamLimiter, LanguageEnum lang) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(streamLimiter.getNewInputStream()));
		try {
			String line;
			DisallowSet currentDisallowSet = null;
			while ((line = br.readLine()) != null) {
				line = line.trim();
				if (line.startsWith("#"))
					continue;
				if (line.length() == 0)
					continue;
				StringTokenizer st = new StringTokenizer(line, ":");
				if (!st.hasMoreTokens())
					continue;
				String key = st.nextToken().trim();
				String value = null;
				if (!st.hasMoreTokens())
					continue;
				value = st.nextToken().trim();
				if ("User-agent".equalsIgnoreCase(key)) {
					currentDisallowSet = getOrCreate(value.toLowerCase());
				} else if ("Disallow".equalsIgnoreCase(key)) {
					if (currentDisallowSet != null)
						currentDisallowSet.add(value, false);
				} else if ("Allow".equalsIgnoreCase(key)) {
					if (currentDisallowSet != null)
						currentDisallowSet.add(value, true);
				}
			}
		} finally {
			br.close();
		}
	}

	public long size() {
		if (list == null)
			return 0;
		return list.size();
	}

}
