/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2011 Emmanuel Keller / Jaeksoft
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
package com.jaeksoft.searchlib.crawler.web.database;

public class LinkItem {

	public enum Origin {
		manual, sitemap, content, redirect, frameset
	}

	final private String url;
	final private String parentUrl;
	final private Origin origin;

	public LinkItem(String url, Origin origin, String parentUrl) {
		this.url = url;
		this.origin = origin;
		this.parentUrl = parentUrl;
	}

	public String getUrl() {
		return url;
	}

	public Origin getOrigin() {
		return origin;
	}

	public String getParentUrl() {
		return parentUrl;
	}

	public static Origin findOrigin(String valueContent) {
		if (valueContent == null)
			return null;
		for (Origin o : Origin.values())
			if (o.name().equals(valueContent))
				return o;
		return null;
	}
}
