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

package com.jaeksoft.searchlib.util;

import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;

public class JSoupUtils {

	final private static void getNodes(Node parent, int pos, String[] path,
			List<Node> nodes) {
		if (pos == path.length) {
			nodes.add(parent);
			return;
		}
		List<Node> childrens = parent.childNodes();
		int l = childrens.size();
		int nextPos = pos + 1;
		for (int i = 0; i < l; i++) {
			Node node = childrens.get(i);
			if (node instanceof Element) {
				if (node.nodeName().equals(path[pos]))
					getNodes(node, nextPos, path, nodes);
			}
		}
	}

	final public static void getNodes(List<Node> nodes, Node parent,
			String... path) {
		if (path == null)
			return;
		if (path.length == 0)
			return;
		getNodes(parent, 0, path, nodes);
	}

	final public static String getTextNode(Node parent, String... path) {
		List<Node> nodes = JSoupUtils.getNodes(parent, path);
		if (nodes == null)
			return null;
		if (nodes.size() < 1)
			return null;
		return JSoupUtils.getText(nodes.get(0));
	}

	final public static List<Node> getNodes(Node parent, String... path) {
		if (path == null)
			return null;
		if (path.length == 0)
			return null;
		List<Node> nodes = new ArrayList<Node>();
		getNodes(nodes, parent, path);
		return nodes;
	}

	final private static void getText(Node parent, StringBuffer sb) {
		if (parent instanceof TextNode)
			sb.append(((TextNode) parent).text());
		List<Node> childrens = parent.childNodes();
		int l = childrens.size();
		for (int i = 0; i < l; i++)
			getText(childrens.get(i), sb);
	}

	final public static String getText(Node node) {
		StringBuffer sb = new StringBuffer();
		getText(node, sb);
		return sb.toString();
	}

	final public static int countElements(Node parent) {
		int count = 0;
		List<Node> childNodes = parent.childNodes();
		for (Node childNode : childNodes) {
			if (childNode instanceof Element) {
				count++;
				count += countElements(childNode);
			}
		}
		return count;
	}

	final public static String getAttributeText(Node node, String name) {
		Attributes attributes = node.attributes();
		if (attributes == null)
			return null;
		String attr = attributes.get(name);
		return attr;
	}

	final private static void getAllNodes(Node parent, String[] tags,
			List<Node> nodes) {
		for (String tag : tags) {
			if (parent.nodeName().equals(tag)) {
				nodes.add(parent);
				break;
			}
		}
		List<Node> childrens = parent.childNodes();
		int l = childrens.size();
		for (int i = 0; i < l; i++)
			getAllNodes(childrens.get(i), tags, nodes);

	}

	final public static List<Node> getAllNodes(Node parent, String... tags) {
		List<Node> nodes = new ArrayList<Node>();
		getAllNodes(parent, tags, nodes);
		return nodes;
	}

	final public static String getCleanHtml(String html) {
		return Jsoup.parse(html).text();
	}

}
