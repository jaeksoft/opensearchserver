/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.plugin;

import java.util.Map.Entry;
import java.util.Properties;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.util.XPathParser;
import com.jaeksoft.searchlib.util.XmlWriter;

public class IndexPluginItem {

	private String className;
	private Properties properties;

	public IndexPluginItem(XPathParser xpp, Node parentNode)
			throws XPathExpressionException {
		className = XPathParser.getAttributeString(parentNode, "class");
		NodeList nodes = xpp.getNodeList(parentNode, "param");
		properties = new Properties();
		for (int i = 0; i < nodes.getLength(); i++) {
			Node node = nodes.item(i);
			String name = XPathParser.getAttributeString(node, "name");
			String value = xpp.getNodeString(node, false);
			properties.put(name, value);
		}
	}

	public void writeXmlConfig(XmlWriter writer) throws SAXException {
		writer.startElement("indexPlugin", "class", className);
		for (Entry<Object, Object> entry : properties.entrySet()) {
			writer.startElement("param", "name", entry.getKey().toString());
			writer.textNode(entry.getValue());
			writer.endElement();
		}
		writer.endElement();
	}

	public IndexPluginInterface newInstance() throws InstantiationException,
			IllegalAccessException, ClassNotFoundException {
		IndexPluginInterface indexPlugin = (IndexPluginInterface) Class
				.forName(className).newInstance();
		indexPlugin.setProperties((Properties) properties.clone());
		return indexPlugin;
	}

}
