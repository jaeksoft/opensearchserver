/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2008 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.crawler;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.index.FieldContent;
import com.jaeksoft.searchlib.index.IndexDocument;
import com.jaeksoft.searchlib.util.DomUtils;
import com.jaeksoft.searchlib.util.GenericLink;
import com.jaeksoft.searchlib.util.GenericMap;
import com.jaeksoft.searchlib.util.XPathParser;
import com.jaeksoft.searchlib.util.XmlWriter;

public class FieldMap extends GenericMap<String> {

	private File mapFile;

	public FieldMap(File mapFile) throws ParserConfigurationException,
			SAXException, IOException, XPathExpressionException {
		synchronized (this) {
			this.mapFile = mapFile;
			if (!mapFile.exists())
				return;
			XPathParser xpp = new XPathParser(mapFile);
			load(xpp, xpp.getNode("/map"));
		}
	}

	public FieldMap(XPathParser xpp, Node parentNode)
			throws XPathExpressionException {
		load(xpp, parentNode);
	}

	private void load(XPathParser xpp, Node parentNode)
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
				String target = DomUtils.getAttributeText(node, "target");
				if (target == null)
					continue;
				add(source, target);
			}
		}
	}

	public void store(XmlWriter xmlWriter) throws SAXException {
		for (GenericLink<String> link : getList()) {
			xmlWriter.startElement("link", "source", link.getSource(),
					"target", link.getTarget());
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

	public void mapIndexDocument(IndexDocument source, IndexDocument target) {
		for (GenericLink<String> link : getList()) {
			FieldContent fc = source.getField(link.getSource());
			String targetField = link.getTarget();
			if (fc != null) {
				List<String> values = fc.getValues();
				if (values != null)
					for (String value : values)
						target.add(targetField, value);
			}
		}
	}

}
