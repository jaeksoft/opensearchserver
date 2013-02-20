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

import org.apache.http.auth.Credentials;
import org.apache.http.auth.NTCredentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.util.DomUtils;
import com.jaeksoft.searchlib.util.StringUtils;
import com.jaeksoft.searchlib.util.XmlWriter;

public class CredentialItem {

	public enum CredentialType {
		BASIC_DIGEST("Basic or Digest"),

		NTLM("NTLMv1, NTLMv2, or NTLM2SessionResponse");

		private final String label;

		private CredentialType(String label) {
			this.label = label;
		}

		private static CredentialType find(String value) {
			for (CredentialType type : values())
				if (type.name().equalsIgnoreCase(value))
					return type;
			return null;
		}

		public String getLabel() {
			return label;
		}
	}

	private CredentialType type;

	private String pattern;

	private String username;

	private String password;

	private String workstation;

	private String domain;

	public CredentialItem() {
		type = null;
		pattern = null;
		username = null;
		password = null;
		workstation = null;
		domain = null;
	}

	public CredentialItem(CredentialType type, String pattern, String username,
			String password, String workstation, String domain) {
		this.type = type;
		this.pattern = pattern;
		this.username = username;
		this.password = password;
		this.workstation = workstation;
		this.domain = domain;
	}

	public static CredentialItem fromXml(Node node) {
		CredentialItem credentialItem = new CredentialItem();
		credentialItem.setPattern(DomUtils.getText(node));
		credentialItem.setUsername(StringUtils.base64decode(DomUtils
				.getAttributeText(node, "username")));
		credentialItem.setPassword(StringUtils.base64decode(DomUtils
				.getAttributeText(node, "password")));
		credentialItem.setWorkstation(StringUtils.base64decode(DomUtils
				.getAttributeText(node, "workstation")));
		credentialItem.setDomain(StringUtils.base64decode(DomUtils
				.getAttributeText(node, "domain")));
		credentialItem.setType(CredentialType.find(DomUtils.getAttributeText(
				node, "type")));
		return credentialItem;
	}

	public void writeXml(XmlWriter xmlWriter)
			throws UnsupportedEncodingException, SAXException {
		xmlWriter.startElement("credential", "username",
				new String(StringUtils.base64encode(username)), "password",
				new String(StringUtils.base64encode(password)), "workstation",
				new String(StringUtils.base64encode(workstation)), "domain",
				new String(StringUtils.base64encode(domain)), "type",
				type.name());
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

	/**
	 * @return the workstation
	 */
	public String getWorkstation() {
		return workstation;
	}

	/**
	 * @param workstation
	 *            the workstation to set
	 */
	public void setWorkstation(String workstation) {
		this.workstation = workstation;
	}

	/**
	 * @return the domain
	 */
	public String getDomain() {
		return domain;
	}

	/**
	 * @param domain
	 *            the domain to set
	 */
	public void setDomain(String domain) {
		this.domain = domain;
	}

	/**
	 * @return the type
	 */
	public CredentialType getType() {
		return type;
	}

	/**
	 * @param type
	 *            the type to set
	 */
	public void setType(CredentialType type) {
		this.type = type;
	}

	public Credentials getHttpCredential() {
		switch (type) {
		case BASIC_DIGEST:
			return new UsernamePasswordCredentials(getUsername(), getPassword());
		case NTLM:
			return new NTCredentials(getUsername(), getPassword(),
					getWorkstation(), getDomain());
		default:
			return null;
		}
	}

	public boolean isNtlm() {
		return type == CredentialType.NTLM;
	}

}
