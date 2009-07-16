/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2008 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.crawler.file.database;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.util.DomUtils;
import com.jaeksoft.searchlib.util.XPathParser;
import com.jaeksoft.searchlib.util.XmlWriter;

public class FilePathManager {

	private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock(true);
	private final Lock r = rwl.readLock();
	private final Lock w = rwl.writeLock();

	private static final int MAX_FILE_BROWSED = 10000;

	private Map<String, List<PathItem>> pathMap = null;

	private final File filePath;

	public FilePathManager(File indexDir) throws SearchLibException {
		filePath = new File(indexDir, "filePaths.xml");
		pathMap = new TreeMap<String, List<PathItem>>();
		try {
			load();
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

	private void load() throws ParserConfigurationException, SAXException,
			IOException, XPathExpressionException, SearchLibException {
		if (!filePath.exists())
			return;

		XPathParser xpp = new XPathParser(filePath);
		NodeList nodeList = xpp.getNodeList("/paths/path");

		int l = nodeList.getLength();

		List<PathItem> patternList = new ArrayList<PathItem>(l);
		for (int i = 0; i < l; i++) {
			String path = DomUtils.getText(nodeList.item(i));

			String withSubString = DomUtils.getAttributeText(nodeList.item(i),
					"withSub");
			patternList.add(new PathItem(path, PathItem.parse(withSubString)));
		}
		addListWithoutStoreAndLock(patternList, true);
	}

	private void store() throws IOException, TransformerConfigurationException,
			SAXException {
		if (!filePath.exists())
			filePath.createNewFile();
		PrintWriter pw = new PrintWriter(filePath);
		try {
			XmlWriter xmlWriter = new XmlWriter(pw, "UTF-8");
			xmlWriter.startElement("paths");
			Iterator<List<PathItem>> it = pathMap.values().iterator();
			while (it.hasNext()) {
				for (PathItem item : it.next()) {
					xmlWriter.startElement("path", "withSub", ""
							+ item.isWithSubToString());
					xmlWriter.textNode(item.getPath());
					xmlWriter.endElement();
				}
			}
			xmlWriter.endElement();
			xmlWriter.endDocument();
		} finally {
			pw.close();
		}
	}

	private void addListWithoutStoreAndLock(List<PathItem> patternList,
			boolean bDeleteAll) throws SearchLibException {
		if (bDeleteAll)
			pathMap.clear();

		// First pass: Identify already present
		for (PathItem item : patternList) {
			if (!bDeleteAll && findPath(item) != null)
				item.setStatus(PathItem.Status.ALREADY);
			else {
				addPathWithoutLock(item);
				item.setStatus(PathItem.Status.INJECTED);
			}
		}
	}

	public void addList(List<PathItem> patternList, boolean bDeleteAll)
			throws SearchLibException {
		w.lock();
		try {
			addListWithoutStoreAndLock(patternList, bDeleteAll);
			store();
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

	private void delPatternWithoutLock(String sPath) {
		List<PathItem> itemList = pathMap.get(sPath);
		if (itemList == null)
			return;
		Iterator<PathItem> it = itemList.iterator();
		while (it.hasNext())
			if (it.next().getPath().equals(sPath))
				it.remove();
	}

	public void delPath(String path) throws SearchLibException {
		w.lock();
		try {
			delPatternWithoutLock(path);
			store();
		} catch (TransformerConfigurationException e) {
			throw new SearchLibException(e);
		} catch (IOException e) {
			throw new SearchLibException(e);
		} catch (SAXException e) {
			throw new SearchLibException(e);
		} finally {
			w.unlock();
		}
	}

	private void addPathWithoutLock(PathItem patternItem) {
		String host = patternItem.getPath();
		List<PathItem> itemList = pathMap.get(host);
		if (itemList == null) {
			itemList = new ArrayList<PathItem>();
			pathMap.put(host, itemList);
		}
		itemList.add(patternItem);
	}

	public void addPath(PathItem ite) throws SearchLibException {
		w.lock();
		try {
			addPathWithoutLock(ite);
			store();
		} catch (TransformerConfigurationException e) {
			throw new SearchLibException(e);
		} catch (IOException e) {
			throw new SearchLibException(e);
		} catch (SAXException e) {
			throw new SearchLibException(e);
		} finally {
			w.unlock();
		}
	}

	public int getAllPaths(List<PathItem> list) throws SearchLibException {
		return getPaths("", 0, MAX_FILE_BROWSED, list);
	}

	public int getPaths(String startsWith, long start, long rows,
			List<PathItem> list) throws SearchLibException {
		r.lock();
		try {
			Iterator<List<PathItem>> it = pathMap.values().iterator();
			long end = start + rows;
			int pos = 0;
			int total = 0;
			while (it.hasNext())
				for (PathItem item : it.next()) {
					if (startsWith != null) {
						if (!item.getPath().startsWith(startsWith)) {
							pos++;
							continue;
						}
					}
					if (pos >= start && pos < end)
						list.add(item);

					total++;
					pos++;
				}
			return total;
		} finally {
			r.unlock();
		}
	}

	private PathItem findPath(PathItem item) {
		r.lock();
		try {
			List<PathItem> patternList = pathMap.get(item.getPath());
			if (patternList == null)
				return null;
			String sPath = item.getPath();
			for (PathItem patternItem : patternList)
				if (patternItem.getPath().equals(sPath))
					return patternItem;
			return null;
		} finally {
			r.unlock();
		}
	}

	private static void addLine(List<PathItem> list, String path,
			boolean withSub) {
		if (path.length() == 0)
			return;
		list.add(new PathItem(path, withSub));
	}

	public static List<PathItem> getPathList(String path, boolean withSub) {
		if (path == null)
			return null;

		List<PathItem> pathList = new ArrayList<PathItem>();
		addLine(pathList, path, withSub);
		return pathList;
	}

	public static List<PathItem> getPathList(BufferedReader reader)
			throws IOException {
		List<PathItem> pathList = new ArrayList<PathItem>();
		String line;
		while ((line = reader.readLine()) != null)
			addLine(pathList, line, false);
		return pathList;
	}

	public static String getStringPatternList(List<PathItem> patternList) {
		StringBuffer sPath = new StringBuffer();
		for (PathItem item : patternList) {
			sPath.append(item.getPath());
			sPath.append("\n");
		}
		return sPath.toString();
	}

}
