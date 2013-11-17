/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008-2013 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.InvalidPropertiesFormatException;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.naming.NamingException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.lucene.search.BooleanQuery;
import org.xml.sax.SAXException;
import org.zkoss.zk.ui.WebApp;

import com.jaeksoft.searchlib.config.ConfigFileRotation;
import com.jaeksoft.searchlib.config.ConfigFiles;
import com.jaeksoft.searchlib.crawler.cache.CrawlCacheManager;
import com.jaeksoft.searchlib.ocr.OcrManager;
import com.jaeksoft.searchlib.renderer.RendererResults;
import com.jaeksoft.searchlib.template.TemplateAbstract;
import com.jaeksoft.searchlib.template.TemplateList;
import com.jaeksoft.searchlib.user.Role;
import com.jaeksoft.searchlib.user.User;
import com.jaeksoft.searchlib.user.UserList;
import com.jaeksoft.searchlib.util.LastModifiedAndSize;
import com.jaeksoft.searchlib.util.ReadWriteLock;
import com.jaeksoft.searchlib.util.XPathParser;
import com.jaeksoft.searchlib.util.XmlWriter;
import com.jaeksoft.searchlib.web.StartStopListener;
import com.jaeksoft.searchlib.web.controller.PushEvent;

/**
 * This class handles a list of indexes stored in a given directory.
 * 
 * 
 */
public class ClientCatalog {

	private static transient volatile Map<File, Client> CLIENTS = new TreeMap<File, Client>();

	private static final ReadWriteLock rwl = new ReadWriteLock();

	private static UserList userList = null;

	private static final ConfigFiles configFiles = new ConfigFiles();

	private static final RendererResults rendererResults = new RendererResults();

	private static final ThreadGroup threadGroup = new ThreadGroup("Catalog");

	/**
	 * This method should be called first before using any other function of
	 * OpenSearchServer. It will initialize all internal resources. The
	 * data_directory is the folder which will contain all the indexes (data and
	 * configuration).
	 * 
	 * @param data_directory
	 *            The directory which contain the indexes
	 */
	public static final void init(File data_directory) {
		StartStopListener.start(data_directory);
	}

	/**
	 * Close OpenSearchServer. This method closes all indexes and stops any
	 * running task..
	 */
	public static final void close() {
		StartStopListener.shutdown();
	}

	private static final Client getClient(File indexDirectory)
			throws SearchLibException {

		rwl.r.lock();
		try {
			Client client = CLIENTS.get(indexDirectory);
			if (client != null)
				return client;
		} finally {
			rwl.r.unlock();
		}

		rwl.w.lock();
		try {
			Client client = CLIENTS.get(indexDirectory);
			if (client != null)
				return client;
			client = ClientFactory.INSTANCE.newClient(indexDirectory, true,
					false);
			CLIENTS.put(indexDirectory, client);
			return client;
		} finally {
			rwl.w.unlock();
		}
	}

	public static final void openAll() {
		rwl.w.lock();
		try {

			for (ClientCatalogItem catalogItem : getClientCatalog(null)) {
				Logging.info("OSS loads index " + catalogItem.getIndexName());
				getClient(catalogItem.getIndexName());
			}
		} catch (SearchLibException e) {
			Logging.error(e);
		} finally {
			rwl.w.unlock();
		}
	}

	public static final void closeAll() {
		rwl.w.lock();
		try {
			for (Client client : CLIENTS.values()) {
				if (client != null) {
					Logging.info("OSS unloads index " + client.getIndexName());
					client.close();
				}
			}
			CLIENTS.clear();
			rendererResults.release();
		} finally {
			rwl.w.unlock();
		}
	}

	public static final long countAllDocuments() throws IOException,
			SearchLibException {
		rwl.r.lock();
		try {
			long count = 0;
			for (Client client : CLIENTS.values()) {
				if (client.isTrueReplicate())
					continue;
				count += client.getStatistics().getNumDocs();
			}
			return count;
		} finally {
			rwl.r.unlock();
		}
	}

	private static volatile long lastInstanceSize = 0;

	public static final long calculateInstanceSize() throws SearchLibException {
		rwl.r.lock();
		try {
			if (StartStopListener.OPENSEARCHSERVER_DATA_FILE == null)
				return 0;
			lastInstanceSize = new LastModifiedAndSize(
					StartStopListener.OPENSEARCHSERVER_DATA_FILE, false)
					.getSize();
			return lastInstanceSize;
		} finally {
			rwl.r.unlock();
		}
	}

	public static long getInstanceSize() throws SearchLibException {
		if (lastInstanceSize != 0)
			return lastInstanceSize;
		return calculateInstanceSize();
	}

	public static final LastModifiedAndSize getLastModifiedAndSize(
			String indexName) throws SearchLibException {
		if (!isValidIndexName(indexName))
			throw new SearchLibException("The name '" + indexName
					+ "' is not allowed");
		File file = new File(StartStopListener.OPENSEARCHSERVER_DATA_FILE,
				indexName);
		if (!file.exists())
			return null;
		return new LastModifiedAndSize(file, false);
	}

	public static final Client getClient(String indexName)
			throws SearchLibException {
		if (!isValidIndexName(indexName))
			throw new SearchLibException("The name '" + indexName
					+ "' is not allowed");
		return getClient(new File(StartStopListener.OPENSEARCHSERVER_DATA_FILE,
				indexName));
	}

	public static final Set<ClientCatalogItem> getClientCatalog(User user)
			throws SearchLibException {
		File[] files = StartStopListener.OPENSEARCHSERVER_DATA_FILE
				.listFiles((FileFilter) DirectoryFileFilter.DIRECTORY);
		Set<ClientCatalogItem> set = new TreeSet<ClientCatalogItem>();
		for (File file : files) {
			if (!file.isDirectory())
				continue;
			String indexName = file.getName();
			if (!isValidIndexName(indexName))
				continue;
			if (user == null || user.hasAnyRole(indexName, Role.GROUP_INDEX))
				set.add(new ClientCatalogItem(indexName));
		}
		return set;
	}

	/**
	 * Tests if an index exists
	 * 
	 * @param indexName
	 *            The name of an index
	 * @return
	 * @throws SearchLibException
	 */
	public static final boolean exists(String indexName)
			throws SearchLibException {
		return exists(null, indexName);
	}

	public static final boolean exists(User user, String indexName)
			throws SearchLibException {
		if (user != null && !user.isAdmin())
			throw new SearchLibException("Operation not permitted");
		if (!isValidIndexName(indexName))
			throw new SearchLibException("The name '" + indexName
					+ "' is not allowed");
		return getClientCatalog(null)
				.contains(new ClientCatalogItem(indexName));
	}

	private static CrawlCacheManager crawlCacheManager = null;

	public static synchronized final CrawlCacheManager getCrawlCacheManager()
			throws SearchLibException {
		if (crawlCacheManager != null)
			return crawlCacheManager;
		try {
			return crawlCacheManager = new CrawlCacheManager(
					StartStopListener.OPENSEARCHSERVER_DATA_FILE);
		} catch (InvalidPropertiesFormatException e) {
			throw new SearchLibException(e);
		} catch (IOException e) {
			throw new SearchLibException(e);
		} catch (InstantiationException e) {
			throw new SearchLibException(e);
		} catch (IllegalAccessException e) {
			throw new SearchLibException(e);
		}
	}

	private static OcrManager ocrManager = null;

	public static synchronized final OcrManager getOcrManager()
			throws SearchLibException {
		if (ocrManager != null)
			return ocrManager;
		try {
			return ocrManager = new OcrManager(
					StartStopListener.OPENSEARCHSERVER_DATA_FILE);
		} catch (InvalidPropertiesFormatException e) {
			throw new SearchLibException(e);
		} catch (IOException e) {
			throw new SearchLibException(e);
		} catch (InstantiationException e) {
			throw new SearchLibException(e);
		} catch (IllegalAccessException e) {
			throw new SearchLibException(e);
		}
	}

	final private static boolean isValidIndexName(String name) {
		if (name.startsWith("."))
			return false;
		if ("logs".equals(name))
			return false;
		return true;
	}

	/**
	 * Create a new index.
	 * 
	 * @param indexName
	 *            The name of the index.
	 * @param template
	 *            The name of the template (EMPTY_INDEX, WEB_CRAWLER,
	 *            FILE_CRAWLER)
	 * @throws IOException
	 * @throws SearchLibException
	 */
	public static void createIndex(String indexName, String templateName)
			throws SearchLibException, IOException {
		TemplateAbstract template = TemplateList.findTemplate(templateName);
		if (template == null)
			throw new SearchLibException("Template not found: " + templateName);
		createIndex(null, indexName, template);
	}

	public static void createIndex(User user, String indexName,
			TemplateAbstract template) throws SearchLibException, IOException {
		if (user != null && !user.isAdmin())
			throw new SearchLibException("Operation not permitted");
		ClientFactory.INSTANCE.properties.checkMaxIndexNumber();
		if (!isValidIndexName(indexName))
			throw new SearchLibException("The name '" + indexName
					+ "' is not allowed");
		rwl.w.lock();
		try {
			File indexDir = new File(
					StartStopListener.OPENSEARCHSERVER_DATA_FILE, indexName);
			if (indexDir.exists())
				throw new SearchLibException("directory " + indexName
						+ " already exists");
			template.createIndex(indexDir);
		} finally {
			rwl.w.unlock();
		}

	}

	/**
	 * Delete an index.
	 * 
	 * @param indexName
	 *            The name of the index
	 * @throws SearchLibException
	 * @throws NamingException
	 * @throws IOException
	 */
	public static void eraseIndex(String indexName) throws SearchLibException,
			NamingException, IOException {
		eraseIndex(null, indexName);
	}

	public static void eraseIndex(User user, String indexName)
			throws SearchLibException, NamingException, IOException {
		if (user != null && !user.isAdmin())
			throw new SearchLibException("Operation not permitted");
		if (!isValidIndexName(indexName))
			throw new SearchLibException("The name '" + indexName
					+ "' is not allowed");
		Client client = getClient(indexName);
		rwl.w.lock();
		try {
			CLIENTS.remove(client.getDirectory());
			client.close();
			client.delete();
		} finally {
			rwl.w.unlock();
		}
	}

	public static UserList getUserList() throws SearchLibException {
		rwl.r.lock();
		try {
			if (userList == null) {
				File userFile = new File(
						StartStopListener.OPENSEARCHSERVER_DATA_FILE,
						"users.xml");
				if (userFile.exists()) {
					XPathParser xpp = new XPathParser(userFile);
					userList = UserList.fromXml(xpp,
							xpp.getNode("/" + UserList.usersElement));
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
			rwl.r.unlock();
		}
	}

	public static void flushPrivileges() {
		rwl.w.lock();
		try {
			userList = null;
		} finally {
			rwl.w.unlock();
		}
	}

	private static void saveUserListWithoutLock()
			throws TransformerConfigurationException, SAXException,
			IOException, SearchLibException {
		ConfigFileRotation cfr = configFiles.get(
				StartStopListener.OPENSEARCHSERVER_DATA_FILE, "users.xml");
		try {
			XmlWriter xmlWriter = new XmlWriter(
					cfr.getTempPrintWriter("UTF-8"), "UTF-8");
			getUserList().writeXml(xmlWriter);
			xmlWriter.endDocument();
			cfr.rotate();
		} finally {
			cfr.abort();
		}
	}

	public static void saveUserList() throws SearchLibException {
		rwl.w.lock();
		try {
			saveUserListWithoutLock();
		} catch (IOException e) {
			throw new SearchLibException(e);
		} catch (TransformerConfigurationException e) {
			throw new SearchLibException(e);
		} catch (SAXException e) {
			throw new SearchLibException(e);
		} finally {
			rwl.w.unlock();
		}
	}

	public static User authenticate(String login, String password)
			throws SearchLibException {
		rwl.r.lock();
		try {
			User user = getUserList().get(login);
			if (user == null)
				return null;
			if (!user.authenticate(password))
				return null;
			return user;
		} finally {
			rwl.r.unlock();
		}
	}

	public static User authenticateKey(String login, String key)
			throws SearchLibException {
		rwl.r.lock();
		try {
			User user = getUserList().get(login);
			if (user == null)
				return null;
			if (!user.authenticateKey(key))
				return null;
			return user;
		} finally {
			rwl.r.unlock();
		}
	}

	private static final File getTempReceiveDir(Client client) {
		File clientDir = client.getDirectory();
		return new File(clientDir.getParentFile(), '.' + clientDir.getName());
	}

	private static final File getTrashReceiveDir(Client client) {
		File clientDir = client.getDirectory();
		return new File(clientDir.getParentFile(), "._" + clientDir.getName());
	}

	public static final void receive_init(Client client) throws IOException,
			SearchLibException {
		ClientFactory.INSTANCE.properties.checkMaxStorageLimit();
		File rootDir = getTempReceiveDir(client);
		FileUtils.deleteDirectory(rootDir);
		rootDir.mkdir();
	}

	public static void receive_switch(WebApp webapp, Client client)
			throws SearchLibException, NamingException, IOException {
		File trashDir = getTrashReceiveDir(client);
		File clientDir = client.getDirectory();
		if (trashDir.exists())
			FileUtils.deleteDirectory(trashDir);
		rwl.w.lock();
		try {
			client.trash(trashDir);
			getTempReceiveDir(client).renameTo(clientDir);
			CLIENTS.remove(clientDir);
			Client newClient = ClientFactory.INSTANCE.newClient(clientDir,
					true, true);
			newClient.writeReplCheck();
			CLIENTS.put(clientDir, newClient);
			PushEvent.eventClientSwitch.publish(client);
		} finally {
			rwl.w.unlock();
		}
		client.close();
		FileUtils.deleteDirectory(trashDir);
	}

	public static final void receive_dir(Client client, String filePath)
			throws IOException {
		File rootDir = getTempReceiveDir(client);
		File targetFile = new File(rootDir, filePath);
		targetFile.mkdir();
	}

	public static final boolean receive_file_exists(Client client,
			String filePath, long lastModified, long length) throws IOException {
		File existsFile = new File(client.getDirectory(), filePath);
		if (!existsFile.exists())
			return false;
		if (existsFile.lastModified() != lastModified)
			return false;
		if (existsFile.length() != length)
			return false;
		File rootDir = getTempReceiveDir(client);
		File targetFile = new File(rootDir, filePath);
		FileUtils.copyFile(existsFile, targetFile);
		return true;
	}

	public static final void receive_file(Client client, String filePath,
			long lastModified, InputStream is) throws IOException {
		File rootDir = getTempReceiveDir(client);
		File targetFile = new File(rootDir, filePath);
		targetFile.createNewFile();
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(targetFile);
			int len;
			byte[] buffer = new byte[131072];
			while ((len = is.read(buffer)) != -1)
				fos.write(buffer, 0, len);
		} catch (IOException e) {
			throw e;
		} finally {
			if (fos != null)
				IOUtils.closeQuietly(fos);
		}
		targetFile.setLastModified(lastModified);
	}

	public static int getMaxClauseCount() {
		return BooleanQuery.getMaxClauseCount();
	}

	public static void setMaxClauseCount(int value) {
		BooleanQuery.setMaxClauseCount(value);
	}

	public final static RendererResults getRendererResults() {
		rwl.r.lock();
		try {
			return rendererResults;
		} finally {
			rwl.r.unlock();
		}
	}

	public static ThreadGroup getThreadGroup() {
		rwl.r.lock();
		try {
			return threadGroup;
		} finally {
			rwl.r.unlock();
		}
	}

}
