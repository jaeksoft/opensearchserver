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

package com.jaeksoft.searchlib.crawler.file.database;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;

import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.config.Config;
import com.jaeksoft.searchlib.util.DomUtils;
import com.jaeksoft.searchlib.util.StringUtils;
import com.jaeksoft.searchlib.util.XmlWriter;

public class FilePathItem implements Comparable<FilePathItem> {

	private FileInstanceType type;
	private String host;
	private String path;
	private String domain;

	private String username;
	private String password;
	private boolean withSub;
	private boolean enabled;
	private int delay;

	public FilePathItem(Config config) throws SearchLibException {
		this.type = config.getFileManager().getFileTypeEnum().getFirst();
		host = null;
		path = null;
		domain = null;
		username = null;
		password = null;
		withSub = false;
		enabled = false;
		delay = 0;
	}

	public void copyTo(FilePathItem destFilePath) throws URISyntaxException {
		destFilePath.withSub = withSub;
		destFilePath.type = type;
		destFilePath.host = host;
		destFilePath.path = path;
		destFilePath.domain = domain;
		destFilePath.username = username;
		destFilePath.password = password;
		destFilePath.enabled = enabled;
		destFilePath.delay = delay;
	}

	/**
	 * Set the type
	 * 
	 * @param type
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
		this.domain = domain;
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
	 * @param node
	 * @return
	 * @throws SearchLibException
	 */
	public static FilePathItem fromXml(Config config, Node node)
			throws SearchLibException {
		FilePathItem filePathItem = new FilePathItem(config);
		filePathItem.setPath(DomUtils.getText(node));
		String type = DomUtils.getAttributeText(node, "type");
		if (type != null)
			filePathItem.setType(config.getFileManager().getFileTypeEnum()
					.getValue(type));
		filePathItem.setDomain(DomUtils.getAttributeText(node, "domain"));
		filePathItem.setUsername(DomUtils.getAttributeText(node, "username"));
		String password = DomUtils.getAttributeText(node, "password");
		if (password != null)
			filePathItem.setPassword(StringUtils.base64decode(password));
		filePathItem.setHost(DomUtils.getAttributeText(node, "host"));
		String withSubString = DomUtils.getAttributeText(node, "withSub");
		filePathItem.setWithSubDir("yes".equalsIgnoreCase(withSubString));
		String enabled = DomUtils.getAttributeText(node, "enabled");
		filePathItem.setEnabled("yes".equalsIgnoreCase(enabled));
		String delay = DomUtils.getAttributeText(node, "delay");
		if (delay != null)
			filePathItem.setDelay(Integer.parseInt(delay));
		return filePathItem;
	}

	/**
	 * Write the FilePathItem in XML format
	 * 
	 * @param xmlWriter
	 * @param nodeName
	 * @throws SAXException
	 * @throws UnsupportedEncodingException
	 */
	public void writeXml(XmlWriter xmlWriter, String nodeName)
			throws SAXException, UnsupportedEncodingException {
		xmlWriter.startElement(nodeName, "type", type.getName(), "domain",
				domain, "username", username, "password",
				password == null ? null : StringUtils.base64encode(password),
				"host", host, "withSub", withSub ? "yes" : "no", "enabled",
				enabled ? "yes" : "no", "delay", Integer.toString(delay));
		if (path != null)
			xmlWriter.textNode(path);
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
		return 0;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
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
		if (path != null) {
			if (!path.startsWith("/"))
				sb.append('/');
			sb.append(path);
		}
		return sb.toString();
	}

}
