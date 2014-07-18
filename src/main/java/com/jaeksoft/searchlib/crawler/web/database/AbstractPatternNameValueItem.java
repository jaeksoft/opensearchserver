/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2014 Emmanuel Keller / Jaeksoft
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

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.util.DomUtils;
import com.jaeksoft.searchlib.util.LinkUtils;
import com.jaeksoft.searchlib.util.StringUtils;
import com.jaeksoft.searchlib.util.XmlWriter;

public abstract class AbstractPatternNameValueItem {

	protected String pattern;

	protected String name;

	protected String value;

	public AbstractPatternNameValueItem() {
		pattern = null;
		name = null;
		value = null;
	}

	public AbstractPatternNameValueItem(String pattern, String name,
			String value) {
		this.pattern = pattern;
		this.name = name;
		this.value = value;
	}

	public AbstractPatternNameValueItem(Node node) {
		setPattern(DomUtils.getText(node));
		setName(StringUtils.base64decode(DomUtils
				.getAttributeText(node, "name")));
		setValue(StringUtils.base64decode(DomUtils.getAttributeText(node,
				"value")));
	}

	protected void writeXml(String nodeName, XmlWriter xmlWriter)
			throws UnsupportedEncodingException, SAXException {
		xmlWriter.startElement(nodeName, "name",
				new String(StringUtils.base64encode(name)), "value",
				new String(StringUtils.base64encode(value)));
		if (pattern != null)
			xmlWriter.textNode(pattern);
		xmlWriter.endElement();
	}

	abstract void writeXml(XmlWriter xmlWriter)
			throws UnsupportedEncodingException, SAXException;

	public void copyTo(AbstractPatternNameValueItem item) {
		item.pattern = this.pattern;
		item.name = this.name;
		item.value = this.value;
		item.changeEvent();
	}

	/**
	 * @return the pattern
	 */
	final public String getPattern() {
		return pattern;
	}

	/**
	 * 
	 * @return an URL object
	 * @throws MalformedURLException
	 * @throws URISyntaxException
	 */
	final public URL extractUrl() throws MalformedURLException,
			URISyntaxException {
		return LinkUtils.newEncodedURL(pattern);
	}

	/**
	 * @param pattern
	 *            the pattern to set
	 */
	final public void setPattern(String pattern) {
		this.pattern = pattern;
		changeEvent();
	}

	/**
	 * @return the name
	 */
	final public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	final public void setName(String name) {
		this.name = name;
		changeEvent();
	}

	/**
	 * @return the value
	 */
	final public String getValue() {
		return value;
	}

	/**
	 * @param value
	 *            the value to set
	 */
	final public void setValue(String value) {
		this.value = value;
		changeEvent();
	}

	final public boolean match(String sUrl) {
		return sUrl.startsWith(pattern);
	}

	protected abstract void changeEvent();

}
