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

package com.jaeksoft.searchlib.crawler.web.database;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
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

public class FilePatternManager {

	private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock(true);
	private final Lock r = rwl.readLock();
	private final Lock w = rwl.writeLock();

	private Map<String, List<FileItem>> patternMap = null;

	private final File patternFile;

	public FilePatternManager(File indexDir) throws SearchLibException {
		patternFile = new File(indexDir, "filePatterns.xml");
		patternMap = new TreeMap<String, List<FileItem>>();
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
		if (!patternFile.exists())
			return;
		XPathParser xpp = new XPathParser(patternFile);
		NodeList nodeList = xpp.getNodeList("/patterns/pattern");
		int l = nodeList.getLength();
		List<FileItem> patternList = new ArrayList<FileItem>(l);
		for (int i = 0; i < l; i++)
			patternList.add(new FileItem(DomUtils.getText(nodeList.item(i))));
		addListWithoutStoreAndLock(patternList, true);
	}

	private void store() throws IOException, TransformerConfigurationException,
			SAXException {
		if (!patternFile.exists())
			patternFile.createNewFile();
		PrintWriter pw = new PrintWriter(patternFile);
		try {
			XmlWriter xmlWriter = new XmlWriter(pw, "UTF-8");
			xmlWriter.startElement("patterns");
			Iterator<List<FileItem>> it = patternMap.values().iterator();
			while (it.hasNext()) {
				for (FileItem item : it.next()) {
					xmlWriter.startElement("pattern");
					xmlWriter.textNode(item.getPattern());
					xmlWriter.endElement();
				}
			}
			xmlWriter.endElement();
			xmlWriter.endDocument();
		} finally {
			pw.close();
		}
	}

	private void addListWithoutStoreAndLock(List<FileItem> patternList,
			boolean bDeleteAll) throws SearchLibException {
		if (bDeleteAll)
			patternMap.clear();

		// First pass: Identify already present
		for (FileItem item : patternList) {
			if (!bDeleteAll && findPattern(item) != null)
				item.setStatus(FileItem.Status.ALREADY);
			else {
				addPatternWithoutLock(item);
				item.setStatus(FileItem.Status.INJECTED);
			}
		}
	}

	public void addList(List<FileItem> patternList, boolean bDeleteAll)
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

	private void delPatternWithoutLock(String sPattern) {
		List<FileItem> itemList = patternMap.get(sPattern);
		if (itemList == null)
			return;
		Iterator<FileItem> it = itemList.iterator();
		while (it.hasNext())
			if (it.next().sPattern.equals(sPattern))
				it.remove();
	}

	public void delPattern(Collection<String> patterns)
			throws SearchLibException {
		w.lock();
		try {
			for (String pattern : patterns)
				delPatternWithoutLock(pattern);
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

	private void addPatternWithoutLock(FileItem patternItem) {
		String host = patternItem.getPattern();
		List<FileItem> itemList = patternMap.get(host);
		if (itemList == null) {
			itemList = new ArrayList<FileItem>();
			patternMap.put(host, itemList);
		}
		itemList.add(patternItem);
	}

	public void addPattern(FileItem patternItem) throws SearchLibException {
		w.lock();
		try {
			addPatternWithoutLock(patternItem);
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

	public int getPatterns(String startsWith, long start, long rows,
			List<FileItem> list) throws SearchLibException {
		r.lock();
		try {
			Iterator<List<FileItem>> it = patternMap.values().iterator();
			long end = start + rows;
			int pos = 0;
			int total = 0;
			while (it.hasNext())
				for (FileItem item : it.next()) {
					if (startsWith != null) {
						if (!item.getPattern().startsWith(startsWith)) {
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

	private FileItem findPattern(FileItem item) {
		r.lock();
		try {
			List<FileItem> patternList = patternMap.get(item.getPattern());
			if (patternList == null)
				return null;
			String sPattern = item.getPattern();
			for (FileItem patternItem : patternList)
				if (patternItem.getPattern().equals(sPattern))
					return patternItem;
			return null;
		} finally {
			r.unlock();
		}
	}

	public FileItem matchPattern(String pattern) {
		r.lock();
		try {
			List<FileItem> patternList = patternMap.get(pattern);
			if (patternList == null)
				return null;
			for (FileItem patternItem : patternList)
				if (patternItem.match(pattern))
					return patternItem;
			return null;
		} finally {
			r.unlock();
		}
	}

	/*
	 * public void injectUrl(List<InjectUrlItem> urlListItems) throws
	 * SearchLibException { Iterator<InjectUrlItem> it =
	 * urlListItems.iterator(); List<FileItem> patternList = new
	 * ArrayList<FileItem>(); while (it.hasNext()) { InjectUrlItem item =
	 * it.next(); if (matchPattern(item.getURL()) != null) continue;
	 * patternList.add(new FileItem(item.getURL())); } addList(patternList,
	 * false); }
	 */

	private static void addLine(List<FileItem> list, String pattern) {
		pattern = pattern.trim();
		if (pattern.length() == 0)
			return;
		list.add(new FileItem(pattern));
	}

	public static List<FileItem> getPatternList(String pattern) {
		List<FileItem> patternList = new ArrayList<FileItem>();
		for (String sPattern : pattern.split("\n"))
			addLine(patternList, sPattern);
		return patternList;
	}

	public static List<FileItem> getPatternList(BufferedReader reader)
			throws IOException {
		List<FileItem> patternList = new ArrayList<FileItem>();
		String line;
		while ((line = reader.readLine()) != null)
			addLine(patternList, line);
		return patternList;
	}

	public static String getStringPatternList(List<FileItem> patternList) {
		StringBuffer sPattern = new StringBuffer();
		for (FileItem item : patternList) {
			sPattern.append(item.getPattern());
			sPattern.append("\n");
		}
		return sPattern.toString();
	}

}
