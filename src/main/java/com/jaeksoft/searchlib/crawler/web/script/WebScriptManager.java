/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2013-2014 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.crawler.web.script;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.crawler.web.database.AbstractPatternNameValueManager;

public class WebScriptManager extends
		AbstractPatternNameValueManager<WebScriptItem> {

	public final static String XPP_PATH = "/webscripts/webscript";

	public final static String ITEM_NODE_NAME = "webscript";
	public final static String ROOT_NODE_NAME = "webscripts";

	public WebScriptManager(File indexDir, String filename)
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
	protected WebScriptItem getNewItem(Node node) {
		return new WebScriptItem(node);
	}

}
