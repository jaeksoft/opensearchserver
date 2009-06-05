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

import com.jaeksoft.searchlib.crawler.FieldMap;
import com.jaeksoft.searchlib.util.XPathParser;

public class ParserFactory implements Comparable<ParserFactory> {

	private String className;
	private long sizeLimit;

	private Set<String> mimeTypeList;

	private Set<String> extensionList;

	private FieldMap fieldMap;

	public ParserFactory(String className, long sizeLimit, FieldMap fieldMap) {
		this.className = className;
		this.sizeLimit = sizeLimit;
		this.fieldMap = fieldMap;
		mimeTypeList = null;
		extensionList = null;
	}

	public Parser getNewParser() throws InstantiationException,
			IllegalAccessException, ClassNotFoundException {
		Parser parser = (Parser) Class.forName(className).newInstance();
		parser.setSizeLimit(sizeLimit);
		return parser;
	}

	public String getClassName() {
		return className;
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
		FieldMap fieldMap = new FieldMap(xpp, xpp.getNode("map"));
		long sizeLimit = XPathParser.getAttributeValue(parserNode, "sizeLimit");
		ParserFactory parserFactory = new ParserFactory(parserClassName,
				sizeLimit, fieldMap);
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

}
