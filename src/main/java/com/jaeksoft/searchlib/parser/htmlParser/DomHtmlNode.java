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

import com.jaeksoft.searchlib.util.DomUtils;
import com.jaeksoft.searchlib.util.XPathParser;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import java.io.StringWriter;
import java.util.Collection;
import java.util.List;

public class DomHtmlNode extends HtmlNodeAbstract<Node> implements HtmlDocumentProvider.XPath {

	private XPathParser xPathParser = null;

	public DomHtmlNode(Node node) {
		super(node);
	}

	@Override
	final public int countElements() {
		return DomUtils.countElements(node);
	}

	@Override
	public String getFirstTextNode(String... path) {
		return DomUtils.getFirstTextNode(node, path);
	}

	@Override
	public String getText() {
		return DomUtils.getText(node);
	}

	@Override
	public String getAttributeText(String name) {
		return DomUtils.getAttributeText(node, name);
	}

	@Override
	public void getNodes(List<HtmlNodeAbstract<Node>> nodes, String... path) {
		List<Node> nodeList = DomUtils.getNodes(node, path);
		for (Node node : nodeList)
			nodes.add(new DomHtmlNode(node));
	}

	@Override
	public List<HtmlNodeAbstract<Node>> getAllNodes(String... tags) {
		final List<HtmlNodeAbstract<Node>> nodes = getNewNodeList();
		final List<Node> nodeList = DomUtils.getAllNodes(node, tags);
		for (Node node : nodeList)
			nodes.add(new DomHtmlNode(node));
		return nodes;
	}

	@Override
	protected List<HtmlNodeAbstract<Node>> getNewChildNodes() {
		final List<HtmlNodeAbstract<Node>> nodes = getNewNodeList();
		final NodeList nodeList = node.getChildNodes();
		int l = nodeList.getLength();
		for (int i = 0; i < l; i++)
			nodes.add(new DomHtmlNode(nodeList.item(i)));
		return nodes;
	}

	@Override
	public boolean isComment() {
		return node.getNodeType() == Node.COMMENT_NODE;
	}

	@Override
	public boolean isTextNode() {
		return node.getNodeType() == Node.TEXT_NODE;
	}

	@Override
	public String getNodeName() {
		return node.getNodeName();
	}

	@Override
	public String getAttribute(String name) {
		return DomUtils.getAttributeText(node, name);
	}

	@Override
	public void xPath(String xPath, Collection<Object> nodes) throws XPathExpressionException {
		if (xPathParser == null)
			xPathParser = new XPathParser(node);
		final Object obj = xPathParser.evaluate(node, xPath, XPathConstants.NODESET);
		if (obj == null)
			return;
		if (obj instanceof Node) {
			nodes.add(new DomHtmlNode((Node) obj));
		} else if (obj instanceof NodeList) {
			NodeList nodeList = (NodeList) obj;
			int length = nodeList.getLength();
			for (int i = 0; i < length; i++) {
				final Node nodeToExclude = nodeList.item(i);
				nodeToExclude.getParentNode().removeChild(nodeToExclude);
				nodes.add(nodeList.item(i));
			}
		}
	}

	private final static TransformerFactory transformerFactory = TransformerFactory.newInstance();

	@Override
	public String generatedSource() {
		final StringWriter writer = new StringWriter();
		final Transformer transformer;
		try {
			transformer = transformerFactory.newTransformer();
			transformer.transform(new DOMSource(node), new StreamResult(writer));
		} catch (TransformerException e) {
			throw new RuntimeException(e);
		}
		return writer.toString();
	}
}
