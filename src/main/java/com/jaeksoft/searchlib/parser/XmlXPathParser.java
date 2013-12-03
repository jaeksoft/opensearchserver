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
package com.jaeksoft.searchlib.parser;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.analysis.ClassPropertyEnum;
import com.jaeksoft.searchlib.analysis.LanguageEnum;
import com.jaeksoft.searchlib.streamlimiter.StreamLimiter;
import com.jaeksoft.searchlib.util.XPathParser;

public class XmlXPathParser extends Parser {

	private static ParserFieldEnum[] fl = { ParserFieldEnum.parser_name };

	public XmlXPathParser() {
		super(fl);
	}

	@Override
	public void initProperties() throws SearchLibException {
		super.initProperties();
		addProperty(ClassPropertyEnum.SIZE_LIMIT, "0", null, 20, 1);
		addProperty(ClassPropertyEnum.XPATH_DOCUMENT_SELECTOR, "", null, 30, 1);
	}

	@Override
	protected void parseContent(StreamLimiter streamLimiter, LanguageEnum lang)
			throws IOException {

		String xPathDocumentSelector = getProperty(
				ClassPropertyEnum.XPATH_DOCUMENT_SELECTOR).getValue();

		try {
			XPathParser xPathParser = new XPathParser(streamLimiter.getFile());
			NodeList nodeList = xPathParser.getNodeList(xPathDocumentSelector);
			if (nodeList == null)
				return;
			int l = nodeList.getLength();
			if (l == 0)
				return;
			for (int i = 0; i < l; i++) {
				Node documentNode = nodeList.item(i);
				ParserResultItem parserResultItem = getNewParserResultItem();
				parserResultItem.setXmlForXPath(xPathParser, documentNode);
			}
		} catch (ParserConfigurationException e) {
			throw new IOException(e);
		} catch (SAXException e) {
			throw new IOException(e);
		} catch (SearchLibException e) {
			throw new IOException(e);
		} catch (XPathExpressionException e) {
			throw new IOException(e);
		}
	}
}
