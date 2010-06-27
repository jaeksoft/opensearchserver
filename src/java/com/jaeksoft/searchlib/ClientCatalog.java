/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2008-2010 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.naming.NamingException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.config.ConfigFileRotation;
import com.jaeksoft.searchlib.config.ConfigFiles;
import com.jaeksoft.searchlib.template.TemplateAbstract;
import com.jaeksoft.searchlib.user.Role;
import com.jaeksoft.searchlib.user.User;
import com.jaeksoft.searchlib.user.UserList;
import com.jaeksoft.searchlib.util.LastModifiedAndSize;
import com.jaeksoft.searchlib.util.XPathParser;
import com.jaeksoft.searchlib.util.XmlWriter;

public class ClientCatalog {

	private static final String OPENSEARCHSERVER_DATA = System
			.getenv("OPENSEARCHSERVER_DATA");

	private static volatile Map<File, Client> CLIENTS = new TreeMap<File, Client>();

	private static final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock(
			true);
	private static final Lock r = rwl.readLock();
	private static final Lock w = rwl.writeLock();

	private static UserList userList = null;

	private static final ConfigFiles configFiles = new ConfigFiles();

	private static final Client getClient(File indexDirectory)
			throws SearchLibException, NamingException {

		r.lock();
		try {
			Client client = CLIENTS.get(indexDirectory);
			if (client != null)
				return client;
		} finally {
			r.unlock();
		}

		w.lock();
		try {
			Client client = CLIENTS.get(indexDirectory);
			if (client != null)
				return client;
			File dataDir = new File(OPENSEARCHSERVER_DATA);
			if (!indexDirectory.getParentFile().equals(dataDir))
				throw new SearchLibException("Security alert: "
						+ indexDirectory
						+ " is outside OPENSEARCHSERVER_DATA (" + dataDir + ")");
			client = new Client(indexDirectory, true);
			CLIENTS.put(indexDirectory, client);
			return client;
		} finally {
			w.unlock();
		}
	}

	public static final void openAll() {
		w.lock();
		try {

			for (ClientCatalogItem catalogItem : getClientCatalog(null)) {
				System.out.println("OSS load index "
						+ catalogItem.getIndexName());
				getClient(catalogItem.getIndexName());
			}
		} catch (SearchLibException e) {
			e.printStackTrace();
		} catch (NamingException e) {
			e.printStackTrace();
		} finally {
			w.unlock();
		}
	}

	public static final void closeAll() {
		w.lock();
		try {
			for (Client client : CLIENTS.values()) {
				System.out.println("OSS unload index "
						+ client.getIndexDirectory().getName());
				client.close();
			}
		} finally {
			w.unlock();
		}
	}

	public static final LastModifiedAndSize getLastModifiedAndSize(
			String indexName) throws SearchLibException {
		File file = new File(getDataDir(), indexName);
		if (!file.exists())
			return null;
		return new LastModifiedAndSize(file);
	}

	public static final Client getClient(String indexDirectoryName)
			throws SearchLibException, NamingException {
		return getClient(new File(getDataDir(), indexDirectoryName));
	}

	public static File getDataDir() throws SearchLibException {
		if (OPENSEARCHSERVER_DATA == null)
			throw new SearchLibException("OPENSEARCHSERVER_DATA is not defined");
		File dataDir = new File(OPENSEARCHSERVER_DATA);
		if (!dataDir.exists())
			throw new SearchLibException("Data directory does not exists ("
					+ dataDir + ")");
		return dataDir;
	}

	public static final Set<ClientCatalogItem> getClientCatalog(User user)
			throws SearchLibException {
		File dataDir = getDataDir();
		File[] files = dataDir
				.listFiles((FileFilter) DirectoryFileFilter.INSTANCE);
		Set<ClientCatalogItem> set = new TreeSet<ClientCatalogItem>();
		for (File file : files) {
			if (!file.isDirectory())
				continue;
			String indexName = file.getName();
			if (user == null || user.hasAnyRole(indexName, Role.GROUP_INDEX))
				set.add(new ClientCatalogItem(indexName));
		}
		return set;
	}

	public static final boolean exists(User user, String indexName)
			throws SearchLibException {
		if (user != null && !user.isAdmin())
			throw new SearchLibException("Operation not permitted");
		return getClientCatalog(null)
				.contains(new ClientCatalogItem(indexName));
	}

	public static void createIndex(User user, String indexName,
			TemplateAbstract template) throws SearchLibException, IOException {
		if (user != null && !user.isAdmin())
			throw new SearchLibException("Operation not permitted");
		w.lock();
		try {
			File indexDir = new File(getDataDir(), indexName);
			if (indexDir.exists())
				throw new SearchLibException("directory " + indexName
						+ " already exists");
			template.createIndex(indexDir);
		} finally {
			w.unlock();
		}

	}

	public static void eraseIndex(User user, String indexName)
			throws SearchLibException, NamingException, IOException {
		if (user != null && !user.isAdmin())
			throw new SearchLibException("Operation not permitted");
		Client client = getClient(indexName);
		client.close();
		FileUtils.deleteDirectory(client.getIndexDirectory());
	}

	public static UserList getUserList() throws SearchLibException {
		r.lock();
		try {
			if (userList == null) {
				File userFile = new File(getDataDir(), "users.xml");
				if (userFile.exists()) {
					XPathParser xpp = new XPathParser(userFile);
					userList = UserList.fromXml(xpp, xpp.getNode("/users"));
				} else
					userList = new UserList();
			}
			return userList;
		} catch (ParserConfigurationException e) {
			throw new SearchLibException(e);
		} catch (SAXException e) {
			throw new SearchLibException(e);
		} catch (IOException e) {
			throw new SearchLibException(e);
		} catch (XPathExpressionException e) {
			throw new SearchLibException(e);
		} finally {
			r.unlock();
		}
	}

	public static void flushPrivileges() {
		w.lock();
		try {
			userList = null;
		} finally {
			w.unlock();
		}
	}

	private static void saveUserListWithoutLock()
			throws TransformerConfigurationException, SAXException,
			IOException, SearchLibException {
		ConfigFileRotation cfr = configFiles.get(getDataDir(), "users.xml");
		try {
			XmlWriter xmlWriter = new XmlWriter(cfr.getTempPrintWriter(),
					"UTF-8");
			getUserList().writeXml(xmlWriter);
			xmlWriter.endDocument();
			cfr.rotate();
		} finally {
			cfr.abort();
		}
	}

	public static void saveUserList() throws SearchLibException {
		w.lock();
		try {
			saveUserListWithoutLock();
		} catch (IOException e) {
			throw new SearchLibException(e);
		} catch (TransformerConfigurationException e) {
			throw new SearchLibException(e);
		} catch (SAXException e) {
			throw new SearchLibException(e);
		} finally {
			w.unlock();
		}
	}

	public static User authenticate(String login, String password)
			throws SearchLibException {
		r.lock();
		try {
			User user = getUserList().get(login);
			if (user == null)
				return null;
			if (!user.authenticate(password))
				return null;
			return user;
		} finally {
			r.unlock();
		}
	}

	public static User authenticateKey(String login, String key)
			throws SearchLibException {
		r.lock();
		try {
			User user = getUserList().get(login);
			if (user == null)
				return null;
			if (!user.authenticateKey(key))
				return null;
			return user;
		} finally {
			r.unlock();
		}
	}

}
