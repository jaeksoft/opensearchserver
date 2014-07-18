/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2014 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.crawler.web.database;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.util.LinkUtils;
import com.jaeksoft.searchlib.util.ReadWriteLock;
import com.jaeksoft.searchlib.util.XPathParser;
import com.jaeksoft.searchlib.util.XmlWriter;

public abstract class AbstractPatternNameValueManager<T extends AbstractPatternNameValueItem> {

	final protected ReadWriteLock rwl = new ReadWriteLock();

	// For better performances, pattern are grouped by hostname in a map
	private Map<String, List<T>> itemMap = null;

	private File xmlFile;

	public AbstractPatternNameValueManager(File indexDir, String filename)
			throws SearchLibException {
		xmlFile = new File(indexDir, filename);
		itemMap = new TreeMap<String, List<T>>();
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

	protected abstract void load() throws ParserConfigurationException,
			SAXException, IOException, XPathExpressionException,
			SearchLibException;

	protected abstract T getNewItem(Node node);

	protected void load(String XPP_PATH) throws ParserConfigurationException,
			SAXException, IOException, XPathExpressionException,
			SearchLibException {
		if (!xmlFile.exists())
			return;
		XPathParser xpp = new XPathParser(xmlFile);
		NodeList nodeList = xpp.getNodeList(XPP_PATH);
		int l = nodeList.getLength();
		for (int i = 0; i < l; i++) {
			Node node = nodeList.item(i);
			T item = getNewItem(node);
			addItem(item);
		}
	}

	protected abstract void store() throws IOException,
			TransformerConfigurationException, SAXException;

	protected void store(String rootNodeName) throws IOException,
			TransformerConfigurationException, SAXException {
		if (!xmlFile.exists())
			xmlFile.createNewFile();
		PrintWriter pw = new PrintWriter(xmlFile);
		try {
			XmlWriter xmlWriter = new XmlWriter(pw, "UTF-8");
			xmlWriter.startElement(rootNodeName);
			Iterator<List<T>> it = itemMap.values().iterator();
			while (it.hasNext())
				for (AbstractPatternNameValueItem item : it.next())
					item.writeXml(xmlWriter);
			xmlWriter.endElement();
			xmlWriter.endDocument();
		} finally {
			pw.close();
		}
	}

	public List<T> getItems(String url) throws MalformedURLException,
			URISyntaxException {
		rwl.r.lock();
		try {
			String host = LinkUtils.newEncodedURL(url).getHost();
			List<T> itList = itemMap.get(host);
			if (itList == null)
				return null;
			List<T> itemList = new ArrayList<T>(0);
			Iterator<T> it = itList.iterator();
			while (it.hasNext()) {
				T item = it.next();
				if (url.startsWith(item.getPattern()))
					itemList.add(item);
			}
			return itemList;
		} finally {
			rwl.r.unlock();
		}
	}

	private void delItemWithoutLock(T item) throws MalformedURLException,
			URISyntaxException {
		String host = LinkUtils.newEncodedURL(item.getPattern()).getHost();
		List<T> itemList = itemMap.get(host);
		if (itemList == null)
			return;
		itemList.remove(item);
		if (itemList.size() == 0)
			itemMap.remove(host);
	}

	public void delItem(T item) throws SearchLibException {
		rwl.w.lock();
		try {
			delItemWithoutLock(item);
			store();
		} catch (MalformedURLException e) {
			throw new SearchLibException(e);
		} catch (TransformerConfigurationException e) {
			throw new SearchLibException(e);
		} catch (IOException e) {
			throw new SearchLibException(e);
		} catch (SAXException e) {
			throw new SearchLibException(e);
		} catch (URISyntaxException e) {
			throw new SearchLibException(e);
		} finally {
			rwl.w.unlock();
		}
	}

	private void addItemWithoutLock(T item) throws MalformedURLException,
			URISyntaxException {
		String host = item.extractUrl().getHost();
		List<T> itemList = itemMap.get(host);
		if (itemList == null) {
			itemList = new ArrayList<T>(0);
			itemMap.put(host, itemList);
		}
		itemList.add(item);
	}

	public void addItem(T item) throws SearchLibException {
		rwl.w.lock();
		try {
			addItemWithoutLock(item);
			store();
		} catch (TransformerConfigurationException e) {
			throw new SearchLibException(e);
		} catch (IOException e) {
			throw new SearchLibException(e);
		} catch (SAXException e) {
			throw new SearchLibException(e);
		} catch (URISyntaxException e) {
			throw new SearchLibException(e);
		} finally {
			rwl.w.unlock();
		}
	}

	public void updateItem(AbstractPatternNameValueItem item)
			throws SearchLibException {
		rwl.w.lock();
		try {
			boolean found = false;
			Iterator<List<T>> it = itemMap.values().iterator();
			while (it.hasNext() && !found)
				found = it.next().contains(item);
			if (!found)
				throw new SearchLibException("Unknown item");
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

	public int getItems(String startsWith, long start, long rows, List<T> list)
			throws SearchLibException {
		rwl.r.lock();
		try {
			Iterator<List<T>> it = itemMap.values().iterator();
			long end = start + rows;
			int pos = 0;
			int total = 0;
			while (it.hasNext())
				for (T item : it.next()) {
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
			rwl.r.unlock();
		}
	}

}
