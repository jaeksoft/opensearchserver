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

package com.jaeksoft.searchlib.crawler;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.util.DomUtils;
import com.jaeksoft.searchlib.util.XPathParser;
import com.jaeksoft.searchlib.util.XmlWriter;
import com.jaeksoft.searchlib.util.map.GenericLink;
import com.jaeksoft.searchlib.util.map.GenericMap;
import com.jaeksoft.searchlib.util.map.Target;

public abstract class FieldMapGeneric<T extends Target> extends
		GenericMap<String, T> {

	private File mapFile;

	protected FieldMapGeneric() {
		mapFile = null;
	}

	protected FieldMapGeneric(XPathParser xpp, Node parentNode)
			throws XPathExpressionException {
		mapFile = null;
		load(xpp, parentNode);
	}

	protected FieldMapGeneric(File mapFile, String rootXPath)
			throws ParserConfigurationException, SAXException, IOException,
			XPathExpressionException {
		this.mapFile = mapFile;
		if (!mapFile.exists())
			return;
		XPathParser xpp = new XPathParser(mapFile);
		load(xpp, xpp.getNode(rootXPath));
	}

	protected abstract T loadTarget(String targetName, Node node);

	public void load(XPathParser xpp, Node parentNode)
			throws XPathExpressionException {
		synchronized (this) {
			if (parentNode == null)
				return;
			NodeList nodeList = xpp.getNodeList(parentNode, "link");
			int l = nodeList.getLength();
			for (int i = 0; i < l; i++) {
				Node node = nodeList.item(i);
				String source = DomUtils.getAttributeText(node, "source");
				if (source == null)
					continue;
				String targetName = DomUtils.getAttributeText(node, "target");
				T target = loadTarget(targetName, node);
				if (target == null)
					continue;
				add(source, target);
			}
		}
	}

	protected abstract void writeTarget(XmlWriter xmlWriter, T target)
			throws SAXException;

	public void store(XmlWriter xmlWriter) throws SAXException {
		for (GenericLink<String, T> link : getList()) {
			xmlWriter.startElement("link", "source", link.getSource(),
					"target", link.getTarget().getName());
			writeTarget(xmlWriter, link.getTarget());
			xmlWriter.endElement();
		}
	}

	public void store() throws TransformerConfigurationException, SAXException,
			IOException {
		synchronized (this) {
			if (!mapFile.exists())
				mapFile.createNewFile();
			PrintWriter pw = new PrintWriter(mapFile);
			try {
				XmlWriter xmlWriter = new XmlWriter(pw, "UTF-8");
				xmlWriter.startElement("map");
				store(xmlWriter);
				xmlWriter.endElement();
				xmlWriter.endDocument();
			} finally {
				pw.close();
			}
		}
	}
}
