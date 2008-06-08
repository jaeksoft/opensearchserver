/**   
 * License Agreement for Jaeksoft SearchLib Community
 *
 * Copyright (C) 2008 Emmanuel Keller / Jaeksoft
 * 
 * http://www.jaeksoft.com
 * 
 * This file is part of Jaeksoft SearchLib Community.
 *
 * Jaeksoft SearchLib Community is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * Jaeksoft SearchLib Community is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Jaeksoft SearchLib Community. 
 *  If not, see <http://www.gnu.org/licenses/>.
 **/

package com.jaeksoft.searchlib.crawler.robotstxt;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.Map.Entry;

import com.jaeksoft.searchlib.crawler.spider.Crawl;
import com.jaeksoft.searchlib.crawler.spider.LinkList;
import com.jaeksoft.searchlib.crawler.spider.Parser;
import com.jaeksoft.searchlib.index.IndexDocument;

/**
 * Contient la liste des clauses "Disallow" d'un fichier "robots.txt" regroup�
 * par "User-agent".
 * 
 * @author ekeller
 * 
 */
public class DisallowList implements Parser {

	private HashMap<String, DisallowSet> list;

	public DisallowList() {
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
				list = new HashMap<String, DisallowSet>();
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
	public void parseContent(Crawl crawl, InputStream inputStream)
			throws IOException {
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

	public long size() {
		if (list == null)
			return 0;
		return list.size();
	}

	public void xmlInfo(PrintWriter writer, HashSet<String> classDetail) {
		String cl = getClass().getCanonicalName();
		int size = (list == null) ? 0 : list.size();
		writer.print("<disallowList size=\"" + size + "\">");
		if (list != null
				&& (classDetail.contains("full") || classDetail.contains(cl))) {
			synchronized (this) {
				Iterator<Entry<String, DisallowSet>> it = list.entrySet()
						.iterator();
				while (it.hasNext())
					it.next().getValue().xmlInfo(writer, classDetail);
			}
		}
		writer.println("</disallowList>");
	}

	public IndexDocument getDocument() {
		return null;
	}

	public LinkList getInlinks() {
		return null;
	}

	public LinkList getOutlinks() {
		return null;
	}

}
