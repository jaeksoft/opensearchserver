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

import org.cyberneko.html.parsers.DOMParser;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;

public class NekoHtmlParser extends HtmlDocumentProvider<Node> {

	public NekoHtmlParser() {
		super(HtmlParserEnum.NekoHtmlParser);
	}

	private DomHtmlNode getDomHtmlNode(InputSource inputSource) throws SAXException, IOException {
		DOMParser parser = new DOMParser();
		parser.setFeature("http://xml.org/sax/features/namespaces", true);
		parser.setFeature("http://cyberneko.org/html/features/balance-tags/ignore-outside-content", false);
		parser.setFeature("http://cyberneko.org/html/features/balance-tags", true);
		parser.setFeature("http://cyberneko.org/html/features/report-errors", false);
		parser.setProperty("http://cyberneko.org/html/properties/names/elems", "lower");
		parser.setProperty("http://cyberneko.org/html/properties/names/attrs", "lower");
		parser.parse(inputSource);
		return new DomHtmlNode(parser.getDocument());
	}

	@Override
	protected DomHtmlNode getDocument(String charset, InputStream inputStream) throws SAXException, IOException {
		InputSource inputSource = new InputSource(inputStream);
		inputSource.setEncoding(charset);
		return getDomHtmlNode(inputSource);
	}

	@Override
	protected HtmlNodeAbstract<Node> getDocument(String htmlSource) throws IOException, SAXException {
		return getDomHtmlNode(new InputSource(new StringReader(htmlSource)));
	}

	@Override
	public boolean isXPathSupported() {
		return true;
	}

	@Override
	public String generateSource() {
		return this.getRootNode().generatedSource();
	}
}
