/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008-2014 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.crawler.file.database;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.config.Config;
import com.jaeksoft.searchlib.util.ReadWriteLock;
import com.jaeksoft.searchlib.util.XPathParser;
import com.jaeksoft.searchlib.util.XmlWriter;

public class FilePathManager {

	final private ReadWriteLock rwl = new ReadWriteLock();

	private TreeMap<FilePathItem, FilePathItem> filePathMap = null;

	private final File filePathFile;

	public FilePathManager(Config config, File indexDir) throws SearchLibException {
		filePathFile = new File(indexDir, "filePaths.xml");
		filePathMap = new TreeMap<FilePathItem, FilePathItem>();
		try {
			load(config);
		} catch (ParserConfigurationException e) {
			throw new SearchLibException(e);
		} catch (SAXException e) {
			throw new SearchLibException(e);
		} catch (IOException e) {
			throw new SearchLibException(e);
		} catch (XPathExpressionException e) {
			throw new SearchLibException(e);
		} catch (URISyntaxException e) {
			throw new SearchLibException(e);
		}
	}

	private void load(Config config) throws ParserConfigurationException, SAXException, IOException,
			XPathExpressionException, SearchLibException, URISyntaxException {
		if (!filePathFile.exists())
			return;

		XPathParser xpp = new XPathParser(filePathFile);
		NodeList nodeList = xpp.getNodeList("/paths/path");

		int l = nodeList.getLength();

		List<FilePathItem> filePathList = new ArrayList<FilePathItem>(l);
		for (int i = 0; i < l; i++)
			filePathList.add(FilePathItem.fromXml(config, nodeList.item(i)));
		addListWithoutStoreAndLock(filePathList, true);
	}

	private void store() throws IOException, TransformerConfigurationException, SAXException {
		if (!filePathFile.exists())
			filePathFile.createNewFile();
		PrintWriter pw = new PrintWriter(filePathFile);
		try {
			XmlWriter xmlWriter = new XmlWriter(pw, "UTF-8");
			xmlWriter.startElement("paths");
			for (FilePathItem item : filePathMap.keySet())
				item.writeXml(xmlWriter, "path");
			xmlWriter.endElement();
			xmlWriter.endDocument();
		} finally {
			pw.close();
		}
	}

	private void addListWithoutStoreAndLock(List<FilePathItem> filePathList, boolean bDeleteAll)
			throws SearchLibException {
		if (bDeleteAll)
			filePathMap.clear();

		for (FilePathItem item : filePathList)
			addPathWithoutLock(item);
	}

	public void addList(List<FilePathItem> filePathList, boolean bDeleteAll) throws SearchLibException {
		rwl.w.lock();
		try {
			addListWithoutStoreAndLock(filePathList, bDeleteAll);
			store();
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

	private void addPathWithoutLock(FilePathItem filePathItem) throws SearchLibException {
		if (filePathMap.containsKey(filePathItem))
			throw new SearchLibException("This filePathItem already exists");
		filePathMap.put(filePathItem, filePathItem);
	}

	public void add(FilePathItem item) throws SearchLibException {
		rwl.w.lock();
		try {
			addPathWithoutLock(item);
			store();
		} catch (TransformerConfigurationException e) {
			throw new SearchLibException(e);
		} catch (IOException e) {
			throw new SearchLibException(e);
		} catch (SAXException e) {
			throw new SearchLibException(e);
		} finally {
			rwl.w.unlock();
		}
	}

	public int getFilePaths(long start, long rows, List<FilePathItem> list) throws SearchLibException {
		rwl.r.lock();
		try {
			long end = start + rows;
			int pos = 0;
			int total = 0;
			for (FilePathItem item : filePathMap.keySet()) {
				if (pos >= start && pos < end)
					list.add(item);
				total++;
				pos++;
			}
			return total;
		} finally {
			rwl.r.unlock();
		}
	}

	public void getAllFilePaths(List<FilePathItem> list) {
		rwl.r.lock();
		try {
			for (FilePathItem item : filePathMap.keySet())
				list.add(item);
		} finally {
			rwl.r.unlock();
		}
	}

	public void getAllFilePathsString(List<String> list) {
		rwl.r.lock();
		try {
			for (FilePathItem item : filePathMap.keySet())
				list.add(item.toString());
		} finally {
			rwl.r.unlock();
		}
	}

	public void getFilePathsToFetch(List<FilePathItem> list) {
		rwl.r.lock();
		try {
			for (FilePathItem item : filePathMap.keySet())
				if (item.isEnabled())
					list.add(item);
		} finally {
			rwl.r.unlock();
		}
	}

	public void remove(FilePathItem filePathItem) throws SearchLibException {
		rwl.w.lock();
		try {
			filePathMap.remove(filePathItem);
			store();
		} catch (TransformerConfigurationException e) {
			throw new SearchLibException(e);
		} catch (IOException e) {
			throw new SearchLibException(e);
		} catch (SAXException e) {
			throw new SearchLibException(e);
		} finally {
			rwl.w.unlock();
		}

	}

	/**
	 * Return the FilePathItem
	 * 
	 * @param currentFilePath
	 * @return
	 */
	public FilePathItem get(FilePathItem currentFilePath) {
		rwl.r.lock();
		try {
			return filePathMap.get(currentFilePath);
		} finally {
			rwl.r.unlock();
		}
	}

	/**
	 * Returns the first filePathItem with the given scheme and the given host.
	 * 
	 * @param scheme
	 * @param host
	 * @return
	 */
	public FilePathItem findFirst(String scheme, String host) {
		rwl.r.lock();
		try {
			for (FilePathItem item : filePathMap.keySet())
				if (item.getType().getScheme().equals(scheme) && item.getHost().equals(host))
					return item;
			return null;
		} finally {
			rwl.r.unlock();
		}
	}

	public FilePathItem find(String filePath) {
		if (filePath == null)
			return null;
		rwl.r.lock();
		try {
			for (FilePathItem item : filePathMap.keySet())
				if (filePath.equals(item.toString()))
					return item;
			return null;
		} finally {
			rwl.r.unlock();
		}
	}

}
