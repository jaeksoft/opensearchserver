/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2014 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.renderer;

import java.util.ArrayList;
import java.util.List;

public enum RendererJspEnum {

	SimpleHtml("renderer.jsp"), Boostrap("renderer5.jsp");

	public final String jsp;

	private RendererJspEnum(String jsp) {
		this.jsp = jsp;
	}

	final public static RendererJspEnum find(String name) {
		for (RendererJspEnum e : values())
			if (e.name().equalsIgnoreCase(name))
				return e;
		return SimpleHtml;
	}

	public static List<String> toNameList(List<String> list) {
		if (list == null)
			list = new ArrayList<String>(values().length);
		for (RendererJspEnum e : values())
			list.add(e.name());
		return list;
	}
}
