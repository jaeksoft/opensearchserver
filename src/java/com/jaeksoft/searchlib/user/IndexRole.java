/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2010 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.user;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.util.XPathParser;
import com.jaeksoft.searchlib.util.XmlWriter;

public class IndexRole implements Comparable<IndexRole> {

	private final String indexName;

	private final Role role;

	protected IndexRole(String indexName, Role role) {
		this.indexName = indexName;
		this.role = role;
	}

	protected IndexRole(String indexName, String roleName) {
		this(indexName, Role.valueOf(roleName));
	}

	public Role getRole() {
		return role;
	}

	public String getIndexName() {
		return indexName;
	}

	public static IndexRole fromXml(XPathParser xpp, Node node)
			throws XPathExpressionException {
		if (node == null)
			return null;
		String indexName = XPathParser.getAttributeString(node, "indexName");
		String roleName = XPathParser.getAttributeString(node, "role");

		Role role = Role.find(roleName);
		if (role == null)
			return null;
		return new IndexRole(indexName, role);
	}

	public void writeXml(XmlWriter xmlWriter) throws SAXException {
		xmlWriter.startElement("role", "indexName", indexName, "role",
				role.name());
		xmlWriter.endElement();
	}

	@Override
	public int compareTo(IndexRole o) {
		int i = indexName.compareTo(o.indexName);
		if (i != 0)
			return i;
		return role.name().compareTo(o.role.name());
	}

}
