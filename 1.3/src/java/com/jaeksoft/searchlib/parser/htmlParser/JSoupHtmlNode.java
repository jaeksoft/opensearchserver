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

import org.jsoup.nodes.Comment;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;

import com.jaeksoft.searchlib.util.JSoupUtils;

public class JSoupHtmlNode extends HtmlNodeAbstract<Node> {

	public JSoupHtmlNode(Node node) {
		super(node);
	}

	@Override
	public int countElements() {
		return JSoupUtils.countElements(node);
	}

	@Override
	public String getTextNode(String... path) {
		return JSoupUtils.getTextNode(node, path);
	}

	@Override
	public String getNodeValue() {
		TextNode textNode = (TextNode) node;
		return textNode.text();
	}

	@Override
	public void getNodes(List<HtmlNodeAbstract<?>> nodes, String... path) {
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
	public List<HtmlNodeAbstract<?>> getChildNodes() {
		List<HtmlNodeAbstract<?>> nodes = getNewNodeList();
		List<Node> nodeList = node.childNodes();
		int l = nodeList.size();
		for (int i = 0; i < l; i++)
			nodes.add(new JSoupHtmlNode(nodeList.get(i)));
		return nodes;
	}

	@Override
	public List<HtmlNodeAbstract<?>> getAllNodes(String... tags) {
		List<HtmlNodeAbstract<?>> nodes = getNewNodeList();
		List<Node> nodeList = JSoupUtils.getAllNodes(node, tags);
		for (Node node : nodeList)
			nodes.add(new JSoupHtmlNode(node));
		return nodes;
	}
}
