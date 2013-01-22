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

import org.apache.commons.feedparser.DefaultFeedParserListener;
import org.apache.commons.feedparser.FeedParser;
import org.apache.commons.feedparser.FeedParserException;
import org.apache.commons.feedparser.FeedParserFactory;
import org.apache.commons.feedparser.FeedParserState;

import com.jaeksoft.searchlib.analysis.LanguageEnum;
import com.jaeksoft.searchlib.streamlimiter.StreamLimiter;

public class RssParser extends Parser {

	private static ParserFieldEnum[] fieldList = { ParserFieldEnum.parser_name,
			ParserFieldEnum.title, ParserFieldEnum.note, ParserFieldEnum.body };

	public RssParser() {
		super(fieldList);
	}

	private class FeedListener extends DefaultFeedParserListener {

		@Override
		public void onAuthor(FeedParserState state, String name, String email,
				String resource) throws FeedParserException {
			System.out.println("onAuthor: " + name + " | " + email + " | "
					+ resource);
		}

		@Override
		public void onContent(FeedParserState state, String type,
				String format, String encoding, String mode, String value,
				boolean isSummary) throws FeedParserException {
			System.out.println("onContent: " + type + " | " + format + " | "
					+ mode + " | " + value);
		}

		@Override
		public void onContentEnd() throws FeedParserException {
			System.out.println("CONTENT END");
		}
	}

	@Override
	protected void parseContent(StreamLimiter streamLimiter, LanguageEnum lang)
			throws IOException {

		try {
			FeedParser parser = FeedParserFactory.newFeedParser();
			parser.parse(new FeedListener(), streamLimiter.getNewInputStream(),
					null);
		} catch (FeedParserException e) {
			throw new IOException(e);
		}

	}
}
