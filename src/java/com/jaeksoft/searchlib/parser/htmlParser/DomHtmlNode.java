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

import java.util.List;

import org.w3c.dom.Node;

import com.jaeksoft.searchlib.util.DomUtils;

public class DomHtmlNode extends HtmlNodeAbstract<Node> {

	public DomHtmlNode(Node node) {
		super(node);
	}

	@Override
	final public int countElements(HtmlNodeAbstract<Node> parent) {
		return DomUtils.countElements(parent.node);
	}

	@Override
	public String getTextNode(HtmlNodeAbstract<Node> parent, String... path) {
		return DomUtils.getTextNode(parent.node, path);
	}

	@Override
	public String getAttributeText(HtmlNodeAbstract<Node> node, String name) {
		return DomUtils.getAttributeText(node.node, name);
	}

	@Override
	public void getNodes(List<HtmlNodeAbstract<Node>> nodes,
			HtmlNodeAbstract<Node> parent, String... path) {
		List<Node> nodeList = DomUtils.getNodes(parent.node, path);
		for (Node node : nodeList)
			nodes.add(new DomHtmlNode(node));
	}

}
