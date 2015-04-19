/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2013-2015 Emmanuel Keller / Jaeksoft
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
import java.util.Iterator;
import java.util.List;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.analysis.ClassPropertyEnum;
import com.jaeksoft.searchlib.analysis.LanguageEnum;
import com.jaeksoft.searchlib.streamlimiter.StreamLimiter;
import com.jaeksoft.searchlib.util.IOUtils;
import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;

public class RssParser extends Parser {

	public static final String[] DEFAULT_MIMETYPES = { "application/rss+xml" };

	public static final String[] DEFAULT_EXTENSIONS = { "rss" };

	private static ParserFieldEnum[] fieldList = { ParserFieldEnum.parser_name,
			ParserFieldEnum.channel_title, ParserFieldEnum.channel_link,
			ParserFieldEnum.channel_description, ParserFieldEnum.title,
			ParserFieldEnum.link, ParserFieldEnum.description };

	public RssParser() {
		super(fieldList);
	}

	@Override
	public void initProperties() throws SearchLibException {
		super.initProperties();
		addProperty(ClassPropertyEnum.SIZE_LIMIT, "0", null, 20, 1);
		addProperty(ClassPropertyEnum.RSS_METHOD,
				ClassPropertyEnum.RSS_METHODS[0],
				ClassPropertyEnum.RSS_METHODS, 0, 0);
	}

	@Override
	protected void parseContent(StreamLimiter streamLimiter, LanguageEnum lang)
			throws IOException {

		String p = getProperty(ClassPropertyEnum.RSS_METHOD).getValue();
		boolean oneResult = p == null
				|| ClassPropertyEnum.RSS_METHODS[0].equals(p);

		XmlReader reader = null;
		try {
			SyndFeedInput input = new SyndFeedInput();
			reader = new XmlReader(streamLimiter.getNewInputStream());
			SyndFeed feed = input.build(reader);
			List<?> entries = feed.getEntries();
			if (entries == null)
				return;
			Iterator<?> itEntries = entries.iterator();

			ParserResultItem resultItem = oneResult ? getNewParserResultItem()
					: null;

			while (itEntries.hasNext()) {
				if (!oneResult)
					resultItem = getNewParserResultItem();

				SyndEntry entry = (SyndEntry) itEntries.next();
				resultItem.addField(ParserFieldEnum.channel_title,
						feed.getTitle());
				resultItem.addField(ParserFieldEnum.channel_link,
						feed.getLink());
				resultItem.addField(ParserFieldEnum.channel_description,
						feed.getDescription());
				resultItem.addField(ParserFieldEnum.creation_date,
						entry.getPublishedDate());
				resultItem.addField(ParserFieldEnum.title, entry.getTitle());
				resultItem.addField(ParserFieldEnum.link, entry.getLink());
				SyndContent syndContent = entry.getDescription();
				if (syndContent != null)
					resultItem.addField(ParserFieldEnum.description,
							syndContent.getValue());
				addDetectedLink(entry.getLink());
			}
		} catch (IllegalArgumentException e) {
			throw new IOException(e);
		} catch (FeedException e) {
			throw new IOException(e);
		} finally {
			IOUtils.close(reader);
		}

	}
}
