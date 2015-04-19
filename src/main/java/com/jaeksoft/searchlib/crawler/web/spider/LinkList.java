/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008-2012 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.crawler.web.spider;

import java.net.URL;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import com.jaeksoft.searchlib.index.IndexDocument;
import com.jaeksoft.searchlib.schema.FieldValueItem;
import com.jaeksoft.searchlib.schema.FieldValueOriginEnum;

public class LinkList implements Iterable<Link> {

	private Map<String, Link> links;

	private Link[] linkArray;

	public LinkList() {
		links = new TreeMap<String, Link>();
		linkArray = null;
	}

	public Link get(URL url) {
		return links.get(url.toExternalForm());
	}

	public void put(URL url, Link link) {
		links.put(url.toExternalForm(), link);
		linkArray = null;
	}

	public int size() {
		return links.size();
	}

	public Link[] getArray() {
		if (linkArray != null)
			return linkArray;
		linkArray = new Link[links.size()];
		links.values().toArray(linkArray);
		return linkArray;
	}

	public void populate(String fieldName, IndexDocument document) {
		for (Link link : links.values())
			document.add(fieldName, new FieldValueItem(
					FieldValueOriginEnum.EXTERNAL, link.getUrl()
							.toExternalForm()));
	}

	@Override
	public Iterator<Link> iterator() {
		return links.values().iterator();
	}

}
