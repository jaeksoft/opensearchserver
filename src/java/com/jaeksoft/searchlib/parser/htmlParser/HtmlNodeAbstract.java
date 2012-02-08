/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2012 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.parser.htmlParser;

import java.util.ArrayList;
import java.util.List;

public abstract class HtmlNodeAbstract<T> {

	protected T node;

	public HtmlNodeAbstract(T node) {
		this.node = node;
	}

	public abstract int countElements(HtmlNodeAbstract<T> parent);

	public abstract String getTextNode(HtmlNodeAbstract<T> parent,
			String... path);

	public abstract void getNodes(List<HtmlNodeAbstract<T>> nodes,
			HtmlNodeAbstract<T> parent, String... path);

	public abstract String getAttributeText(HtmlNodeAbstract<T> node,
			String name);

	public List<HtmlNodeAbstract<T>> getNodes(HtmlNodeAbstract<T> parent,
			String... path) {
		if (path == null)
			return null;
		if (path.length == 0)
			return null;
		List<HtmlNodeAbstract<T>> nodes = new ArrayList<HtmlNodeAbstract<T>>(0);
		getNodes(nodes, parent, path);
		return nodes;
	}
}
