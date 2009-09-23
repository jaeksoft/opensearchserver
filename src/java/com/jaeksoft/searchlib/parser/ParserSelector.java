/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2008-2009 Emmanuel Keller / Jaeksoft
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

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.util.XPathParser;
import com.jaeksoft.searchlib.util.XmlWriter;

public class ParserSelector {

	private ParserFactory fileCrawlerDefaultParserFactory;
	private ParserFactory webCrawlerDefaultParserFactory;
	private Set<ParserFactory> parserFactorySet;
	private Map<String, ParserFactory> mimeTypeParserMap;
	private Map<String, ParserFactory> extensionParserMap;

	public ParserSelector() {
		fileCrawlerDefaultParserFactory = null;
		webCrawlerDefaultParserFactory = null;
		mimeTypeParserMap = new TreeMap<String, ParserFactory>();
		extensionParserMap = new TreeMap<String, ParserFactory>();
		parserFactorySet = new TreeSet<ParserFactory>();
	}

	public void setFileCrawlerDefaultParserFactory(
			ParserFactory fileCrawlerDefaultParserFactory) {
		this.fileCrawlerDefaultParserFactory = fileCrawlerDefaultParserFactory;
	}

	public Parser getFileCrawlerDefaultParser() throws InstantiationException,
			IllegalAccessException, ClassNotFoundException {
		return fileCrawlerDefaultParserFactory.getNewParser();
	}

	public void setWebCrawlerDefaultParserFactory(
			ParserFactory webCrawlerDefaultParserFactory) {
		this.webCrawlerDefaultParserFactory = webCrawlerDefaultParserFactory;
	}

	public Parser getWebCrawlerDefaultParser() throws InstantiationException,
			IllegalAccessException, ClassNotFoundException {
		return webCrawlerDefaultParserFactory.getNewParser();
	}

	private void addParserFactory(ParserFactory parserFactory) {
		Set<String> extensionSet = parserFactory.getExtensionSet();
		if (extensionSet != null)
			for (String extension : extensionSet)
				extensionParserMap.put(extension, parserFactory);

		Set<String> mimeTypeSet = parserFactory.getMimeTypeSet();
		if (mimeTypeSet != null)
			for (String mimeType : mimeTypeSet)
				mimeTypeParserMap.put(mimeType, parserFactory);

		parserFactorySet.add(parserFactory);
	}

	public Set<ParserFactory> getParserFactorySet() {
		return parserFactorySet;
	}

	private Parser getParser(ParserFactory parserFactory)
			throws InstantiationException, IllegalAccessException,
			ClassNotFoundException {
		if (parserFactory == null)
			return null;
		return parserFactory.getNewParser();
	}

	public Parser getParserFromExtension(String extension)
			throws InstantiationException, IllegalAccessException,
			ClassNotFoundException, MalformedURLException {
		ParserFactory parserFactory = null;
		if (extensionParserMap != null && extension != null)
			parserFactory = extensionParserMap.get(extension);

		return getParser(parserFactory);
	}

	public Parser getParserFromMimeType(String contentBaseType)
			throws InstantiationException, IllegalAccessException,
			ClassNotFoundException, MalformedURLException {
		ParserFactory parserFactory = null;
		if (mimeTypeParserMap != null)
			parserFactory = mimeTypeParserMap.get(contentBaseType);
		return getParser(parserFactory);
	}

	public static ParserSelector fromXmlConfig(XPathParser xpp, Node parentNode)
			throws XPathExpressionException, DOMException, IOException {
		ParserSelector selector = new ParserSelector();
		if (parentNode == null)
			return selector;

		String fileCrawlerDefaultParserName = XPathParser.getAttributeString(
				parentNode, "fileCrawlerDefault");

		String webCrawlerDefaultParserName = XPathParser.getAttributeString(
				parentNode, "webCrawlerDefault");

		NodeList parserNodes = xpp.getNodeList(parentNode, "parser");
		for (int i = 0; i < parserNodes.getLength(); i++) {
			Node parserNode = parserNodes.item(i);
			ParserFactory parserFactory = ParserFactory.fromXmlConfig(selector,
					xpp, parserNode);

			if (parserFactory != null) {
				selector.addParserFactory(parserFactory);
				if (fileCrawlerDefaultParserName != null
						&& parserFactory.getParserName().equals(
								fileCrawlerDefaultParserName))
					selector.setFileCrawlerDefaultParserFactory(parserFactory);
				if (webCrawlerDefaultParserName != null
						&& parserFactory.getParserName().equals(
								webCrawlerDefaultParserName))
					selector.setWebCrawlerDefaultParserFactory(parserFactory);
			}

		}

		return selector;
	}

	public void writeXmlConfig(XmlWriter xmlWriter) throws SAXException {
		xmlWriter.startElement("parsers");
		for (ParserFactory parser : parserFactorySet)
			parser.writeXmlConfig(xmlWriter);
		xmlWriter.endElement();
	}

}
