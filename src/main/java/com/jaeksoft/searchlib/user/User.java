/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2010-2015 Emmanuel Keller / Jaeksoft
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.codec.digest.DigestUtils;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.jaeksoft.searchlib.util.ReadWriteLock;
import com.jaeksoft.searchlib.util.StringUtils;
import com.jaeksoft.searchlib.util.XPathParser;

public class User implements Comparable<User> {

	@XmlAccessorType(XmlAccessType.FIELD)
	@JsonInclude(Include.NON_EMPTY)
	public static class Record {

		public String name = null;
		public String digestPassword = null;
		public String apiKey = null;
		public boolean isAdmin = false;
		public boolean isMonitoring = false;
		public HashMap<Role, TreeSet<String>> rights = null;
	}

	final private ReadWriteLock rwl = new ReadWriteLock();

	private final Record record;

	public User() {
		record = new Record();
		record.rights = new HashMap<Role, TreeSet<String>>();
	}

	User(Record record) {
		this.record = record;
		if (record.rights == null)
			record.rights = new HashMap<Role, TreeSet<String>>();
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
				user.record.name = record.name;
				user.record.digestPassword = record.digestPassword;
				user.record.isAdmin = record.isAdmin;
				user.record.isMonitoring = record.isMonitoring;
				user.record.apiKey = record.apiKey;
				user.record.rights.clear();
				copyRights(record.rights, user.record.rights);
			} finally {
				user.rwl.w.unlock();
			}
		} finally {
			rwl.r.unlock();
		}
	}

	private static void copyRights(HashMap<Role, TreeSet<String>> src,
			HashMap<Role, TreeSet<String>> dest) {
		for (Map.Entry<Role, TreeSet<String>> entry : src.entrySet())
			dest.put(entry.getKey(), new TreeSet<String>(entry.getValue()));
	}

	public HashMap<Role, TreeSet<String>> cloneRights() {
		rwl.r.lock();
		try {
			HashMap<Role, TreeSet<String>> newRights = new HashMap<Role, TreeSet<String>>();
			copyRights(record.rights, newRights);
			return newRights;
		} finally {
			rwl.r.unlock();
		}
	}

	static User fromXml(XPathParser xpp, Node node)
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
		boolean isMonitoring = "yes".equalsIgnoreCase(XPathParser
				.getAttributeString(node, "isMonitoring"));
		User user = new User();
		user.setName(name);
		user.record.isAdmin = isAdmin;
		user.record.isMonitoring = isMonitoring;
		user.setPassword(password);
		NodeList nodes = xpp.getNodeList(node, "role");
		if (nodes != null) {
			int l = nodes.getLength();
			for (int i = 0; i < l; i++) {
				Node n = nodes.item(i);
				String indexName = XPathParser.getAttributeString(n,
						"indexName");
				String roleName = XPathParser.getAttributeString(n, "role");
				Role role = Role.find(roleName);
				if (role != null)
					user.addRoleNoLock(indexName, role);
			}
		}
		return user;
	}

	static List<User.Record> getRecordList(Collection<User> users) {
		List<User.Record> recordList = new ArrayList<User.Record>(
				users == null ? 0 : users.size());
		if (users != null)
			for (User user : users)
				recordList.add(user.record);
		return recordList;
	}

	@Override
	public int compareTo(User u) {
		if (u == null)
			return -1;
		return record.name.compareTo(u.record.name);
	}

	@Override
	public boolean equals(Object o) {
		User u = (User) o;
		if (u == null)
			return false;
		return record.name.equals(u.record.name);
	}

	public String getName() {
		rwl.r.lock();
		try {
			return record.name;
		} finally {
			rwl.r.unlock();
		}
	}

	public void setName(String name) {
		rwl.w.lock();
		try {
			this.record.name = name;
			this.record.apiKey = null;
			this.record.digestPassword = null;
		} finally {
			rwl.w.unlock();
		}
	}

	public String getApiKey() {
		rwl.r.lock();
		try {
			return record.apiKey;
		} finally {
			rwl.r.unlock();
		}
	}

	private final String digestPassword(String pass) {
		return DigestUtils.md5Hex("ossacc2" + record.name + pass);
	}

	private final String digestApiKey(String pass) {
		return DigestUtils.md5Hex("ossacc" + record.name + pass);
	}

	public void setPassword(String password) {
		rwl.w.lock();
		try {
			if (StringUtils.isEmpty(password)) {
				this.record.digestPassword = null;
				this.record.apiKey = null;
			} else {
				this.record.digestPassword = digestPassword(password);
				this.record.apiKey = digestApiKey(password);
			}
		} finally {
			rwl.w.unlock();
		}
	}

	public boolean isAdmin() {
		rwl.r.lock();
		try {
			return record.isAdmin;
		} finally {
			rwl.r.unlock();
		}
	}

	public void setAdmin(boolean isAdmin) {
		rwl.w.lock();
		try {
			this.record.isAdmin = isAdmin;
		} finally {
			rwl.w.unlock();
		}
	}

	public boolean isMonitoring() {
		rwl.r.lock();
		try {
			return record.isMonitoring;
		} finally {
			rwl.r.unlock();
		}
	}

	public void setMonitoring(boolean monitoring) {
		rwl.w.lock();
		try {
			this.record.isMonitoring = monitoring;
		} finally {
			rwl.w.unlock();
		}
	}

	public boolean authenticate(String password) {
		if (StringUtils.isEmpty(password))
			return false;
		rwl.r.lock();
		try {
			if (StringUtils.isEmpty(record.digestPassword))
				return false;
			return digestPassword(password).equals(record.digestPassword);
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

	public void appendApiCallParameters(StringBuilder sb)
			throws UnsupportedEncodingException {
		sb.append("&login=");
		sb.append(URLEncoder.encode(record.name, "UTF-8"));
		sb.append("&key=");
		sb.append(getApiKey());
	}

	private boolean hasRoleNoLock(String resourceName, Role role) {
		Set<String> set = record.rights.get(role);
		if (set == null)
			return false;
		return set.contains(resourceName);
	}

	public boolean hasRole(String resourceName, Role role) {
		rwl.r.lock();
		try {
			if (record.isAdmin)
				return true;
			return hasRoleNoLock(resourceName, role);
		} finally {
			rwl.r.unlock();
		}
	}

	public boolean hasAnyRole(String resourceName, Role... roles) {
		rwl.r.lock();
		try {
			if (record.isAdmin)
				return true;
			if (roles == null)
				return true;
			for (Role role : roles)
				if (hasRoleNoLock(resourceName, role))
					return true;
			return false;
		} finally {
			rwl.r.unlock();
		}
	}

	public boolean hasAllRole(String resourceName, Role[] roles) {
		rwl.r.lock();
		try {
			if (record.isAdmin)
				return true;
			if (roles == null)
				return false;
			for (Role role : roles)
				if (hasRoleNoLock(resourceName, role))
					return false;
			return true;
		} finally {
			rwl.r.unlock();
		}
	}

	private boolean addRoleNoLock(String resourceName, Role role) {
		TreeSet<String> set = record.rights.get(role);
		if (set == null) {
			set = new TreeSet<String>();
			record.rights.put(role, set);
		}
		return set.add(resourceName);
	}

	public boolean addRole(String resourceName, Role role) {
		rwl.w.lock();
		try {
			return addRoleNoLock(resourceName, role);
		} finally {
			rwl.w.unlock();
		}
	}

	public boolean removeRole(String resourceName, Role role) {
		rwl.w.lock();
		try {
			TreeSet<String> set = record.rights.get(role);
			if (set == null)
				return false;
			boolean res = set.remove(resourceName);
			if (set.isEmpty())
				record.rights.remove(role);
			return res;
		} finally {
			rwl.w.unlock();
		}
	}

}
