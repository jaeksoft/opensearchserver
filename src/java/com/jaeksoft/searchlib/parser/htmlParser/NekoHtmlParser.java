/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2012-2013 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.parser.htmlParser;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;

import javax.xml.parsers.ParserConfigurationException;

import org.cyberneko.html.parsers.DOMParser;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class NekoHtmlParser extends HtmlDocumentProvider {

	public NekoHtmlParser() {
		super(HtmlParserEnum.NekoHtmlParser);
	}

	private DomHtmlNode getDomHtmlNode(InputSource inputSource)
			throws SAXException, IOException {
		DOMParser parser = new DOMParser();
		parser.setFeature("http://xml.org/sax/features/namespaces", true);
		parser.setFeature(
				"http://cyberneko.org/html/features/balance-tags/ignore-outside-content",
				false);
		parser.setFeature("http://cyberneko.org/html/features/balance-tags",
				true);
		parser.setFeature("http://cyberneko.org/html/features/report-errors",
				false);
		parser.setProperty("http://cyberneko.org/html/properties/names/elems",
				"lower");
		parser.setProperty("http://cyberneko.org/html/properties/names/attrs",
				"lower");
		parser.parse(inputSource);
		return new DomHtmlNode(parser.getDocument());
	}

	@Override
	protected DomHtmlNode getDocument(String charset, InputStream inputStream)
			throws SAXException, IOException, ParserConfigurationException {
		InputSource inputSource = new InputSource(inputStream);
		inputSource.setEncoding(charset);
		return getDomHtmlNode(inputSource);
	}

	@Override
	protected HtmlNodeAbstract<?> getDocument(String htmlSource)
			throws IOException, ParserConfigurationException, SAXException {
		return getDomHtmlNode(new InputSource(new StringReader(htmlSource)));
	}

}
