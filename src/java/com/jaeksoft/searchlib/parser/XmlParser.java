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
package com.jaeksoft.searchlib.parser;

import java.io.IOException;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.analysis.ClassPropertyEnum;
import com.jaeksoft.searchlib.analysis.LanguageEnum;
import com.jaeksoft.searchlib.streamlimiter.StreamLimiter;

public class XmlParser extends Parser {

	private static ParserFieldEnum[] fl = { ParserFieldEnum.parser_name,
			ParserFieldEnum.content };
	private SAXParserFactory saxParserFactory;
	private SAXParser saxParser;
	private StringBuilder content;
	private Boolean addContent;

	public XmlParser() {
		super(fl);
	}

	@Override
	public void initProperties() throws SearchLibException {
		super.initProperties();
		addProperty(ClassPropertyEnum.SIZE_LIMIT, "0", null);
	}

	@Override
	protected void parseContent(StreamLimiter streamLimiter, LanguageEnum lang)
			throws IOException {
		try {
			saxParserFactory = SAXParserFactory.newInstance();
			saxParser = saxParserFactory.newSAXParser();
			content = new StringBuilder();
			DefaultHandler handler = new DefaultHandler() {
				@Override
				public void startElement(String uri, String localName,
						String qName, Attributes attributes)
						throws SAXException {
					addContent = true;
				}

				@Override
				public void endElement(String uri, String localName,
						String qName) throws SAXException {
					addContent = true;
				}

				@Override
				public void characters(char ch[], int start, int length)
						throws SAXException {
					if (addContent) {
						content.append(ch, start, length);
					}
				}
			};
			saxParser.parse(new InputSource(streamLimiter.getNewInputStream()),
					handler);
			addField(ParserFieldEnum.content, content.toString().trim());
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
