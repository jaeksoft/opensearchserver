/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2013-2014 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.renderer.field;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;

public enum RendererWidgetType {

	TEXT(null, RendererWidget.class),

	THUMBNAIL(null, RendererWidget.class),

	TOOLS(null, RendererWidget.class),

	URLWRAP(null, RendererWidget.class),

	DATETIME("inputformat=yyyyMMddHHmmssSSS" + StringUtils.LF
			+ "outputformat=yyyy/MM/dd HH:mm:ss", RendererWidgetDatetime.class);

	private final String jspPath;
	private final String defaultProperties;
	private final Class<? extends RendererWidget> widgetClass;

	private RendererWidgetType(String defaultValue,
			Class<? extends RendererWidget> widgetClass) {
		this.jspPath = "widget/" + name().toLowerCase() + ".jsp";
		this.defaultProperties = defaultValue;
		this.widgetClass = widgetClass;
	}

	public static RendererWidgetType find(String name) {
		if (StringUtils.isEmpty(name))
			return TEXT;
		return valueOf(name);
	}

	public final String getJspPath() {
		return jspPath;
	}

	public final String getDefaultProperties() {
		return defaultProperties;
	}

	public RendererWidget newInstance(String properties)
			throws InstantiationException, IllegalAccessException, IOException {
		RendererWidget widget = widgetClass.newInstance();
		widget.init(properties);
		return widget;
	}
}
