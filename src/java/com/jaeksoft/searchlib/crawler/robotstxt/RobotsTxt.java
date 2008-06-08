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
import java.text.DateFormat;
import java.util.HashSet;

import com.jaeksoft.searchlib.crawler.spider.Crawl;
import com.jaeksoft.searchlib.util.XmlInfo;

public class RobotsTxt implements XmlInfo {

	private long expiredTime;

	private DisallowList disallowList;

	private Crawl crawl;

	protected RobotsTxt(Crawl crawl) {
		this.expiredTime = System.currentTimeMillis() + 1000 * 60 * 60 * 24;
		this.disallowList = (DisallowList) crawl.getParser();
		this.crawl = crawl;
	}

	/**
	 * Construit l'URL d'acc�s au fichier robots.txt � partir d'une URL donn�e
	 * 
	 * @param url
	 * @return
	 * @throws MalformedURLException
	 */
	protected static URL getRobotsUrl(URL url) throws MalformedURLException {
		String sUrl = url.getProtocol() + "://" + url.getHost() + ":"
				+ (url.getPort() == -1 ? url.getDefaultPort() : url.getPort())
				+ "/robots.txt";
		return new URL(sUrl);
	}

	/**
	 * Renvoie false si le robots.txt ne permet pas le crawl de l'url pass�e en
	 * param�tre pour le userAgent indiqu�
	 * 
	 * @param url
	 * @param userAgent
	 * @return
	 */
	public boolean isAllowed(URL url, String userAgent) {
		if (crawl.getHttpResponseCode() == 0)
			return false;
		if (disallowList == null)
			return true;
		DisallowSet disallowSet = disallowList.get(userAgent.toLowerCase());
		if (disallowSet == null)
			disallowSet = disallowList.get("*");
		if (disallowSet == null)
			return true;
		return disallowSet.isAllowed(url);
	}

	/**
	 * Retourne la date d'expiration. Lorsque la date est expir�e, le robots.txt
	 * est � nouveau t�l�charg�.
	 * 
	 * @return
	 */
	protected long getExpiredTime() {
		return this.expiredTime;
	}

	public Crawl getCrawl() {
		return crawl;
	}

	public void xmlInfo(PrintWriter writer, HashSet<String> classDetail) {
		String cl = getClass().getCanonicalName();
		long size = (disallowList != null) ? disallowList.size() : 0;
		writer.println("<robotsTxt url=\"" + crawl.getUrl()
				+ "\" httpResponseCode=\"" + crawl.getHttpResponseCode()
				+ "\" expiration=\""
				+ DateFormat.getInstance().format(expiredTime)
				+ "\" userAgentCount=\"" + size + "\">");
		if (classDetail.contains("full") || classDetail.contains(cl))
			if (disallowList != null)
				disallowList.xmlInfo(writer, classDetail);
		writer.println("</robotsTxt>");

	}
}
