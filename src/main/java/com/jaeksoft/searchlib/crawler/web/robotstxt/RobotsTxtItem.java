/*
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
 */
package com.jaeksoft.searchlib.crawler.web.robotstxt;

import com.jaeksoft.searchlib.crawler.web.GenericCache;
import com.jaeksoft.searchlib.crawler.web.database.RobotsTxtStatus;
import com.jaeksoft.searchlib.crawler.web.database.UrlItem;
import com.jaeksoft.searchlib.crawler.web.spider.Crawl;
import com.jaeksoft.searchlib.util.LinkUtils;
import com.qwazr.crawler.web.robotstxt.RobotsTxtClauseSet;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Date;
import java.util.Map;

public class RobotsTxtItem implements GenericCache.Expirable {

	private final long crawlTime;

	private final long expirationTime;

	private final RobotsTxtCache.RobotsTxtParser robotsTxtParser;

	private final Crawl crawl;

	protected RobotsTxtItem(Crawl crawl) {
		this.crawlTime = System.currentTimeMillis();
		this.expirationTime = this.crawlTime + 1000 * 60 * 60 * 24;
		this.robotsTxtParser = (RobotsTxtCache.RobotsTxtParser) crawl.getParser();
		this.crawl = crawl;
	}

	/**
	 * Construit l'URL d'accès au fichier robots.txt à partir d'une URL donnée
	 *
	 * @param url
	 * @return
	 * @throws MalformedURLException
	 * @throws URISyntaxException
	 */
	protected static URL getRobotsUrl(URL url) throws MalformedURLException, URISyntaxException {
		StringBuilder sb = new StringBuilder();
		sb.append(url.getProtocol());
		sb.append("://");
		sb.append(url.getHost());
		if (url.getPort() != -1) {
			sb.append(':');
			sb.append(url.getPort());
		}
		sb.append("/robots.txt");
		return LinkUtils.newEncodedURL(sb.toString());
	}

	/**
	 * Return the status of the specified URL
	 *
	 * @param userAgent
	 * @param urlItem
	 * @return
	 * @throws MalformedURLException
	 * @throws URISyntaxException
	 */
	public RobotsTxtStatus getStatus(String userAgent, UrlItem urlItem)
			throws MalformedURLException, URISyntaxException {
		final Integer code = crawl.getUrlItem().getResponseCode();
		if (code == null)
			return RobotsTxtStatus.ERROR;
		final URL url = urlItem.getURL();
		if (url == null)
			throw new MalformedURLException("Malformed URL: " + urlItem.getUrl());
		switch (code) {
		case 400:
		case 404:
			return RobotsTxtStatus.NO_ROBOTSTXT;
		case 200:
			break;
		default:
			return RobotsTxtStatus.ERROR;
		}
		if (robotsTxtParser == null || robotsTxtParser.robotsTxt == null)
			return RobotsTxtStatus.ALLOW;
		switch (robotsTxtParser.robotsTxt.getStatus(url.toURI(), userAgent)) {
		case ERROR:
			return RobotsTxtStatus.ERROR;
		case NO_ROBOTSTXT:
			return RobotsTxtStatus.NO_ROBOTSTXT;
		case ALLOW:
			return RobotsTxtStatus.ALLOW;
		case DISALLOW:
			return RobotsTxtStatus.DISALLOW;
		}
		return RobotsTxtStatus.DISALLOW;
	}

	/**
	 * Retourne la date d'expiration. Lorsque la date est expirée, le robots.txt
	 * est à nouveau téléchargé.
	 *
	 * @return
	 */
	public long getExpirationTime() {
		return expirationTime;
	}

	public Date getCrawlDate() {
		return new Date(crawlTime);
	}

	public Date getExpirationDate() {
		return new Date(expirationTime);
	}

	public Crawl getCrawl() {
		return crawl;
	}

	public String getHostname() {
		if (crawl == null)
			return null;
		UrlItem urlItem = crawl.getUrlItem();
		if (urlItem == null)
			return null;
		return urlItem.getHost();
	}

	public Map<String, RobotsTxtClauseSet> getClauses() {
		return robotsTxtParser == null ? null : robotsTxtParser.robotsTxt.getClausesMap();
	}

	@Override
	public boolean isCacheable() {
		if (crawl == null)
			return false;
		UrlItem urlItem = crawl.getUrlItem();
		if (urlItem == null)
			return false;
		Integer code = urlItem.getResponseCode();
		if (code == null)
			return false;
		switch (code) {
		case 200:
			return true;
		case 400:
		case 404:
			return true;
		default:
			return false;
		}
	}

}
