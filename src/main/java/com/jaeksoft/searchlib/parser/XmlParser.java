/**
 * License Agreement for OpenSearchServer
 * <p>
 * Copyright (C) 2012-2015 Emmanuel Keller / Jaeksoft
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
package com.jaeksoft.searchlib.parser;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.analysis.ClassPropertyEnum;
import com.jaeksoft.searchlib.analysis.LanguageEnum;
import com.jaeksoft.searchlib.streamlimiter.StreamLimiter;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;

public class XmlParser extends Parser {

	public static final String[] DEFAULT_MIMETYPES = { "application/xml" };

	public static final String[] DEFAULT_EXTENSIONS = { "xml" };

	private static ParserFieldEnum[] fl = { ParserFieldEnum.parser_name, ParserFieldEnum.content };

	public XmlParser() {
		super(fl);
	}

	@Override
	public void initProperties() throws SearchLibException {
		super.initProperties();
		addProperty(ClassPropertyEnum.SIZE_LIMIT, "0", null, 20, 1);
	}

	@Override
	protected void parseContent(StreamLimiter streamLimiter, LanguageEnum lang) throws IOException {

		try {
			final ParserResultItem result = getNewParserResultItem();
			final StringBuilder stringBuilder = new StringBuilder();
			SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
			saxParserFactory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
			SAXParser saxParser = saxParserFactory.newSAXParser();

			DefaultHandler handler = new DefaultHandler() {

				@Override
				public void startElement(String uri, String localName, String qName, Attributes attributes) {
					stringBuilder.setLength(0);
				}

				@Override
				public void endElement(String uri, String localName, String qName) {
					if (stringBuilder.length() > 0) {
						String t = stringBuilder.toString().trim();
						result.addField(ParserFieldEnum.content, t);
					}
				}

				@Override
				public void characters(char ch[], int start, int length) throws SAXException {
					stringBuilder.append(ch, start, length);
				}
			};
			saxParser.parse(new InputSource(streamLimiter.getNewInputStream()), handler);
		} catch (ParserConfigurationException | SAXException e) {
			throw new IOException(e);
		}

	}
}
