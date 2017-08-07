/*
 * License Agreement for OpenSearchServer
 * <p>
 * Copyright (C) 2013-2017 Emmanuel Keller / Jaeksoft
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

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;

public enum RendererWidgetType {

	TEXT("placeholder=No content", RendererWidgetText.class),

	THUMBNAIL(null, RendererWidget.class),

	TOOLS(null, RendererWidget.class),

	URLWRAP("size=80", RendererWidgetUrlWrap.class),

	DATETIME("inputformat=yyyyMMddHHmmssSSS" + StringUtils.LF + "outputformat=yyyy/MM/dd HH:mm:ss",
			RendererWidgetDatetime.class);

	private final String templatePath;
	private final String defaultProperties;
	private final Class<? extends RendererWidget> widgetClass;

	RendererWidgetType(String defaultValue, Class<? extends RendererWidget> widgetClass) {
		this.templatePath = "widget/" + name().toLowerCase() + ".ftl";
		this.defaultProperties = defaultValue;
		this.widgetClass = widgetClass;
	}

	public static RendererWidgetType find(String name) {
		if (StringUtils.isEmpty(name))
			return TEXT;
		return valueOf(name);
	}

	public final String getTemplatePath() {
		return templatePath;
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
