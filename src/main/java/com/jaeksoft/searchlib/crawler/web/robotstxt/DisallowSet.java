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

import java.util.LinkedHashMap;
import java.util.Map;

public class DisallowSet {

	/**
	 * Contains the clause list of a "robots.txt" file for one "User-agent".
	 */

	private LinkedHashMap<String, Boolean> clauseMap;

	protected DisallowSet(String userAgent) {
		clauseMap = null;
	}

	/**
	 * Add a Allow/Disallow clause
	 * 
	 * @param clause
	 *            the path of the clause
	 * @param allow
	 *            allow or disallow
	 */
	protected void add(String clause, Boolean allow) {
		synchronized (this) {
			if (clauseMap == null)
				clauseMap = new LinkedHashMap<String, Boolean>();
			clauseMap.put(clause, allow);
		}
	}

	/**
	 * @param path
	 *            the path to check
	 * @return false if the URL is not allowed
	 */
	protected boolean isAllowed(String path) {
		synchronized (this) {
			if (clauseMap == null)
				return true;
			if ("".equals(path))
				path = "/";
			for (Map.Entry<String, Boolean> clause : clauseMap.entrySet())
				if (path.startsWith(clause.getKey()))
					return clause.getValue();
			return true;
		}
	}

	public Map<String, Boolean> getMap() {
		return clauseMap;
	}
}
