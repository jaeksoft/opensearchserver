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
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.util.XPathParser;
import com.jaeksoft.searchlib.util.XmlWriter;

public class ReplicationList {

	private TreeSet<ReplicationItem> replicationSet;

	public ReplicationList(ReplicationMaster replicationMaster, File file)
			throws ParserConfigurationException, SAXException, IOException,
			XPathExpressionException {
		replicationSet = new TreeSet<ReplicationItem>();
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

	public Set<ReplicationItem> getSet() {
		synchronized (replicationSet) {
			return replicationSet;
		}
	}

	public List<String> getNameList() {
		synchronized (replicationSet) {
			List<String> list = new ArrayList<String>();
			for (ReplicationItem item : replicationSet)
				list.add(item.getName());
			return list;
		}
	}

	public ReplicationItem get(String replicationItemName) {
		synchronized (replicationSet) {
			if (replicationItemName == null)
				return null;
			ReplicationItem item = new ReplicationItem();
			item.setName(replicationItemName);
			ReplicationItem found = replicationSet.ceiling(item);
			if (found.compareTo(item) != 0)
				return null;
			return found;
		}
	}

	public void put(ReplicationItem item) {
		synchronized (replicationSet) {
			replicationSet.add(item);
		}
	}

	public void remove(ReplicationItem selectedItem) {
		synchronized (replicationSet) {
			replicationSet.remove(selectedItem);
		}
	}

	public void writeXml(XmlWriter xmlWriter) throws SAXException,
			UnsupportedEncodingException {
		synchronized (replicationSet) {
			xmlWriter.startElement("replicationList");
			for (ReplicationItem item : replicationSet)
				item.writeXml(xmlWriter);
			xmlWriter.endElement();
		}
	}

}
