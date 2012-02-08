/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2012 Emmanuel Keller / Jaeksoft
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

import javax.xml.parsers.ParserConfigurationException;

import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.DomSerializer;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.parser.LimitException;
import com.jaeksoft.searchlib.parser.LimitInputStream;

public class HtmlCleanerParser extends HtmlDocumentProvider {

	public HtmlCleanerParser(String charset, LimitInputStream inputStream)
			throws LimitException {
		super(charset, inputStream);
	}

	@Override
	public String getName() {
		return "HtmlCleaner";
	}

	@Override
	protected DomHtmlNode getDocument(String charset,
			LimitInputStream inputStream) throws SAXException, IOException,
			ParserConfigurationException {
		HtmlCleaner cleaner = new HtmlCleaner();
		CleanerProperties props = cleaner.getProperties();
		props.setNamespacesAware(true);
		TagNode node = cleaner.clean(inputStream, charset);
		if (!inputStream.isComplete())
			throw new LimitException();
		Document document = new DomSerializer(props, true).createDOM(node);
		return new DomHtmlNode(document);
	}
}
