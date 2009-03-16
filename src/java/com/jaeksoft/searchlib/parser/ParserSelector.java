/**   
 * License Agreement for Jaeksoft SearchLib Community
 *
 * Copyright (C) 2008-2009 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.parser;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.jaeksoft.searchlib.util.XPathParser;

public class ParserSelector {

	private ParserFactory defaultParser;
	private Map<String, ParserFactory> mimeParserList;
	private Map<String, ParserFactory> extensionParserList;

	private ParserSelector() {
		mimeParserList = new TreeMap<String, ParserFactory>();
		extensionParserList = new TreeMap<String, ParserFactory>();
	}

	public ParserSelector(ParserFactory defaultParser) {
		this();
		setDefaultParser(defaultParser);
	}

	public void setDefaultParser(ParserFactory defaultParser) {
		this.defaultParser = defaultParser;
	}

	public Set<Map.Entry<String, ParserFactory>> getMimeParserSet() {
		return mimeParserList.entrySet();
	}

	public Set<Map.Entry<String, ParserFactory>> getExtensionParserSet() {
		return extensionParserList.entrySet();
	}

	protected void addMimeParser(String contentType, ParserFactory parserFactory) {
		mimeParserList.put(contentType, parserFactory);
	}

	protected void addExtensionParser(String extension,
			ParserFactory parserFactory) {
		extensionParserList.put(extension, parserFactory);
	}

	private Parser getParser(ParserFactory parserFactory)
			throws InstantiationException, IllegalAccessException,
			ClassNotFoundException {
		if (parserFactory == null)
			parserFactory = defaultParser;
		if (parserFactory == null)
			return null;
		return parserFactory.getNewParser();
	}

	public Parser getParserFromExtension(String extension)
			throws InstantiationException, IllegalAccessException,
			ClassNotFoundException, MalformedURLException {
		ParserFactory parserFactory = null;
		if (extensionParserList != null)
			parserFactory = extensionParserList.get(extension);
		return getParser(parserFactory);
	}

	public Parser getParserFromMimeType(String contentBaseType)
			throws InstantiationException, IllegalAccessException,
			ClassNotFoundException, MalformedURLException {
		ParserFactory parserFactory = null;
		if (mimeParserList != null)
			parserFactory = mimeParserList.get(contentBaseType);
		return getParser(parserFactory);
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
			long sizeLimit = XPathParser.getAttributeValue(parserNode,
					"sizeLimit");
			if (parserClassName != null) {
				ParserFactory parserFactory = new ParserFactory(
						parserClassName, sizeLimit);
				NodeList mimeNodes = xpp.getNodeList(parserNode, "contentType");
				for (int j = 0; j < mimeNodes.getLength(); j++) {
					Node mimeNode = mimeNodes.item(j);
					String contentType = xpp.getNodeString(mimeNode);
					selector.addMimeParser(contentType, parserFactory);
				}
				NodeList extensionNodes = xpp.getNodeList(parserNode,
						"extension");
				for (int j = 0; j < extensionNodes.getLength(); j++) {
					Node extensionNode = extensionNodes.item(j);
					String extension = xpp.getNodeString(extensionNode);
					selector.addExtensionParser(extension, parserFactory);
				}

			}
		}
		return selector;
	}
}
