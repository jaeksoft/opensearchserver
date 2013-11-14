/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2013 Emmanuel Keller / Jaeksoft
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

import org.apache.http.cookie.ClientCookie;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.google.common.net.InternetDomainName;
import com.jaeksoft.searchlib.util.DomUtils;
import com.jaeksoft.searchlib.util.LinkUtils;
import com.jaeksoft.searchlib.util.StringUtils;
import com.jaeksoft.searchlib.util.XmlWriter;

public class CookieItem {

	private String pattern;

	private String name;

	private String value;

	private BasicClientCookie basicClientCookie;

	public CookieItem() {
		pattern = null;
		name = null;
		value = null;
		basicClientCookie = null;
	}

	public CookieItem(String pattern, String name, String value) {
		this.pattern = pattern;
		this.name = name;
		this.value = value;
	}

	public CookieItem(BasicClientCookie basicClientCookie) {
		this.pattern = null;
		this.name = name;
		this.value = value;
		this.basicClientCookie = basicClientCookie;
	}

	public static CookieItem fromXml(Node node) {
		CookieItem cookieItem = new CookieItem();
		cookieItem.setPattern(DomUtils.getText(node));
		cookieItem.setName(StringUtils.base64decode(DomUtils.getAttributeText(
				node, "name")));
		cookieItem.setValue(StringUtils.base64decode(DomUtils.getAttributeText(
				node, "value")));
		return cookieItem;
	}

	public void writeXml(XmlWriter xmlWriter)
			throws UnsupportedEncodingException, SAXException {
		xmlWriter.startElement("cookie", "name",
				new String(StringUtils.base64encode(name)), "value",
				new String(StringUtils.base64encode(value)));
		xmlWriter.textNode(pattern);
		xmlWriter.endElement();
	}

	public void copyTo(CookieItem cookie) {
		cookie.pattern = this.pattern;
		cookie.name = this.name;
		cookie.value = this.value;
	}

	/**
	 * @return the pattern
	 */
	public String getPattern() {
		return pattern;
	}

	/**
	 * 
	 * @return an URL object
	 * @throws MalformedURLException
	 * @throws URISyntaxException
	 */
	public URL extractUrl() throws MalformedURLException, URISyntaxException {
		return LinkUtils.newEncodedURL(pattern);
	}

	/**
	 * @param pattern
	 *            the pattern to set
	 */
	public void setPattern(String pattern) {
		this.pattern = pattern;
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

	/**
	 * @return the value
	 */
	public String getValue() {
		return value;
	}

	/**
	 * @param value
	 *            the value to set
	 */
	public void setValue(String value) {
		this.value = value;
	}

	public boolean match(String sUrl) {
		return sUrl.startsWith(pattern);
	}

	public BasicClientCookie getCookie() throws MalformedURLException,
			URISyntaxException {
		if (basicClientCookie != null)
			return basicClientCookie;
		basicClientCookie = new BasicClientCookie(name, value);
		basicClientCookie.setVersion(1);
		String domain_attr = "."
				+ InternetDomainName.from(extractUrl().getHost()).name();
		basicClientCookie.setDomain(domain_attr);
		basicClientCookie.setPath("/");
		basicClientCookie.setSecure(true);
		basicClientCookie.setAttribute(ClientCookie.VERSION_ATTR, "1");
		basicClientCookie.setAttribute(ClientCookie.DOMAIN_ATTR, domain_attr);
		return basicClientCookie;
	}

}
