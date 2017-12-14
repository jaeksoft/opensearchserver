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

import com.jaeksoft.searchlib.util.JSoupUtils;
import org.jsoup.nodes.Comment;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;

import java.util.List;

public class JSoupHtmlNode extends HtmlNodeAbstract<Node> {

	public JSoupHtmlNode(Node node) {
		super(node);
	}

	@Override
	public int countElements() {
		return JSoupUtils.countElements(node);
	}

	@Override
	public String getFirstTextNode(String... path) {
		return JSoupUtils.getFirstTextNode(node, path);
	}

	@Override
	public String getText() {
		TextNode textNode = (TextNode) node;
		return textNode.text();
	}

	@Override
	public void getNodes(List<HtmlNodeAbstract<Node>> nodes, String... path) {
		List<Node> nodeList = JSoupUtils.getNodes(node, path);
		for (Node node : nodeList)
			nodes.add(new JSoupHtmlNode(node));
	}

	@Override
	public String getAttributeText(String name) {
		return JSoupUtils.getAttributeText(node, name);
	}

	@Override
	public boolean isComment() {
		if (node instanceof Comment) {
			return true;
		}
		return false;
	}

	@Override
	public boolean isTextNode() {
		if (node instanceof TextNode) {
			return true;
		}
		return false;
	}

	@Override
	public String getNodeName() {
		return node.nodeName();
	}

	@Override
	public String getAttribute(String name) {
		return JSoupUtils.getAttributeText(node, name);
	}

	@Override
	public List<HtmlNodeAbstract<Node>> getNewChildNodes() {
		final List<HtmlNodeAbstract<Node>> nodes = getNewNodeList();
		final List<Node> nodeList = node.childNodes();
		int l = nodeList.size();
		for (int i = 0; i < l; i++)
			nodes.add(new JSoupHtmlNode(nodeList.get(i)));
		return nodes;
	}

	@Override
	public List<HtmlNodeAbstract<Node>> getAllNodes(String... tags) {
		final List<HtmlNodeAbstract<Node>> nodes = getNewNodeList();
		final List<Node> nodeList = JSoupUtils.getAllNodes(node, tags);
		for (Node node : nodeList)
			nodes.add(new JSoupHtmlNode(node));
		return nodes;
	}

	@Override
	public String generatedSource() {
		return node.outerHtml();
	}

}
