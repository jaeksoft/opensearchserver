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

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.config.ConfigFileRotation;
import com.jaeksoft.searchlib.config.ConfigFiles;
import com.jaeksoft.searchlib.util.ReadWriteLock;
import com.jaeksoft.searchlib.util.XPathParser;
import com.jaeksoft.searchlib.web.StartStopListener;
import com.opensearchserver.utils.json.JsonMapper;

public class UserManager {

	public final static TypeReference<List<User.Record>> MapUserListRef = new TypeReference<List<User.Record>>() {
	};

	private static UserManager INSTANCE = null;

	private final static String USERS_FILE_NAME = "users.json";

	final private ReadWriteLock rwl = new ReadWriteLock();

	private Map<String, User> users;

	public UserManager() {
		users = null;
	}

	private UserManager(File jsonFile) throws JsonParseException,
			JsonMappingException, IOException {
		users = null;
		if (jsonFile == null || !jsonFile.exists() || jsonFile.length() == 0)
			return;
		List<User.Record> recordList = JsonMapper.MAPPER.readValue(jsonFile,
				MapUserListRef);
		if (recordList == null || recordList.isEmpty())
			return;
		users = new TreeMap<String, User>();
		for (User.Record record : recordList)
			users.put(record.name, new User(record));
	}

	public boolean add(User user, boolean checkAdminFirst)
			throws SearchLibException {
		rwl.w.lock();
		try {
			if (users != null && users.containsKey(user.getName()))
				return false;
			if (checkAdminFirst && !user.isAdmin() && findFirstAdmin() == null)
				throw new SearchLibException("The first user must be an admin");
			users = new TreeMap<String, User>();
			users.put(user.getName(), user);
			return true;
		} finally {
			rwl.w.unlock();
		}
	}

	public boolean remove(String selectedUserName) {
		rwl.w.lock();
		try {
			if (users == null)
				return false;
			boolean result = users.remove(selectedUserName) != null;
			if (users.isEmpty())
				users = null;
			return result;
		} finally {
			rwl.w.unlock();
		}
	}

	final private User getNoLock(String name) {
		if (name == null || users == null)
			return null;
		return users.get(name);

	}

	public User get(String name) {
		rwl.r.lock();
		try {
			return getNoLock(name);
		} finally {
			rwl.r.unlock();
		}
	}

	private User findFirstAdmin() {
		if (users == null || users.isEmpty())
			return null;
		for (User u : users.values())
			if (u.isAdmin())
				return u;
		return null;
	}

	public void populateUserList(List<User> userList) {
		rwl.r.lock();
		try {
			if (users == null)
				return;
			for (User user : users.values())
				userList.add(user);
		} finally {
			rwl.r.unlock();
		}
	}

	// Kept for migration from 1.5 to 2.0
	@Deprecated
	private static UserManager fromXml(XPathParser xpp, Node parentNode)
			throws XPathExpressionException, SearchLibException {
		UserManager userManager = new UserManager();
		if (parentNode == null)
			return userManager;
		NodeList nodes = xpp.getNodeList(parentNode, "user");
		if (nodes == null)
			return userManager;
		for (int i = 0; i < nodes.getLength(); i++) {
			User user = User.fromXml(xpp, nodes.item(i));
			userManager.add(user, false);
		}
		return userManager;
	}

	public boolean isEmpty() {
		rwl.r.lock();
		try {
			return users == null || users.size() == 0;
		} finally {
			rwl.r.unlock();
		}
	}

	public User authenticate(String login, String password)
			throws SearchLibException {
		rwl.r.lock();
		try {
			User user = getNoLock(login);
			if (user == null)
				return null;
			if (!user.authenticate(password))
				return null;
			return user;
		} finally {
			rwl.r.unlock();
		}
	}

	public User authenticateKey(String login, String key)
			throws SearchLibException {
		rwl.r.lock();
		try {
			User user = getNoLock(login);
			if (user == null)
				return null;
			if (!user.authenticateKey(key))
				return null;
			return user;
		} finally {
			rwl.r.unlock();
		}
	}

	private void saveUserListWithoutLock()
			throws TransformerConfigurationException, SAXException,
			IOException, SearchLibException {
		ConfigFileRotation cfr = ConfigFiles.getInstance().get(
				StartStopListener.OPENSEARCHSERVER_DATA_FILE, USERS_FILE_NAME);
		try {
			JsonMapper.MAPPER.writeValue(cfr.getTempFile(),
					User.getRecordList(users.values()));
			cfr.rotate();
		} finally {
			cfr.abort();
		}
	}

	public void save() throws SearchLibException {
		rwl.r.lock();
		try {
			saveUserListWithoutLock();
		} catch (IOException e) {
			throw new SearchLibException(e);
		} catch (TransformerConfigurationException e) {
			throw new SearchLibException(e);
		} catch (SAXException e) {
			throw new SearchLibException(e);
		} finally {
			rwl.r.unlock();
		}
	}

	public static UserManager getInstance() throws SearchLibException {
		synchronized (UserManager.class) {
			try {
				if (INSTANCE != null)
					return INSTANCE;
				File jsonFile = new File(
						StartStopListener.OPENSEARCHSERVER_DATA_FILE,
						USERS_FILE_NAME);
				if (jsonFile.exists()) {
					INSTANCE = new UserManager(jsonFile);
					return INSTANCE;
				}
				File xmlFile = new File(
						StartStopListener.OPENSEARCHSERVER_DATA_FILE,
						"users.xml");
				if (xmlFile.exists()) {
					XPathParser xpp = new XPathParser(xmlFile);
					INSTANCE = fromXml(xpp, xpp.getNode("/users"));
					INSTANCE.save();
					return INSTANCE;
				}
				INSTANCE = new UserManager();
				return INSTANCE;
			} catch (ParserConfigurationException e) {
				throw new SearchLibException(e);
			} catch (SAXException e) {
				throw new SearchLibException(e);
			} catch (IOException e) {
				throw new SearchLibException(e);
			} catch (XPathExpressionException e) {
				throw new SearchLibException(e);
			}
		}
	}

}
