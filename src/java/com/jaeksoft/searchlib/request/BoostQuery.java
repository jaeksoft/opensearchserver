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

import java.util.List;

import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BoostingQuery;
import org.apache.lucene.search.Query;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.query.ParseException;
import com.jaeksoft.searchlib.util.DomUtils;
import com.jaeksoft.searchlib.util.XPathParser;
import com.jaeksoft.searchlib.util.XmlWriter;

public class BoostQuery {

	private final static String BOOSTQUERY_NODE = "boostQuery";
	private final static String BOOSTQUERY_ATTR_BOOST = "boost";

	private float boost = 1.0f;

	private String query = null;

	private BoostQuery(Node node) {
		query = StringEscapeUtils.unescapeXml(DomUtils.getText(node));
		boost = XPathParser.getAttributeFloat(node, BOOSTQUERY_ATTR_BOOST);
	}

	public BoostQuery(String query, float boost) {
		this.query = query;
		this.boost = boost;
	}

	public BoostQuery(BoostQuery boostQuery) {
		this(boostQuery.query, boostQuery.boost);
	}

	public void copyFrom(BoostQuery from) {
		this.query = from.query;
		this.boost = from.boost;
	}

	public Query getNewQuery(Query complexQuery, QueryParser queryParser)
			throws ParseException {
		try {
			return new BoostingQuery(complexQuery, queryParser.parse(query),
					boost);
		} catch (org.apache.lucene.queryParser.ParseException e) {
			throw new ParseException(e);
		}
	}

	public void writeXmlConfig(XmlWriter writer) throws SAXException {
		writer.startElement(BOOSTQUERY_NODE, BOOSTQUERY_ATTR_BOOST,
				Float.toString(boost));
		if (query != null)
			writer.textNode(StringEscapeUtils.escapeXml(query));
		writer.endElement();
	}

	public static void loadFromXml(XPathParser xpp, Node node,
			List<BoostQuery> boostingQueries) throws XPathExpressionException {
		NodeList nodeList = xpp.getNodeList(node, BOOSTQUERY_NODE);
		int l = nodeList.getLength();
		for (int i = 0; i < l; i++)
			boostingQueries.add(new BoostQuery(nodeList.item(i)));
	}

	/**
	 * @return the boost
	 */
	public float getBoost() {
		return boost;
	}

	/**
	 * @param boost
	 *            the boost to set
	 */
	public void setBoost(float boost) {
		this.boost = boost;
	}

	/**
	 * @return the query
	 */
	public String getQuery() {
		return query;
	}

	/**
	 * @param query
	 *            the query to set
	 */
	public void setQuery(String query) {
		this.query = query;
	}

	public final static String getCacheKey(BoostQuery[] boostingQueries) {
		if (boostingQueries.length == 0)
			return "";
		StringBuffer sb = new StringBuffer();
		for (BoostQuery boostQuery : boostingQueries) {
			sb.append(boostQuery.query);
			sb.append('/');
			sb.append(boostQuery.boost);
			sb.append('|');
		}
		return sb.toString();
	}
}
