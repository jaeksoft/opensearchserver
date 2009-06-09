/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2008-2009 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.parser;

import java.util.Set;
import java.util.TreeSet;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.crawler.FieldMap;
import com.jaeksoft.searchlib.util.XPathParser;
import com.jaeksoft.searchlib.util.XmlWriter;

public class ParserFactory implements Comparable<ParserFactory> {

	private String parserName;
	private String className;
	private long sizeLimit;

	private Set<String> mimeTypeList;

	private Set<String> extensionList;

	private FieldMap fieldMap;

	public ParserFactory(String parserName, String className, long sizeLimit,
			FieldMap fieldMap) {
		this.parserName = parserName;
		this.className = className;
		if (this.className.indexOf('.') == -1)
			this.className = "com.jaeksoft.searchlib.parser." + className;
		this.sizeLimit = sizeLimit;
		this.fieldMap = fieldMap;
		mimeTypeList = null;
		extensionList = null;
	}

	public Parser getNewParser() throws InstantiationException,
			IllegalAccessException, ClassNotFoundException {
		Parser parser = (Parser) Class.forName(className).newInstance();
		parser.setSizeLimit(sizeLimit);
		parser.setFieldMap(fieldMap);
		return parser;
	}

	public String getParserName() {
		return parserName;
	}

	public long getSizeLimit() {
		return sizeLimit;
	}

	public FieldMap getFieldMap() {
		return fieldMap;
	}

	public void addExtension(String extension) {
		synchronized (this) {
			if (extensionList == null)
				extensionList = new TreeSet<String>();
			extensionList.add(extension);
		}
	}

	public void addMimeType(String mimeType) {
		synchronized (this) {
			if (mimeTypeList == null)
				mimeTypeList = new TreeSet<String>();
			mimeTypeList.add(mimeType);
		}
	}

	public static ParserFactory fromXmlConfig(ParserSelector parserSelector,
			XPathParser xpp, Node parserNode) throws XPathExpressionException {
		String parserClassName = XPathParser.getAttributeString(parserNode,
				"class");
		if (parserClassName == null)
			return null;
		String parserName = XPathParser.getAttributeString(parserNode, "name");
		if (parserName == null)
			parserName = parserClassName;
		FieldMap fieldMap = new FieldMap(xpp, xpp.getNode(parserNode, "map"));
		long sizeLimit = XPathParser.getAttributeValue(parserNode, "sizeLimit");
		ParserFactory parserFactory = new ParserFactory(parserName,
				parserClassName, sizeLimit, fieldMap);
		NodeList mimeNodes = xpp.getNodeList(parserNode, "contentType");
		for (int j = 0; j < mimeNodes.getLength(); j++) {
			Node mimeNode = mimeNodes.item(j);
			String contentType = xpp.getNodeString(mimeNode);
			parserFactory.addMimeType(contentType);
		}
		NodeList extensionNodes = xpp.getNodeList(parserNode, "extension");
		for (int j = 0; j < extensionNodes.getLength(); j++) {
			Node extensionNode = extensionNodes.item(j);
			String extension = xpp.getNodeString(extensionNode);
			parserFactory.addExtension(extension);
		}
		return parserFactory;
	}

	public Set<String> getExtensionSet() {
		return extensionList;
	}

	public Set<String> getMimeTypeSet() {
		return mimeTypeList;
	}

	@Override
	public int compareTo(ParserFactory parserFactory) {
		return className.compareTo(parserFactory.className);
	}

	public void writeXmlConfig(XmlWriter xmlWriter) throws SAXException {
		xmlWriter.startElement("parser", "name", parserName, "class",
				className, "sizeLimit", Long.toString(sizeLimit));
		if (mimeTypeList != null) {
			for (String mimeType : mimeTypeList) {
				xmlWriter.startElement("contentType");
				xmlWriter.textNode(mimeType);
				xmlWriter.endElement();
			}
		}
		if (extensionList != null) {
			for (String extension : extensionList) {
				xmlWriter.startElement("extension");
				xmlWriter.textNode(extension);
				xmlWriter.endElement();
			}
		}
		if (fieldMap != null) {
			xmlWriter.startElement("map");
			fieldMap.store(xmlWriter);
			xmlWriter.endElement();
		}
		xmlWriter.endElement();

	}
}
