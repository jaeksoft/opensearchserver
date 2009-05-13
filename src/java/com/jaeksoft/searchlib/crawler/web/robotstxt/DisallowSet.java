/**   
 * License Agreement for Jaeksoft WebSearch
 *
 * Copyright (C) 2008 Emmanuel Keller / Jaeksoft
 * 
 * http://www.jaeksoft.com
 * 
 * This file is part of Jaeksoft WebSearch.
 *
 * Jaeksoft WebSearch is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * Jaeksoft WebSearch is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Jaeksoft WebSearch. 
 *  If not, see <http://www.gnu.org/licenses/>.
 **/

package com.jaeksoft.searchlib.crawler.web.robotstxt;

import java.io.PrintWriter;
import java.net.URL;
import java.util.HashSet;
import java.util.Iterator;

import org.apache.commons.lang.StringEscapeUtils;

import com.jaeksoft.searchlib.util.XmlInfo;

public class DisallowSet implements XmlInfo {

	/**
	 * Contient la liste des clauses "Disallow" d'un fichier "robots.txt" pour
	 * un "User-agent".
	 */

	private HashSet<String> set;
	private String userAgent;

	protected DisallowSet(String userAgent) {
		set = null;
		this.userAgent = userAgent;
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
	protected boolean isAllowed(URL url) {
		synchronized (this) {
			if (set == null)
				return true;
			String path = url.getPath();
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

	public void xmlInfo(PrintWriter writer) {
		int size = (set == null) ? 0 : set.size();
		writer.print("<disallowSet userAgent=\"" + userAgent + "\" size=\""
				+ size + "\">");
		if (set != null) {
			synchronized (this) {
				Iterator<String> i = set.iterator();
				while (i.hasNext())
					writer.println("<disallow>"
							+ StringEscapeUtils.escapeXml(i.next())
							+ "</disallow>");
			}
		}
		writer.println("</disallowSet>");

	}
}
