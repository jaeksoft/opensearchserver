/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2010 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.crawler.database;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.util.XPathParser;
import com.jaeksoft.searchlib.util.XmlWriter;

public class DatabaseCrawlList {

	private Set<DatabaseCrawl> set;

	private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock(true);
	private final Lock r = rwl.readLock();
	private final Lock w = rwl.writeLock();

	private DatabaseCrawlList() {
		w.lock();
		try {
			set = new TreeSet<DatabaseCrawl>();
		} finally {
			w.unlock();
		}
	}

	private void add(DatabaseCrawl dbCrawl) {
		w.lock();
		try {
			set.add(dbCrawl);
		} finally {
			w.unlock();
		}
	}

	public Set<DatabaseCrawl> getSet() {
		r.lock();
		try {
			return set;
		} finally {
			r.unlock();
		}
	}

	private final static String DBCRAWLLIST_ROOTNODE_NAME = "databaseCrawlList";

	public void writeXml(XmlWriter xmlWriter) throws SAXException {
		r.lock();
		try {
			xmlWriter.startElement(DBCRAWLLIST_ROOTNODE_NAME);
			for (DatabaseCrawl dbCrawl : set)
				dbCrawl.writeXml(xmlWriter);
			xmlWriter.endElement();
		} finally {
			r.unlock();
		}
	}

	public static DatabaseCrawlList fromXml(File file)
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
			DatabaseCrawl dbCrawl = DatabaseCrawl.fromXml(xpp, nodes.item(i));
			dbCrawlList.add(dbCrawl);
		}
		return dbCrawlList;
	}
}
