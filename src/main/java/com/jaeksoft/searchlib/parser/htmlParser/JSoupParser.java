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

import com.jaeksoft.searchlib.util.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Node;

import java.io.IOException;
import java.io.InputStream;

public class JSoupParser extends HtmlDocumentProvider<Node> {

	public JSoupParser() {
		super(HtmlParserEnum.JSoupParser);
	}

	@Override
	protected HtmlNodeAbstract<Node> getDocument(String charset, InputStream inputStream) throws IOException {
		Node node = Jsoup.parse(IOUtils.toString(inputStream, charset));
		return new JSoupHtmlNode(node);
	}

	@Override
	protected HtmlNodeAbstract<Node> getDocument(String htmlSource) {
		Node node = Jsoup.parse(htmlSource);
		return new JSoupHtmlNode(node);
	}

	@Override
	public boolean isXPathSupported() {
		return false;
	}

	@Override
	public String generateSource() {
		return getRootNode().node.outerHtml();
	}
}
