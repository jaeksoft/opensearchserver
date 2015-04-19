/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2011 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.web;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import javax.servlet.ServletContext;

public class Version {

	private final String title;

	private final String version;

	private final String build;

	public Version(ServletContext servletContext) throws IOException {
		InputStream is = null;
		try {
			is = servletContext.getResourceAsStream("/META-INF/MANIFEST.MF");
			if (is != null) {
				Manifest manifest = new Manifest(is);
				Attributes attributes = manifest.getMainAttributes();
				title = attributes.getValue("Implementation-Title");
				version = attributes.getValue("Implementation-Version");
				build = attributes.getValue("Implementation-Build");
			} else {
				title = null;
				version = null;
				build = null;
			}
		} finally {
			if (is != null)
				is.close();
		}
	}

	public String getUpdateUrl() throws UnsupportedEncodingException {
		StringBuilder sb = new StringBuilder(
				"http://www.open-search-server.com/updatecheck?check");
		if (version != null) {
			sb.append("&v=");
			sb.append(URLEncoder.encode(version, "UTF-8"));
		}
		if (build != null) {
			sb.append("&b=");
			sb.append(URLEncoder.encode(build, "UTF-8"));
		}
		return sb.toString();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(title == null ? "OpenSearchServer" : title);
		if (version != null) {
			sb.append(" v");
			sb.append(version);
		}
		if (build != null) {
			sb.append(" - build ");
			sb.append(build);
		}
		return sb.toString();
	}
}
