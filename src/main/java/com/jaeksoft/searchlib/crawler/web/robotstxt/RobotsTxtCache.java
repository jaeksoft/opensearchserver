/**
 * License Agreement for OpenSearchServer
 * <p>
 * Copyright (C) 2008-2017 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.crawler.web.robotstxt;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.analysis.LanguageEnum;
import com.jaeksoft.searchlib.config.Config;
import com.jaeksoft.searchlib.crawler.web.GenericCache;
import com.jaeksoft.searchlib.crawler.web.database.UrlItem;
import com.jaeksoft.searchlib.crawler.web.spider.Crawl;
import com.jaeksoft.searchlib.crawler.web.spider.HttpDownloader;
import com.jaeksoft.searchlib.parser.Parser;
import com.jaeksoft.searchlib.parser.ParserFactory;
import com.jaeksoft.searchlib.parser.ParserSelector;
import com.jaeksoft.searchlib.streamlimiter.StreamLimiter;
import com.jaeksoft.searchlib.util.LinkUtils;
import com.qwazr.crawler.web.robotstxt.RobotsTxt;
import com.qwazr.utils.CharsetUtils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

public class RobotsTxtCache extends GenericCache<String, RobotsTxtItem> {

	private ParserSelector parserSelector;

	public RobotsTxtCache() throws SearchLibException, ClassNotFoundException {
		parserSelector = new ParserSelector(null,
				ParserFactory.create(null, "RobotsTxt parser", RobotsTxtParser.class.getName()));
	}

	@Override
	protected RobotsTxtItem[] newArray(int size) {
		return new RobotsTxtItem[size];
	}

	/**
	 * Return the RobotsTxt object related to the URL.
	 *
	 * @param httpDownloader
	 * @param config
	 * @param url
	 * @param reloadRobotsTxt
	 * @return
	 * @throws SearchLibException
	 * @throws URISyntaxException
	 * @throws IOException
	 */
	public RobotsTxtItem getRobotsTxt(final HttpDownloader httpDownloader, final Config config, URL url,
			boolean reloadRobotsTxt) throws SearchLibException, URISyntaxException, IOException {
		final UrlItem urlItem = config.getUrlManager().getNewUrlItem(RobotsTxtItem.getRobotsUrl(url).toExternalForm());
		final String robotsKey = urlItem.getUrl();

		return getOrCreate(robotsKey, reloadRobotsTxt, new ItemSupplier<RobotsTxtItem>() {
			@Override
			public RobotsTxtItem get() throws IOException, SearchLibException {
				Crawl crawl = new Crawl(null, urlItem, config, parserSelector);
				crawl.download(httpDownloader);
				return new RobotsTxtItem(crawl);
			}
		});
	}

	public RobotsTxtItem[] getRobotsTxtList() {
		return getList();
	}

	private static String getRobotsUrlKey(String pattern) throws MalformedURLException, URISyntaxException {
		pattern = pattern.trim();
		if (pattern.length() == 0)
			return null;
		if (pattern.indexOf(':') == -1)
			pattern = "http://" + pattern;
		return RobotsTxtItem.getRobotsUrl(LinkUtils.newEncodedURL(pattern)).toExternalForm();
	}

	public RobotsTxtItem findRobotsTxt(String pattern) throws MalformedURLException, URISyntaxException {
		return get(getRobotsUrlKey(pattern));
	}

	public static class RobotsTxtParser extends Parser {

		volatile RobotsTxt robotsTxt;

		public RobotsTxtParser() {
			super(null, true);
		}

		@Override
		protected void parseContent(StreamLimiter streamLimiter, LanguageEnum lang)
				throws IOException, SearchLibException {
			robotsTxt = new RobotsTxt(streamLimiter.getNewInputStream(), CharsetUtils.CharsetUTF8);
		}
	}
}
