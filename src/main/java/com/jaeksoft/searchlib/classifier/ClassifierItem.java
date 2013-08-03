/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2011-2013 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.classifier;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.xpath.XPathExpressionException;

import org.apache.lucene.index.memory.MemoryIndex;
import org.apache.lucene.search.Query;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.analysis.LanguageEnum;
import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.query.ParseException;
import com.jaeksoft.searchlib.request.SearchRequest;
import com.jaeksoft.searchlib.result.AbstractResultSearch;
import com.jaeksoft.searchlib.util.DomUtils;
import com.jaeksoft.searchlib.util.StringUtils;
import com.jaeksoft.searchlib.util.XPathParser;
import com.jaeksoft.searchlib.util.XmlWriter;

public class ClassifierItem implements Comparable<ClassifierItem> {

	private String value;

	private Float boost;

	private String requestName;

	private String query;

	private transient Map<LanguageEnum, Query> queryMap;

	private static final String CLASSIFIER_ITEM_REQUESTNAME_ATTRIBUTE = "request";
	private static final String CLASSIFIER_ITEM_VALUE_NODE = "value";
	private static final String CLASSIFIER_ITEM_VALUE_BOOST_ATTRIBUTE = "boost";
	private static final String CLASSIFIER_ITEM_QUERY_NODE = "query";

	public ClassifierItem() {
		value = null;
		boost = null;
		requestName = null;
		query = null;
		queryMap = new HashMap<LanguageEnum, Query>();
	}

	public ClassifierItem(Node node) throws XPathExpressionException {
		this();
		Node valueNode = DomUtils
				.getFirstNode(node, CLASSIFIER_ITEM_VALUE_NODE);
		if (valueNode != null) {
			setValue(DomUtils.getText(valueNode));
			setBoost(XPathParser.getAttributeFloat(valueNode,
					CLASSIFIER_ITEM_VALUE_BOOST_ATTRIBUTE));
		}
		Node queryNode = DomUtils
				.getFirstNode(node, CLASSIFIER_ITEM_QUERY_NODE);
		if (queryNode != null) {
			setQuery(DomUtils.getText(queryNode));
			setRequestName(DomUtils.getAttributeText(queryNode,
					CLASSIFIER_ITEM_REQUESTNAME_ATTRIBUTE));
		}
	}

	public ClassifierItem(ClassifierItem item) {
		this();
		item.copyTo(this);
	}

	public void copyTo(ClassifierItem target) {
		target.value = value;
		target.boost = boost;
		target.requestName = requestName;
		target.query = query;
	}

	/**
	 * @param value
	 *            the value to set
	 */
	public void setValue(String value) {
		this.value = value;
	}

	/**
	 * @param boost
	 *            the boost to set
	 */
	public void setBoost(Float boost) {
		this.boost = boost;
	}

	/**
	 * @return the boost
	 */
	public Float getBoost() {
		return boost;
	}

	/**
	 * @return the value
	 */
	public String getValue() {
		return value;
	}

	/**
	 * @param requestName
	 *            the requestName to set
	 */
	public void setRequestName(String requestName) {
		this.requestName = requestName;
	}

	/**
	 * @return the requestName
	 */
	public String getRequestName() {
		return requestName;
	}

	/**
	 * @param query
	 *            the query to set
	 */
	public void setQuery(String query) {
		this.query = query;
		queryMap.clear();
	}

	/**
	 * @return the query
	 */
	public String getQuery() {
		return query;
	}

	@Override
	public int compareTo(ClassifierItem item) {
		int i;
		if ((i = StringUtils.compareNullValues(value, item.value)) != 0)
			return i;
		if (value != null)
			if ((i = value.compareTo(item.value)) != 0)
				return i;
		if ((i = StringUtils.compareNullValues(requestName, item.requestName)) != 0)
			return i;
		if (requestName != null)
			if ((i = requestName.compareTo(item.requestName)) != 0)
				return i;
		if ((i = StringUtils.compareNullValues(query, item.query)) != 0)
			return i;
		if (query != null)
			if ((i = query.compareTo(item.query)) != 0)
				return i;
		return 0;
	}

	public void writeXml(XmlWriter xmlWriter, String itemNodeName)
			throws SAXException {
		// Start item element
		xmlWriter.startElement(itemNodeName);
		// Start value element
		xmlWriter.startElement(CLASSIFIER_ITEM_VALUE_NODE,
				CLASSIFIER_ITEM_VALUE_BOOST_ATTRIBUTE, boost == null ? null
						: boost.toString());
		xmlWriter.textNode(value);
		xmlWriter.endElement();
		// Query element
		xmlWriter.startElement(CLASSIFIER_ITEM_QUERY_NODE,
				CLASSIFIER_ITEM_REQUESTNAME_ATTRIBUTE, requestName);
		xmlWriter.textNode(query);
		xmlWriter.endElement();
		// End Item element
		xmlWriter.endElement();
	}

	private SearchRequest getSearchRequest(Client client, LanguageEnum lang)
			throws SearchLibException {
		SearchRequest searchRequest;
		if (requestName != null && requestName.length() > 0)
			searchRequest = (SearchRequest) client.getNewRequest(requestName);
		else
			searchRequest = new SearchRequest(client);
		searchRequest.setLang(lang);
		searchRequest.setQueryString(query);
		return searchRequest;
	}

	public int query(Client client, LanguageEnum lang)
			throws SearchLibException {
		SearchRequest searchRequest = getSearchRequest(client, lang);
		searchRequest.setRows(0);
		return ((AbstractResultSearch) client.request(searchRequest))
				.getNumFound();
	}

	protected final float score(Client client, LanguageEnum lang,
			MemoryIndex index) throws ParseException, SearchLibException,
			SyntaxError, IOException {
		Query qry = queryMap.get(lang);
		if (qry == null) {
			SearchRequest searchRequest = getSearchRequest(client, lang);
			qry = searchRequest.getQuery();
			queryMap.put(lang, qry);
		}
		return index.search(qry);
	}
}
