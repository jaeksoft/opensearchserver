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
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.config.Config;
import com.jaeksoft.searchlib.query.ParseException;
import com.jaeksoft.searchlib.request.AbstractLocalSearchRequest;
import com.jaeksoft.searchlib.request.AbstractRequest;
import com.jaeksoft.searchlib.request.AbstractSearchRequest;
import com.jaeksoft.searchlib.result.ResultSearchSingle;
import com.jaeksoft.searchlib.schema.SchemaField;
import com.jaeksoft.searchlib.util.StringUtils;
import com.jaeksoft.searchlib.util.Timer;
import com.jaeksoft.searchlib.util.XPathParser;
import com.jaeksoft.searchlib.util.XmlWriter;
import com.jaeksoft.searchlib.web.ServletTransaction;
import com.jaeksoft.searchlib.webservice.query.search.SearchQueryAbstract.OperatorEnum;

public class RequestTemplateFilter extends
		FilterAbstract<RequestTemplateFilter> {

	private String requestName;
	private String queryString;

	public RequestTemplateFilter() {
		this(StringUtils.EMPTY, StringUtils.EMPTY, false, Source.REQUEST, null);
	}

	public RequestTemplateFilter(String requestName, String queryString,
			boolean negative, Source src, String paramPosition) {
		super(FilterType.REQUEST_TEMPLATE_FILTER, src, negative, paramPosition);
		this.requestName = requestName;
		this.queryString = queryString;
	}

	public RequestTemplateFilter(XPathParser xpp, Node node)
			throws XPathExpressionException {
		this(XPathParser.getAttributeString(node, "requestName"), xpp
				.getNodeString(node, false), "yes".equals(XPathParser
				.getAttributeString(node, "negative")), Source.CONFIGXML, null);
	}

	@Override
	public String getDescription() {
		StringBuilder sb = new StringBuilder("Template filter: ");
		sb.append(requestName);
		sb.append(" - ");
		sb.append(queryString);
		return sb.toString();
	}

	@Override
	public String getCacheKey(SchemaField defaultField, Analyzer analyzer,
			AbstractLocalSearchRequest request) throws ParseException {
		return StringUtils.fastConcat("TemplateFilter - ", requestName,
				queryString);
	}

	@Override
	public void writeXmlConfig(XmlWriter xmlWriter) throws SAXException {
		xmlWriter.startElement("requestTemplateFilter", "negative",
				isNegative() ? "yes" : "no", "requestName", requestName);
		xmlWriter.textNode(queryString);
		xmlWriter.endElement();
	}

	@Override
	public FilterHits getFilterHits(SchemaField defaultField,
			Analyzer analyzer, AbstractLocalSearchRequest request, Timer timer)
			throws ParseException, IOException, SearchLibException {
		Config config = request.getConfig();
		AbstractRequest filterRequest = config.getNewRequest(requestName);
		if (!(filterRequest instanceof AbstractSearchRequest))
			throw new SearchLibException(
					"Filter failure. Request type not compatible: "
							+ requestName);
		((AbstractSearchRequest) filterRequest).setForFilter(true);
		ResultSearchSingle result = (ResultSearchSingle) config
				.getIndexAbstract().request(filterRequest);
		return new FilterHits(result, isNegative(), timer);
	}

	@Override
	public RequestTemplateFilter duplicate() {
		return new RequestTemplateFilter(requestName, queryString,
				isNegative(), getSource(), getParamPosition());
	}

	@Override
	public void copyTo(FilterAbstract<?> selectedItem) {
		if (!(selectedItem instanceof RequestTemplateFilter))
			throw new RuntimeException("Wrong filter type "
					+ selectedItem.getClass().getName());
		super.copyTo(selectedItem);
		RequestTemplateFilter copyTo = (RequestTemplateFilter) selectedItem;
		copyTo.requestName = requestName;
		copyTo.queryString = queryString;
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
	public OperatorEnum getOperator(OperatorEnum defaultOperator) {
		return defaultOperator;
	}

	/**
	 * @return the requestName
	 */
	public String getRequestName() {
		return requestName;
	}

	/**
	 * @param requestName
	 *            the requestName to set
	 */
	public void setRequestName(String requestName) {
		this.requestName = requestName;
	}

	@Override
	public void reset() {
	}

	/**
	 * @return the queryString
	 */
	public String getQueryString() {
		return queryString;
	}

	/**
	 * @param queryString
	 *            the queryString to set
	 */
	public void setQueryString(String queryString) {
		this.queryString = queryString;
	}
}
