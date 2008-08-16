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

package com.jaeksoft.searchlib.crawler.spider;

import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;

import org.apache.commons.lang.StringEscapeUtils;

import com.jaeksoft.searchlib.util.XmlInfo;

public class Link implements XmlInfo {

	public enum Type {
		OUTLINK, INLINK;
	}

	private Type type;
	private URL url;
	private int count;
	private boolean follow;

	private Link(Type type, URL url, boolean follow) {
		this.type = type;
		this.url = url;
		this.count = 1;
		this.follow = follow;
	}

	public void increment() {
		count++;
	}

	public int getCount() {
		return count;
	}

	public Type getType() {
		return type;
	}

	public URL getUrl() {
		return url;
	}

	public boolean getFollow() {
		return follow;
	}

	public void xmlInfo(PrintWriter writer, HashSet<String> classDetail) {
		writer.println("<link count=\"" + count + "\" follow=\"" + follow
				+ "\" href=\""
				+ StringEscapeUtils.escapeXml(url.toExternalForm()) + "\" />");
	}

	public static Link getLink(URL currentURL, String uri, boolean follow,
			boolean allowRefAnchor) {

		// Relative URI starting with slash
		if (uri.startsWith("/"))
			uri = currentURL.getProtocol() + "://" + currentURL.getHost()
					+ (currentURL.getPort() == -1 ? "" : currentURL.getPort())
					+ uri;
		// Relative URI not starting with slash
		else if (!uri.contains("://")) {
			String sUrl = currentURL.toExternalForm();
			// Removing all parameters for current url
			int i = sUrl.indexOf('?');
			if (i != -1)
				sUrl = sUrl.substring(0, i);
			// Searching the last slash
			i = sUrl.lastIndexOf('/');
			if (i != -1)
				uri = sUrl.substring(0, i + 1) + uri;
			else
				uri = sUrl + "/" + uri;
		}

		// Do we have to remove anchor ?
		if (!allowRefAnchor) {
			int p = uri.indexOf('#');
			if (p != -1)
				uri = uri.substring(0, p);
		}

		try {
			URL url = new URL(uri);
			Type type;
			if (url.getHost().equals(currentURL.getHost()))
				type = Type.INLINK;
			else
				type = Type.OUTLINK;
			return new Link(type, url, follow);
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return null;
		}
	}
}
