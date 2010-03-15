/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2010 Emmanuel Keller / Jaeksoft
 * 
 * http://www.open-search-server.com
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

package com.jaeksoft.searchlib.user;

import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.util.XPathParser;
import com.jaeksoft.searchlib.util.XmlWriter;

public class Roles {

	private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock(true);
	private final Lock r = rwl.readLock();
	private final Lock w = rwl.writeLock();

	private String indexName;

	private Set<String> roles;

	protected Roles(String indexName) {
		this.roles = new TreeSet<String>();
		this.indexName = indexName;
	}

	private void add(String role) {
		roles.add(role);
	}

	public void add(Role role) {
		w.lock();
		try {
			add(role.name());
		} finally {
			w.unlock();
		}
	}

	public boolean get(Role role) {
		r.lock();
		try {
			return roles.contains(role.name());
		} finally {
			r.unlock();
		}
	}

	public String getIndexName() {
		r.lock();
		try {
			return indexName;
		} finally {
			r.unlock();
		}
	}

	public static Roles fromXml(XPathParser xpp, Node node)
			throws XPathExpressionException {
		if (node == null)
			return null;
		String indexName = XPathParser.getAttributeString(node, "indexName");
		NodeList nodeList = xpp.getNodeList(node, "role");
		if (nodeList == null)
			return null;
		Roles roles = new Roles(indexName);
		int l = nodeList.getLength();
		for (int i = 0; i < l; i++) {
			Node item = nodeList.item(i);
			if (item != null) {
				String role = item.getNodeValue();
				if (role != null)
					roles.add(role);
			}
		}
		return roles;
	}

	public void writeXml(XmlWriter xmlWriter) throws SAXException {
		r.lock();
		try {
			if (roles == null)
				return;
			xmlWriter.startElement("roles", "indexName", indexName);
			Iterator<String> it = roles.iterator();
			while (it.hasNext()) {
				xmlWriter.startElement("role");
				xmlWriter.textNode(it.next());
				xmlWriter.endElement();
			}
			xmlWriter.endElement();
		} finally {
			r.unlock();
		}
	}

}
