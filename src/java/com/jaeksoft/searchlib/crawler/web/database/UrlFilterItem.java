/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2010-2012 Emmanuel Keller / Jaeksoft
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

import java.util.regex.Pattern;

import org.apache.commons.lang.StringEscapeUtils;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.util.DomUtils;
import com.jaeksoft.searchlib.util.XmlWriter;

public class UrlFilterItem implements Comparable<UrlFilterItem> {

	private transient Pattern compiledPattern;

	private String name;

	private String pattern;

	private UrlFilterItem() {
		compiledPattern = null;
	}

	public UrlFilterItem(String name, String pattern) {
		this();
		setName(name);
		setPattern(pattern);
	}

	public UrlFilterItem(Node node) {
		this();
		setName(DomUtils.getAttributeText(node, "name"));
		setPattern(StringEscapeUtils.unescapeXml(DomUtils.getText(node)));
	}

	public void copyTo(UrlFilterItem filter) {
		filter.name = this.name;
		filter.pattern = this.pattern;
		filter.compiledPattern = this.compiledPattern;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	private void compilePattern() {
		if (pattern == null || pattern.length() == 0) {
			compiledPattern = null;
			return;
		}
		compiledPattern = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
	}

	/**
	 * @return the pattern
	 */
	public String getPattern() {
		return pattern;
	}

	/**
	 * @param pattern
	 *            the pattern to set
	 */
	public void setPattern(String pattern) {
		if (pattern == null)
			this.pattern = null;
		else
			this.pattern = pattern;
		compilePattern();
	}

	public void writeXml(XmlWriter xmlWriter) throws SAXException {
		xmlWriter.startElement("urlFilter", "name", name);
		xmlWriter.textNode(pattern);
		xmlWriter.endElement();
	}

	@Override
	public int compareTo(UrlFilterItem o) {
		return this.name.compareTo(o.name);
	}

	public final boolean isReplaceProspero(String part) {
		if (compiledPattern == null)
			return false;
		if (part == null)
			return false;
		return compiledPattern.matcher(part).matches();
	}

	public final void doReplaceQuery(String[] queryParts) {
		if (compiledPattern == null)
			return;
		if (queryParts == null)
			return;
		for (int i = 0; i < queryParts.length; i++) {
			String queryPart = queryParts[i];
			if (queryPart != null)
				if (compiledPattern.matcher(queryPart).matches())
					queryParts[i] = null;
		}
	}

}
