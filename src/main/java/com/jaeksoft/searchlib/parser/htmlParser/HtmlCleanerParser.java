/**
 * License Agreement for OpenSearchServer
 * <p>
 * Copyright (C) 2012-2017 Emmanuel Keller / Jaeksoft
 * <p>
 * http://www.open-search-server.com
 * <p>
 * This file is part of OpenSearchServer.
 * <p>
 * OpenSearchServer is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * OpenSearchServer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with OpenSearchServer.
 * If not, see <http://www.gnu.org/licenses/>.
 **/

package com.jaeksoft.searchlib.parser.htmlParser;

import com.jaeksoft.searchlib.Logging;
import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.DomSerializer;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.SimpleHtmlSerializer;
import org.htmlcleaner.TagNode;
import org.htmlcleaner.XPatherException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Collection;

public class HtmlCleanerParser extends HtmlDocumentProvider<Node> {

	private final HtmlCleaner cleaner;

	private TagNode rootTagNode = null;

	private String charsetCache = null;

	public HtmlCleanerParser() {
		super(HtmlParserEnum.HtmlCleanerParser);
		cleaner = new HtmlCleaner();
		CleanerProperties props = cleaner.getProperties();
		props.setNamespacesAware(true);
	}

	@Override
	protected HtmlNodeAbstract<Node> getDocument(String charset, InputStream inputStream)
			throws IOException, ParserConfigurationException {
		rootTagNode = cleaner.clean(inputStream, charset);
		charsetCache = null;
		return getDomHtmlNode();
	}

	@Override
	protected HtmlNodeAbstract<Node> getDocument(String pageSource) throws IOException, ParserConfigurationException {
		rootTagNode = cleaner.clean(new StringReader(pageSource));
		charsetCache = null;
		return getDomHtmlNode();
	}

	private DomHtmlNode getDomHtmlNode() throws ParserConfigurationException {
		Document document = new DomSerializer(cleaner.getProperties(), true).createDOM(rootTagNode);
		String lang = rootTagNode.getAttributeByName("lang");
		if (lang != null)
			document.getDocumentElement().setAttribute("lang", lang);
		return new DomHtmlNode(document);
	}

	public String findCharset() {
		if (charsetCache != null)
			return charsetCache;
		String charsetCache = getMetaCharset();
		if (charsetCache == null)
			return null;
		try {
			Charset.forName(charsetCache);
			return charsetCache;
		} catch (UnsupportedCharsetException e1) {
			try {
				charsetCache = charsetCache.toUpperCase();
				Charset.forName(charsetCache);
				return charsetCache;
			} catch (UnsupportedCharsetException e2) {
				Logging.warn(e2);
				return null;
			}
		}
	}

	public void writeHtmlToFile(File htmlFile) throws IOException {
		SimpleHtmlSerializer htmlSerializer = new SimpleHtmlSerializer(cleaner.getProperties());
		String charset = findCharset();
		if (charset != null)
			htmlSerializer.writeToFile(rootTagNode, htmlFile.getAbsolutePath(), charset);
		else
			htmlSerializer.writeToFile(rootTagNode, htmlFile.getAbsolutePath());
	}

	public TagNode getTagNode() {
		return rootTagNode;
	}

	final public int xpath(String xPathExpression, Collection<TagNode> tagNodeCollection) throws XPatherException {
		if (xPathExpression.startsWith("/html"))
			xPathExpression = xPathExpression.substring(5);
		final Object[] objects = rootTagNode.evaluateXPath(xPathExpression);
		if (objects == null)
			return 0;
		for (final Object object : objects)
			tagNodeCollection.add((TagNode) object);
		return objects.length;
	}

	@Override
	public boolean isXPathSupported() {
		return true;
	}

	@Override
	public String generateSource() {
		return getRootNode().generatedSource();
	}

}
