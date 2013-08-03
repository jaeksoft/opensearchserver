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
import java.util.Properties;

import javax.servlet.ServletContext;

public class Version {

	private String version = null;

	private String stage = null;

	private String revision = null;

	private String build = null;

	public Version(ServletContext servletContext) throws IOException {
		InputStream is = null;
		try {
			is = servletContext.getResourceAsStream("/version");
			if (is == null)
				return;
			Properties properties = new Properties();
			properties.load(is);
			version = properties.getProperty("VERSION");
			stage = properties.getProperty("STAGE");
			revision = properties.getProperty("REVISION");
			build = properties.getProperty("BUILD");
		} finally {
			if (is != null)
				is.close();
		}
	}

	public String getUpdateUrl() throws UnsupportedEncodingException {
		StringBuffer sb = new StringBuffer(
				"http://www.open-search-server.com/updatecheck?check");
		if (version != null) {
			sb.append("&v=");
			sb.append(URLEncoder.encode(version, "UTF-8"));
		}
		if (stage != null) {
			sb.append("&s=");
			sb.append(URLEncoder.encode(stage, "UTF-8"));
		}
		if (revision != null) {
			sb.append("&r=");
			sb.append(URLEncoder.encode(revision, "UTF-8"));
		}
		if (build != null) {
			sb.append("&b=");
			sb.append(URLEncoder.encode(build, "UTF-8"));
		}
		return sb.toString();
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("OpenSearchServer");
		if (version != null) {
			sb.append(" v");
			sb.append(version);
		}
		if (stage != null) {
			sb.append(" - ");
			sb.append(stage);
		}
		if (revision != null) {
			sb.append(" - rev ");
			sb.append(revision);
		}
		if (build != null) {
			sb.append(" - build ");
			sb.append(build);
		}
		return sb.toString();
	}
}
