/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2008 Emmanuel Keller / Jaeksoft
 * 
 * http://www.jaeksoft.com
 * 
 * This file is part of Jaeksoft OpenSearchServer.
 *
 * Jaeksoft OpenSearchServer is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * Jaeksoft OpenSearchServer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Jaeksoft OpenSearchServer. 
 *  If not, see <http://www.gnu.org/licenses/>.
 **/

package com.jaeksoft.searchlib.crawler.web.spider;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

public class Link {

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

	public static Link getLink(String baseUrl, String href, boolean follow) {
		try {
			String uri = UrlHref.getUrl(baseUrl, href);
			if (uri == null)
				return null;
			URL url = new URL(uri);
			Type type;
			if (url.getHost().equals(new URL(baseUrl).getHost()))
				type = Type.INLINK;
			else
				type = Type.OUTLINK;
			return new Link(type, url, follow);
		} catch (MalformedURLException e) {
			return null;
		} catch (URISyntaxException e) {
			return null;
		}
	}

}
