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

import com.jaeksoft.searchlib.util.StringUtils;

import java.io.IOException;

public class RendererWidgetUrlWrap extends RendererWidget {

	private int size;

	@Override
	void init(String properties) throws IOException {
		super.init(properties);
		String p = this.properties != null ? this.properties.getProperty("size", "80") : "80";
		try {
			size = Integer.parseInt(p);
		} catch (NumberFormatException e) {
			size = 80;
		}
	}

	@Override
	public String getValue(String value) {
		if (value == null)
			return null;
		return StringUtils.urlHostPathWrapReduce(value, size);
	}
}
