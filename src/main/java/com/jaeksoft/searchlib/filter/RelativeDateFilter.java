/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008-2013 Emmanuel Keller / Jaeksoft
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
import java.text.SimpleDateFormat;

import javax.xml.xpath.XPathExpressionException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Query;
import org.apache.lucene.util.Version;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.analysis.PerFieldAnalyzer;
import com.jaeksoft.searchlib.crawler.common.database.TimeInterval;
import com.jaeksoft.searchlib.index.ReaderLocal;
import com.jaeksoft.searchlib.query.ParseException;
import com.jaeksoft.searchlib.request.AbstractSearchRequest;
import com.jaeksoft.searchlib.schema.SchemaField;
import com.jaeksoft.searchlib.util.DomUtils;
import com.jaeksoft.searchlib.util.Timer;
import com.jaeksoft.searchlib.util.XPathParser;
import com.jaeksoft.searchlib.util.XmlWriter;
import com.jaeksoft.searchlib.web.ServletTransaction;

public class RelativeDateFilter extends FilterAbstract<RelativeDateFilter> {

	private final TimeInterval from;

	private final TimeInterval to;

	private String field;

	private String dateFormat;

	private transient SimpleDateFormat simpleDateFormat;

	private transient Query query;

	private transient String cachedQueryString;

	public RelativeDateFilter(TimeInterval from, TimeInterval to, String field,
			String dateFormat, boolean negative, Source src,
			String paramPosition) {
		super(FilterType.RELATIVE_DATE_FILTER, src, negative, paramPosition);
		query = null;
		this.from = from == null ? new TimeInterval() : from;
		this.to = to == null ? new TimeInterval() : to;
		this.field = field;
		this.dateFormat = dateFormat;
		simpleDateFormat = null;
	}

	public RelativeDateFilter() {
		this(null, null, null, null, false, Source.REQUEST, null);
	}

	public RelativeDateFilter(XPathParser xpp, Node node)
			throws XPathExpressionException {
		this(new TimeInterval(DomUtils.getAttributeText(node, "from")),
				new TimeInterval(DomUtils.getAttributeText(node, "to")),
				DomUtils.getAttributeText(node, "field"), DomUtils
						.getAttributeText(node, "dateFormat"), "yes"
						.equals(XPathParser
								.getAttributeString(node, "negative")),
				Source.CONFIGXML, null);
	}

	private String getQueryString() {
		long l = System.currentTimeMillis();
		StringBuffer sb = new StringBuffer();
		if (field != null && field.length() > 0) {
			sb.append(field);
			sb.append(':');
		}
		if (simpleDateFormat == null)
			if (dateFormat != null && dateFormat.length() > 0)
				simpleDateFormat = new SimpleDateFormat(dateFormat);
		if (simpleDateFormat == null)
			return sb.toString();
		sb.append('[');
		synchronized (simpleDateFormat) {
			sb.append(simpleDateFormat.format(from.getPastDate(l)));
			sb.append(" TO ");
			sb.append(simpleDateFormat.format(to.getPastDate(l)));
		}
		sb.append("]");
		return sb.toString();
	}

	private Query getQuery(SchemaField defaultField, Analyzer analyzer)
			throws ParseException {
		String queryString = getQueryString();
		if (query != null)
			if (queryString.equals(cachedQueryString))
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

	@Override
	public String getDescription() {
		StringBuffer sb = new StringBuffer("Date query filter: ");
		sb.append(getQueryString());
		return sb.toString();
	}

	@Override
	public String getCacheKey(SchemaField defaultField,
			PerFieldAnalyzer analyzer, AbstractSearchRequest request)
			throws ParseException {
		return "QueryFilter - " + getQuery(defaultField, analyzer).toString();
	}

	@Override
	public void writeXmlConfig(XmlWriter xmlWriter) throws SAXException {
		xmlWriter.startElement("relativeDateFilter", "field", field, "from",
				from.getByText(), "to", to.getByText(), "dateFormat",
				dateFormat, "negative", isNegative() ? "yes" : "no");
		xmlWriter.endElement();
	}

	@Override
	public FilterHits getFilterHits(ReaderLocal reader,
			SchemaField defaultField, PerFieldAnalyzer analyzer,
			AbstractSearchRequest request, Timer timer) throws ParseException,
			IOException {
		Query query = getQuery(defaultField, analyzer);
		FilterHits filterHits = new FilterHits(query, isNegative(), reader,
				timer);
		return filterHits;
	}

	@Override
	public RelativeDateFilter duplicate() {
		return new RelativeDateFilter(new TimeInterval(from), new TimeInterval(
				to), field, dateFormat, isNegative(), getSource(),
				getParamPosition());
	}

	@Override
	public void copyTo(FilterAbstract<?> selectedItem) {
		if (!(selectedItem instanceof RelativeDateFilter))
			throw new RuntimeException("Wrong filter type "
					+ selectedItem.getClass().getName());
		super.copyTo(selectedItem);
		RelativeDateFilter copyTo = (RelativeDateFilter) selectedItem;
		copyTo.field = field;
		copyTo.dateFormat = dateFormat;
		copyTo.from.set(from);
		copyTo.to.set(to);
	}

	@Override
	public void setFromServlet(ServletTransaction transaction) {
	}

	@Override
	public void setParam(String param) throws SearchLibException {
	}

	/**
	 * @return the field
	 */
	public String getField() {
		return field;
	}

	/**
	 * @param field
	 *            the field to set
	 */
	public void setField(String field) {
		this.field = field;
	}

	/**
	 * @return the dateFormat
	 */
	public String getDateFormat() {
		return dateFormat;
	}

	/**
	 * @param dateFormat
	 *            the dateFormat to set
	 */
	public void setDateFormat(String dateFormat) {
		this.dateFormat = dateFormat;
	}

	/**
	 * @return the from
	 */
	public TimeInterval getFrom() {
		return from;
	}

	/**
	 * @return the to
	 */
	public TimeInterval getTo() {
		return to;
	}

}
