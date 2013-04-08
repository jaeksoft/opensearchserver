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

package com.jaeksoft.searchlib.crawler.rest;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.TreeMap;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.util.ReadWriteLock;
import com.jaeksoft.searchlib.util.XPathParser;
import com.jaeksoft.searchlib.util.XmlWriter;

public class RestCrawlList {

	final private ReadWriteLock rwl = new ReadWriteLock();

	private TreeMap<String, RestCrawlItem> map;
	private RestCrawlItem[] array;

	private RestCrawlList() {
		map = new TreeMap<String, RestCrawlItem>();
	}

	private final static String REST_CRAWLLIST_ROOTNODE_NAME = "restCrawlList";

	public static RestCrawlList fromXml(RestCrawlMaster crawlMaster, File file)
			throws XPathExpressionException, ParserConfigurationException,
			SAXException, IOException {
		RestCrawlList crawlList = new RestCrawlList();
		if (!file.exists())
			return crawlList;
		XPathParser xpp = new XPathParser(file);
		Node rootNode = xpp.getNode(REST_CRAWLLIST_ROOTNODE_NAME);
		if (rootNode == null)
			return crawlList;
		NodeList nodes = xpp.getNodeList(rootNode,
				RestCrawlItem.REST_CRAWL_NODE_NAME);
		if (nodes == null)
			return crawlList;
		for (int i = 0; i < nodes.getLength(); i++) {
			RestCrawlItem crawlItem = new RestCrawlItem(crawlMaster, xpp,
					nodes.item(i));
			if (crawlItem != null)
				crawlList.add(crawlItem);
		}
		return crawlList;
	}

	public void add(RestCrawlItem crawlItem) {
		rwl.w.lock();
		try {
			map.put(crawlItem.getName(), crawlItem);
			buildArray();
		} finally {
			rwl.w.unlock();
		}
	}

	public RestCrawlItem get(String name) {
		rwl.r.lock();
		try {
			return map.get(name);
		} finally {
			rwl.r.unlock();
		}
	}

	public void remove(RestCrawlItem crawlItem) {
		rwl.w.lock();
		try {
			map.remove(crawlItem.getName());
			buildArray();
		} finally {
			rwl.w.unlock();
		}
	}

	public void writeXml(XmlWriter xmlWriter) throws SAXException,
			UnsupportedEncodingException {
		rwl.r.lock();
		try {
			xmlWriter.startElement(REST_CRAWLLIST_ROOTNODE_NAME);
			for (RestCrawlItem item : map.values())
				item.writeXml(xmlWriter);
			xmlWriter.endElement();
		} finally {
			rwl.r.unlock();
		}
	}

	private void buildArray() {
		array = new RestCrawlItem[map.size()];
		map.values().toArray(array);
	}

	public RestCrawlItem[] getArray() {
		rwl.r.lock();
		try {
			return array;
		} finally {
			rwl.r.unlock();
		}
	}

}
