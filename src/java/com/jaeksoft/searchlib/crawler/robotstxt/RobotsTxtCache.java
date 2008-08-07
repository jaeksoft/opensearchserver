/**   
 * License Agreement for Jaeksoft SearchLib Community
 *
 * Copyright (C) 2008 Emmanuel Keller / Jaeksoft
 * 
 * http://www.jaeksoft.com
 * 
 * This file is part of Jaeksoft SearchLib Community.
 *
 * Jaeksoft SearchLib Community is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * Jaeksoft SearchLib Community is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Jaeksoft SearchLib Community. 
 *  If not, see <http://www.gnu.org/licenses/>.
 **/

package com.jaeksoft.searchlib.crawler.robotstxt;

import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;

import com.jaeksoft.searchlib.crawler.database.url.UrlItem;
import com.jaeksoft.searchlib.crawler.spider.Crawl;
import com.jaeksoft.searchlib.crawler.spider.ParserSelector;
import com.jaeksoft.searchlib.util.XmlInfo;

public class RobotsTxtCache implements XmlInfo {

	private HashMap<String, RobotsTxt> robotsTxtList;

	private ParserSelector parserSelector;

	public RobotsTxtCache() {
		robotsTxtList = new HashMap<String, RobotsTxt>();
		parserSelector = new ParserSelector(DisallowList.class
				.getCanonicalName());
	}

	/**
	 * Retire les robotsTxt dont la date d'expiration est inf�rieure ou �gale �
	 * t
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
				if (t > e.getValue().getExpiredTime()) {
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
	 * Retourne l'objet RobotsTxt correspondant � l'url pass�e en param�tre.
	 * 
	 * @param userAgent
	 * @param url
	 * @param reloadRobotsTxt
	 * @return
	 * @throws MalformedURLException
	 */
	public RobotsTxt getRobotsTxt(String userAgent, URL url,
			boolean reloadRobotsTxt) throws MalformedURLException {
		UrlItem urlItem = new UrlItem();
		urlItem.setUrl(RobotsTxt.getRobotsUrl(url).toExternalForm());
		String robotsKey = urlItem.getUrl();
		synchronized (robotsTxtList) {
			checkExpiration(System.currentTimeMillis());
			if (reloadRobotsTxt)
				robotsTxtList.remove(robotsKey);
			RobotsTxt robotsTxt = robotsTxtList.get(robotsKey);
			if (robotsTxt == null) {
				Crawl crawl = new Crawl(urlItem, userAgent, parserSelector);
				robotsTxt = new RobotsTxt(crawl);
				robotsTxtList.put(robotsKey, robotsTxt);
			}
			return robotsTxt;
		}
	}

	public void xmlInfo(PrintWriter writer, HashSet<String> classDetail) {
		synchronized (robotsTxtList) {
			int size = (robotsTxtList == null) ? 0 : robotsTxtList.size();
			writer.print("<robotsTxtCache size=\"" + size + "\">");
			checkExpiration(System.currentTimeMillis());
			for (RobotsTxt robotsTxt : robotsTxtList.values())
				robotsTxt.xmlInfo(writer, classDetail);
		}
		writer.println("</robotsTxtCache>");

	}

}
