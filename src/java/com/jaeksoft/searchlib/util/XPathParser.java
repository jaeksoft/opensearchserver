/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008-2012 Emmanuel Keller / Jaeksoft
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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class XPathParser {

	private XPath xPath;
	private Node rootNode;
	private File currentFile;

	public XPathParser() {
		XPathFactory xPathfactory = XPathFactory.newInstance();
		xPath = xPathfactory.newXPath();
		currentFile = null;
	}

	public XPathParser(File file) throws ParserConfigurationException,
			SAXException, IOException {
		this();
		currentFile = file;
		setRoot(DomUtils.getNewDocumentBuilder(false, true).parse(
				currentFile.getAbsoluteFile()));
	}

	public XPathParser(InputSource inputSource) throws SAXException,
			IOException, ParserConfigurationException {
		this();
		Document document = DomUtils.getNewDocumentBuilder(false, true).parse(
				inputSource);
		document.normalize();
		setRoot(document);
	}

	public XPathParser(InputStream inputStream) throws SAXException,
			IOException, ParserConfigurationException {
		this();
		setRoot(DomUtils.getNewDocumentBuilder(false, true).parse(inputStream));
	}

	public XPathParser(Node rootNode) {
		this();
		setRoot(rootNode);
	}

	public File getCurrentFile() {
		return currentFile;
	}

	private void setRoot(Node rootNode) {
		this.rootNode = rootNode;
	}

	public Object evaluate(Node parentNode, String query)
			throws XPathExpressionException {
		return xPath.evaluate(query, parentNode);
	}

	public Object evaluate(String query) throws XPathExpressionException {
		return evaluate(rootNode, query);
	}

	public Node getNode(Node parentNode, String query)
			throws XPathExpressionException {
		return (Node) xPath.evaluate(query, parentNode, XPathConstants.NODE);
	}

	public Node getNode(String query) throws XPathExpressionException {
		return getNode(rootNode, query);
	}

	public String getNodeString(Node parentNode, String query)
			throws XPathExpressionException {
		return (String) xPath
				.evaluate(query, parentNode, XPathConstants.STRING);
	}

	public String getNodeString(String query) throws XPathExpressionException {
		return getNodeString(rootNode, query);
	}

	public String getNodeString(Node node) throws XPathExpressionException {
		return xPath.evaluate("text()", node);
	}

	public NodeList getNodeList(Node parentNode, String query)
			throws XPathExpressionException {
		return (NodeList) xPath.evaluate(query, parentNode,
				XPathConstants.NODESET);
	}

	public NodeList getNodeList(String query) throws XPathExpressionException {
		return getNodeList(rootNode, query);
	}

	public String getAttributeString(String query, String attributeName)
			throws XPathExpressionException {
		Node node = getNode(query);
		if (node == null)
			return null;
		return getAttributeString(node, attributeName);
	}

	public int getAttributeValue(String query, String attributeName)
			throws XPathExpressionException {
		Node node = getNode(query);
		if (node == null)
			return 0;
		return getAttributeValue(node, attributeName);
	}

	public static String getAttributeString(Node node, String attributeName) {
		NamedNodeMap attr = node.getAttributes();
		if (attr == null)
			return null;
		Node n = attr.getNamedItem(attributeName);
		if (n == null)
			return null;
		return n.getTextContent();
	}

	public static int getAttributeValue(Node node, String attributeName) {
		String value = getAttributeString(node, attributeName);
		if (value == null || value.length() == 0)
			return 0;
		return Integer.parseInt(value);
	}

	public static long getAttributeLong(Node node, String attributeName) {
		String value = getAttributeString(node, attributeName);
		if (value == null || value.length() == 0)
			return 0;
		return Long.parseLong(value);
	}

	public static Float getAttributeFloat(Node node, String attributeName) {
		String value = getAttributeString(node, attributeName);
		if (value == null || value.length() == 0)
			return null;
		return Float.parseFloat(value);
	}

	final public String getSubNodeTextIfAny(Node parentNode, String nodeName)
			throws XPathExpressionException {
		Node node = getNode(parentNode, nodeName);
		if (node == null)
			return null;
		return node.getTextContent();
	}

}
