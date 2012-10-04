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
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang.StringEscapeUtils;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class XPathParser {

	private final static XPathFactory xPathfactory = XPathFactory.newInstance();

	private final XPath xPath;
	private final Node rootNode;
	private final File currentFile;

	private XPathParser(File currentFile, Node rootNode) {
		synchronized (xPathfactory) {
			xPath = xPathfactory.newXPath();
		}
		this.currentFile = currentFile;
		this.rootNode = rootNode;
	}

	public XPathParser(File file) throws ParserConfigurationException,
			SAXException, IOException {
		this(file, DomUtils.readXml(new StreamSource(file.getAbsoluteFile()),
				true));
	}

	public XPathParser(InputSource inputSource) throws SAXException,
			IOException, ParserConfigurationException {
		this(null, DomUtils.readXml(inputSource, true));
		Document document = (Document) rootNode;
		document.normalize();
	}

	public XPathParser(InputStream inputStream) throws SAXException,
			IOException, ParserConfigurationException {
		this(null, DomUtils.readXml(new InputSource(inputStream), true));
	}

	public XPathParser(Node rootNode) {
		this(null, rootNode);
	}

	public File getCurrentFile() {
		return currentFile;
	}

	public Object evaluate(Node parentNode, String query)
			throws XPathExpressionException {
		return xPath.evaluate(query, parentNode);
	}

	public Object evaluate(String query) throws XPathExpressionException {
		return evaluate(rootNode, query);
	}

	public final Node getNode(Node parentNode, String query)
			throws XPathExpressionException {
		return (Node) xPath.evaluate(query, parentNode, XPathConstants.NODE);
	}

	public final Node getNode(String query) throws XPathExpressionException {
		return getNode(rootNode, query);
	}

	public final String getNodeString(Node parentNode, String query)
			throws XPathExpressionException {
		return (String) xPath
				.evaluate(query, parentNode, XPathConstants.STRING);
	}

	public final String getNodeString(String query)
			throws XPathExpressionException {
		return getNodeString(rootNode, query);
	}

	public final String getNodeString(Node node, boolean trim)
			throws XPathExpressionException {
		String txt = xPath.evaluate("text()", node);
		if (txt == null)
			return null;
		return trim ? txt.trim() : txt;
	}

	public final NodeList getNodeList(Node parentNode, String query)
			throws XPathExpressionException {
		return (NodeList) xPath.evaluate(query, parentNode,
				XPathConstants.NODESET);
	}

	public final NodeList getNodeList(String query)
			throws XPathExpressionException {
		return getNodeList(rootNode, query);
	}

	public final String getAttributeString(String query, String attributeName)
			throws XPathExpressionException {
		Node node = getNode(query);
		if (node == null)
			return null;
		return getAttributeString(node, attributeName);
	}

	public final int getAttributeValue(String query, String attributeName)
			throws XPathExpressionException {
		Node node = getNode(query);
		if (node == null)
			return 0;
		return getAttributeValue(node, attributeName);
	}

	private final static String getAttributeString(Node node,
			String attributeName, boolean unescapeXml) {
		NamedNodeMap attr = node.getAttributes();
		if (attr == null)
			return null;
		Node n = attr.getNamedItem(attributeName);
		if (n == null)
			return null;
		String t = n.getTextContent();
		if (t == null)
			return null;
		return unescapeXml ? StringEscapeUtils.unescapeXml(t) : t;
	}

	public final static boolean getAttributeStringMatch(Node node,
			String attributeName, String value) {
		if (value == null)
			return false;
		return value.equals(getAttributeString(node, attributeName, true));
	}

	public final static String getAttributeString(Node node,
			String attributeName) {
		return getAttributeString(node, attributeName, true);
	}

	public final static int getAttributeValue(Node node, String attributeName) {
		String value = getAttributeString(node, attributeName, false);
		if (value == null || value.length() == 0)
			return 0;
		return Integer.parseInt(value);
	}

	public final static long getAttributeLong(Node node, String attributeName) {
		String value = getAttributeString(node, attributeName, false);
		if (value == null || value.length() == 0)
			return 0;
		return Long.parseLong(value);
	}

	public final static Float getAttributeFloat(Node node, String attributeName) {
		String value = getAttributeString(node, attributeName, false);
		if (value == null || value.length() == 0)
			return null;
		return Float.parseFloat(value);
	}

	public final static double getAttributeDouble(Node node,
			String attributeName) {
		String value = getAttributeString(node, attributeName, false);
		if (value == null || value.length() == 0)
			return 0;
		return Double.parseDouble(value);
	}

	final public String getSubNodeTextIfAny(Node parentNode, String nodeName,
			boolean trim) throws XPathExpressionException {
		Node node = getNode(parentNode, nodeName);
		if (node == null)
			return null;
		String txt = node.getTextContent();
		if (txt == null)
			return null;
		return trim ? txt.trim() : txt;
	}

}
