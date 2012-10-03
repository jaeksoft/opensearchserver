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

package com.jaeksoft.searchlib.crawler.database;

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

public class DatabaseCrawlList {

	final private ReadWriteLock rwl = new ReadWriteLock();

	private TreeMap<String, DatabaseCrawl> map;
	private DatabaseCrawl[] array;

	private DatabaseCrawlList() {
		map = new TreeMap<String, DatabaseCrawl>();
	}

	private final static String DBCRAWLLIST_ROOTNODE_NAME = "databaseCrawlList";

	public static DatabaseCrawlList fromXml(
			DatabaseCrawlMaster databaseCrawlMaster, File file)
			throws XPathExpressionException, ParserConfigurationException,
			SAXException, IOException {
		DatabaseCrawlList dbCrawlList = new DatabaseCrawlList();
		if (!file.exists())
			return dbCrawlList;
		XPathParser xpp = new XPathParser(file);
		Node rootNode = xpp.getNode(DBCRAWLLIST_ROOTNODE_NAME);
		if (rootNode == null)
			return dbCrawlList;
		NodeList nodes = xpp.getNodeList(rootNode,
				DatabaseCrawl.DBCRAWL_NODE_NAME);
		if (nodes == null)
			return dbCrawlList;
		for (int i = 0; i < nodes.getLength(); i++) {
			DatabaseCrawl dbCrawl = DatabaseCrawl.fromXml(databaseCrawlMaster,
					xpp, nodes.item(i));
			dbCrawlList.add(dbCrawl);
		}
		return dbCrawlList;
	}

	public void add(DatabaseCrawl dbCrawl) {
		rwl.w.lock();
		try {
			map.put(dbCrawl.getName(), dbCrawl);
			buildArray();
		} finally {
			rwl.w.unlock();
		}
	}

	public DatabaseCrawl get(String name) {
		rwl.r.lock();
		try {
			return map.get(name);
		} finally {
			rwl.r.unlock();
		}
	}

	public void remove(DatabaseCrawl dbCrawl) {
		rwl.w.lock();
		try {
			map.remove(dbCrawl.getName());
			buildArray();
		} finally {
			rwl.w.unlock();
		}
	}

	public void writeXml(XmlWriter xmlWriter) throws SAXException,
			UnsupportedEncodingException {
		rwl.r.lock();
		try {
			xmlWriter.startElement(DBCRAWLLIST_ROOTNODE_NAME);
			for (DatabaseCrawl item : map.values())
				item.writeXml(xmlWriter);
			xmlWriter.endElement();
		} finally {
			rwl.r.unlock();
		}
	}

	private void buildArray() {
		array = new DatabaseCrawl[map.size()];
		map.values().toArray(array);
	}

	public DatabaseCrawl[] getArray() {
		rwl.r.lock();
		try {
			return array;
		} finally {
			rwl.r.unlock();
		}
	}

}
