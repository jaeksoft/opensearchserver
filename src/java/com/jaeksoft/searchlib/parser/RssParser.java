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

package com.jaeksoft.searchlib.parser;

import java.io.IOException;
import java.util.Date;

import org.apache.commons.feedparser.DefaultFeedParserListener;
import org.apache.commons.feedparser.FeedParser;
import org.apache.commons.feedparser.FeedParserException;
import org.apache.commons.feedparser.FeedParserFactory;
import org.apache.commons.feedparser.FeedParserState;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.analysis.ClassPropertyEnum;
import com.jaeksoft.searchlib.analysis.LanguageEnum;
import com.jaeksoft.searchlib.streamlimiter.StreamLimiter;

public class RssParser extends Parser {

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
		addProperty(ClassPropertyEnum.SIZE_LIMIT, "0", null);
		addProperty(ClassPropertyEnum.RSS_METHOD,
				ClassPropertyEnum.RSS_METHODS[0], ClassPropertyEnum.RSS_METHODS);
	}

	private class FeedListener extends DefaultFeedParserListener {

		private String currentChannelTitle = null;
		private String currentChannelLink = null;
		private String currentChannelDescription = null;
		private ParserResultItem currentResult = null;
		private final boolean oneResult;

		private FeedListener(boolean oneResult) {
			this.oneResult = oneResult;
		}

		private ParserResultItem nextResult() {
			if (oneResult && currentResult != null)
				return currentResult;
			currentResult = getNewParserResultItem();
			return currentResult;
		}

		@Override
		public void onChannel(FeedParserState state, String title, String link,
				String description) throws FeedParserException {
			this.currentChannelTitle = title;
			this.currentChannelLink = link;
			this.currentChannelDescription = description;
		}

		@Override
		public void onItem(FeedParserState state, String title, String link,
				String description, String permalink)
				throws FeedParserException {
			currentResult = nextResult();
			currentResult.addField(ParserFieldEnum.channel_title,
					currentChannelTitle);
			currentResult.addField(ParserFieldEnum.channel_link,
					currentChannelLink);
			currentResult.addField(ParserFieldEnum.channel_description,
					currentChannelDescription);
			currentResult.addField(ParserFieldEnum.title, title);
			currentResult.addField(ParserFieldEnum.link, link);
			currentResult.addField(ParserFieldEnum.description, description);
			addDetectedLink(link);
		}

		@Override
		public void onCreated(FeedParserState state, Date date)
				throws FeedParserException {
			if (currentResult == null)
				return;
			currentResult.addField(ParserFieldEnum.creation_date, date);
		}

	}

	@Override
	protected void parseContent(StreamLimiter streamLimiter, LanguageEnum lang)
			throws IOException {

		try {
			String p = getProperty(ClassPropertyEnum.RSS_METHOD).getValue();
			boolean oneResult = p == null
					|| ClassPropertyEnum.RSS_METHODS[0].equals(p);

			FeedParser parser = FeedParserFactory.newFeedParser();
			parser.parse(new FeedListener(oneResult),
					streamLimiter.getNewInputStream(), null);
		} catch (FeedParserException e) {
			throw new IOException(e);
		}

	}
}
