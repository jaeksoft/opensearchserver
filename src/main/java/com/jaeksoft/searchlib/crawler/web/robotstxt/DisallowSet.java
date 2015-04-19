/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008-2014 Emmanuel Keller / Jaeksoft
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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class DisallowSet {

	/**
	 * Contient la liste des clauses "Disallow" d'un fichier "robots.txt" pour
	 * un "User-agent".
	 */

	private HashSet<String> set;

	protected DisallowSet(String userAgent) {
		set = null;
	}

	/**
	 * Ajoute une clause Disallow
	 * 
	 * @param disallowClause
	 */
	protected void add(String disallowClause) {
		synchronized (this) {
			if (set == null)
				set = new HashSet<String>();
			set.add(disallowClause);
		}
	}

	/**
	 * Renvoie false si l'URL n'est pas autoris�e
	 * 
	 * @param url
	 * @return
	 */
	protected boolean isAllowed(String path) {
		synchronized (this) {
			if (set == null)
				return true;
			if ("".equals(path))
				path = "/";
			Iterator<String> i = set.iterator();
			while (i.hasNext()) {
				String disallow = i.next();
				if (path.startsWith(disallow))
					return false;
			}
			return true;
		}
	}

	public Set<String> getSet() {
		return set;
	}
}
