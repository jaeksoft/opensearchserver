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

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.io.FilenameUtils;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.config.Config;
import com.jaeksoft.searchlib.util.ReadWriteLock;
import com.jaeksoft.searchlib.util.XPathParser;
import com.jaeksoft.searchlib.util.XmlWriter;

public class ParserSelector {

	private ReadWriteLock rwl = new ReadWriteLock();

	private ParserFactory fileCrawlerDefaultParserFactory;
	private ParserFactory webCrawlerDefaultParserFactory;
	private Set<ParserFactory> parserFactorySet;
	private Map<String, ParserFactory> mimeTypeParserMap;
	private Map<String, ParserFactory> extensionParserMap;
	private ParserTypeEnum parserTypeEnum;

	public ParserSelector() {
		fileCrawlerDefaultParserFactory = null;
		webCrawlerDefaultParserFactory = null;
		parserTypeEnum = null;
		mimeTypeParserMap = new TreeMap<String, ParserFactory>();
		extensionParserMap = new TreeMap<String, ParserFactory>();
		parserFactorySet = new TreeSet<ParserFactory>();
	}

	public ParserSelector(Config config, XPathParser xpp, Node parentNode)
			throws XPathExpressionException, DOMException, IOException,
			SearchLibException {
		this();
		fromXmlConfig(config, xpp, parentNode);
	}

	public void setFileCrawlerDefaultParserFactory(
			ParserFactory fileCrawlerDefaultParserFactory) {
		rwl.w.lock();
		try {
			this.fileCrawlerDefaultParserFactory = fileCrawlerDefaultParserFactory;
		} finally {
			rwl.w.unlock();
		}
	}

	public Parser getFileCrawlerDefaultParser() throws InstantiationException,
			IllegalAccessException, ClassNotFoundException, SearchLibException {
		rwl.r.lock();
		try {
			if (fileCrawlerDefaultParserFactory == null)
				return null;
			return (Parser) ParserFactory
					.create(fileCrawlerDefaultParserFactory);
		} finally {
			rwl.r.unlock();
		}
	}

	public void setWebCrawlerDefaultParserFactory(
			ParserFactory webCrawlerDefaultParserFactory) {
		rwl.w.lock();
		try {
			this.webCrawlerDefaultParserFactory = webCrawlerDefaultParserFactory;
		} finally {
			rwl.w.unlock();
		}
	}

	public Parser getWebCrawlerDefaultParser() throws InstantiationException,
			IllegalAccessException, ClassNotFoundException, SearchLibException {
		rwl.r.lock();
		try {
			if (webCrawlerDefaultParserFactory == null)
				return null;
			return (Parser) ParserFactory
					.create(webCrawlerDefaultParserFactory);
		} finally {
			rwl.r.unlock();
		}
	}

	private void rebuildParserMap() {
		extensionParserMap.clear();
		mimeTypeParserMap.clear();
		for (ParserFactory parserFactory : parserFactorySet) {
			Set<String> extensionSet = parserFactory.getExtensionSet();
			if (extensionSet != null)
				for (String extension : extensionSet)
					extensionParserMap.put(extension, parserFactory);
			Set<String> mimeTypeSet = parserFactory.getMimeTypeSet();
			if (mimeTypeSet != null)
				for (String mimeType : mimeTypeSet)
					mimeTypeParserMap.put(mimeType, parserFactory);
		}
	}

	public void replaceParserFactory(ParserFactory oldParser,
			ParserFactory newParser) throws SearchLibException {
		rwl.w.lock();
		try {
			if (oldParser != null)
				parserFactorySet.remove(oldParser);
			if (newParser != null)
				if (!parserFactorySet.add(newParser))
					throw new SearchLibException("Error, parser not added");
			rebuildParserMap();
		} finally {
			rwl.w.unlock();
		}
	}

	public Set<ParserFactory> getParserFactorySet() {
		rwl.r.lock();
		try {
			return parserFactorySet;
		} finally {
			rwl.r.unlock();
		}
	}

	private Parser getParser(ParserFactory parserFactory)
			throws InstantiationException, IllegalAccessException,
			ClassNotFoundException, SearchLibException {
		rwl.r.lock();
		try {
			if (parserFactory == null)
				return null;
			return (Parser) ParserFactory.create(parserFactory);
		} finally {
			rwl.r.unlock();
		}
	}

	private Parser getParserFromExtension(String extension)
			throws InstantiationException, IllegalAccessException,
			ClassNotFoundException, MalformedURLException, SearchLibException {
		rwl.r.lock();
		try {
			ParserFactory parserFactory = null;
			if (extensionParserMap != null && extension != null)
				parserFactory = extensionParserMap.get(extension);

			return getParser(parserFactory);
		} finally {
			rwl.r.unlock();
		}
	}

	public ParserFactory checkParserFromExtension(String extension) {
		rwl.r.lock();
		try {
			if (extensionParserMap != null && extension != null)
				return extensionParserMap.get(extension);
			return null;
		} finally {
			rwl.r.unlock();
		}
	}

	private Parser getParserFromMimeType(String contentBaseType)
			throws InstantiationException, IllegalAccessException,
			ClassNotFoundException, MalformedURLException, SearchLibException {
		rwl.r.lock();
		try {
			ParserFactory parserFactory = null;
			if (mimeTypeParserMap != null)
				parserFactory = mimeTypeParserMap.get(contentBaseType);
			return getParser(parserFactory);
		} finally {
			rwl.r.unlock();
		}
	}

	public ParserFactory checkParserFromMimeType(String mimeType) {
		rwl.r.lock();
		try {
			if (mimeTypeParserMap != null && mimeType != null)
				return mimeTypeParserMap.get(mimeType);
			return null;
		} finally {
			rwl.r.unlock();
		}
	}

	public Parser getParser(String filename, String contentBaseType)
			throws MalformedURLException, InstantiationException,
			IllegalAccessException, ClassNotFoundException, SearchLibException {
		rwl.r.lock();
		try {
			Parser parser = null;
			if (contentBaseType != null)
				parser = getParserFromMimeType(contentBaseType);
			if (parser == null && filename != null)
				parser = getParserFromExtension(FilenameUtils
						.getExtension(filename));
			if (parser == null)
				return null;
			return parser;
		} finally {
			rwl.r.unlock();
		}
	}

	private void fromXmlConfig(Config config, XPathParser xpp, Node parentNode)
			throws XPathExpressionException, DOMException, IOException,
			SearchLibException {

		String fileCrawlerDefaultParserName = XPathParser.getAttributeString(
				parentNode, "fileCrawlerDefault");

		String webCrawlerDefaultParserName = XPathParser.getAttributeString(
				parentNode, "webCrawlerDefault");

		NodeList parserNodes = xpp.getNodeList(parentNode, "parser");
		for (int i = 0; i < parserNodes.getLength(); i++) {
			Node parserNode = parserNodes.item(i);
			ParserFactory parserFactory = ParserFactory.create(config, xpp,
					parserNode);

			if (parserFactory != null) {
				parserFactorySet.add(parserFactory);
				if (fileCrawlerDefaultParserName != null
						&& parserFactory.getParserName().equals(
								fileCrawlerDefaultParserName))
					setFileCrawlerDefaultParserFactory(parserFactory);
				if (webCrawlerDefaultParserName != null
						&& parserFactory.getParserName().equals(
								webCrawlerDefaultParserName))
					setWebCrawlerDefaultParserFactory(parserFactory);
				rebuildParserMap();
			}
		}
	}

	public void writeXmlConfig(XmlWriter xmlWriter) throws SAXException {
		rwl.r.lock();
		try {
			xmlWriter.startElement("parsers");
			for (ParserFactory parser : parserFactorySet)
				parser.writeXmlConfig(xmlWriter);
			xmlWriter.endElement();
		} finally {
			rwl.r.unlock();
		}
	}

	protected ParserTypeEnum getNewParserTypeEnum() {
		return new ParserTypeEnum();
	}

	final public ParserTypeEnum getParserTypeEnum() {
		synchronized (this) {
			if (parserTypeEnum != null)
				return parserTypeEnum;
			parserTypeEnum = getNewParserTypeEnum();
			return parserTypeEnum;
		}
	}

}
