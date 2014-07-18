/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2014 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.crawler.web.database;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.SearchLibException;

public class HeaderManager extends AbstractPatternNameValueManager<HeaderItem> {

	public final static String XPP_PATH = "/headers/header";

	public final static String ITEM_NODE_NAME = "header";
	public final static String ROOT_NODE_NAME = "headers";

	public HeaderManager(File indexDir, String filename)
			throws SearchLibException {
		super(indexDir, filename);
	}

	@Override
	protected void load() throws ParserConfigurationException, SAXException,
			IOException, XPathExpressionException, SearchLibException {
		load(XPP_PATH);
	}

	@Override
	protected void store() throws IOException,
			TransformerConfigurationException, SAXException {
		store(ROOT_NODE_NAME);
	}

	@Override
	protected HeaderItem getNewItem(Node node) {
		return new HeaderItem(node);
	}

}
