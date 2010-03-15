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

import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.util.XPathParser;
import com.jaeksoft.searchlib.util.XmlWriter;

public class User implements Comparable<User> {

	private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock(true);
	private final Lock r = rwl.readLock();
	private final Lock w = rwl.writeLock();

	private String name;
	private String password;
	private Map<String, Roles> rolesMap;

	public User(String name, String password) {
		this.name = name;
		this.password = password;
		this.rolesMap = new TreeMap<String, Roles>();
	}

	public boolean hasRole(Client client, Role role) {
		r.lock();
		try {
			Roles roles = rolesMap.get(client.getIndexDirectory().getName());
			if (roles == null)
				return false;
			return roles.get(role);
		} finally {
			r.unlock();
		}
	}

	public void addRole(Client client, Role role) {
		w.lock();
		try {
			String indexName = client.getIndexDirectory().getName();
			Roles roles = rolesMap.get(indexName);
			if (roles == null) {
				roles = new Roles(null);
				rolesMap.put(indexName, roles);
			}
			roles.add(role);
		} finally {
			w.unlock();
		}
	}

	private void setRoles(Roles roles) {
		w.lock();
		try {
			rolesMap.put(roles.getIndexName(), roles);
		} finally {
			w.unlock();
		}
	}

	public static User fromXml(XPathParser xpp, Node node)
			throws XPathExpressionException {
		if (node == null)
			return null;
		String name = XPathParser.getAttributeString(node, "name");
		String password = XPathParser.getAttributeString(node, "password");
		if (name == null || password == null)
			return null;
		User user = new User(name, password);
		NodeList nodes = xpp.getNodeList(node, "roles");
		if (nodes != null) {
			int l = nodes.getLength();
			for (int i = 0; i < l; i++) {
				Roles roles = Roles.fromXml(xpp, nodes.item(i));
				if (roles != null)
					user.setRoles(roles);
			}
		}
		return user;
	}

	public void writeXml(XmlWriter xmlWriter) throws SAXException {
		r.lock();
		try {
			xmlWriter.startElement("user", "name", name, "password", password);
			for (Roles roles : rolesMap.values())
				roles.writeXml(xmlWriter);
			xmlWriter.endElement();
		} finally {
			r.unlock();
		}
	}

	@Override
	public int compareTo(User u) {
		return name.compareTo(u.name);
	}

	public String getName() {
		r.lock();
		try {
			return name;
		} finally {
			r.unlock();
		}
	}

}
