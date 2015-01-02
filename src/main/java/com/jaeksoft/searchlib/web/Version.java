/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2011-2015 Emmanuel Keller / Jaeksoft
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

import org.apache.commons.lang3.StringUtils;

public class Version {

	private final String title;

	private final String version;

	private final String build;

	private final String updateUrl;

	private final String versionString;

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
			updateUrl = toUpdateUrl();
			versionString = toVersionString();
		} finally {
			if (is != null)
				is.close();
		}
	}

	private String toUpdateUrl() throws UnsupportedEncodingException {
		StringBuilder sb = new StringBuilder(
				"http://www.open-search-server.com/updatecheck?check");
		if (!StringUtils.isEmpty(version)) {
			sb.append("&v=");
			sb.append(URLEncoder.encode(version, "UTF-8"));
		}
		if (!StringUtils.isEmpty(build)) {
			sb.append("&b=");
			sb.append(URLEncoder.encode(build, "UTF-8"));
		}
		return sb.toString();
	}

	public String getUpdateUrl() {
		return updateUrl;
	}

	private String toVersionString() {
		StringBuilder sb = new StringBuilder();
		sb.append(StringUtils.isEmpty(title) ? "OpenSearchServer" : title);
		if (!StringUtils.isEmpty(version)) {
			sb.append(" v");
			sb.append(version);
		}
		if (!StringUtils.isEmpty(build)) {
			sb.append(" - build ");
			sb.append(build);
		}
		return sb.toString();
	}

	@Override
	public String toString() {
		return versionString;
	}
}
