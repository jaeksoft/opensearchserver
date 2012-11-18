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
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class DomUtils {

	final private static void getNodes(Node parent, int pos, String[] path,
			List<Node> nodes) {
		if (pos == path.length) {
			nodes.add(parent);
			return;
		}
		NodeList childrens = parent.getChildNodes();
		int l = childrens.getLength();
		int nextPos = pos + 1;
		for (int i = 0; i < l; i++) {
			Node node = childrens.item(i);
			switch (node.getNodeType()) {
			case Node.ELEMENT_NODE:
				if (node.getNodeName().equals(path[pos]))
					getNodes(node, nextPos, path, nodes);
				break;
			default:
				continue;
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
		List<Node> nodes = DomUtils.getNodes(parent, path);
		if (nodes == null)
			return null;
		if (nodes.size() < 1)
			return null;
		return DomUtils.getText(nodes.get(0));
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
		if (parent.getNodeType() == Node.TEXT_NODE)
			sb.append(parent.getNodeValue());
		NodeList childrens = parent.getChildNodes();
		int l = childrens.getLength();
		for (int i = 0; i < l; i++)
			getText(childrens.item(i), sb);
	}

	final public static String getText(Node node) {
		StringBuffer sb = new StringBuffer();
		getText(node, sb);
		return sb.toString();
	}

	final public static int countElements(Node parent) {
		int counter = 0;
		NodeList childrens = parent.getChildNodes();
		int l = childrens.getLength();
		for (int i = 0; i < l; i++) {
			Node node = childrens.item(i);
			switch (node.getNodeType()) {
			case Node.ELEMENT_NODE:
				counter++;
				counter += countElements(node);
				break;
			default:
				continue;
			}
		}
		return counter;
	}

	final public static String getAttributeText(Node node, String name) {
		NamedNodeMap nnm = node.getAttributes();
		if (nnm == null)
			return null;
		Node attr = nnm.getNamedItem(name);
		if (attr == null)
			return null;
		return getText(attr);
	}

	final private static void getAllNodes(Node parent, String[] tags,
			List<Node> nodes) {
		for (String tag : tags) {
			if (parent.getNodeName().equals(tag)) {
				nodes.add(parent);
				break;
			}
		}
		NodeList childrens = parent.getChildNodes();
		int l = childrens.getLength();
		for (int i = 0; i < l; i++)
			getAllNodes(childrens.item(i), tags, nodes);

	}

	final public static List<Node> getAllNodes(Node parent, String... tags) {
		List<Node> nodes = new ArrayList<Node>();
		getAllNodes(parent, tags, nodes);
		return nodes;
	}

	private final static SimpleLock dbfLock = new SimpleLock();

	private static DocumentBuilder getDocumentBuilder(boolean errorSilent)
			throws ParserConfigurationException {
		DocumentBuilderFactory dbf = null;
		dbfLock.rl.lock();
		try {
			dbf = DocumentBuilderFactory.newInstance();
		} finally {
			dbfLock.rl.unlock();
		}

		dbf.setValidating(false);
		dbf.setIgnoringComments(false);
		dbf.setIgnoringElementContentWhitespace(true);
		dbf.setNamespaceAware(true);

		DocumentBuilder db = null;
		db = dbf.newDocumentBuilder();
		db.setEntityResolver(new NullResolver());

		db.setErrorHandler(errorSilent ? ParserErrorHandler.SILENT_ERROR_HANDLER
				: ParserErrorHandler.STANDARD_ERROR_HANDLER);
		return db;
	}

	public static Document readXml(StreamSource source, boolean errorSilent)
			throws SAXException, IOException, ParserConfigurationException {

		InputSource is2 = new InputSource();
		is2.setSystemId(source.getSystemId());
		is2.setByteStream(source.getInputStream());
		is2.setCharacterStream(source.getReader());

		return getDocumentBuilder(errorSilent).parse(is2);
	}

	public static Document readXml(InputSource source, boolean errorSilent)
			throws SAXException, IOException, ParserConfigurationException {
		return getDocumentBuilder(errorSilent).parse(source);
	}

	public static class NullResolver implements EntityResolver {
		@Override
		public InputSource resolveEntity(String publicId, String systemId)
				throws SAXException, IOException {
			return new InputSource(new StringReader(""));
		}
	}

	public final static void xslt(Source xmlSource, String xsl, Result xmlResult)
			throws TransformerException {
		StreamSource xslSource = new StreamSource(new StringReader(xsl));
		TransformerFactory tf = TransformerFactory.newInstance();
		Transformer trans = tf.newTransformer(xslSource);
		trans.setErrorListener(ParserErrorHandler.STANDARD_ERROR_HANDLER);
		trans.transform(xmlSource, xmlResult);
	}

	public final static Result xslt(Source xmlSource, String xsl,
			File destination) throws TransformerException {
		StreamResult xmlResult = new StreamResult(destination);
		xslt(xmlSource, xsl, xmlResult);
		return xmlResult;
	}

}
