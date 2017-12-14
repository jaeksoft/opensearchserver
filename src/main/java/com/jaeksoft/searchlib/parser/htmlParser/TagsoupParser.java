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

import org.apache.xalan.xsltc.trax.SAX2DOM;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;

public class TagsoupParser extends HtmlDocumentProvider<Node> {

	public TagsoupParser() {
		super(HtmlParserEnum.TagSoupParser);
	}

	private DomHtmlNode getDomHtmlNode(InputSource inputSource)
			throws ParserConfigurationException, IOException, SAXException {
		org.ccil.cowan.tagsoup.Parser parser = new org.ccil.cowan.tagsoup.Parser();
		parser.setFeature("http://xml.org/sax/features/namespace-prefixes", true);
		SAX2DOM sax2dom = new SAX2DOM();
		parser.setContentHandler(sax2dom);
		parser.parse(inputSource);
		return new DomHtmlNode(sax2dom.getDOM());
	}

	@Override
	protected DomHtmlNode getDocument(String charset, InputStream inputStream)
			throws SAXException, IOException, ParserConfigurationException {
		InputSource inputSource = new InputSource(inputStream);
		inputSource.setEncoding(charset);
		return getDomHtmlNode(inputSource);
	}

	@Override
	protected HtmlNodeAbstract<Node> getDocument(String htmlSource)
			throws IOException, ParserConfigurationException, SAXException {
		return getDomHtmlNode(new InputSource(new StringReader(htmlSource)));
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
