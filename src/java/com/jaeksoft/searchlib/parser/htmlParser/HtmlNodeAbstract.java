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

	public abstract int countElements();

	public abstract String getTextNode(String... path);

	public abstract String getNodeValue();

	public abstract void getNodes(List<HtmlNodeAbstract<?>> nodes,
			String... path);

	public abstract String getAttributeText(String name);

	final public List<HtmlNodeAbstract<?>> getNewNodeList() {
		return new ArrayList<HtmlNodeAbstract<?>>(0);
	}

	final public List<HtmlNodeAbstract<?>> getNodes(String... path) {
		if (path == null)
			return null;
		if (path.length == 0)
			return null;
		List<HtmlNodeAbstract<?>> nodes = getNewNodeList();
		getNodes(nodes, path);
		return nodes;
	}

	public abstract boolean isComment();

	public abstract boolean isTextNode();

	public abstract String getNodeName();

	public abstract String getAttribute(String name);

	public abstract List<HtmlNodeAbstract<?>> getChildNodes();

	public abstract List<HtmlNodeAbstract<?>> getAllNodes(String... names);

}
