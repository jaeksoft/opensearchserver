/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008-2013 Emmanuel Keller / Jaeksoft
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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.analysis.LanguageEnum;
import com.jaeksoft.searchlib.config.Config;
import com.jaeksoft.searchlib.crawler.file.process.FileInstanceAbstract;
import com.jaeksoft.searchlib.index.IndexDocument;
import com.jaeksoft.searchlib.streamlimiter.StreamLimiter;
import com.jaeksoft.searchlib.streamlimiter.StreamLimiterBase64;
import com.jaeksoft.searchlib.streamlimiter.StreamLimiterFile;
import com.jaeksoft.searchlib.streamlimiter.StreamLimiterFileInstance;
import com.jaeksoft.searchlib.streamlimiter.StreamLimiterInputStream;
import com.jaeksoft.searchlib.util.ReadWriteLock;
import com.jaeksoft.searchlib.util.XPathParser;
import com.jaeksoft.searchlib.util.XmlWriter;

public class ParserSelector {

	private ReadWriteLock rwl = new ReadWriteLock();

	private final Config config;

	private String fileCrawlerDefaultParserName;
	private String webCrawlerDefaultParserName;
	private ParserFactory fileCrawlerDefaultParserFactory;
	private ParserFactory webCrawlerDefaultParserFactory;
	private Map<String, ParserFactory> parserFactoryMap;
	private ParserFactory[] parserFactoryArray;
	private Map<String, Set<ParserFactory>> mimeTypeParserMap;
	private Map<String, ParserFactory> extensionParserMap;
	private ParserTypeEnum parserTypeEnum;

	public ParserSelector(Config config) {
		this.config = config;
		fileCrawlerDefaultParserName = null;
		webCrawlerDefaultParserName = null;
		fileCrawlerDefaultParserFactory = null;
		webCrawlerDefaultParserFactory = null;
		parserTypeEnum = null;
		mimeTypeParserMap = new TreeMap<String, Set<ParserFactory>>();
		extensionParserMap = new TreeMap<String, ParserFactory>();
		parserFactoryMap = new TreeMap<String, ParserFactory>();
		parserFactoryArray = null;
	}

	/**
	 * This constructor build a parser selector and add the parser factory
	 * passed as parameter. The parser factory become also the default web and
	 * file parser.
	 * 
	 * @param parserFactory
	 * @throws SearchLibException
	 */
	public ParserSelector(Config config, ParserFactory parserFactory)
			throws SearchLibException {
		this(config);
		String parserName = parserFactory.getParserName();
		parserFactoryMap.put(parserName, parserFactory);
		setFileCrawlerDefaultParserName(parserName);
		setWebCrawlerDefaultParserName(parserName);
		rebuildParserMap();
	}

	public ParserSelector(Config config, XPathParser xpp, Node parentNode)
			throws XPathExpressionException, DOMException, IOException,
			SearchLibException {
		this(config);
		fromXmlConfig(config, xpp, parentNode);
	}

	public void setFileCrawlerDefaultParserName(String parserName)
			throws SearchLibException {
		rwl.w.lock();
		try {
			fileCrawlerDefaultParserName = parserName;
			rebuildParserMap();
		} finally {
			rwl.w.unlock();
		}
	}

	public String getFileCrawlerDefaultParserName() {
		rwl.r.lock();
		try {
			return fileCrawlerDefaultParserName;
		} finally {
			rwl.r.unlock();
		}
	}

	public Parser getFileCrawlerDefaultParser() throws SearchLibException {
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

	public void setWebCrawlerDefaultParserName(String parserName)
			throws SearchLibException {
		rwl.w.lock();
		try {
			webCrawlerDefaultParserName = parserName;
			rebuildParserMap();
		} finally {
			rwl.w.unlock();
		}
	}

	public String getWebCrawlerDefaultParserName() {
		rwl.r.lock();
		try {
			return webCrawlerDefaultParserName;
		} finally {
			rwl.r.unlock();
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

	private void rebuildParserMap() throws SearchLibException {
		extensionParserMap.clear();
		mimeTypeParserMap.clear();
		for (ParserFactory parserFactory : parserFactoryMap.values()) {
			if (config != null)
				parserFactory.getFieldMap().compileAnalyzer(
						config.getSchema().getAnalyzerList());
			Set<String> extensionSet = parserFactory.getExtensionSet();
			if (extensionSet != null)
				for (String extension : extensionSet)
					extensionParserMap.put(extension, parserFactory);
			Set<String> mimeTypeSet = parserFactory.getMimeTypeSet();
			if (mimeTypeSet != null) {
				for (String mimeType : mimeTypeSet) {
					Set<ParserFactory> parserSet = mimeTypeParserMap
							.get(mimeType);
					if (parserSet == null) {
						parserSet = new HashSet<ParserFactory>();
						mimeTypeParserMap.put(mimeType, parserSet);
					}
					parserSet.add(parserFactory);
				}
			}
		}
		parserFactoryArray = new ParserFactory[parserFactoryMap.size()];
		int i = 0;
		for (ParserFactory parserFactory : parserFactoryMap.values())
			parserFactoryArray[i++] = parserFactory;
		webCrawlerDefaultParserFactory = webCrawlerDefaultParserName == null ? null
				: parserFactoryMap.get(webCrawlerDefaultParserName);
		fileCrawlerDefaultParserFactory = fileCrawlerDefaultParserName == null ? null
				: parserFactoryMap.get(fileCrawlerDefaultParserName);
	}

	public void replaceParserFactory(ParserFactory oldParser,
			ParserFactory newParser) throws SearchLibException {
		rwl.w.lock();
		try {
			if (oldParser != null)
				parserFactoryMap.remove(oldParser.getParserName());
			if (newParser != null)
				parserFactoryMap.put(newParser.getParserName(), newParser);
			rebuildParserMap();
		} finally {
			rwl.w.unlock();
		}
	}

	public ParserFactory[] getParserFactoryArray() {
		rwl.r.lock();
		try {
			return parserFactoryArray;
		} finally {
			rwl.r.unlock();
		}
	}

	final private Parser getParser(ParserFactory parserFactory)
			throws SearchLibException {
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
			throws SearchLibException {
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

	private ParserFactory getParserFactoryFromMimeTypeNoLock(
			String contentBaseType, String url) {
		if (mimeTypeParserMap == null)
			return null;
		Set<ParserFactory> parserSet = mimeTypeParserMap.get(contentBaseType);
		if (parserSet == null)
			return null;
		for (ParserFactory parser : parserSet)
			if (parser.matchUrlPattern(url))
				return parser;
		if (url == null)
			return null;
		return getParserFactoryFromMimeTypeNoLock(contentBaseType, null);
	}

	private Parser getParserFromMimeType(String contentBaseType, String url)
			throws SearchLibException {
		rwl.r.lock();
		try {
			ParserFactory parserFactory = getParserFactoryFromMimeTypeNoLock(
					contentBaseType, url);
			return getParser(parserFactory);
		} finally {
			rwl.r.unlock();
		}
	}

	public ParserFactory checkParserFromMimeType(String contentBaseType,
			String url) {
		rwl.r.lock();
		try {
			return getParserFactoryFromMimeTypeNoLock(contentBaseType, url);
		} finally {
			rwl.r.unlock();
		}
	}

	final private Parser getParser(String filename, String contentBaseType,
			String url, Parser defaultParser) throws SearchLibException {
		rwl.r.lock();
		try {
			Parser parser = null;
			if (contentBaseType != null)
				parser = getParserFromMimeType(contentBaseType, url);
			if (parser == null && filename != null)
				parser = getParserFromExtension(FilenameUtils
						.getExtension(filename));
			return parser == null ? defaultParser : parser;
		} finally {
			rwl.r.unlock();
		}
	}

	private final static String FILE_CRAWLER_DEFAULT_ATTRIBUTE = "fileCrawlerDefault";

	private final static String WEB_CRAWLER_DEFAULT_ATTRIBUTE = "webCrawlerDefault";

	private void fromXmlConfig(Config config, XPathParser xpp, Node parentNode)
			throws XPathExpressionException, DOMException, IOException,
			SearchLibException {

		fileCrawlerDefaultParserName = XPathParser.getAttributeString(
				parentNode, FILE_CRAWLER_DEFAULT_ATTRIBUTE);

		webCrawlerDefaultParserName = XPathParser.getAttributeString(
				parentNode, WEB_CRAWLER_DEFAULT_ATTRIBUTE);

		NodeList parserNodes = xpp.getNodeList(parentNode, "parser");
		for (int i = 0; i < parserNodes.getLength(); i++) {
			Node parserNode = parserNodes.item(i);
			ParserFactory parserFactory = ParserFactory.create(config, xpp,
					parserNode);

			if (parserFactory != null)
				parserFactoryMap.put(parserFactory.getParserName(),
						parserFactory);
		}
		rebuildParserMap();
	}

	public void writeXmlConfig(XmlWriter xmlWriter) throws SAXException {
		rwl.r.lock();
		try {
			xmlWriter.startElement("parsers", WEB_CRAWLER_DEFAULT_ATTRIBUTE,
					webCrawlerDefaultParserName,
					FILE_CRAWLER_DEFAULT_ATTRIBUTE,
					fileCrawlerDefaultParserName);
			for (ParserFactory parser : parserFactoryMap.values())
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

	final public ParserFactory getParserByName(String parserName) {
		if (parserName == null || parserName.length() == 0)
			return null;
		rwl.r.lock();
		try {
			return parserFactoryMap.get(parserName);
		} finally {
			rwl.r.unlock();
		}
	}

	private final Parser parserLoop(IndexDocument sourceDocument,
			StreamLimiter streamLimiter, LanguageEnum lang, Parser parser)
			throws SearchLibException {
		try {
			Set<ParserType> parserSet = new HashSet<ParserType>();
			while (parser != null) {
				if (parserSet.contains(parser.getParserType()))
					throw new SearchLibException(
							"Infinite loop in parser fail over loop");
				parserSet.add(parser.getParserType());
				parser.doParserContent(sourceDocument, streamLimiter, lang);
				if (parser.getError() == null)
					return parser;
				ParserFactory parserFactory = getParserByName(parser
						.getFailOverParserName());
				if (parserFactory == null)
					return parser;
				Parser nextParser = getParser(parserFactory);
				if (nextParser == null)
					return parser;
				parser = nextParser;
			}
			return parser;
		} finally {
			IOUtils.closeQuietly(streamLimiter);
		}
	}

	public final Parser parseStream(IndexDocument sourceDocument,
			String filename, String contentBaseType, String url,
			InputStream inputStream, LanguageEnum lang, Parser defaultParser)
			throws SearchLibException, IOException {
		Parser parser = getParser(filename, contentBaseType, url, defaultParser);
		if (parser == null)
			return null;
		StreamLimiter streamLimiter = new StreamLimiterInputStream(
				parser.getSizeLimit(), inputStream, filename, url);
		return parserLoop(sourceDocument, streamLimiter, lang, parser);
	}

	public final Parser parseFile(File file, LanguageEnum lang)
			throws SearchLibException, IOException {
		return parseFile(null, file.getName(), null, null, file, lang);
	}

	public final Parser parseFile(IndexDocument sourceDocument,
			String filename, String contentBaseType, String url, File file,
			LanguageEnum lang) throws SearchLibException, IOException {
		Parser parser = getParser(filename, contentBaseType, url, null);
		if (parser == null)
			return null;
		StreamLimiter streamLimiter = new StreamLimiterFile(
				parser.getSizeLimit(), file);
		return parserLoop(sourceDocument, streamLimiter, lang, parser);
	}

	public final Parser parseBase64(IndexDocument sourceDocument,
			String filename, String contentBaseType, String url,
			String base64text, LanguageEnum lang) throws SearchLibException,
			IOException {
		Parser parser = getParser(filename, contentBaseType, url, null);
		if (parser == null)
			return null;
		StreamLimiter streamLimiter = new StreamLimiterBase64(base64text,
				parser.getSizeLimit(), filename);
		return parserLoop(sourceDocument, streamLimiter, lang, parser);
	}

	public final Parser parseFileInstance(IndexDocument sourceDocument,
			String filename, String contentBaseType, String url,
			FileInstanceAbstract fileInstance, LanguageEnum lang,
			Parser defaultParser) throws SearchLibException, IOException {
		Parser parser = getParser(filename, contentBaseType, url, defaultParser);
		if (parser == null)
			return null;
		StreamLimiter streamLimiter = new StreamLimiterFileInstance(
				fileInstance, parser.getSizeLimit());
		return parserLoop(sourceDocument, streamLimiter, lang, parser);
	}
}
