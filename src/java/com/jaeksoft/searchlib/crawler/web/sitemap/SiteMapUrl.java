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
import java.util.regex.Pattern;

import org.w3c.dom.Node;

import com.jaeksoft.searchlib.Logging;
import com.jaeksoft.searchlib.util.DomUtils;
import com.jaeksoft.searchlib.util.FormatUtils.ThreadSafeDateFormat;
import com.jaeksoft.searchlib.util.FormatUtils.ThreadSafeSimpleDateFormat;
import com.jaeksoft.searchlib.util.LinkUtils;
import com.jaeksoft.searchlib.util.RegExpUtils;

public class SiteMapUrl implements Comparable<SiteMapUrl> {

	public enum ChangeFreq {
		always(0),

		hourly(60 * 60),

		daily(60 * 60 * 24),

		weekly(60 * 60 * 24 * 7),

		monthly(60 * 60 * 24 * 30),

		yearly(60 * 60 * 24 * 365),

		never(Long.MAX_VALUE),

		unknown(Long.MAX_VALUE);

		private final long maxDistanceMs;

		private ChangeFreq(long sec) {
			this.maxDistanceMs = sec * 1000;
		}

		public boolean needUpdate(long timeDistanceMs) {
			return timeDistanceMs > maxDistanceMs;
		}

		public static ChangeFreq find(String text) {
			if (text == null)
				return unknown;
			for (ChangeFreq changeFreq : values())
				if (text.equalsIgnoreCase(changeFreq.name()))
					return changeFreq;
			return unknown;
		}
	};

	private final URI loc;
	private final Date lastMod;
	private final ChangeFreq changeFreq;
	private final Float priority;

	private final static ThreadSafeDateFormat w3cdateFormat = new ThreadSafeSimpleDateFormat(
			"yyyy-MM-dd'T'HH:mm:ssZ");

	private final static Pattern w3cTimeZonePattern = Pattern
			.compile(":(?=[0-9]{2}$)");

	public SiteMapUrl(Node urlNode) throws MalformedURLException,
			URISyntaxException {
		Node node = DomUtils.getFirstNode(urlNode, "loc");
		loc = node == null ? null : LinkUtils.newEncodedURI(DomUtils
				.getText(node));
		node = DomUtils.getFirstNode(urlNode, "lastmod");
		Date d = null;
		try {
			if (node != null) {
				String t = RegExpUtils.replaceFirst(DomUtils.getText(node),
						w3cTimeZonePattern, "");
				if (t != null)
					d = w3cdateFormat.parse(t);
			}
		} catch (ParseException e) {
			Logging.warn(e);
		}
		lastMod = d;
		node = DomUtils.getFirstNode(urlNode, "changefreq");
		changeFreq = ChangeFreq.find(DomUtils.getText(node));
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
