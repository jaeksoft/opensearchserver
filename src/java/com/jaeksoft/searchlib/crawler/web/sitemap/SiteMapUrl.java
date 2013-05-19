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

package com.jaeksoft.searchlib.crawler.web.sitemap;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.Date;

import org.w3c.dom.Node;

import com.jaeksoft.searchlib.Logging;
import com.jaeksoft.searchlib.util.DomUtils;
import com.jaeksoft.searchlib.util.FormatUtils.ThreadSafeDateFormat;
import com.jaeksoft.searchlib.util.FormatUtils.ThreadSafeSimpleDateFormat;
import com.jaeksoft.searchlib.util.LinkUtils;

public class SiteMapUrl implements Comparable<SiteMapUrl> {

	public enum ChangeFreq {
		always, hourly, daily, weekly, monthly, yearly, never
	};

	private final URI loc;
	private final Date lastMod;
	private final ChangeFreq changeFreq;
	private final Float priority;

	private final static ThreadSafeDateFormat w3cdateFormat = new ThreadSafeSimpleDateFormat(
			"yyyy-MM-dd'T'HH:mm:ssZ");

	public SiteMapUrl(Node urlNode) throws MalformedURLException,
			URISyntaxException {
		Node node = DomUtils.getFirstNode(urlNode, "loc");
		loc = node == null ? null : LinkUtils.newEncodedURI(DomUtils
				.getText(node));
		node = DomUtils.getFirstNode(urlNode, "lastmod");
		Date d = null;
		try {
			d = node == null ? null : w3cdateFormat.parse(DomUtils
					.getText(node));
		} catch (ParseException e) {
			Logging.warn(e);
		}
		lastMod = d;
		node = DomUtils.getFirstNode(urlNode, "changefreq");
		changeFreq = node == null ? null : ChangeFreq.valueOf(DomUtils
				.getText(node));
		node = DomUtils.getFirstNode(urlNode, "priority");
		priority = node == null ? null : new Float(DomUtils.getText(node));
	}

	/**
	 * @return the loc
	 */
	public URI getLoc() {
		return loc;
	}

	/**
	 * @return the lastMod
	 */
	public Date getLastMod() {
		return lastMod;
	}

	/**
	 * @return the changeFreq
	 */
	public ChangeFreq getChangeFreq() {
		return changeFreq;
	}

	/**
	 * @return the priority
	 */
	public float getPriority() {
		return priority;
	}

	@Override
	public int compareTo(SiteMapUrl o) {
		return loc.compareTo(o.loc);
	}
}
