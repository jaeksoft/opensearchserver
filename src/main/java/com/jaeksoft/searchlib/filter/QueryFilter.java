/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008-2014 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.filter;

import java.io.IOException;

import javax.xml.xpath.XPathExpressionException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Query;
import org.apache.lucene.util.Version;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.query.ParseException;
import com.jaeksoft.searchlib.request.AbstractSearchRequest;
import com.jaeksoft.searchlib.schema.SchemaField;
import com.jaeksoft.searchlib.util.StringUtils;
import com.jaeksoft.searchlib.util.Timer;
import com.jaeksoft.searchlib.util.XPathParser;
import com.jaeksoft.searchlib.util.XmlWriter;
import com.jaeksoft.searchlib.web.ServletTransaction;

public class QueryFilter extends FilterAbstract<QueryFilter> {

	private transient Query query;

	private String queryString;

	public QueryFilter() {
		this("", false, Source.REQUEST, null);
	}

	public QueryFilter(String req, boolean negative, Source src,
			String paramPosition) {
		super(FilterType.QUERY_FILTER, src, negative, paramPosition);
		this.queryString = req;
		this.query = null;
	}

	public QueryFilter(XPathParser xpp, Node node)
			throws XPathExpressionException {
		this(xpp.getNodeString(node, false), "yes".equals(XPathParser
				.getAttributeString(node, "negative")), Source.CONFIGXML, null);
	}

	protected Query getQuery(SchemaField defaultField, Analyzer analyzer)
			throws ParseException {
		if (query != null)
			return query;
		QueryParser queryParser = new QueryParser(Version.LUCENE_36,
				defaultField.getName(), analyzer);
		queryParser.setLowercaseExpandedTerms(false);
		try {
			query = queryParser.parse(queryString);
		} catch (org.apache.lucene.queryParser.ParseException e) {
			throw new ParseException(e);
		}
		return query;
	}

	public String getQueryString() {
		return this.queryString;
	}

	public void setQueryString(String queryString) {
		this.queryString = queryString;
		this.query = null;
	}

	@Override
	public String getDescription() {
		StringBuilder sb = new StringBuilder("Query filter: ");
		sb.append(queryString);
		return sb.toString();
	}

	@Override
	public String getCacheKey(SchemaField defaultField, Analyzer analyzer,
			AbstractSearchRequest request) throws ParseException {
		return "QueryFilter - " + getQuery(defaultField, analyzer).toString();
	}

	@Override
	public void writeXmlConfig(XmlWriter xmlWriter) throws SAXException {
		xmlWriter.startElement("filter", "negative", isNegative() ? "yes"
				: "no");
		xmlWriter.textNode(queryString);
		xmlWriter.endElement();
	}

	@Override
	public FilterHits getFilterHits(SchemaField defaultField,
			Analyzer analyzer, AbstractSearchRequest request, Timer timer)
			throws ParseException, IOException, SearchLibException {
		Query query = getQuery(defaultField, analyzer);
		return new FilterHits(
				getResult(request.getConfig(), query, null, timer),
				isNegative(), timer);
	}

	@Override
	public QueryFilter duplicate() {
		return new QueryFilter(queryString, isNegative(), getSource(),
				getParamPosition());
	}

	@Override
	public void copyTo(FilterAbstract<?> selectedItem) {
		if (!(selectedItem instanceof QueryFilter))
			throw new RuntimeException("Wrong filter type "
					+ selectedItem.getClass().getName());
		super.copyTo(selectedItem);
		QueryFilter copyTo = (QueryFilter) selectedItem;
		copyTo.queryString = queryString;
		copyTo.query = null;
	}

	@Override
	final public void setFromServlet(final ServletTransaction transaction,
			final String prefix) {
		String q = transaction.getParameterString(StringUtils.fastConcat(
				prefix, getParamPosition()));
		if (q != null)
			setQueryString(q);
	}

	@Override
	final public void setParam(final String param) {
		if (param != null)
			setQueryString(param);
	}

	@Override
	public void reset() {
		query = null;
	}
}
