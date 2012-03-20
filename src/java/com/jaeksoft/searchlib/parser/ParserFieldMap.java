/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2012 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.parser;

import java.util.List;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.crawler.FieldMapGeneric;
import com.jaeksoft.searchlib.index.FieldContent;
import com.jaeksoft.searchlib.index.IndexDocument;
import com.jaeksoft.searchlib.schema.FieldValueItem;
import com.jaeksoft.searchlib.util.XPathParser;
import com.jaeksoft.searchlib.util.XmlWriter;
import com.jaeksoft.searchlib.util.map.GenericLink;

public class ParserFieldMap extends FieldMapGeneric<ParserFieldTarget> {

	public ParserFieldMap(XPathParser xpp, Node node)
			throws XPathExpressionException {
		super(xpp, node);
	}

	public ParserFieldMap() {
		super();
	}

	@Override
	protected ParserFieldTarget loadTarget(String targetName, Node node) {
		return new ParserFieldTarget(targetName, node);
	}

	@Override
	protected void writeTarget(XmlWriter xmlWriter, ParserFieldTarget target)
			throws SAXException {
		target.writeXml(xmlWriter);
	}

	public void mapIndexDocument(IndexDocument source, IndexDocument target) {
		for (GenericLink<String, ParserFieldTarget> link : getList()) {
			FieldContent fc = source.getField(link.getSource());
			ParserFieldTarget fieldTarget = link.getTarget();
			String targetField = fieldTarget.getName();
			if (fc != null) {
				List<FieldValueItem> values = fc.getValues();
				if (values != null)
					for (FieldValueItem valueItem : values)
						fieldTarget.addValue(target, targetField, valueItem);
			}
		}
	}
}
