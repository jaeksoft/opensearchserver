/**   
 * License Agreement for Jaeksoft SearchLib Community
 *
 * Copyright (C) 2008 Emmanuel Keller / Jaeksoft
 * 
 * http://www.jaeksoft.com
 * 
 * This file is part of Jaeksoft SearchLib Community.
 *
 * Jaeksoft SearchLib Community is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * Jaeksoft SearchLib Community is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Jaeksoft SearchLib Community. 
 *  If not, see <http://www.gnu.org/licenses/>.
 **/

package com.jaeksoft.searchlib.util;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class XPathParser {

	private XPath xPath;
	private Document document;
	private File currentFile;

	public XPathParser() {
		XPathFactory xPathfactory = XPathFactory.newInstance();
		xPath = xPathfactory.newXPath();
	}

	public XPathParser(File file) throws ParserConfigurationException,
			SAXException, IOException {
		this();
		currentFile = file;
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		setDocument(builder.parse(currentFile.getAbsoluteFile()));
	}

	public XPathParser(Document document) {
		this();
		setDocument(document);
	}

	public File getCurrentFile() {
		return currentFile;
	}

	private void setDocument(Document document) {
		this.document = document;
	}

	public Node getNode(Node parentNode, String query)
			throws XPathExpressionException {
		return (Node) xPath.evaluate(query, parentNode, XPathConstants.NODE);
	}

	public Node getNode(String query) throws XPathExpressionException {
		return getNode(document, query);
	}

	public String getNodeString(Node parentNode, String query)
			throws XPathExpressionException {
		return xPath.evaluate(query, parentNode);
	}

	public String getNodeString(String query) throws XPathExpressionException {
		return getNodeString(document, query);
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
		return getNodeList(document, query);
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
		return n.getNodeValue();
	}

	public static int getAttributeValue(Node node, String attributeName) {
		String value = getAttributeString(node, attributeName);
		if (value == null)
			return 0;
		return Integer.parseInt(value);
	}
}
