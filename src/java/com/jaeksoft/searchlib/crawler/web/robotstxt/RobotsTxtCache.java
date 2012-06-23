/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008-2012 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.crawler.web.robotstxt;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.config.Config;
import com.jaeksoft.searchlib.crawler.web.database.UrlItem;
import com.jaeksoft.searchlib.crawler.web.spider.Crawl;
import com.jaeksoft.searchlib.crawler.web.spider.HttpDownloader;
import com.jaeksoft.searchlib.parser.ParserFactory;
import com.jaeksoft.searchlib.parser.ParserSelector;

public class RobotsTxtCache {

	private Map<String, RobotsTxt> robotsTxtList;

	private ParserSelector parserSelector;

	public RobotsTxtCache() throws SearchLibException {
		robotsTxtList = new TreeMap<String, RobotsTxt>();
		parserSelector = new ParserSelector(ParserFactory.create(null,
				"RobotsTxt parser", DisallowList.class.getCanonicalName()));
	}

	/**
	 * Remove the expired robotsTxt items (relative to the t parameter)
	 * 
	 * @param t
	 */
	private void checkExpiration(long t) {
		synchronized (robotsTxtList) {
			Iterator<Entry<String, RobotsTxt>> it = robotsTxtList.entrySet()
					.iterator();
			ArrayList<String> keyToRemove = null;
			while (it.hasNext()) {
				Entry<String, RobotsTxt> e = it.next();
				if (t > e.getValue().getExpirationTime()) {
					if (keyToRemove == null)
						keyToRemove = new ArrayList<String>();
					keyToRemove.add(e.getKey());
				}
			}
			if (keyToRemove != null)
				for (String key : keyToRemove)
					robotsTxtList.remove(key);
		}
	}

	/**
	 * Return the RobotsTxt object related to the URL.
	 * 
	 * @param userAgent
	 * @param url
	 * @param reloadRobotsTxt
	 * @return
	 * @throws MalformedURLException
	 * @throws SearchLibException
	 */
	public RobotsTxt getRobotsTxt(HttpDownloader httpDownloader, Config config,
			URL url, boolean reloadRobotsTxt) throws MalformedURLException,
			SearchLibException {
		UrlItem urlItem = config.getUrlManager().getNewUrlItem();
		urlItem.setUrl(RobotsTxt.getRobotsUrl(url).toExternalForm());
		String robotsKey = urlItem.getUrl();
		synchronized (robotsTxtList) {
			checkExpiration(System.currentTimeMillis());
			if (reloadRobotsTxt)
				robotsTxtList.remove(robotsKey);
			RobotsTxt robotsTxt = robotsTxtList.get(robotsKey);
			if (robotsTxt != null)
				return robotsTxt;
		}
		Crawl crawl = new Crawl(null, urlItem, config, parserSelector);
		crawl.download(httpDownloader);
		synchronized (robotsTxtList) {
			RobotsTxt robotsTxt = new RobotsTxt(crawl);
			robotsTxtList.remove(robotsKey);
			if (robotsTxt.isCacheable())
				robotsTxtList.put(robotsKey, robotsTxt);
			return robotsTxt;
		}
	}

	public RobotsTxt[] getRobotsTxtList() {
		synchronized (robotsTxtList) {
			RobotsTxt[] array = new RobotsTxt[robotsTxtList.size()];
			robotsTxtList.values().toArray(array);
			return array;
		}
	}

	private static String getRobotsUrlKey(String pattern)
			throws MalformedURLException {
		pattern = pattern.trim();
		if (pattern.length() == 0)
			return null;
		if (pattern.indexOf(':') == -1)
			pattern = "http://" + pattern;
		return RobotsTxt.getRobotsUrl(new URL(pattern)).toExternalForm();
	}

	public RobotsTxt findRobotsTxt(String pattern) throws MalformedURLException {
		synchronized (robotsTxtList) {
			return robotsTxtList.get(getRobotsUrlKey(pattern));
		}
	}
}
