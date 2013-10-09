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

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.net.UnknownHostException;

import jcifs.UniAddress;
import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbException;
import jcifs.smb.SmbSession;

import org.apache.http.auth.AuthSchemeRegistry;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.NTCredentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.params.HttpParams;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.util.DomUtils;
import com.jaeksoft.searchlib.util.LinkUtils;
import com.jaeksoft.searchlib.util.StringUtils;
import com.jaeksoft.searchlib.util.XmlWriter;
import com.jaeksoft.searchlib.util.cifs.NTLMSchemeFactory;
import com.jaeksoft.searchlib.utils.Variables;

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
			return BASIC_DIGEST;
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
		type = CredentialType.BASIC_DIGEST;
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
				StringUtils.base64encode(username), "password",
				StringUtils.base64encode(password), "workstation",
				StringUtils.base64encode(workstation), "domain",
				StringUtils.base64encode(domain), "type", type.name());
		xmlWriter.textNode(pattern);
		xmlWriter.endElement();
	}

	public void copyTo(CredentialItem credential) {
		credential.pattern = this.pattern;
		credential.username = this.username;
		credential.password = this.password;
		credential.domain = this.domain;
		credential.workstation = this.workstation;
		credential.type = this.type;
	}

	public void apply(Variables variables) {
		if (variables == null)
			return;
		pattern = variables.replace(pattern);
		username = variables.replace(username);
		password = variables.replace(password);
		domain = variables.replace(domain);
		workstation = variables.replace(workstation);
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

	public void setUpCredentials(HttpParams params,
			AuthSchemeRegistry authSchemesRegistry,
			CredentialsProvider credentialProvider) {
		if (StringUtils.isEmpty(username))
			return;
		Credentials credentials = null;
		switch (type) {
		case BASIC_DIGEST:
			credentials = new UsernamePasswordCredentials(getUsername(),
					getPassword());
			break;
		case NTLM:
			authSchemesRegistry.register("ntlm", new NTLMSchemeFactory());
			credentials = new NTCredentials(getUsername(), getPassword(),
					getWorkstation(), getDomain());
			break;
		}
		if (credentials != null)
			credentialProvider.setCredentials(AuthScope.ANY, credentials);
	}

	public boolean isNtlm() {
		return type == CredentialType.NTLM;
	}

	public void checkAuth(String serverHostname) throws UnknownHostException,
			SmbException, SearchLibException {
		switch (type) {
		case NTLM:
			UniAddress uniaddress = UniAddress.getByName(serverHostname);
			NtlmPasswordAuthentication ntlmpasswordauthentication = new NtlmPasswordAuthentication(
					domain, username, password);
			SmbSession.logon(uniaddress, ntlmpasswordauthentication);
			break;
		default:
			throw new SearchLibException("Not yet implemented");
		}
	}

}
