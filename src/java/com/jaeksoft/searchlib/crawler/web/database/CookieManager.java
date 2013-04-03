/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2013 Emmanuel Keller / Jaeksoft
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

public class CookieManager {

	final private ReadWriteLock rwl = new ReadWriteLock();

	// For better performances, pattern are grouped by hostname in a map
	private Map<String, List<CookieItem>> cookieMap = null;

	private File cookieFile;

	public CookieManager(File indexDir, String filename)
			throws SearchLibException {
		cookieFile = new File(indexDir, filename);
		cookieMap = new TreeMap<String, List<CookieItem>>();
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
		if (!cookieFile.exists())
			return;
		XPathParser xpp = new XPathParser(cookieFile);
		NodeList nodeList = xpp.getNodeList("/cookies/cookie");
		int l = nodeList.getLength();
		for (int i = 0; i < l; i++) {
			Node node = nodeList.item(i);
			CookieItem cookieItem = CookieItem.fromXml(node);
			addCookie(cookieItem);
		}
	}

	private void store() throws IOException, TransformerConfigurationException,
			SAXException {
		if (!cookieFile.exists())
			cookieFile.createNewFile();
		PrintWriter pw = new PrintWriter(cookieFile);
		try {
			XmlWriter xmlWriter = new XmlWriter(pw, "UTF-8");
			xmlWriter.startElement("cookies");
			Iterator<List<CookieItem>> it = cookieMap.values().iterator();
			while (it.hasNext())
				for (CookieItem item : it.next())
					item.writeXml(xmlWriter);
			xmlWriter.endElement();
			xmlWriter.endDocument();
		} finally {
			pw.close();
		}
	}

	public List<CookieItem> getCookies(String url)
			throws MalformedURLException, URISyntaxException {
		rwl.r.lock();
		try {
			String host = LinkUtils.newEncodedURL(url).getHost();
			List<CookieItem> itemList = cookieMap.get(host);
			if (itemList == null)
				return null;
			List<CookieItem> cookieList = new ArrayList<CookieItem>(0);
			Iterator<CookieItem> it = itemList.iterator();
			while (it.hasNext()) {
				CookieItem item = it.next();
				if (url.startsWith(item.getPattern()))
					cookieList.add(item);
			}
			return cookieList;
		} finally {
			rwl.r.unlock();
		}
	}

	private void delCookieWithoutLock(CookieItem cookie)
			throws MalformedURLException, URISyntaxException {
		String host = LinkUtils.newEncodedURL(cookie.getPattern()).getHost();
		List<CookieItem> itemList = cookieMap.get(host);
		if (itemList == null)
			return;
		itemList.remove(cookie);
		if (itemList.size() == 0)
			cookieMap.remove(host);
	}

	public void delCookie(CookieItem cookie) throws SearchLibException {
		rwl.w.lock();
		try {
			delCookieWithoutLock(cookie);
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

	private void addCookieWithoutLock(CookieItem cookieItem)
			throws MalformedURLException, URISyntaxException {
		String host = cookieItem.extractUrl().getHost();
		List<CookieItem> itemList = cookieMap.get(host);
		if (itemList == null) {
			itemList = new ArrayList<CookieItem>(0);
			cookieMap.put(host, itemList);
		}
		itemList.add(cookieItem);
	}

	public void addCookie(CookieItem cookieItem) throws SearchLibException {
		rwl.w.lock();
		try {
			addCookieWithoutLock(cookieItem);
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

	public void updateCookie(CookieItem cookie) throws SearchLibException {
		rwl.w.lock();
		try {
			boolean found = false;
			Iterator<List<CookieItem>> it = cookieMap.values().iterator();
			while (it.hasNext() && !found)
				found = it.next().contains(cookie);
			if (!found)
				throw new SearchLibException("Unknown cookie item");
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

	public int getCookies(String startsWith, long start, long rows,
			List<CookieItem> list) throws SearchLibException {
		rwl.r.lock();
		try {
			Iterator<List<CookieItem>> it = cookieMap.values().iterator();
			long end = start + rows;
			int pos = 0;
			int total = 0;
			while (it.hasNext())
				for (CookieItem item : it.next()) {
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
