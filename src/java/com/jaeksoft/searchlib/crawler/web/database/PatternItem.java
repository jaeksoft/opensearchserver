/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008-2012 Emmanuel Keller / Jaeksoft
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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Pattern;

import com.jaeksoft.searchlib.crawler.common.database.Selector;
import com.jaeksoft.searchlib.util.StringUtils;

public class PatternItem {

	public enum Status {
		UNDEFINED("Undefined"), INJECTED("Injected"), ALREADY(
				"Already injected"), ERROR("Unknown Error");

		private String name;

		private Status(String name) {
			this.name = name;
		}

		@Override
		public String toString() {
			return name;
		}
	}

	private Status status;

	protected String sPattern;

	private Pattern pattern;

	private Selector<PatternItem> patternSelector;

	public PatternItem() {
		status = Status.UNDEFINED;
		sPattern = null;
		pattern = null;
		patternSelector = null;
	}

	public PatternItem(URL url) {
		this();
		setPattern(url.toExternalForm());
	}

	public PatternItem(String sPattern) {
		this();
		setPattern(sPattern);
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status v) {
		status = v;
	}

	public void setSelected(boolean v) {
		if (v)
			patternSelector.addSelection(this);
		else
			patternSelector.removeSelection(this);
	}

	public boolean isSelected() {
		return patternSelector.isSelected(this);
	}

	public boolean match(String sUrl) {
		if (pattern == null)
			return sUrl.equals(sPattern);
		return pattern.matcher(sUrl).matches();
	}

	public void setPattern(String s) {
		sPattern = s.trim();
		pattern = StringUtils.wildcardPattern(s);
	}

	public URL extractUrl(boolean removeWildcard) throws MalformedURLException {
		return new URL(removeWildcard ? sPattern.replace("*", "") : sPattern);
	}

	public String getPattern() {
		return sPattern;
	}

	public void setPatternSelector(Selector<PatternItem> patternSelector) {
		this.patternSelector = patternSelector;
	}

}
