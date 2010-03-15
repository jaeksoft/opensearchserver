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

import com.jaeksoft.searchlib.util.XPathParser;
import com.jaeksoft.searchlib.util.XmlWriter;

public class UserList {

	private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock(true);
	private final Lock r = rwl.readLock();
	private final Lock w = rwl.writeLock();

	private Map<String, User> users;

	private UserList() {
		users = new TreeMap<String, User>();
	}

	private void add(User user) {
		w.lock();
		try {
			users.put(user.getName(), user);
		} finally {
			w.unlock();
		}
	}

	public User create(String name, String password) {
		User user = new User(name, password);
		add(user);
		return user;
	}

	public User get(String name) {
		r.lock();
		try {
			return users.get(name);
		} finally {
			r.unlock();
		}
	}

	public static UserList fromXml(XPathParser xpp, Node parentNode)
			throws XPathExpressionException {
		UserList userList = new UserList();
		if (parentNode == null)
			return userList;
		NodeList nodes = xpp.getNodeList(parentNode, "user");
		if (nodes == null)
			return userList;
		for (int i = 0; i < nodes.getLength(); i++) {
			User user = User.fromXml(xpp, nodes.item(i));
			userList.add(user);
		}
		return null;
	}

	public void writeXml(XmlWriter xmlWriter) throws SAXException {
		r.lock();
		try {
			xmlWriter.startElement("users");
			for (User user : users.values())
				user.writeXml(xmlWriter);
			xmlWriter.endElement();
		} finally {
			r.unlock();
		}
	}
}
