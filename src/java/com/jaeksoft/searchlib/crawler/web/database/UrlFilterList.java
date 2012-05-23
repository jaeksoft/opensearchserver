/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2010-2012 Emmanuel Keller / Jaeksoft
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
import java.util.SortedSet;
import java.util.TreeSet;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.util.ReadWriteLock;
import com.jaeksoft.searchlib.util.XPathParser;
import com.jaeksoft.searchlib.util.XmlWriter;

public class UrlFilterList {

	final private ReadWriteLock rwl = new ReadWriteLock();

	private File configFile;
	private TreeSet<UrlFilterItem> filterSet;
	private UrlFilterItem[] array;

	public UrlFilterList(File indexDir, String filename)
			throws SearchLibException {
		configFile = new File(indexDir, filename);
		filterSet = new TreeSet<UrlFilterItem>();
		array = null;
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
		if (!configFile.exists())
			return;
		XPathParser xpp = new XPathParser(configFile);
		NodeList nodeList = xpp.getNodeList("/urlFilters/urlFilter");
		int l = nodeList.getLength();
		TreeSet<UrlFilterItem> set = new TreeSet<UrlFilterItem>();
		for (int i = 0; i < l; i++) {
			UrlFilterItem item = new UrlFilterItem(nodeList.item(i));
			set.add(item);
		}
		rwl.w.lock();
		try {
			filterSet = set;
			array = null;
		} finally {
			rwl.w.unlock();
		}
	}

	public void writeXml(XmlWriter xmlWriter) throws IOException,
			TransformerConfigurationException, SAXException {
		rwl.w.lock();
		try {
			xmlWriter.startElement("urlFilters");
			for (UrlFilterItem item : filterSet)
				item.writeXml(xmlWriter);
			xmlWriter.endElement();
			xmlWriter.endDocument();
		} finally {
			rwl.w.unlock();
		}
	}

	public UrlFilterItem[] getArray() {
		rwl.r.lock();
		try {
			if (array != null)
				return array;
			array = new UrlFilterItem[filterSet.size()];
			filterSet.toArray(array);
			return array;
		} finally {
			rwl.r.unlock();
		}
	}

	public void add(UrlFilterItem item) {
		rwl.w.lock();
		try {
			filterSet.add(item);
			array = null;
		} finally {
			rwl.w.unlock();
		}
	}

	public void remove(UrlFilterItem item) {
		rwl.w.lock();
		try {
			filterSet.remove(item);
			array = null;
		} finally {
			rwl.w.unlock();
		}
	}

	public UrlFilterItem get(String name) {
		rwl.r.lock();
		try {
			UrlFilterItem finder = new UrlFilterItem(name, null);
			SortedSet<UrlFilterItem> s = filterSet.subSet(finder, true, finder,
					true);
			if (s == null)
				return null;
			if (s.size() == 0)
				return null;
			return s.first();
		} finally {
			rwl.r.unlock();
		}
	}

	private static final String doReplaceQuery(String uriString,
			UrlFilterItem[] urlFilterArray) {
		int i = uriString.indexOf('?');
		if (i == -1)
			return uriString;
		if (urlFilterArray == null)
			return uriString;
		StringBuffer newUrl = new StringBuffer(uriString.substring(0, i++));
		String queryString = uriString.substring(i);

		String[] queryParts = queryString.split("\\" + '&');

		if (queryParts == null || queryParts.length == 0)
			return uriString;

		for (UrlFilterItem urlFilter : urlFilterArray)
			urlFilter.doReplaceQuery(queryParts);
		boolean first = true;
		for (String queryPart : queryParts) {
			if (queryPart != null) {
				if (first) {
					newUrl.append('?');
					first = false;
				} else
					newUrl.append('&');
				newUrl.append(queryPart);
			}
		}
		return newUrl.toString();
	}

	private static final String doReplaceProspero(String uriString,
			UrlFilterItem[] urlFilterArray) {
		int i1 = uriString.indexOf(';');
		if (i1 == -1)
			return uriString;
		i1++;
		if (i1 == uriString.length())
			return uriString;
		int i2 = uriString.indexOf('/', i1);
		if (i2 == -1)
			i2 = uriString.indexOf('?', i1);
		String part = i2 == -1 ? uriString.substring(i1) : uriString.substring(
				i1, i2);
		boolean bReplace = false;
		for (UrlFilterItem urlFilter : urlFilterArray) {
			if (urlFilter.isReplaceProspero(part)) {
				bReplace = true;
				break;
			}
		}
		if (!bReplace)
			return uriString;
		StringBuffer newUrl = new StringBuffer(uriString.substring(0, i1 - 1));
		if (i2 != -1)
			newUrl.append(uriString.substring(i2));
		return newUrl.toString();
	}

	public static final String doReplace(String uriString,
			UrlFilterItem[] urlFilterArray) {
		uriString = doReplaceQuery(uriString, urlFilterArray);
		uriString = doReplaceProspero(uriString, urlFilterArray);
		return uriString;
	}
}
