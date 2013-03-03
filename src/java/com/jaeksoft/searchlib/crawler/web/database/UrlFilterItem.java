/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2010-2013 Emmanuel Keller / Jaeksoft
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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
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

	private Set<String> hostnames;

	private UrlFilterItem() {
		compiledPattern = null;
		name = null;
		pattern = null;
		hostnames = null;
	}

	public UrlFilterItem(String name, String pattern) {
		this();
		setName(name);
		setPattern(pattern);
	}

	public UrlFilterItem(Node node) {
		this();
		setName(DomUtils.getAttributeText(node, "name"));
		List<Node> nodes = DomUtils.getNodes(node, "pattern");
		if (nodes != null && nodes.size() > 0)
			setPattern(StringEscapeUtils.unescapeXml(DomUtils.getText(nodes
					.get(0))));
		else
			setPattern(StringEscapeUtils.unescapeXml(DomUtils.getText(node)));
		nodes = DomUtils.getNodes(node, "hostname");
		if (nodes != null)
			for (Node n : nodes)
				addHostname(StringEscapeUtils.unescapeXml(DomUtils.getText(n)));
	}

	public void copyTo(UrlFilterItem filter) {
		filter.name = this.name;
		filter.pattern = this.pattern;
		filter.compiledPattern = this.compiledPattern;
		filter.hostnames = this.hostnames == null ? null : new TreeSet<String>(
				this.hostnames);
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

	public Set<String> getHostnameSet() {
		return hostnames;
	}

	public List<String> getHostnameList() {
		if (hostnames == null)
			return null;
		return new ArrayList<String>(hostnames);
	}

	public void addHostname(String hostname) {
		if (hostname == null)
			return;
		if (hostname.length() == 0)
			return;
		if (hostnames == null)
			hostnames = new TreeSet<String>();
		hostnames.add(hostname);
	}

	public void removeHostname(String hostname) {
		if (hostnames == null)
			return;
		hostnames.remove(hostname);
	}

	public boolean isHostnames() {
		if (hostnames == null)
			return false;
		return hostnames.size() > 0;
	}

	public boolean hostnameCheck(String hostname) {
		if (hostnames == null)
			return true;
		return hostnames.contains(hostname);
	}

	public void writeXml(XmlWriter xmlWriter) throws SAXException {
		xmlWriter.startElement("urlFilter", "name", name);
		xmlWriter.startElement("pattern");
		xmlWriter.textNode(pattern);
		xmlWriter.endElement();
		if (hostnames != null) {
			for (String hostname : hostnames) {
				xmlWriter.startElement("hostname");
				xmlWriter.textNode(hostname);
				xmlWriter.endElement();
			}
		}
		xmlWriter.endElement();
	}

	@Override
	public int compareTo(UrlFilterItem o) {
		return this.name.compareTo(o.name);
	}

	public final boolean isReplaceProspero(String hostname, String part) {
		if (compiledPattern == null)
			return false;
		if (part == null)
			return false;
		if (!hostnameCheck(hostname))
			return false;
		return compiledPattern.matcher(part).matches();
	}

	public final void doReplaceQuery(String hostname, String[] queryParts) {
		if (compiledPattern == null)
			return;
		if (queryParts == null)
			return;
		if (!hostnameCheck(hostname))
			return;
		for (int i = 0; i < queryParts.length; i++) {
			String queryPart = queryParts[i];
			if (queryPart != null)
				if (compiledPattern.matcher(queryPart).matches())
					queryParts[i] = null;
		}
	}

}
