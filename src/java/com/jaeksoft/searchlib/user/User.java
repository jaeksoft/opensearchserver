/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2010-2012 Emmanuel Keller / Jaeksoft
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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Set;
import java.util.TreeSet;

import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.codec.digest.DigestUtils;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.util.ReadWriteLock;
import com.jaeksoft.searchlib.util.StringUtils;
import com.jaeksoft.searchlib.util.XPathParser;
import com.jaeksoft.searchlib.util.XmlWriter;

public class User implements Comparable<User> {

	final private ReadWriteLock rwl = new ReadWriteLock();

	public final static String userElement = "user";

	private String name;
	private String password;
	private String apiKey;
	private Set<IndexRole> indexRoles;
	private boolean isAdmin;
	private boolean isMonitoring;
	private boolean readOnly;

	public User() {
		indexRoles = new TreeSet<IndexRole>();
	}

	public User(String name, String password, boolean isAdmin, boolean readOnly) {
		this();
		this.name = name;
		this.password = password;
		this.apiKey = null;
		this.isAdmin = isAdmin;
		this.readOnly = readOnly;
	}

	public User(User user) {
		this();
		user.copyTo(this);
	}

	public void copyTo(User user) {
		rwl.r.lock();
		try {
			user.rwl.w.lock();
			try {
				user.name = name;
				user.password = password;
				user.isAdmin = isAdmin;
				user.readOnly = readOnly;
				user.indexRoles.clear();
				for (IndexRole indexRole : indexRoles)
					user.addRoleNoLock(indexRole);
			} finally {
				user.rwl.w.unlock();
			}
		} finally {
			rwl.r.unlock();
		}
	}

	public boolean hasRole(String indexName, Role role) {
		rwl.r.lock();
		try {
			if (isAdmin)
				return true;
			return indexRoles.contains(new IndexRole(indexName, role));
		} finally {
			rwl.r.unlock();
		}
	}

	public boolean hasAnyRole(String indexName, Role... roles) {
		rwl.r.lock();
		try {
			if (isAdmin)
				return true;
			for (Role role : roles)
				if (indexRoles.contains(new IndexRole(indexName, role)))
					return true;
			return false;
		} finally {
			rwl.r.unlock();
		}
	}

	public boolean hasAnyRole(String indexName, Role[]... groupRoles) {
		for (Role[] roles : groupRoles)
			if (hasAnyRole(indexName, roles))
				return true;
		return false;
	}

	public boolean hasAllRole(String indexName, Role... roles) {
		rwl.r.lock();
		try {
			if (isAdmin)
				return true;
			for (Role role : roles)
				if (!indexRoles.contains(new IndexRole(indexName, role)))
					return false;
			return true;
		} finally {
			rwl.r.unlock();
		}
	}

	public boolean hasAllRole(String indexName, Role[]... groupRoles) {
		for (Role[] roles : groupRoles)
			if (!hasAllRole(indexName, roles))
				return false;
		return true;
	}

	private void addRoleNoLock(IndexRole indexRole) {
		indexRoles.add(indexRole);
	}

	public void removeRole(IndexRole indexRole) {
		rwl.w.lock();
		try {
			indexRoles.remove(indexRole);
		} finally {
			rwl.w.unlock();
		}
	}

	public void addRole(String indexName, String roleName)
			throws SearchLibException {
		rwl.w.lock();
		try {
			checkWritable();
			addRoleNoLock(new IndexRole(indexName, roleName));
		} finally {
			rwl.w.unlock();
		}
	}

	public Set<IndexRole> getRoles() {
		rwl.r.lock();
		try {
			return indexRoles;
		} finally {
			rwl.r.unlock();
		}
	}

	public static User fromXml(XPathParser xpp, Node node)
			throws XPathExpressionException {
		if (node == null)
			return null;
		String name = XPathParser.getAttributeString(node, "name");
		String encodedPassword = XPathParser.getAttributeString(node,
				"password");
		String password = StringUtils.base64decode(encodedPassword);
		if (name == null || password == null)
			return null;
		boolean isAdmin = "yes".equalsIgnoreCase(XPathParser
				.getAttributeString(node, "isAdmin"));
		boolean readOnly = "yes".equalsIgnoreCase(XPathParser
				.getAttributeString(node, "readOnly"));
		User user = new User(name, password, isAdmin, readOnly);
		NodeList nodes = xpp.getNodeList(node, "role");
		if (nodes != null) {
			int l = nodes.getLength();
			for (int i = 0; i < l; i++) {
				IndexRole indexRole = IndexRole.fromXml(xpp, nodes.item(i));
				if (indexRole != null)
					user.addRoleNoLock(indexRole);
			}
		}
		return user;
	}

	public void writeXml(XmlWriter xmlWriter) throws SAXException,
			UnsupportedEncodingException {
		rwl.r.lock();
		try {
			String encodedPassword = StringUtils.base64encode(password);
			xmlWriter.startElement(userElement, "name", name, "password",
					encodedPassword, "isAdmin", isAdmin ? "yes" : "no",
					"readOnly", readOnly ? "yes" : "no");
			for (IndexRole indexRole : indexRoles)
				indexRole.writeXml(xmlWriter);
			xmlWriter.endElement();
		} finally {
			rwl.r.unlock();
		}
	}

	@Override
	public int compareTo(User u) {
		if (u == null)
			return -1;
		return name.compareTo(u.name);
	}

	@Override
	public boolean equals(Object o) {
		User u = (User) o;
		if (u == null)
			return false;
		return name.equals(u.name);
	}

	public String getName() {
		rwl.r.lock();
		try {
			return name;
		} finally {
			rwl.r.unlock();
		}
	}

	public void setName(String name) throws SearchLibException {
		rwl.w.lock();
		try {
			checkWritable();
			this.name = name;
			this.apiKey = null;
		} finally {
			rwl.w.unlock();
		}
	}

	public String getPassword() {
		rwl.r.lock();
		try {
			return password;
		} finally {
			rwl.r.unlock();
		}
	}

	public String getApiKey() {
		rwl.r.lock();
		try {
			if (apiKey != null)
				return apiKey;
			apiKey = "";
			if (name != null || password != null)
				if (name.length() > 0 && password.length() > 0)
					apiKey = DigestUtils.md5Hex("ossacc" + name + password);
			return apiKey;
		} finally {
			rwl.r.unlock();
		}

	}

	public void setPassword(String password) throws SearchLibException {
		rwl.w.lock();
		try {
			checkWritable();
			this.password = password;
			this.apiKey = null;
		} finally {
			rwl.w.unlock();
		}
	}

	public boolean isAdmin() {
		rwl.r.lock();
		try {
			return isAdmin;
		} finally {
			rwl.r.unlock();
		}
	}

	public boolean isMonitoring() {
		rwl.r.lock();
		try {
			return isMonitoring;
		} finally {
			rwl.r.unlock();
		}
	}

	public void setAdmin(boolean isAdmin) throws SearchLibException {
		rwl.w.lock();
		try {
			checkWritable();
			this.isAdmin = isAdmin;
		} finally {
			rwl.w.unlock();
		}
	}

	public void setMonitoring(boolean isMonitoring) throws SearchLibException {
		rwl.w.lock();
		try {
			checkWritable();
			this.isMonitoring = isMonitoring;
		} finally {
			rwl.w.unlock();
		}
	}

	public boolean authenticate(String password) {
		rwl.r.lock();
		try {
			return this.password.equals(password);
		} finally {
			rwl.r.unlock();
		}
	}

	public boolean authenticateKey(String key) {
		rwl.r.lock();
		try {
			return getApiKey().equals(key);
		} finally {
			rwl.r.unlock();
		}
	}

	public void appendApiCallParameters(StringBuffer sb)
			throws UnsupportedEncodingException {
		sb.append("&login=");
		sb.append(URLEncoder.encode(name, "UTF-8"));
		sb.append("&key=");
		sb.append(getApiKey());
	}

	/**
	 * @return the readOnly
	 */
	public boolean isReadOnly() {
		rwl.r.lock();
		try {
			return readOnly;
		} finally {
			rwl.r.unlock();
		}
	}

	public boolean isEditable() {
		return !isReadOnly();
	}

	/**
	 * @param readOnly
	 *            the readOnly to set
	 * @throws SearchLibException
	 */
	public void setReadOnly(boolean readOnly) throws SearchLibException {
		rwl.w.lock();
		try {
			checkWritable();
			this.readOnly = readOnly;
		} finally {
			rwl.w.unlock();
		}
	}

	private void checkWritable() throws SearchLibException {
		if (!readOnly)
			return;
		throw new SearchLibException("User is not editable");
	}
}
