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

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.util.DomUtils;
import com.jaeksoft.searchlib.util.ReadWriteLock;
import com.jaeksoft.searchlib.util.XPathParser;
import com.jaeksoft.searchlib.util.XmlWriter;

public class FilePathManager {

	final private ReadWriteLock rwl = new ReadWriteLock();

	private Map<File, FilePathItem> filePathMap = null;

	private final File filePathFile;

	public FilePathManager(File indexDir) throws SearchLibException {
		filePathFile = new File(indexDir, "filePaths.xml");
		filePathMap = new TreeMap<File, FilePathItem>();
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
		if (!filePathFile.exists())
			return;

		XPathParser xpp = new XPathParser(filePathFile);
		NodeList nodeList = xpp.getNodeList("/paths/path");

		int l = nodeList.getLength();

		List<FilePathItem> patternList = new ArrayList<FilePathItem>(l);
		for (int i = 0; i < l; i++) {
			String path = DomUtils.getText(nodeList.item(i));
			File filePath = new File(path);

			String withSubString = DomUtils.getAttributeText(nodeList.item(i),
					"withSub");
			patternList.add(new FilePathItem(filePath, FilePathItem
					.parse(withSubString)));
		}
		addListWithoutStoreAndLock(patternList, true);
	}

	private void store() throws IOException, TransformerConfigurationException,
			SAXException {
		if (!filePathFile.exists())
			filePathFile.createNewFile();
		PrintWriter pw = new PrintWriter(filePathFile);
		try {
			XmlWriter xmlWriter = new XmlWriter(pw, "UTF-8");
			xmlWriter.startElement("paths");
			for (FilePathItem item : filePathMap.values()) {
				xmlWriter.startElement("path", "withSub",
						"" + item.getWithSubToString());
				xmlWriter.textNode(item.getFilePath().getAbsolutePath());
				xmlWriter.endElement();
			}
			xmlWriter.endElement();
			xmlWriter.endDocument();
		} finally {
			pw.close();
		}
	}

	private void addListWithoutStoreAndLock(List<FilePathItem> filePathList,
			boolean bDeleteAll) throws SearchLibException {
		if (bDeleteAll)
			filePathMap.clear();

		// First pass: Identify already present
		for (FilePathItem item : filePathList) {
			if (!bDeleteAll && filePathMap.containsKey(item.getFilePath()))
				item.setStatus(FilePathItem.Status.ALREADY);
			else {
				addPathWithoutLock(item);
				item.setStatus(FilePathItem.Status.INJECTED);
			}
		}
	}

	public void addList(List<FilePathItem> filePathList, boolean bDeleteAll)
			throws SearchLibException {
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

	private void delPatternWithoutLock(String sPath) {
		filePathMap.remove(new File(sPath));
	}

	public void delPath(String path) throws SearchLibException {
		rwl.w.lock();
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
			rwl.w.unlock();
		}
	}

	private void addPathWithoutLock(FilePathItem filePathItem) {
		filePathMap.put(filePathItem.getFilePath(), filePathItem);
	}

	public void addPath(FilePathItem item) throws SearchLibException {
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

	public void add(File file, boolean withSubDir) throws SearchLibException {
		addPath(new FilePathItem(file, withSubDir));
	}

	public int getFilePaths(String startsWith, long start, long rows,
			List<FilePathItem> list) throws SearchLibException {
		rwl.r.lock();
		try {
			long end = start + rows;
			int pos = 0;
			int total = 0;
			for (FilePathItem item : filePathMap.values()) {
				if (startsWith != null) {
					if (!item.getFilePath().getAbsolutePath()
							.startsWith(startsWith)) {
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
			rwl.r.unlock();
		}
	}

	public FilePathItem getFilePath(File file) {
		rwl.r.lock();
		try {
			return filePathMap.get(file);
		} finally {
			rwl.r.unlock();
		}
	}

	public void getAllFilePaths(List<FilePathItem> list) {
		rwl.r.lock();
		try {
			for (FilePathItem item : filePathMap.values())
				list.add(item);
		} finally {
			rwl.r.unlock();
		}
	}

	public void remove(File file) {
		rwl.w.lock();
		try {
			filePathMap.remove(file);
		} finally {
			rwl.w.unlock();
		}

	}

}
