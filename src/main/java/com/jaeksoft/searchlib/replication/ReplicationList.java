/**
 * License Agreement for OpenSearchServer
 * <p>
 * Copyright (C) 2010-2016 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.replication;

import com.jaeksoft.searchlib.SearchLibException;
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
import java.net.URISyntaxException;
import java.util.List;
import java.util.TreeMap;

public class ReplicationList implements XmlWriter.Interface {

	private final ReadWriteLock rwl = new ReadWriteLock();
	private TreeMap<String, ReplicationItem> replicationMap;
	private ReplicationItem[] replicationArray;

	public ReplicationList(ReplicationMaster replicationMaster, File file)
			throws ParserConfigurationException, SAXException, IOException, XPathExpressionException,
			SearchLibException, URISyntaxException {
		replicationMap = new TreeMap<>();
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
			ReplicationItem replicationItem = new ReplicationItem(replicationMaster, nodes.item(i));
			save(null, replicationItem);
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

	public void save(ReplicationItem oldItem, ReplicationItem newItem) throws SearchLibException {
		rwl.w.lock();
		try {
			if (oldItem != null)
				replicationMap.remove(oldItem.getName());
			if (newItem != null) {
				if (replicationMap.containsKey(newItem.getName()))
					throw new SearchLibException("Replication item already exists: " + newItem.getName());
				replicationMap.put(newItem.getName(), newItem);
			}
			buildArray();
		} finally {
			rwl.w.unlock();
		}
	}

	@Override
	public void writeXml(XmlWriter xmlWriter) throws SAXException, UnsupportedEncodingException {
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
