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
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.index.ReaderLocal;
import com.jaeksoft.searchlib.query.ParseException;
import com.jaeksoft.searchlib.request.AbstractSearchRequest;
import com.jaeksoft.searchlib.schema.SchemaField;
import com.jaeksoft.searchlib.util.DomUtils;
import com.jaeksoft.searchlib.util.StringUtils;
import com.jaeksoft.searchlib.util.Timer;
import com.jaeksoft.searchlib.util.XPathParser;
import com.jaeksoft.searchlib.util.XmlWriter;
import com.jaeksoft.searchlib.web.ServletTransaction;

public class TermFilter extends FilterAbstract<TermFilter> {

	private transient Query query;

	private String field;

	private String term;

	public TermFilter() {
		this("", "", false, Source.REQUEST, null);
	}

	public TermFilter(String field, String term, boolean negative, Source src,
			String paramPosition) {
		super(FilterType.TERM_FILTER, src, negative, paramPosition);
		this.field = field;
		this.term = term;
	}

	public TermFilter(XPathParser xpp, Node node)
			throws XPathExpressionException {
		this(DomUtils.getAttributeText(node, "field"), xpp.getNodeString(node,
				false), "yes".equals(DomUtils
				.getAttributeText(node, "negative")), Source.CONFIGXML, null);
	}

	public Query getQuery() throws ParseException {
		if (query != null)
			return query;
		query = new TermQuery(new Term(field, term));
		return query;
	}

	public String getField() {
		return this.field;
	}

	public String getTerm() {
		return this.term;
	}

	public void setField(String field) {
		this.field = field;
		this.query = null;
	}

	public void setTerm(String term) {
		this.term = term;
		this.query = null;
	}

	@Override
	public String getDescription() {
		return StringUtils.fastConcat("Term filter: ", field, ": " + term);
	}

	@Override
	public String getCacheKey(SchemaField defaultField, Analyzer analyzer,
			AbstractSearchRequest request) throws ParseException {
		return "QueryFilter - " + getQuery().toString();
	}

	@Override
	public void writeXmlConfig(XmlWriter xmlWriter) throws SAXException {
		xmlWriter.startElement("filter", "negative", isNegative() ? "yes"
				: "no", "field", field);
		xmlWriter.textNode(term);
		xmlWriter.endElement();
	}

	@Override
	public FilterHits getFilterHits(ReaderLocal reader,
			SchemaField defaultField, Analyzer analyzer,
			AbstractSearchRequest request, Timer timer) throws ParseException,
			IOException {
		Query query = getQuery();
		FilterHits filterHits = new FilterHits(query, isNegative(), reader,
				timer);
		return filterHits;
	}

	@Override
	public TermFilter duplicate() {
		return new TermFilter(field, term, isNegative(), getSource(),
				getParamPosition());
	}

	@Override
	public void copyTo(FilterAbstract<?> selectedItem) {
		if (!(selectedItem instanceof TermFilter))
			throw new RuntimeException("Wrong filter type "
					+ selectedItem.getClass().getName());
		super.copyTo(selectedItem);
		TermFilter copyTo = (TermFilter) selectedItem;
		copyTo.field = field;
		copyTo.term = term;
		copyTo.query = null;
	}

	@Override
	final public void setFromServlet(final ServletTransaction transaction,
			final String prefix) {
		String q = transaction.getParameterString(StringUtils.fastConcat(
				prefix, getParamPosition()));
		if (q != null)
			setTerm(q);
	}

	@Override
	final public void setParam(final String param) {
		if (param != null)
			setTerm(param);
	}
}
