/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2008-2010 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.crawler;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.index.FieldContent;
import com.jaeksoft.searchlib.index.IndexDocument;
import com.jaeksoft.searchlib.schema.FieldValueItem;
import com.jaeksoft.searchlib.util.XPathParser;
import com.jaeksoft.searchlib.util.XmlWriter;
import com.jaeksoft.searchlib.util.map.GenericLink;
import com.jaeksoft.searchlib.util.map.Target;

public class FieldMap extends FieldMapGeneric<Target> {

	public FieldMap(File file) throws XPathExpressionException,
			ParserConfigurationException, SAXException, IOException {
		super(file, "/map");
	}

	public FieldMap(XPathParser xpp, Node node) throws XPathExpressionException {
		super(xpp, node);
	}

	@Override
	protected Target loadTarget(String targetName, Node node) {
		return new Target(targetName);
	}

	@Override
	protected void writeTarget(XmlWriter xmlWriter, Target target)
			throws SAXException {
	}

	public void mapIndexDocument(IndexDocument source, IndexDocument target) {
		for (GenericLink<String, Target> link : getList()) {
			FieldContent fc = source.getField(link.getSource());
			String targetField = link.getTarget().getName();
			if (fc != null) {
				List<FieldValueItem> values = fc.getValues();
				if (values != null)
					for (FieldValueItem valueItem : values)
						target.add(targetField, valueItem);
			}
		}
	}

}
