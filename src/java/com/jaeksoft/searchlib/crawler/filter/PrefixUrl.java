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

package com.jaeksoft.searchlib.crawler.filter;

import java.net.MalformedURLException;
import java.net.URL;

public class PrefixUrl {

	protected String sUrl;

	protected int lUrl;

	public PrefixUrl() {
		sUrl = null;
		lUrl = 0;
	}

	protected boolean startsWith(String sUrl, int lUrl) {
		if (lUrl < this.lUrl)
			return false;
		return sUrl.startsWith(this.sUrl);
	}

	public void setUrl(String url) {
		sUrl = url.trim();
		lUrl = sUrl.length();
	}

	public URL normalize() throws MalformedURLException {
		URL url = new URL(sUrl);
		setUrl(url.toExternalForm());
		return url;
	}

	public String getUrl() {
		return sUrl;
	}
}
