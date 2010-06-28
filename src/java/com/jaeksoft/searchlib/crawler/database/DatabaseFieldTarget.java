/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2010 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.crawler.database;

import java.util.List;

import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.util.DomUtils;
import com.jaeksoft.searchlib.util.XmlWriter;
import com.jaeksoft.searchlib.util.map.Target;

public class DatabaseFieldTarget extends Target {

	private boolean removeTag;

	public DatabaseFieldTarget(String targetName, boolean removeTag) {
		super(targetName);
		this.removeTag = removeTag;
	}

	public DatabaseFieldTarget(String targetName, Node targetNode) {
		super(targetName);
		removeTag = false;
		List<Node> nodeList = DomUtils.getNodes(targetNode, "filter");
		for (Node node : nodeList) {
			if ("yes".equalsIgnoreCase(DomUtils.getAttributeText(node,
					"removeTag")))
				removeTag = true;
		}
	}

	public void writeXml(XmlWriter xmlWriter) throws SAXException {
		xmlWriter.startElement("filter", "removeTag", removeTag ? "yes" : "no");
		xmlWriter.endElement();
	}

	/**
	 * @param removeTag
	 *            the removeTag to set
	 */
	public void setRemoveTag(boolean removeTag) {
		this.removeTag = removeTag;
	}

	/**
	 * @return the removeTag
	 */
	public boolean isRemoveTag() {
		return removeTag;
	}

}
