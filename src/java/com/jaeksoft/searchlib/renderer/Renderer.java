/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2011 Emmanuel Keller / Jaeksoft
 * 
 * http://www.open-search-server.com
 * 
 * This file is part of Jaeksoft OpenSearchServer.
 *
 * Jaeksoft OpenSearchServer is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * Jaeksoft OpenSearchServer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Jaeksoft OpenSearchServer. 
 *  If not, see <http://www.gnu.org/licenses/>.
 **/

package com.jaeksoft.searchlib.renderer;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.util.ReadWriteLock;
import com.jaeksoft.searchlib.util.XPathParser;
import com.jaeksoft.searchlib.util.XmlWriter;

public class Renderer implements Comparable<Renderer> {

	private final static String RENDERER_ITEM_ROOTNODE_NAME = "renderer";
	private final static String RENDERER_ITEM_ROOT_ATTR_NAME = "name";

	private final ReadWriteLock rwl = new ReadWriteLock();

	private String name;

	public Renderer() {
		name = null;
	}

	public Renderer(File file) throws ParserConfigurationException,
			SAXException, IOException, XPathExpressionException {
		this();
		if (!file.exists())
			return;
		XPathParser xpp = new XPathParser(file);
		Node rootNode = xpp.getNode(RENDERER_ITEM_ROOTNODE_NAME);
		if (rootNode == null)
			return;
		setName(XPathParser.getAttributeString(rootNode,
				RENDERER_ITEM_ROOT_ATTR_NAME));
	}

	public Renderer(Renderer source) {
		this();
		source.copyTo(this);
	}

	public void copyTo(Renderer target) {
		rwl.r.lock();
		try {
			target.rwl.w.lock();
			try {
				target.name = name;
			} finally {
				target.rwl.w.unlock();
			}
		} finally {
			rwl.r.unlock();
		}
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	@Override
	public final int compareTo(Renderer o) {
		return name.compareTo(o.name);
	}

	public void writeXml(XmlWriter xmlWriter) throws SAXException {
		rwl.r.lock();
		try {
			xmlWriter.startElement(RENDERER_ITEM_ROOTNODE_NAME,
					RENDERER_ITEM_ROOT_ATTR_NAME, name);
			xmlWriter.endElement();
		} finally {
			rwl.r.unlock();
		}
	}

}
