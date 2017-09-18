/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008-2015 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.crawler.file.database;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.regex.Matcher;

import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.Logging;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.config.Config;
import com.jaeksoft.searchlib.crawler.file.process.FileInstanceAbstract;
import com.jaeksoft.searchlib.crawler.file.process.fileInstances.SmbFileInstance.SmbSecurityPermissions;
import com.jaeksoft.searchlib.crawler.file.process.fileInstances.swift.SwiftToken.AuthType;
import com.jaeksoft.searchlib.util.DomUtils;
import com.jaeksoft.searchlib.util.RegExpUtils;
import com.jaeksoft.searchlib.util.StringUtils;
import com.jaeksoft.searchlib.util.XmlWriter;

public class FilePathItem implements Comparable<FilePathItem> {

	private FileInstanceType type;

	private String host;
	private String path;

	/**
	 * For CIFS/SMB
	 */
	private String domain;
	private SmbSecurityPermissions smbSecurityPermissions;
	private String keyTabPath;
	private String krb5IniPath;

	/**
	 * For SWIFT
	 */
	private AuthType swiftAuthType;
	private String swiftTenant;
	private String swiftAuthURL;
	private String swiftContainer;

	private String username;
	private String password;

	private boolean withSub;
	private boolean ignoreHiddenFiles;
	private String exclusionPatterns;
	private transient Matcher[] exclusionMatchers;
	private boolean enabled;
	private int delay;

	private boolean ftpUsePassiveMode;

	public FilePathItem(Config config) throws SearchLibException {
		this.type = FileInstanceType.Local;
		host = null;
		path = null;
		domain = null;
		keyTabPath = null;
		krb5IniPath = null;
		username = null;
		password = null;
		withSub = false;
		enabled = false;
		ignoreHiddenFiles = false;
		delay = 0;
		smbSecurityPermissions = null;
		swiftAuthType = null;
		swiftTenant = null;
		swiftAuthURL = null;
		swiftContainer = null;
		exclusionPatterns = null;
		exclusionMatchers = null;
		ftpUsePassiveMode = true;
	}

	public void copyTo(FilePathItem destFilePath) throws URISyntaxException {
		destFilePath.withSub = withSub;
		destFilePath.ignoreHiddenFiles = ignoreHiddenFiles;
		destFilePath.exclusionPatterns = exclusionPatterns;
		destFilePath.exclusionMatchers = exclusionMatchers;
		destFilePath.type = type;
		destFilePath.host = host;
		destFilePath.path = path;
		destFilePath.domain = domain;
		destFilePath.keyTabPath = keyTabPath;
		destFilePath.krb5IniPath = krb5IniPath;
		destFilePath.smbSecurityPermissions = smbSecurityPermissions;
		destFilePath.username = username;
		destFilePath.password = password;
		destFilePath.enabled = enabled;
		destFilePath.delay = delay;
		destFilePath.swiftAuthType = swiftAuthType;
		destFilePath.swiftTenant = swiftTenant;
		destFilePath.swiftAuthURL = swiftAuthURL;
		destFilePath.swiftContainer = swiftContainer;
		destFilePath.ftpUsePassiveMode = ftpUsePassiveMode;
	}

	/**
	 * Set the type
	 * 
	 * @param type
	 *            the fileinstance type to set
	 */
	public void setType(FileInstanceType type) {
		this.type = type;
	}

	public FileInstanceType getType() {
		return type;
	}

	public boolean isWithSubDir() {
		return withSub;
	}

	public void setWithSubDir(boolean b) {
		this.withSub = b;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean b) {
		this.enabled = b;
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
		if (domain != null)
			if (domain.length() == 0)
				domain = null;
		this.domain = domain;
	}

	/**
	 * @return the krb5IniPath
	 */
	public String getKrb5IniPath() {
		return krb5IniPath;
	}

	/**
	 * @param krb5IniPath
	 *            the krb5IniPath to set
	 */
	public void setKrb5IniPath(String krb5IniPath) {
		if (krb5IniPath != null)
			if (krb5IniPath.length() == 0)
				krb5IniPath = null;
		this.krb5IniPath = krb5IniPath;
	}

	/**
	 * @return the keyTabPath
	 */
	public String getKeyTabPath() {
		return keyTabPath;
	}

	/**
	 * @param keyTabPath
	 *            the keyTabPath to set
	 */
	public void setKeyTabPath(String keyTabPath) {
		if (keyTabPath != null)
			if (keyTabPath.length() == 0)
				keyTabPath = null;
		this.keyTabPath = keyTabPath;
	}

	/**
	 * @return the smbSecurityPermissions
	 */
	public SmbSecurityPermissions getSmbSecurityPermissions() {
		return smbSecurityPermissions;
	}

	/**
	 * @param smbSecurityPermissions
	 *            the smbSecurityPermissions to set
	 */
	public void setSmbSecurityPermissions(SmbSecurityPermissions smbSecurityPermissions) {
		this.smbSecurityPermissions = smbSecurityPermissions;
	}

	/**
	 * @return the path
	 */
	public String getPath() {
		return path;
	}

	/**
	 * @param path
	 *            the path to set
	 */
	public void setPath(String path) {
		this.path = path;
	}

	/**
	 * @return the host
	 */
	public String getHost() {
		return host;
	}

	/**
	 * @param host
	 *            the host to set
	 */
	public void setHost(String host) {
		this.host = host;
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
		if (username != null)
			if (username.length() == 0)
				username = null;
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
		if (password != null)
			if (password.length() == 0)
				password = null;
		this.password = password;
	}

	/**
	 * @return the delay
	 */
	public int getDelay() {
		return delay;
	}

	/**
	 * @param delay
	 *            the delay to set
	 */
	public void setDelay(int delay) {
		this.delay = delay;
	}

	public boolean isGuest() {
		return username == null && domain == null;
	}

	/**
	 * Create a new FilePathItem instance by reading XML
	 * 
         * @param config
	 * @param node
	 *            the node with the parameters
	 * @return a new FilePathItem
	 * @throws SearchLibException
	 *             inherited error
	 * @throws IOException
	 *             inherited error
	 */
	public static FilePathItem fromXml(Config config, Node node) throws SearchLibException, IOException {
		FilePathItem filePathItem = new FilePathItem(config);
		String path = DomUtils.getFirstTextNode(node, "fpath");
		if (path == null)
			path = DomUtils.getText(node);
		filePathItem.setPath(path);
		String type = DomUtils.getAttributeText(node, "type");
		if (type != null)
			filePathItem.setType(FileInstanceType.findByName(type));
		filePathItem.setDomain(DomUtils.getAttributeText(node, "domain"));
		filePathItem.setKeyTabPath(DomUtils.getAttributeText(node, "keyTabPath"));
		filePathItem.setKrb5IniPath(DomUtils.getAttributeText(node, "krb5IniPath"));

		filePathItem.setSmbSecurityPermissions(
				SmbSecurityPermissions.find(DomUtils.getAttributeText(node, "smbSecurityPermissions")));
		filePathItem.setUsername(DomUtils.getAttributeText(node, "username"));
		String password = DomUtils.getAttributeText(node, "password");
		if (password != null)
			filePathItem.setPassword(StringUtils.base64decode(password));
		filePathItem.setHost(DomUtils.getAttributeText(node, "host"));
		String withSubString = DomUtils.getAttributeText(node, "withSub");
		filePathItem.setWithSubDir("yes".equalsIgnoreCase(withSubString));
		String ignoreHiddenFiles = DomUtils.getAttributeText(node, "ignoreHiddenFiles");
		filePathItem.setIgnoreHiddenFiles("yes".equalsIgnoreCase(ignoreHiddenFiles));
		String enabled = DomUtils.getAttributeText(node, "enabled");
		filePathItem.setEnabled("yes".equalsIgnoreCase(enabled));
		String delay = DomUtils.getAttributeText(node, "delay");
		if (delay != null)
			filePathItem.setDelay(Integer.parseInt(delay));
		filePathItem.setSwiftAuthType(AuthType.find(DomUtils.getAttributeText(node, "swiftAuthType")));
		filePathItem.setSwiftTenant(DomUtils.getAttributeText(node, "swiftTenant"));
		filePathItem.setSwiftAuthURL(DomUtils.getAttributeText(node, "swiftAuthURL"));
		filePathItem.setSwiftContainer(DomUtils.getAttributeText(node, "swiftContainer"));
		filePathItem.setExclusionPatterns(DomUtils.getFirstTextNode(node, "exclusionPatterns"));
		filePathItem.setFtpUsePassiveMode(DomUtils.getAttributeBoolean(node, "ftpUsePassiveMode", true));
		return filePathItem;
	}

	/**
	 * Write the FilePathItem in XML format
	 * 
	 * @param xmlWriter
	 *            the writer to use
	 * @param nodeName
	 *            the name of the node to write
	 * @throws SAXException
	 *             inherited error
	 * @throws UnsupportedEncodingException
	 *             inherited error
	 */
	public void writeXml(XmlWriter xmlWriter, String nodeName) throws SAXException, UnsupportedEncodingException {
		xmlWriter.startElement(nodeName, "type", type.getName(), "domain", domain, "krb5IniPath", krb5IniPath,
				"keyTabPath", keyTabPath, "smbSecurityPermissions",
				smbSecurityPermissions != null ? smbSecurityPermissions.name() : null, "username", username, "password",
				password == null ? null : StringUtils.base64encode(password), "host", host, "withSub",
				withSub ? "yes" : "no", "ignoreHiddenFiles", ignoreHiddenFiles ? "yes" : "no", "enabled",
				enabled ? "yes" : "no", "delay", Integer.toString(delay), "swiftAuthType",
				swiftAuthType != null ? swiftAuthType.name() : null, "swiftTenant", swiftTenant, "swiftAuthURL",
				swiftAuthURL, "swiftContainer", swiftContainer, "ftpUsePassiveMode",
				Boolean.toString(ftpUsePassiveMode));
		if (path != null) {
			xmlWriter.startElement("fpath");
			xmlWriter.textNode(path);
			xmlWriter.endElement();
		}
		if (exclusionPatterns != null) {
			xmlWriter.startElement("exclusionPatterns");
			xmlWriter.textNode(exclusionPatterns);
			xmlWriter.endElement();
		}
		xmlWriter.endElement();
	}

	@Override
	public int compareTo(FilePathItem fpi) {
		int c;
		if ((c = StringUtils.compareNullValues(type, fpi.type)) != 0)
			return c;
		if (type != null)
			if ((c = type.compareTo(fpi.type)) != 0)
				return c;
		if ((c = StringUtils.compareNullValues(host, fpi.host)) != 0)
			return c;
		if (host != null)
			if ((c = host.compareTo(fpi.host)) != 0)
				return c;
		if ((c = StringUtils.compareNullValues(domain, fpi.domain)) != 0)
			return c;
		if (domain != null)
			if ((c = domain.compareTo(fpi.domain)) != 0)
				return c;
		if ((c = StringUtils.compareNullValues(krb5IniPath, fpi.krb5IniPath)) != 0)
			return c;
		if ((c = StringUtils.compareNullValues(keyTabPath, fpi.keyTabPath)) != 0)
			return c;
		if ((c = StringUtils.compareNullValues(username, fpi.username)) != 0)
			return c;
		if (username != null)
			if ((c = username.compareTo(fpi.username)) != 0)
				return c;
		if ((c = StringUtils.compareNullValues(path, fpi.path)) != 0)
			return c;
		if (path != null)
			if ((c = path.compareTo(fpi.path)) != 0)
				return c;
		if (swiftTenant != null)
			if ((c = swiftTenant.compareTo(fpi.swiftTenant)) != 0)
				return c;
		if (swiftContainer != null)
			if ((c = swiftContainer.compareTo(fpi.swiftContainer)) != 0)
				return c;
		return 0;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(type.getScheme());
		sb.append("://");
		if (domain != null) {
			sb.append(domain);
			sb.append(';');
		}
		if (username != null) {
			sb.append(username);
			sb.append('@');
		}
		if (host != null) {
			sb.append(host);
		}
		if (swiftContainer != null) {
			if (!swiftContainer.startsWith("/"))
				sb.append('/');
			sb.append(swiftContainer);
		}
		if (path != null) {
			if (!path.startsWith("/"))
				sb.append('/');
			sb.append(path);
		}
		return sb.toString();
	}

	public String check() throws URISyntaxException, IOException {
		if (Logging.isDebug)
			Logging.debug("CHECK " + this.toString());
		return FileInstanceAbstract.create(this, null, path).check();
	}

	/**
	 * @return the swiftAuthType
	 */
	public AuthType getSwiftAuthType() {
		return swiftAuthType;
	}

	/**
	 * @param swiftAuthType
	 *            the swiftAuthType to set
	 */
	public void setSwiftAuthType(AuthType swiftAuthType) {
		this.swiftAuthType = swiftAuthType;
	}

	/**
	 * @return the swiftTenant
	 */
	public String getSwiftTenant() {
		return swiftTenant;
	}

	/**
	 * @param tenant
	 *            the swiftTenant to set
	 */
	public void setSwiftTenant(String tenant) {
		this.swiftTenant = tenant;
	}

	/**
	 * @return the swiftAuthURL
	 */
	public String getSwiftAuthURL() {
		return swiftAuthURL;
	}

	/**
	 * @param swiftAuthURL
	 *            the swiftAuthURL to set
	 */
	public void setSwiftAuthURL(String swiftAuthURL) {
		this.swiftAuthURL = swiftAuthURL;
	}

	/**
	 * @return the ignoreHiddenFiles
	 */
	public boolean isIgnoreHiddenFiles() {
		return ignoreHiddenFiles;
	}

	/**
	 * @param ignoreHiddenFiles
	 *            the ignoreHiddenFiles to set
	 */
	public void setIgnoreHiddenFiles(boolean ignoreHiddenFiles) {
		this.ignoreHiddenFiles = ignoreHiddenFiles;
	}

	/**
	 * @return the swiftContainer
	 */
	public String getSwiftContainer() {
		return swiftContainer;
	}

	/**
	 * @param swiftContainer
	 *            the swiftContainer to set
	 */
	public void setSwiftContainer(String swiftContainer) {
		this.swiftContainer = swiftContainer;
	}

	/**
	 * @return the exclusionPattern
	 */
	public String getExclusionPatterns() {
		return exclusionPatterns;
	}

	/**
	 * @param exclusionPatterns
	 *            the exclusionPattern to set
	 * @throws IOException
	 *             inherited error
	 */
	public void setExclusionPatterns(String exclusionPatterns) throws IOException {
		this.exclusionPatterns = exclusionPatterns;
		exclusionMatchers = RegExpUtils.wildcardMatcherArray(exclusionPatterns);
	}

	public Matcher[] getExclusionMatchers() {
		return exclusionMatchers;
	}

	/**
	 * @return the ftpUsePassiveMode
	 */
	public boolean isFtpUsePassiveMode() {
		return ftpUsePassiveMode;
	}

	/**
	 * @param ftpUsePassiveMode
	 *            the ftpUsePassiveMode to set
	 */
	public void setFtpUsePassiveMode(boolean ftpUsePassiveMode) {
		this.ftpUsePassiveMode = ftpUsePassiveMode;
	}
}
