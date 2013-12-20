/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2012-2013 Emmanuel Keller / Jaeksoft
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

import java.io.IOException;
import java.util.List;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.analysis.AnalyzerList;
import com.jaeksoft.searchlib.analysis.LanguageEnum;
import com.jaeksoft.searchlib.crawler.FieldMapGeneric;
import com.jaeksoft.searchlib.index.FieldContent;
import com.jaeksoft.searchlib.index.IndexDocument;
import com.jaeksoft.searchlib.schema.FieldValueItem;
import com.jaeksoft.searchlib.util.DomUtils;
import com.jaeksoft.searchlib.util.XPathParser;
import com.jaeksoft.searchlib.util.XmlWriter;
import com.jaeksoft.searchlib.util.map.GenericLink;
import com.jaeksoft.searchlib.util.map.SourceField;

public class ParserFieldMap extends
		FieldMapGeneric<SourceField, ParserFieldTarget> {

	public ParserFieldMap(Node node) throws XPathExpressionException {
		super(node);
	}

	public ParserFieldMap() {
		super();
	}

	@Override
	protected ParserFieldTarget loadTarget(String targetName, Node node) {
		return new ParserFieldTarget(targetName, node);
	}

	@Override
	protected SourceField loadSource(String source) {
		return new SourceField(source);
	}

	@Override
	protected void writeTarget(XmlWriter xmlWriter, ParserFieldTarget target)
			throws SAXException {
		target.writeXml(xmlWriter);
	}

	protected boolean isMapped(ParserFieldEnum field) {
		List<ParserFieldTarget> list = getLinks(new SourceField(field.name()));
		if (list == null)
			return false;
		return list.size() > 0;
	}

	final public void mapIndexDocument(final IndexDocument source,
			final IndexDocument target) throws IOException {
		for (GenericLink<SourceField, ParserFieldTarget> link : getList()) {
			FieldContent fc = link.getSource().getUniqueString(source);
			ParserFieldTarget fieldTarget = link.getTarget();
			if (fc != null) {
				FieldValueItem[] values = fc.getValues();
				fieldTarget.add(values, target);
			}
		}
	}

	final public void mapXmlXPathDocument(final Node xmlForXPath,
			final IndexDocument target) throws XPathExpressionException,
			IOException {
		XPathParser xpp = new XPathParser(xmlForXPath);
		for (GenericLink<SourceField, ParserFieldTarget> link : getList()) {
			ParserFieldTarget fieldTarget = link.getTarget();
			Object obj = xpp.evaluate(xmlForXPath, link.getSource()
					.getUniqueName());
			if (obj instanceof Node) {
				fieldTarget.add(DomUtils.getText((Node) obj), target);
			} else if (obj instanceof NodeList) {
				NodeList nodeList = (NodeList) obj;
				int length = nodeList.getLength();
				for (int i = 0; i < length; i++)
					fieldTarget.add(DomUtils.getText(nodeList.item(i)), target);
			} else if (obj instanceof String) {
				fieldTarget.add((String) obj, target);
			} else if (obj instanceof Object) {
				fieldTarget.add(obj.toString(), target);
			}
		}
	}

	public void compileAnalyzer(AnalyzerList analyzerList)
			throws SearchLibException {
		for (GenericLink<SourceField, ParserFieldTarget> link : getList()) {
			link.getTarget().setCachedAnalyzer(analyzerList,
					LanguageEnum.UNDEFINED);
		}
	}

}
