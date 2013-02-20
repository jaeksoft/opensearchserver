/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2013 Emmanuel Keller / Jaeksoft
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
import java.util.ArrayList;
import java.util.List;

import com.jaeksoft.searchlib.streamlimiter.LimitException;
import com.jaeksoft.searchlib.streamlimiter.StreamLimiter;

public enum HtmlParserEnum {

	FirefoxParser("Firefox", FirefoxParser.class),

	HtmlUnitParser("HtmlUnit", HtmlUnitParser.class),

	HtmlCleanerParser("HtmlCleaner", HtmlCleanerParser.class),

	JSoupParser("Jsoup", JSoupParser.class),

	NekoHtmlParser("NekoHtml", NekoHtmlParser.class),

	StrictXhtmlParser("StrictXml", StrictXhtmlParser.class),

	TagSoupParser("TagSoup", TagsoupParser.class),

	BestScoreParser("Best score", null);

	private final String label;

	private final Class<? extends HtmlDocumentProvider> classDef;

	private static HtmlParserEnum[] bestScoreOrder = { TagSoupParser,
			NekoHtmlParser, HtmlCleanerParser, JSoupParser };

	private HtmlParserEnum(String label,
			Class<? extends HtmlDocumentProvider> classDef) {
		this.label = label;
		this.classDef = classDef;
	}

	public String getLabel() {
		return label;
	}

	private static HtmlDocumentProvider findBestProvider(String charset,
			StreamLimiter streamLimiter) throws IOException {

		HtmlDocumentProvider provider = HtmlParserEnum.StrictXhtmlParser
				.getHtmlParser(charset, streamLimiter);
		if (provider.getRootNode() != null)
			return provider;

		List<HtmlDocumentProvider> providerList = new ArrayList<HtmlDocumentProvider>(
				bestScoreOrder.length);
		for (HtmlParserEnum htmlParserEnum : bestScoreOrder)
			providerList.add(htmlParserEnum.getHtmlParser(charset,
					streamLimiter));
		return HtmlDocumentProvider.bestScore(providerList);
	}

	public HtmlDocumentProvider getHtmlParser(String charset,
			StreamLimiter streamLimiter) throws LimitException, IOException {
		try {
			if (this == BestScoreParser)
				return findBestProvider(charset, streamLimiter);
			HtmlDocumentProvider htmlParser = classDef.newInstance();
			htmlParser.init(charset, streamLimiter);
			return htmlParser;
		} catch (InstantiationException e) {
			throw new IOException(e);
		} catch (IllegalAccessException e) {
			throw new IOException(e);
		}
	}

	public static String[] getLabelArray() {
		String[] labelArray = new String[values().length];
		int i = 0;
		for (HtmlParserEnum htmlParserEnum : values())
			labelArray[i++] = htmlParserEnum.label;
		return labelArray;
	}

	public static HtmlParserEnum find(String value) {
		for (HtmlParserEnum htmlParserEnum : values())
			if (htmlParserEnum.name().equalsIgnoreCase(value)
					|| htmlParserEnum.label.equalsIgnoreCase(value))
				return htmlParserEnum;
		return BestScoreParser;
	}
}
