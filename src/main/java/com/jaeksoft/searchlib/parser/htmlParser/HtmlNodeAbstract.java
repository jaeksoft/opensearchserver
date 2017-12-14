/**
 * License Agreement for OpenSearchServer
 * <p>
 * Copyright (C) 2012-2017 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.parser.htmlParser;

import java.util.ArrayList;
import java.util.List;

public abstract class HtmlNodeAbstract<T> {

	public final T node;

	private List<HtmlNodeAbstract<T>> childNodes;

	public HtmlNodeAbstract(T node) {
		this.node = node;
		childNodes = null;
	}

	public abstract int countElements();

	public abstract String getFirstTextNode(String... path);

	public abstract String getText();

	public abstract void getNodes(List<HtmlNodeAbstract<T>> nodes, String... path);

	public abstract String getAttributeText(String name);

	final public List<HtmlNodeAbstract<T>> getNewNodeList() {
		return new ArrayList<>(0);
	}

	final public List<HtmlNodeAbstract<T>> getNodes(String... path) {
		if (path == null)
			return null;
		if (path.length == 0)
			return null;
		final List<HtmlNodeAbstract<T>> nodes = getNewNodeList();
		getNodes(nodes, path);
		return nodes;
	}

	public abstract boolean isComment();

	public abstract boolean isTextNode();

	public abstract String getNodeName();

	public abstract String getAttribute(String name);

	protected abstract List<HtmlNodeAbstract<T>> getNewChildNodes();

	final public List<HtmlNodeAbstract<T>> getChildNodes() {
		if (childNodes == null)
			childNodes = getNewChildNodes();
		return childNodes;
	}

	public abstract List<HtmlNodeAbstract<T>> getAllNodes(String... names);

	public abstract String generatedSource();
}
