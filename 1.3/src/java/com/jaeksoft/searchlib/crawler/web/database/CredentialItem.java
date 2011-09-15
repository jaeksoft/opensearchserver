/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2010-2011 Emmanuel Keller / Jaeksoft
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
import java.net.URL;
import java.net.URLEncoder;

import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.util.DomUtils;
import com.jaeksoft.searchlib.util.StringUtils;
import com.jaeksoft.searchlib.util.XmlWriter;

public class CredentialItem {

	private String pattern;

	private String username;

	private String password;

	public CredentialItem() {
		pattern = null;
		username = null;
		password = null;
	}

	public CredentialItem(String pattern, String username, String password) {
		this.pattern = pattern;
		this.username = username;
		this.password = password;
	}

	public static CredentialItem fromXml(Node node) {
		CredentialItem credentialItem = new CredentialItem();
		credentialItem.setPattern(DomUtils.getText(node));
		credentialItem.setUsername(StringUtils.base64decode(DomUtils
				.getAttributeText(node, "username")));
		credentialItem.setPassword(StringUtils.base64decode(DomUtils
				.getAttributeText(node, "password")));
		return credentialItem;
	}

	public void writeXml(XmlWriter xmlWriter)
			throws UnsupportedEncodingException, SAXException {
		xmlWriter.startElement("credential", "username",
				new String(StringUtils.base64encode(username)), "password",
				new String(StringUtils.base64encode(password)));
		xmlWriter.textNode(pattern);
		xmlWriter.endElement();
	}

	public void copy(CredentialItem credential) {
		credential.pattern = this.pattern;
		credential.username = this.username;
		credential.password = this.password;
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
	 */
	public URL extractUrl() throws MalformedURLException {
		return new URL(pattern);
	}

	/**
	 * @param pattern
	 *            the pattern to set
	 */
	public void setPattern(String pattern) {
		this.pattern = pattern;
	}

	/**
	 * @return the username
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * @param username
	 *            the username to set
	 */
	public void setUsername(String username) {
		this.username = username;
	}

	/**
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * @param password
	 *            the password to set
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	public boolean match(String sUrl) {
		return sUrl.startsWith(pattern);
	}

	public String getURLUserInfo() throws UnsupportedEncodingException {
		StringBuffer sb = new StringBuffer();
		sb.append(URLEncoder.encode(username, "UTF-8"));
		sb.append(':');
		sb.append(URLEncoder.encode(password, "UTF-8"));
		return sb.toString();
	}

}
