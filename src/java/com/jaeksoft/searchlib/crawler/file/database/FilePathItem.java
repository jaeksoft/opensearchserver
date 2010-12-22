/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2008-2010 Emmanuel Keller / Jaeksoft
 * 
 * http://www.open-search-server.com
 * 
 * This file is part of Jaeksoft OpenSearchServer.
 *
 * Jaeksoft OpenSearchServer is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * Jaeksoft OpenSearchServer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Jaeksoft OpenSearchServer. 
 *  If not, see <http://www.gnu.org/licenses/>.
 **/

package com.jaeksoft.searchlib.crawler.file.database;

import java.net.URISyntaxException;

import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.util.DomUtils;
import com.jaeksoft.searchlib.util.XmlWriter;
import com.sun.xml.internal.messaging.saaj.util.Base64;

public class FilePathItem implements Comparable<FilePathItem> {

	private FileInstanceEnum type;
	private String host;
	private String path;
	private String username;
	private String password;
	private boolean withSub;

	public FilePathItem() {
		type = FileInstanceEnum.LocalFileInstance;
		host = "";
		path = "";
		username = "";
		password = "";
		withSub = false;
	}

	public void copy(FilePathItem destFilePath) throws URISyntaxException {
		destFilePath.withSub = withSub;
		destFilePath.type = type;
		destFilePath.host = host;
		destFilePath.path = path;
		destFilePath.username = username;
		destFilePath.password = password;
	}

	/**
	 * Set the type
	 * 
	 * @param type
	 */
	public void setType(FileInstanceEnum type) {
		this.type = type;
	}

	public FileInstanceEnum getType() {
		return type;
	}

	public boolean isWithSub() {
		return withSub;
	}

	public void setWithSub(boolean withSub) {
		this.withSub = withSub;
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
	 * Create a new FilePathItem instance by reading XML
	 * 
	 * @param node
	 * @return
	 */
	public static FilePathItem fromXml(Node node) {
		FilePathItem filePathItem = new FilePathItem();
		filePathItem.setPath(DomUtils.getText(node));
		String type = DomUtils.getAttributeText(node, "type");
		if (type != null)
			filePathItem.setType(FileInstanceEnum.valueOf(type));
		filePathItem.setUsername(DomUtils.getAttributeText(node, "username"));
		String password = DomUtils.getAttributeText(node, "password");
		if (password != null)
			filePathItem.setPassword(Base64.base64Decode(password));
		filePathItem.setHost(DomUtils.getAttributeText(node, "host"));
		String withSubString = DomUtils.getAttributeText(node, "withSub");
		filePathItem.setWithSub("yes".equalsIgnoreCase(withSubString));
		return filePathItem;
	}

	/**
	 * Write the FilePathItem in XML format
	 * 
	 * @param xmlWriter
	 * @param nodeName
	 * @throws SAXException
	 */
	public void writeXml(XmlWriter xmlWriter, String nodeName)
			throws SAXException {
		xmlWriter.startElement(nodeName, "type", type.name(), "username",
				username, "password", password, "host", host, "withSub",
				withSub ? "yes" : "no");
		if (path != null)
			xmlWriter.textNode(path);
		xmlWriter.endElement();
	}

	@Override
	public int compareTo(FilePathItem fpi) {
		int c;
		if ((c = type.compareTo(fpi.type)) != 0)
			return c;
		if ((c = host.compareTo(fpi.host)) != 0)
			return c;
		if ((c = username.compareTo(fpi.username)) != 0)
			return c;
		if ((c = path.compareTo(fpi.path)) != 0)
			return c;
		return 0;
	}
}
