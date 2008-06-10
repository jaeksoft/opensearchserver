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

import java.net.MalformedURLException;
import java.net.URL;

import com.jaeksoft.searchlib.crawler.filter.PatternUrlItem;

public class InjectUrlItem {

	public enum Status {
		UNDEFINED("Undefined"), INJECTED("Injected"), MALFORMATED(
				"Malformated url"), ALREADY("Already injected"), ERROR(
				"Unknown Error");

		private String name;

		private Status(String name) {
			this.name = name;
		}

		@Override
		public String toString() {
			return name;
		}
	}

	private String badUrl;
	private URL url;
	private Status status;

	private InjectUrlItem() {
		status = Status.UNDEFINED;
		badUrl = null;
	}

	protected InjectUrlItem(PatternUrlItem patternUrl) {
		this();
		try {
			url = patternUrl.extractUrl(true);
		} catch (MalformedURLException e) {
			status = Status.MALFORMATED;
			badUrl = patternUrl.getPattern();
			url = null;
		}
	}

	public InjectUrlItem(String u) {
		this();
		try {
			url = new URL(u);
		} catch (MalformedURLException e) {
			status = Status.MALFORMATED;
			badUrl = u;
			url = null;
		}
	}

	public String getUrl() {
		if (url == null)
			return badUrl;
		return url.toExternalForm();
	}

	public URL getURL() {
		return url;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status v) {
		status = v;
	}

}
