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

package com.jaeksoft.searchlib.request;

import java.util.LinkedHashSet;
import java.util.Set;

import javax.xml.xpath.XPathExpressionException;

import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.config.Config;
import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.index.ReaderInterface;
import com.jaeksoft.searchlib.result.AbstractResult;
import com.jaeksoft.searchlib.result.ResultSpellCheck;
import com.jaeksoft.searchlib.schema.FieldList;
import com.jaeksoft.searchlib.schema.SchemaField;
import com.jaeksoft.searchlib.spellcheck.SpellCheckField;
import com.jaeksoft.searchlib.util.XPathParser;
import com.jaeksoft.searchlib.util.XmlWriter;
import com.jaeksoft.searchlib.web.ServletTransaction;

public class SpellCheckRequest extends AbstractRequest {

	private FieldList<SpellCheckField> spellCheckFieldList;

	private String query;

	public SpellCheckRequest() {
	}

	public SpellCheckRequest(Config config) {
		super(config);
	}

	@Override
	public void setDefaultValues() {
		super.setDefaultValues();
		this.spellCheckFieldList = new FieldList<SpellCheckField>();
		this.query = null;
	}

	@Override
	public void copyFrom(AbstractRequest request) {
		super.copyFrom(request);
		SpellCheckRequest spellCheckrequest = (SpellCheckRequest) request;
		this.spellCheckFieldList = new FieldList<SpellCheckField>(
				spellCheckrequest.spellCheckFieldList);
		this.query = spellCheckrequest.query;
	}

	public FieldList<SpellCheckField> getSpellCheckFieldList() {
		rwl.r.lock();
		try {
			return this.spellCheckFieldList;
		} finally {
			rwl.r.unlock();
		}
	}

	@Override
	public void fromXmlConfig(Config config, XPathParser xpp, Node node)
			throws XPathExpressionException, DOMException, ParseException,
			InstantiationException, IllegalAccessException,
			ClassNotFoundException {
		rwl.w.lock();
		try {
			super.fromXmlConfig(config, xpp, node);
			FieldList<SchemaField> fieldList = config.getSchema()
					.getFieldList();
			NodeList nodes = xpp.getNodeList(node,
					"spellCheckFields/spellCheckField");
			int l = nodes.getLength();
			for (int i = 0; i < l; i++)
				SpellCheckField.copySpellCheckFields(nodes.item(i), fieldList,
						spellCheckFieldList);
			setQuery(xpp.getNodeString(node, "query"));
		} finally {
			rwl.w.unlock();
		}
	}

	@Override
	public void writeXmlConfig(XmlWriter xmlWriter) throws SAXException {
		rwl.r.lock();
		try {
			xmlWriter.startElement(XML_NODE_REQUEST, XML_ATTR_NAME,
					getRequestName(), XML_ATTR_TYPE, getType().name());
			if (spellCheckFieldList.size() > 0) {
				xmlWriter.startElement("spellCheckFields");
				spellCheckFieldList.writeXmlConfig(xmlWriter);
				xmlWriter.endElement();
			}
			if (query != null && query.trim().length() > 0) {
				xmlWriter.startElement("query");
				xmlWriter.textNode(query);
				xmlWriter.endElement();
			}
			xmlWriter.endElement();

		} finally {
			rwl.r.unlock();
		}
	}

	@Override
	public RequestTypeEnum getType() {
		return RequestTypeEnum.SpellCheckRequest;
	}

	@Override
	public void setFromServlet(ServletTransaction transaction)
			throws SyntaxError {
		// TODO Auto-generated method stub

	}

	@Override
	public void reset() {
	}

	@Override
	public AbstractResult<SpellCheckRequest> execute(ReaderInterface reader)
			throws SearchLibException {
		return new ResultSpellCheck(reader, this);
	}

	public Set<Term> getTermSet() {
		Set<Term> set = new LinkedHashSet<Term>();
		// TODO searchRequest.getQuery().extractTerms(set);
		return set;
	}

	@Override
	public String getInfo() {
		rwl.r.lock();
		try {
			StringBuffer sb = new StringBuffer();
			for (SpellCheckField field : spellCheckFieldList) {
				if (sb.length() > 0)
					sb.append(", ");
				sb.append(field.getName());
			}
			return sb.toString();
		} finally {
			rwl.r.unlock();
		}
	}

	/**
	 * @return the query
	 */
	public String getQuery() {
		rwl.r.lock();
		try {
			return query;
		} finally {
			rwl.r.unlock();
		}
	}

	/**
	 * @param query
	 *            the query to set
	 */
	public void setQuery(String query) {
		this.query = query;
	}
}
