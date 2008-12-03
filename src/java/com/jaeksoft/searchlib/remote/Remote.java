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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;

import com.jaeksoft.searchlib.util.XmlInfo;

public class Remote implements XmlInfo {

	private String name;
	private String protocol;
	private String host;
	private int port;
	private String path;

	public Remote(String name, String url) throws MalformedURLException {
		URL u = new URL(url);
		protocol = u.getProtocol();
		host = u.getHost();
		port = u.getPort();
		if (port == -1)
			port = 80;
		path = u.getPath();
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

	public void xmlInfo(PrintWriter writer, HashSet<String> classDetail) {
		writer.println("<remote name=\"" + name + "\" protocol=\"" + protocol
				+ "\" host=\"" + host + "\" port=\"" + port + "\" path=\""
				+ path + "\" />");

	}
}
