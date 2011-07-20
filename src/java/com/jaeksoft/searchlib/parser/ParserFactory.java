/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2008-2011 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.parser;

import java.util.Set;
import java.util.TreeSet;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.Logging;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.analysis.ClassFactory;
import com.jaeksoft.searchlib.analysis.ClassPropertyEnum;
import com.jaeksoft.searchlib.config.Config;
import com.jaeksoft.searchlib.crawler.FieldMap;
import com.jaeksoft.searchlib.util.XPathParser;
import com.jaeksoft.searchlib.util.XmlWriter;

public class ParserFactory extends ClassFactory implements
		Comparable<ParserFactory> {

	private String defaultCharset;

	private Set<String> mimeTypeList;

	private Set<String> extensionList;

	private FieldMap fieldMap;

	private Config config;

	public ParserFactory(Config config, String parserName, String className,
			long sizeLimit, FieldMap fieldMap, String defaultCharset) {
		try {
			Object[] PARSERNAME = { parserName };
			Object[] SIZELIMIT = { sizeLimit };
			addProperty(ClassPropertyEnum.PARSER_NAME, null, PARSERNAME);
			addProperty(ClassPropertyEnum.SIZE_LIMIT, null, SIZELIMIT);

			getProperty(ClassPropertyEnum.PARSER_NAME).setValue(parserName);
			getProperty(ClassPropertyEnum.SIZE_LIMIT).setValue(
					Long.toString(sizeLimit));
			getProperty(ClassPropertyEnum.CLASS).setValue(className);

			this.config = config;
			this.fieldMap = fieldMap;
			this.defaultCharset = defaultCharset == null ? "UTF-8"
					: defaultCharset;
			mimeTypeList = null;
			extensionList = null;
		} catch (Exception e) {
			Logging.logger.error(e);
		}

	}

	public Parser getNewParser() throws InstantiationException,
			IllegalAccessException, ClassNotFoundException, SearchLibException {
		Parser parser = (Parser) Class.forName(getClassName()).newInstance();
		parser.setSizeLimit(getSizeLimit());
		parser.setFieldMap(fieldMap);
		parser.setDefaultCharset(defaultCharset);
		if (config != null)
			parser.setUrlFilterList(config.getUrlFilterList().getArray());
		return parser;
	}

	public String getParserName() {
		return getProperty(ClassPropertyEnum.PARSER_NAME).getValue();
	}

	public long getSizeLimit() {
		return Long.parseLong(getProperty(ClassPropertyEnum.SIZE_LIMIT)
				.getValue());
	}

	public FieldMap getFieldMap() {
		return fieldMap;
	}

	public String getDefaultCharset() {
		return this.defaultCharset;
	}

	public void addAttributes(ClassPropertyEnum classPropertyEnum,
			String defaultValue, Object[] valueList, String attributeValue)
			throws SearchLibException {
		if (attributeValue != null && !attributeValue.equals("")) {
			addProperty(classPropertyEnum, defaultValue, valueList);
			getProperty(classPropertyEnum).setValue(attributeValue);
		}
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

	public static ParserFactory fromXmlConfig(Config config,
			ParserSelector parserSelector, XPathParser xpp, Node parserNode)
			throws XPathExpressionException {

		String parserClassName = XPathParser.getAttributeString(parserNode,
				"class");
		if (parserClassName == null)
			return null;

		String parserName = XPathParser.getAttributeString(parserNode, "name");
		if (parserName == null)
			parserName = parserClassName;

		FieldMap fieldMap = new FieldMap(xpp, xpp.getNode(parserNode, "map"));
		long sizeLimit = XPathParser.getAttributeValue(parserNode, "sizeLimit");
		String defaultCharset = XPathParser.getAttributeString(parserNode,
				"defaultCharset");
		ParserFactory parserFactory = new ParserFactory(config, parserName,
				parserClassName, sizeLimit, fieldMap, defaultCharset);

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
