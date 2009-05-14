/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2008 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.crawler.web.robotstxt;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;

import com.jaeksoft.searchlib.parser.LimitInputStream;
import com.jaeksoft.searchlib.parser.LimitReader;
import com.jaeksoft.searchlib.parser.Parser;

/**
 * Contient la liste des clauses "Disallow" d'un fichier "robots.txt" regroup�
 * par "User-agent".
 * 
 * @author ekeller
 * 
 */
public class DisallowList extends Parser {

	private Map<String, DisallowSet> list;

	public DisallowList() {
		super(null);
		list = null;
	}

	/**
	 * Retourne l'objet DisallowSet correspondant au User-agent pass� en
	 * param�tre.
	 * 
	 * @param userAgent
	 * @return
	 */
	protected DisallowSet get(String userAgent) {
		synchronized (this) {
			if (list == null)
				return null;
			return list.get(userAgent);
		}
	}

	/**
	 * Retourne l'objet DisallowSet correspondant au param�tre User-agent. S'il
	 * n'en existe pas, il en cr�e un nouveau.
	 * 
	 * @param userAgent
	 * @return
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
	 * Extraction des informations disallow du fichier robots.txt
	 * 
	 * @param br
	 * @throws IOException
	 */
	@Override
	public void parseContent(LimitInputStream inputStream) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(
				inputStream));
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
					currentDisallowSet.add(value);
			}
		}
		br.close();
	}

	@Override
	protected void parseContent(LimitReader reader) throws IOException {
		throw new IOException("Unsupported");
	}

	public long size() {
		if (list == null)
			return 0;
		return list.size();
	}

}
