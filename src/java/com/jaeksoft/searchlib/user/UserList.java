/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2010-2011 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.user;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.util.ReadWriteLock;
import com.jaeksoft.searchlib.util.StringUtils;
import com.jaeksoft.searchlib.util.XPathParser;
import com.jaeksoft.searchlib.util.XmlWriter;

public class UserList {

	final private ReadWriteLock rwl = new ReadWriteLock();

	private Map<String, User> users;

	private String key;

	public final static String usersElement = "users";

	private final static String keyAttr = "key";

	public UserList() {
		users = new TreeMap<String, User>();
		key = null;

	}

	public boolean add(User user) {
		rwl.w.lock();
		try {
			if (users.containsKey(user.getName()))
				return false;
			users.put(user.getName(), user);
			return true;
		} finally {
			rwl.w.unlock();
		}
	}

	public boolean remove(String selectedUserName) {
		rwl.w.lock();
		try {
			return users.remove(selectedUserName) != null;
		} finally {
			rwl.w.unlock();
		}
	}

	public User get(String name) {
		rwl.r.lock();
		try {
			if (name == null)
				return null;
			return users.get(name);
		} finally {
			rwl.r.unlock();
		}
	}

	public Set<String> getUserNameSet() {
		rwl.r.lock();
		try {
			return users.keySet();
		} finally {
			rwl.r.unlock();
		}
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getKey() {
		return key;
	}

	public static UserList fromXml(XPathParser xpp, Node parentNode)
			throws XPathExpressionException {
		UserList userList = new UserList();
		if (parentNode == null)
			return userList;
		String eKey = XPathParser.getAttributeString(parentNode, keyAttr);
		if (eKey != null)
			userList.setKey(StringUtils.base64decode(eKey));
		NodeList nodes = xpp.getNodeList(parentNode, User.userElement);
		if (nodes == null)
			return userList;
		for (int i = 0; i < nodes.getLength(); i++) {
			User user = User.fromXml(xpp, nodes.item(i));
			userList.add(user);
		}
		return userList;
	}

	public void writeXml(XmlWriter xmlWriter) throws SAXException {
		rwl.r.lock();
		try {
			String eKey = key != null ? StringUtils.base64encode(key) : null;
			xmlWriter.startElement(usersElement, keyAttr, eKey);
			for (User user : users.values())
				user.writeXml(xmlWriter);
			xmlWriter.endElement();
		} finally {
			rwl.r.unlock();
		}
	}

	public boolean isEmpty() {
		rwl.r.lock();
		try {
			return users.size() == 0;
		} finally {
			rwl.r.unlock();
		}
	}
}
