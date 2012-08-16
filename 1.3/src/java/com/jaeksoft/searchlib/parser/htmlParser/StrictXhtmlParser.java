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
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.streamlimiter.LimitException;
import com.jaeksoft.searchlib.streamlimiter.StreamLimiter;
import com.jaeksoft.searchlib.util.DomUtils;

public class StrictXhtmlParser extends HtmlDocumentProvider {

	public StrictXhtmlParser(String charset, StreamLimiter streamLimiter)
			throws LimitException {
		super(charset, streamLimiter);
	}

	@Override
	public String getName() {
		return "StrictXml";
	}

	@Override
	protected DomHtmlNode getDocument(String charset, InputStream inputStream)
			throws SAXException, IOException, ParserConfigurationException {
		DocumentBuilder builder = DomUtils.getNewDocumentBuilder(false, true);
		InputSource inputSource = new InputSource(inputStream);
		inputSource.setEncoding(charset);
		return new DomHtmlNode(builder.parse(inputSource));
	}

}
