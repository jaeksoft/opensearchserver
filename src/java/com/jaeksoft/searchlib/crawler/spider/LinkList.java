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

package com.jaeksoft.searchlib.crawler.spider;

import java.net.URL;
import java.util.Collection;
import java.util.LinkedHashMap;

import com.jaeksoft.searchlib.index.FieldContent;

public class LinkList {

	private LinkedHashMap<String, Link> links;

	public LinkList() {
		links = new LinkedHashMap<String, Link>();
	}

	public Link get(URL url) {
		return links.get(url.toExternalForm());
	}

	public void put(URL url, Link link) {
		links.put(url.toExternalForm(), link);
	}

	public int size() {
		return links.size();
	}

	public Collection<Link> values() {
		return links.values();
	}

	public void populate(FieldContent fieldContent) {
		for (Link link : values())
			fieldContent.add(link.getUrl().toExternalForm());
	}

}
