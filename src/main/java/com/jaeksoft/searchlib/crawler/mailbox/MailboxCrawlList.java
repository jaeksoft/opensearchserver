/**
 * License Agreement for OpenSearchServer
 * <p>
 * Copyright (C) 2013-2016 Emmanuel Keller / Jaeksoft
 * <p>
 * http://www.open-search-server.com
 * <p>
 * This file is part of OpenSearchServer.
 * <p>
 * OpenSearchServer is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * OpenSearchServer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with OpenSearchServer.
 * If not, see <http://www.gnu.org/licenses/>.
 **/

package com.jaeksoft.searchlib.crawler.mailbox;

import com.jaeksoft.searchlib.util.ReadWriteLock;
import com.jaeksoft.searchlib.util.XPathParser;
import com.jaeksoft.searchlib.util.XmlWriter;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.TreeMap;

public class MailboxCrawlList implements XmlWriter.Interface {

	final private ReadWriteLock rwl = new ReadWriteLock();

	private TreeMap<String, MailboxCrawlItem> map;
	private MailboxCrawlItem[] array;

	private MailboxCrawlList() {
		map = new TreeMap<>();
	}

	private final static String MAILBOX_CRAWLLIST_ROOTNODE_NAME = "mailboxCrawlList";
	private final static String MAILBOX_CRAWL_NODE_NAME = "mailboxCrawlItem";

	public static MailboxCrawlList fromXml(MailboxCrawlMaster crawlMaster, File file)
			throws XPathExpressionException, ParserConfigurationException, SAXException, IOException {
		MailboxCrawlList crawlList = new MailboxCrawlList();
		if (!file.exists())
			return crawlList;
		XPathParser xpp = new XPathParser(file);
		Node rootNode = xpp.getNode(MAILBOX_CRAWLLIST_ROOTNODE_NAME);
		if (rootNode == null)
			return crawlList;
		NodeList nodes = xpp.getNodeList(rootNode, MAILBOX_CRAWL_NODE_NAME);
		if (nodes == null)
			return crawlList;
		for (int i = 0; i < nodes.getLength(); i++) {
			MailboxCrawlItem crawlItem = MailboxCrawlItem.fromXml(crawlMaster, xpp, nodes.item(i));
			if (crawlItem != null)
				crawlList.add(crawlItem);
		}
		return crawlList;
	}

	public void add(MailboxCrawlItem crawlItem) {
		rwl.w.lock();
		try {
			map.put(crawlItem.getName(), crawlItem);
			buildArray();
		} finally {
			rwl.w.unlock();
		}
	}

	public MailboxCrawlItem get(String name) {
		rwl.r.lock();
		try {
			return map.get(name);
		} finally {
			rwl.r.unlock();
		}
	}

	public void remove(MailboxCrawlItem crawlItem) {
		rwl.w.lock();
		try {
			map.remove(crawlItem.getName());
			buildArray();
		} finally {
			rwl.w.unlock();
		}
	}

	@Override
	public void writeXml(XmlWriter xmlWriter) throws SAXException, UnsupportedEncodingException {
		rwl.r.lock();
		try {
			xmlWriter.startElement(MAILBOX_CRAWLLIST_ROOTNODE_NAME);
			for (MailboxCrawlItem item : map.values())
				item.writeXml(MAILBOX_CRAWL_NODE_NAME, xmlWriter);
			xmlWriter.endElement();
		} finally {
			rwl.r.unlock();
		}
	}

	private void buildArray() {
		array = new MailboxCrawlItem[map.size()];
		map.values().toArray(array);
	}

	public MailboxCrawlItem[] getArray() {
		rwl.r.lock();
		try {
			return array;
		} finally {
			rwl.r.unlock();
		}
	}

}
