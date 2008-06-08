/**   
 * License Agreement for Jaeksoft SearchLib Community
 *
 * Copyright (C) 2008 Emmanuel Keller / Jaeksoft
 * 
 * http://www.jaeksoft.com
 * 
 * This file is part of Jaeksoft SearchLib Community.
 *
 * Jaeksoft SearchLib Community is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * Jaeksoft SearchLib Community is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Jaeksoft SearchLib Community. 
 *  If not, see <http://www.gnu.org/licenses/>.
 **/

package com.jaeksoft.searchlib.crawler.spider;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.jaeksoft.searchlib.util.XPathParser;

public class ParserSelector {

	private String defaultParser;
	private HashMap<String, String> mimeParserList;
	private HashMap<String, String> extensionParserList;

	private ParserSelector() {
		mimeParserList = new HashMap<String, String>();
	}

	public ParserSelector(String defaultParser) {
		this();
		setDefaultParser(defaultParser);
	}

	public void setDefaultParser(String defaultParser) {
		this.defaultParser = defaultParser;
	}

	protected void addMimeParser(String contentType, String parserClassName) {
		mimeParserList.put(contentType, parserClassName);
	}

	protected void addExtensionParser(String extension, String parserClassName) {
		extensionParserList.put(extension, parserClassName);
	}

	protected Parser getParser(String contentType, URL url)
			throws InstantiationException, IllegalAccessException,
			ClassNotFoundException {
		String parserClassName = null;
		if (mimeParserList != null)
			parserClassName = mimeParserList.get(contentType);
		if (extensionParserList != null)
			if (parserClassName == null)
				parserClassName = extensionParserList.get(url.getPath());
		if (parserClassName == null)
			parserClassName = defaultParser;
		if (parserClassName == null)
			return null;
		return (Parser) Class.forName(parserClassName).newInstance();
	}

	public static ParserSelector fromXmlConfig(XPathParser xpp, Node parentNode)
			throws XPathExpressionException, DOMException, IOException {
		ParserSelector selector = new ParserSelector();
		if (parentNode == null)
			return selector;
		NodeList parserNodes = xpp.getNodeList(parentNode, "parser");
		for (int i = 0; i < parserNodes.getLength(); i++) {
			Node parserNode = parserNodes.item(i);
			String parserClassName = XPathParser.getAttributeString(parserNode,
					"class");
			if (parserClassName != null) {
				NodeList mimeNodes = xpp.getNodeList(parserNode, "contentType");
				for (int j = 0; j < mimeNodes.getLength(); j++) {
					Node mimeNode = mimeNodes.item(j);
					String contentType = xpp.getNodeString(mimeNode);
					selector.addMimeParser(contentType, parserClassName);
				}
			}
		}
		return selector;
	}
}
