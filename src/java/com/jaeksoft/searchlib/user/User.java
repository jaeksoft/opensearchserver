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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.util.XPathParser;
import com.jaeksoft.searchlib.util.XmlWriter;

public class User implements Comparable<User> {

	private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock(true);
	private final Lock r = rwl.readLock();
	private final Lock w = rwl.writeLock();

	private String name;
	private String password;
	private String apiKey;
	private Set<IndexRole> indexRoles;
	private boolean isAdmin;
	private boolean isMonitoring;

	public User() {
		indexRoles = new TreeSet<IndexRole>();
	}

	public User(String name, String password, boolean isAdmin) {
		this();
		this.name = name;
		this.password = password;
		this.apiKey = null;
		this.isAdmin = isAdmin;
	}

	public User(User user) {
		this();
		user.copyTo(this);
	}

	public void copyTo(User user) {
		r.lock();
		try {
			user.setName(name);
			user.setPassword(password);
			user.setAdmin(isAdmin);
			user.removeRoles();
			for (IndexRole indexRole : indexRoles)
				user.addRole(indexRole);
		} finally {
			r.unlock();
		}
	}

	public boolean hasRole(String indexName, Role role) {
		r.lock();
		try {
			if (isAdmin)
				return true;
			return indexRoles.contains(new IndexRole(indexName, role));
		} finally {
			r.unlock();
		}
	}

	public boolean hasAnyRole(String indexName, Role... roles) {
		r.lock();
		try {
			if (isAdmin)
				return true;
			for (Role role : roles)
				if (indexRoles.contains(new IndexRole(indexName, role)))
					return true;
			return false;
		} finally {
			r.unlock();
		}
	}

	public boolean hasAnyRole(String indexName, Role[]... groupRoles) {
		for (Role[] roles : groupRoles)
			if (hasAnyRole(indexName, roles))
				return true;
		return false;
	}

	public boolean hasAllRole(String indexName, Role... roles) {
		r.lock();
		try {
			if (isAdmin)
				return true;
			for (Role role : roles)
				if (!indexRoles.contains(new IndexRole(indexName, role)))
					return false;
			return true;
		} finally {
			r.unlock();
		}
	}

	public boolean hasAllRole(String indexName, Role[]... groupRoles) {
		for (Role[] roles : groupRoles)
			if (!hasAllRole(indexName, roles))
				return false;
		return true;
	}

	public void addRole(IndexRole indexRole) {
		w.lock();
		try {
			indexRoles.add(indexRole);
		} finally {
			w.unlock();
		}
	}

	public void removeRole(IndexRole indexRole) {
		w.lock();
		try {
			indexRoles.remove(indexRole);
		} finally {
			w.unlock();
		}
	}

	protected void removeRoles() {
		w.lock();
		try {
			indexRoles.clear();
		} finally {
			w.unlock();
		}
	}

	public void addRole(String indexName, String roleName) {
		addRole(new IndexRole(indexName, roleName));
	}

	public Set<IndexRole> getRoles() {
		r.lock();
		try {
			return indexRoles;
		} finally {
			r.unlock();
		}
	}

	public static User fromXml(XPathParser xpp, Node node)
			throws XPathExpressionException {
		if (node == null)
			return null;
		String name = XPathParser.getAttributeString(node, "name");
		String encodedPassword = XPathParser.getAttributeString(node,
				"password");
		String password = new String(Base64.decodeBase64(encodedPassword
				.getBytes()));
		if (name == null || password == null)
			return null;
		boolean isAdmin = "yes".equalsIgnoreCase(XPathParser
				.getAttributeString(node, "isAdmin"));
		User user = new User(name, password, isAdmin);
		NodeList nodes = xpp.getNodeList(node, "role");
		if (nodes != null) {
			int l = nodes.getLength();
			for (int i = 0; i < l; i++) {
				IndexRole indexRole = IndexRole.fromXml(xpp, nodes.item(i));
				if (indexRole != null)
					user.addRole(indexRole);
			}
		}
		return user;
	}

	public void writeXml(XmlWriter xmlWriter) throws SAXException {
		r.lock();
		try {
			String encodedPassword = new String(Base64.encodeBase64(password
					.getBytes()));
			xmlWriter.startElement("user", "name", name, "password",
					encodedPassword, "isAdmin", isAdmin ? "yes" : "no");
			for (IndexRole indexRole : indexRoles)
				indexRole.writeXml(xmlWriter);
			xmlWriter.endElement();
		} finally {
			r.unlock();
		}
	}

	@Override
	public int compareTo(User u) {
		return name.compareTo(u.name);
	}

	@Override
	public boolean equals(Object o) {
		User u = (User) o;
		return name.equals(u.name);
	}

	public String getName() {
		r.lock();
		try {
			return name;
		} finally {
			r.unlock();
		}
	}

	public void setName(String name) {
		w.lock();
		try {
			this.name = name;
			this.apiKey = null;
		} finally {
			w.unlock();
		}
	}

	public String getPassword() {
		r.lock();
		try {
			return password;
		} finally {
			r.unlock();
		}
	}

	public String getApiKey() {
		r.lock();
		try {
			if (apiKey != null)
				return apiKey;
			apiKey = "";
			if (name != null || password != null)
				if (name.length() > 0 && password.length() > 0)
					apiKey = DigestUtils.md5Hex("ossacc" + name + password);
			return apiKey;
		} finally {
			r.unlock();
		}

	}

	public void setPassword(String password) {
		w.lock();
		try {
			this.password = password;
			this.apiKey = null;
		} finally {
			w.unlock();
		}
	}

	public boolean isAdmin() {
		r.lock();
		try {
			return isAdmin;
		} finally {
			r.unlock();
		}
	}

	public boolean isMonitoring() {
		r.lock();
		try {
			return isMonitoring;
		} finally {
			r.unlock();
		}
	}

	public void setAdmin(boolean isAdmin) {
		w.lock();
		try {
			this.isAdmin = isAdmin;
		} finally {
			w.unlock();
		}
	}

	public void setMonitoring(boolean isMonitoring) {
		w.lock();
		try {
			this.isMonitoring = isMonitoring;
		} finally {
			w.unlock();
		}
	}

	public boolean authenticate(String password) {
		r.lock();
		try {
			return this.password.equals(password);
		} finally {
			r.unlock();
		}
	}

	public boolean authenticateKey(String key) {
		r.lock();
		try {
			return getApiKey().equals(key);
		} finally {
			r.unlock();
		}
	}

	public String getApiCallParameters() throws UnsupportedEncodingException {
		return "login=" + URLEncoder.encode(name, "UTF-8") + "&key="
				+ getApiKey();
	}

}
