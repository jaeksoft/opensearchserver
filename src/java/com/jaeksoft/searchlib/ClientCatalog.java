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
import java.io.PrintWriter;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.naming.NamingException;
import javax.servlet.ServletRequest;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.template.TemplateAbstract;
import com.jaeksoft.searchlib.user.UserList;
import com.jaeksoft.searchlib.util.ConfigFileRotation;
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

			for (File indexFile : getClientCatalog()) {
				System.out.println("OSS load index " + indexFile.getName());
				getClient(indexFile);
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

	public static final Client getClient(String indexDirectoryName)
			throws SearchLibException, NamingException {
		return getClient(new File(getDataDir(), indexDirectoryName));
	}

	public static final Client getClient(ServletRequest request)
			throws SearchLibException, NamingException {
		return getClient(request.getParameter("use"));

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

	public static final File[] getClientCatalog() throws SearchLibException {
		File dataDir = getDataDir();
		return dataDir.listFiles((FileFilter) DirectoryFileFilter.INSTANCE);
	}

	public static final boolean exists(String indexName)
			throws SearchLibException {
		for (File file : getClientCatalog())
			if (file.getName().equals(indexName))
				return true;
		return false;
	}

	public static void createIndex(String indexName, TemplateAbstract template)
			throws SearchLibException, IOException {
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

	public static void saveUserList() throws SearchLibException {
		PrintWriter pw = null;
		w.lock();
		try {
			ConfigFileRotation cfr = new ConfigFileRotation(getDataDir(),
					"users.xml");
			pw = cfr.getTempPrintWriter();
			XmlWriter xmlWriter = new XmlWriter(pw, "UTF-8");
			getUserList().writeXml(xmlWriter);
			xmlWriter.endDocument();
			cfr.rotate();
		} catch (IOException e) {
			throw new SearchLibException(e);
		} catch (TransformerConfigurationException e) {
			throw new SearchLibException(e);
		} catch (SAXException e) {
			throw new SearchLibException(e);
		} finally {
			w.unlock();
			if (pw != null)
				pw.close();
		}
	}

}
