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

package com.jaeksoft.searchlib.replication;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.TreeMap;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.util.ReadWriteLock;
import com.jaeksoft.searchlib.util.XPathParser;
import com.jaeksoft.searchlib.util.XmlWriter;

public class ReplicationList {

	private final ReadWriteLock rwl = new ReadWriteLock();
	private TreeMap<String, ReplicationItem> replicationMap;
	private ReplicationItem[] replicationArray;

	public ReplicationList(ReplicationMaster replicationMaster, File file)
			throws ParserConfigurationException, SAXException, IOException,
			XPathExpressionException, SearchLibException {
		replicationMap = new TreeMap<String, ReplicationItem>();
		replicationArray = null;
		if (!file.exists())
			return;
		XPathParser xpp = new XPathParser(file);
		Node parentNode = xpp.getNode("replicationList");
		if (parentNode == null)
			return;
		NodeList nodes = xpp.getNodeList(parentNode, "replicationItem");
		if (nodes == null)
			return;
		for (int i = 0; i < nodes.getLength(); i++) {
			ReplicationItem replicationItem = new ReplicationItem(
					replicationMaster, xpp, nodes.item(i));
			put(replicationItem);
		}
	}

	public ReplicationItem[] getArray() {
		rwl.r.lock();
		try {
			return replicationArray;
		} finally {
			rwl.r.unlock();
		}
	}

	public void populateNameList(List<String> list) {
		rwl.r.lock();
		try {
			for (String name : replicationMap.keySet())
				list.add(name);
		} finally {
			rwl.r.unlock();
		}
	}

	public ReplicationItem get(String replicationItemName) {
		rwl.r.lock();
		try {
			if (replicationItemName == null)
				return null;
			return replicationMap.get(replicationItemName);
		} finally {
			rwl.r.unlock();
		}
	}

	private void buildArray() {
		replicationArray = new ReplicationItem[replicationMap.size()];
		replicationMap.values().toArray(replicationArray);
	}

	public int getSize() {
		rwl.r.lock();
		try {
			if (replicationArray == null)
				return 0;
			return replicationArray.length;
		} finally {
			rwl.r.unlock();
		}
	}

	public void put(ReplicationItem item) throws SearchLibException {
		rwl.w.lock();
		try {
			if (replicationMap.containsKey(item.getName()))
				throw new SearchLibException(
						"Replication item already exists: " + item.getName());
			replicationMap.put(item.getName(), item);
			buildArray();
		} finally {
			rwl.w.unlock();
		}
	}

	public void remove(ReplicationItem selectedItem) {
		rwl.w.lock();
		try {
			replicationMap.remove(selectedItem.getName());
			buildArray();
		} finally {
			rwl.w.unlock();
		}
	}

	public void writeXml(XmlWriter xmlWriter) throws SAXException,
			UnsupportedEncodingException {
		rwl.r.lock();
		try {
			xmlWriter.startElement("replicationList");
			for (ReplicationItem item : replicationMap.values())
				item.writeXml(xmlWriter);
			xmlWriter.endElement();
		} finally {
			rwl.r.unlock();
		}
	}

}
