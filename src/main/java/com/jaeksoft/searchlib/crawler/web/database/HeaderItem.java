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

import java.io.UnsupportedEncodingException;

import org.apache.http.message.BasicHeader;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.util.XmlWriter;

public class HeaderItem extends AbstractPatternNameValueItem {

	public final static String NODE_NAME = "header";

	private BasicHeader basicHeader = null;

	public HeaderItem() {
	}

	public HeaderItem(String name, String value) {
		super(null, name, value);
	}

	public HeaderItem(Node node) {
		super(node);
	}

	@Override
	public void writeXml(XmlWriter xmlWriter)
			throws UnsupportedEncodingException, SAXException {
		super.writeXml(NODE_NAME, xmlWriter);
	}

	public BasicHeader getHeader() {
		if (basicHeader != null)
			return basicHeader;
		basicHeader = new BasicHeader(name, value);
		return basicHeader;
	}

}
