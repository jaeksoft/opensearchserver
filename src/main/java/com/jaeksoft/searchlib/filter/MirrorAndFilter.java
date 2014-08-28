/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2012-2014 Emmanuel Keller / Jaeksoft
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

import org.apache.lucene.search.Query;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.analysis.PerFieldAnalyzer;
import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.query.ParseException;
import com.jaeksoft.searchlib.request.AbstractSearchRequest;
import com.jaeksoft.searchlib.schema.SchemaField;
import com.jaeksoft.searchlib.util.Timer;
import com.jaeksoft.searchlib.util.XPathParser;
import com.jaeksoft.searchlib.util.XmlWriter;
import com.jaeksoft.searchlib.web.ServletTransaction;
import com.jaeksoft.searchlib.webservice.query.search.SearchQueryAbstract.OperatorEnum;

public class MirrorAndFilter extends FilterAbstract<MirrorAndFilter> {

	private transient Query query;

	public MirrorAndFilter() {
		this(Source.REQUEST, false, null);
	}

	public MirrorAndFilter(Source source, boolean negative, String paramPosition) {
		super(FilterType.MIRROR_AND_FILTER, source, negative, paramPosition);
		query = null;
	}

	public MirrorAndFilter(XPathParser xpp, Node node) {
		super(FilterType.MIRROR_AND_FILTER, Source.CONFIGXML, "yes"
				.equals(XPathParser.getAttributeString(node, "negative")), null);
	}

	@Override
	public String getDescription() {
		StringBuilder sb = new StringBuilder("Mirror AND filter: ");
		return sb.toString();
	}

	@Override
	public void writeXmlConfig(XmlWriter xmlWriter) throws SAXException {
		xmlWriter.startElement("mirrorAndFilter");
		xmlWriter.endElement();
	}

	@Override
	public String getCacheKey(SchemaField defaultField,
			PerFieldAnalyzer analyzer, AbstractSearchRequest request)
			throws ParseException, SyntaxError, SearchLibException, IOException {
		StringBuilder sb = new StringBuilder("MirrorAndFilter - ");
		sb.append(getQuery(request).toString());
		return sb.toString();
	}

	private Query getQuery(AbstractSearchRequest request)
			throws ParseException, SyntaxError, SearchLibException, IOException {
		if (query != null)
			return query;
		try {
			request = (AbstractSearchRequest) request.duplicate();
			request.setDefaultOperator(OperatorEnum.AND);
			query = request.getNotBoostedQuery();
			return query;
		} catch (InstantiationException e) {
			throw new SearchLibException(e);
		} catch (IllegalAccessException e) {
			throw new SearchLibException(e);
		}
	}

	@Override
	public FilterHits getFilterHits(SchemaField defaultField,
			PerFieldAnalyzer analyzer, AbstractSearchRequest request,
			Timer timer) throws ParseException, IOException,
			SearchLibException, SyntaxError {
		Query query = getQuery(request);
		return new FilterHits(
				getResult(request.getConfig(), query, null, timer),
				isNegative(), timer);
	}

	@Override
	public MirrorAndFilter duplicate() {
		return new MirrorAndFilter(getSource(), isNegative(),
				getParamPosition());
	}

	@Override
	public void copyTo(FilterAbstract<?> selectedItem) {
		if (!(selectedItem instanceof MirrorAndFilter))
			throw new RuntimeException("Wrong filter type "
					+ selectedItem.getClass().getName());
		super.copyTo(selectedItem);
		MirrorAndFilter copyTo = (MirrorAndFilter) selectedItem;
		copyTo.query = null;
	}

	@Override
	final public void setFromServlet(final ServletTransaction transaction,
			final String prefix) {
	}

	@Override
	public void setParam(String params) throws SearchLibException {
	}

	@Override
	public boolean isDistance() {
		return false;
	}

	@Override
	public void reset() {
		query = null;
	}
}
