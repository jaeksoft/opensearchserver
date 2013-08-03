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

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Node;
import org.xml.sax.SAXException;

public class JSoupParser extends HtmlDocumentProvider {

	public JSoupParser() {
		super(HtmlParserEnum.JSoupParser);
	}

	@Override
	protected HtmlNodeAbstract<?> getDocument(String charset,
			InputStream inputStream) throws SAXException, IOException,
			ParserConfigurationException {
		Node node = Jsoup.parse(IOUtils.toString(inputStream, charset));
		return new JSoupHtmlNode(node);
	}

	@Override
	protected HtmlNodeAbstract<?> getDocument(String htmlSource)
			throws IOException, ParserConfigurationException {
		Node node = Jsoup.parse(htmlSource);
		return new JSoupHtmlNode(node);
	}
}
