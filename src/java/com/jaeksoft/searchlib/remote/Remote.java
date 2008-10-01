/**   
 * License Agreement for Jaeksoft SearchLib Community
 *
 * Copyright (C) 2008 Emmanuel Keller / Jaeksoft
 * 
 * http://www.jaeksoft.com
 * 
 * This file is part of Jaeksoft SearchLib Community.
 *
 * Jaeksoft SearchLib Community is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * Jaeksoft SearchLib Community is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Jaeksoft SearchLib Community. 
 *  If not, see <http://www.gnu.org/licenses/>.
 **/

package com.jaeksoft.searchlib.remote;

import java.io.PrintWriter;
import java.util.HashSet;

import org.w3c.dom.Node;

import com.jaeksoft.searchlib.util.XPathParser;
import com.jaeksoft.searchlib.util.XmlInfo;

public class Remote implements XmlInfo {

	private String name;
	private String protocol;
	private String host;
	private int port;
	private String path;

	public Remote(String name, String protocol, String host, int port,
			String path) {
		this.name = name;
		this.protocol = protocol;
		this.host = host;
		this.port = port;
		this.path = path;
	}

	public String getUrl(String queryString) {
		StringBuffer sb = new StringBuffer();
		sb.append(this.protocol);
		sb.append("://");
		sb.append(this.host);
		sb.append(':');
		sb.append(this.port);
		sb.append('/');
		if (path != null && path.length() != 0) {
			sb.append(path);
			sb.append('/');
		}
		if (queryString != null)
			sb.append(queryString);
		return sb.toString();
	}

	public String getName() {
		return name;
	}

	public static Remote fromXmlConfig(Node node, String remotePathAttribute) {
		String remoteName = XPathParser.getAttributeString(node, "remoteName");
		if (remoteName == null)
			return null;
		String remoteProtocol = XPathParser.getAttributeString(node,
				"remoteProtocol");
		if (remoteProtocol == null)
			remoteProtocol = "http";
		String remoteHost = XPathParser.getAttributeString(node, "remoteHost");
		if (remoteHost == null)
			return null;
		int remotePort = XPathParser.getAttributeValue(node, "remotePort");
		if (remotePort == 0)
			remotePort = 80;
		String remotePath = XPathParser.getAttributeString(node,
				remotePathAttribute);
		if (remotePath == null)
			return null;
		return new Remote(remoteName, remoteProtocol, remoteHost, remotePort,
				remotePath);
	}

	public void xmlInfo(PrintWriter writer, HashSet<String> classDetail) {
		writer.println("<remote name=\"" + name + "\" protocol=\"" + protocol
				+ "\" host=\"" + host + "\" port=\"" + port + "\" path=\""
				+ path + "\" />");

	}
}
