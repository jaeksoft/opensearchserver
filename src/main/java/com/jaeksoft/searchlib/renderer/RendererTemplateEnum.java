/**
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
 **/

package com.jaeksoft.searchlib.renderer;

import java.util.ArrayList;
import java.util.List;

public enum RendererTemplateEnum {

	SimpleHtml("xhtml"), Bootstrap("bootstrap");

	public final String directory;

	RendererTemplateEnum(String directory) {
		this.directory = directory;
	}

	final public static RendererTemplateEnum find(String name) {
		for (RendererTemplateEnum e : values())
			if (e.name().equalsIgnoreCase(name))
				return e;
		return SimpleHtml;
	}

	public static List<String> nameList;

	static {
		nameList = new ArrayList<>(values().length);
		for (RendererTemplateEnum e : values())
			nameList.add(e.name());
	}

	public static List<String> toNameList(List<String> list) {
		if (list == null)
			return nameList;
		list.addAll(nameList);
		return list;
	}
}
