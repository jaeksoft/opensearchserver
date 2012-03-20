/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008-2012 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.parser;

import java.util.Set;
import java.util.TreeSet;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.analysis.ClassFactory;
import com.jaeksoft.searchlib.analysis.ClassProperty;
import com.jaeksoft.searchlib.analysis.ClassPropertyEnum;
import com.jaeksoft.searchlib.config.Config;
import com.jaeksoft.searchlib.crawler.web.database.UrlFilterItem;
import com.jaeksoft.searchlib.util.XPathParser;
import com.jaeksoft.searchlib.util.XmlWriter;

public class ParserFactory extends ClassFactory implements
		Comparable<ParserFactory> {

	final private static String PARSER_PACKAGE = "com.jaeksoft.searchlib.parser";

	private Set<String> mimeTypeList;

	private Set<String> extensionList;

	private ParserFieldMap fieldMap;

	private UrlFilterItem[] urlFilterList;

	private ParserFieldEnum[] fieldList;

	private ParserType parserType;

	protected ParserFactory(ParserFieldEnum[] fieldList) {
		this.fieldList = fieldList;
		this.parserType = null;
		this.fieldMap = null;
		urlFilterList = null;
		mimeTypeList = null;
		extensionList = null;
	}

	@Override
	protected void initProperties() throws SearchLibException {
		addProperty(ClassPropertyEnum.PARSER_NAME, "", null);
	}

	public ParserFieldEnum[] getFieldList() {
		return fieldList;
	}

	public String getParserName() {
		return getProperty(ClassPropertyEnum.PARSER_NAME).getValue();
	}

	public ParserType getParserType() throws SearchLibException {
		if (parserType != null)
			return parserType;
		if (config == null)
			return null;
		parserType = config.getParserSelector().getParserTypeEnum()
				.find(this.getClass());
		return parserType;
	}

	public void setParserName(String parserName) throws SearchLibException {
		getProperty(ClassPropertyEnum.PARSER_NAME).setValue(parserName);
	}

	protected int getSizeLimit() {
		ClassProperty prop = getProperty(ClassPropertyEnum.SIZE_LIMIT);
		if (prop == null)
			return 0;
		return Integer.parseInt(prop.getValue());
	}

	public ParserFieldMap getFieldMap() {
		if (fieldMap == null)
			fieldMap = new ParserFieldMap();
		return fieldMap;
	}

	public void addExtension(String extension) {
		synchronized (this) {
			if (extensionList == null)
				extensionList = new TreeSet<String>();
			extensionList.add(extension);
		}
	}

	public void removeExtension(String extension) {
		synchronized (this) {
			if (extensionList != null)
				extensionList.remove(extension);
		}
	}

	public void addMimeType(String mimeType) {
		synchronized (this) {
			if (mimeTypeList == null)
				mimeTypeList = new TreeSet<String>();
			mimeTypeList.add(mimeType);
		}
	}

	public void removeMimeType(String mimeType) {
		synchronized (this) {
			if (mimeTypeList != null)
				mimeTypeList.remove(mimeType);
		}
	}

	/**
	 * Create a new ParserFactory by reading the attributes of an XML node
	 * 
	 * @param config
	 * @param node
	 * @return a ParserFactory
	 * @throws SearchLibException
	 * @throws XPathExpressionException
	 */
	public static ParserFactory create(Config config, XPathParser xpp,
			Node parserNode) throws SearchLibException,
			XPathExpressionException {
		ParserFactory parserFactory = (ParserFactory) ClassFactory.create(
				config, PARSER_PACKAGE, parserNode);

		parserFactory.fieldMap = new ParserFieldMap(xpp, xpp.getNode(
				parserNode, "map"));

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

	public static ParserFactory create(Config config, String parserName,
			String className) throws SearchLibException {
		ParserFactory parserFactory = (ParserFactory) ClassFactory.create(null,
				PARSER_PACKAGE, className);
		parserFactory.config = config;
		parserFactory.setParserName(parserName);
		return parserFactory;
	}

	/**
	 * Clone a Parser
	 * 
	 * @param filter
	 * @return a FilterFactory
	 * @throws SearchLibException
	 */
	public static ParserFactory create(ParserFactory parser)
			throws SearchLibException {
		ParserFactory newParser = (ParserFactory) ClassFactory.create(parser);
		newParser.fieldMap = new ParserFieldMap();
		if (parser.fieldMap != null)
			parser.fieldMap.copyTo(newParser.fieldMap);
		if (parser.config != null)
			newParser.setUrlFilterList(parser.config.getUrlFilterList()
					.getArray());
		if (parser.extensionList != null)
			newParser.extensionList = new TreeSet<String>(parser.extensionList);
		if (parser.mimeTypeList != null)
			newParser.mimeTypeList = new TreeSet<String>(parser.mimeTypeList);
		return newParser;
	}

	public Set<String> getExtensionSet() {
		return extensionList;
	}

	public Set<String> getMimeTypeSet() {
		return mimeTypeList;
	}

	/**
	 * @param urlFilterList
	 *            the urlFilterList to set
	 */
	public void setUrlFilterList(UrlFilterItem[] urlFilterList) {
		this.urlFilterList = urlFilterList;
	}

	/**
	 * @return the urlFilterList
	 */
	public UrlFilterItem[] getUrlFilterList() {
		return urlFilterList;
	}

	@Override
	public int compareTo(ParserFactory parserFactory) {
		int c;
		if ((c = getParserName().compareTo(parserFactory.getParserName())) != 0)
			return c;
		return getClassName().compareTo(parserFactory.getClassName());
	}

	public void writeXmlConfig(XmlWriter xmlWriter) throws SAXException {

		xmlWriter.startElement("parser", getAttributes());

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
