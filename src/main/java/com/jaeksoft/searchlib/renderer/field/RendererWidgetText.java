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

import java.io.IOException;

public class RendererWidgetText extends RendererWidget {

	private String placeHolder;

	@Override
	void init(String properties) throws IOException {
		super.init(properties);
		placeHolder = this.properties != null ? this.properties.getProperty("placeholder") : null;
	}

	@Override
	public String getValue(String value) {
		return value == null || value.isEmpty() ? placeHolder : value;
	}
}
