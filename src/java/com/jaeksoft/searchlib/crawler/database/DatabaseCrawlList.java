/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2010 Emmanuel Keller / Jaeksoft
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

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.crawler.UniqueNameSetGeneric;
import com.jaeksoft.searchlib.util.XPathParser;
import com.jaeksoft.searchlib.util.XmlWriter;

public class DatabaseCrawlList extends UniqueNameSetGeneric<DatabaseCrawl> {

	private DatabaseCrawlMaster databaseCrawlMaster;

	private DatabaseCrawlList(DatabaseCrawlMaster databaseCrawlMaster) {
		this.databaseCrawlMaster = databaseCrawlMaster;
		init();
	}

	private final static String DBCRAWLLIST_ROOTNODE_NAME = "databaseCrawlList";

	public static DatabaseCrawlList fromXml(
			DatabaseCrawlMaster databaseCrawlMaster, File file)
			throws XPathExpressionException, ParserConfigurationException,
			SAXException, IOException {
		DatabaseCrawlList dbCrawlList = new DatabaseCrawlList(
				databaseCrawlMaster);
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

	public void writeXml(XmlWriter xmlWriter) throws SAXException {
		writeXml(DBCRAWLLIST_ROOTNODE_NAME, xmlWriter);
	}

	@Override
	protected DatabaseCrawl[] newArray(int size) {
		return new DatabaseCrawl[size];
	}

	@Override
	protected DatabaseCrawl newItem(String name) {
		return new DatabaseCrawl(databaseCrawlMaster, name);
	}

}
