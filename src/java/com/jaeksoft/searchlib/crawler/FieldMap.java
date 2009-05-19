/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2008 Emmanuel Keller / Jaeksoft
 * 
 * http://www.jaeksoft.com
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

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

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
			NodeList nodeList = xpp.getNodeList("/map/link");
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

	public void store() throws TransformerConfigurationException, SAXException,
			IOException {
		synchronized (this) {
			if (!mapFile.exists())
				mapFile.createNewFile();
			PrintWriter pw = new PrintWriter(mapFile);
			try {
				XmlWriter xmlWriter = new XmlWriter(pw, "UTF-8");
				AttributesImpl atts = new AttributesImpl();
				TransformerHandler hd = xmlWriter.getTransformerHandler();
				hd.startElement("", "", "map", atts);
				for (GenericLink<String> link : getList()) {
					atts.clear();
					atts.addAttribute("", "", "source", "CDATA", link
							.getSource());
					atts.addAttribute("", "", "target", "CDATA", link
							.getTarget());
					hd.startElement("", "", "link", atts);
					hd.endElement("", "", "link");
				}
				hd.endElement("", "", "map");
				xmlWriter.endDocument();
			} finally {
				pw.close();
			}
		}
	}

}
