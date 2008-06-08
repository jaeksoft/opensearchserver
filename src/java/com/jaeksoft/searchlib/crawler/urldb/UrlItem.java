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

package com.jaeksoft.searchlib.crawler.urldb;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Timestamp;

public class UrlItem implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4043010587042224473L;

	private String url;
	private String host;
	private Timestamp when;
	private int retry;
	private UrlStatus status;
	private int count;

	public UrlItem() {
		url = null;
		host = null;
		when = null;
		retry = 0;
		status = null;
		count = 0;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getStatus() {
		if (status == null)
			return 0;
		return status.getValue();
	}

	public void setStatus(int status) {
		this.status = UrlStatus.findByValue(status);
	}

	public String getStatusName() {
		if (status == null)
			return null;
		return status.getName();
	}

	public URL getURL() {
		try {
			return new URL(url);
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return null;
		}
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public Timestamp getWhen() {
		return when;
	}

	public void setWhen(Timestamp ts) {
		when = ts;
	}

	public int getRetry() {
		return retry;
	}

	public void setRetry(int retry) {
		this.retry = retry;
	}

	public String getCount() {
		return Integer.toString(count);
	}

}
