/*
 * License Agreement for OpenSearchServer
 * <p>
 * Copyright (C) 2014-2017 Emmanuel Keller / Jaeksoft
 * <p>
 * http://www.open-search-server.com
 * <p>
 * This file is part of OpenSearchServer.
 * <p>
 * OpenSearchServer is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * OpenSearchServer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with OpenSearchServer.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package com.jaeksoft.searchlib.renderer.field;

import com.jaeksoft.searchlib.util.IOUtils;
import com.jaeksoft.searchlib.util.StringUtils;

import java.io.IOException;
import java.io.StringReader;
import java.util.Properties;

public class RendererWidget {

	protected Properties properties = null;

	public final static Properties loadProperties(final String properties) throws IOException {
		Properties props = new Properties();
		if (properties == null)
			return props;
		StringReader sr = new StringReader(properties);
		try {
			props.load(sr);
		} finally {
			IOUtils.close(sr);
		}
		return props;
	}

	void init(String properties) throws IOException {
		if (StringUtils.isEmpty(properties))
			return;
		this.properties = loadProperties(properties);
	}

	final public String getProperty(String propertyName) {
		if (properties == null)
			return null;
		return properties.getProperty(propertyName);
	}

	public String getValue(String value) {
		return value;
	}

}
