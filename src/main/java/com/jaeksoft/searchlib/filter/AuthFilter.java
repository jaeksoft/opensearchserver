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

package com.jaeksoft.searchlib.filter;

import java.io.IOException;
import java.util.Collection;

import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.authentication.AuthManager;
import com.jaeksoft.searchlib.index.ReaderLocal;
import com.jaeksoft.searchlib.query.ParseException;
import com.jaeksoft.searchlib.request.AbstractSearchRequest;
import com.jaeksoft.searchlib.schema.SchemaField;
import com.jaeksoft.searchlib.util.Timer;
import com.jaeksoft.searchlib.util.XmlWriter;
import com.jaeksoft.searchlib.web.ServletTransaction;

public class AuthFilter extends FilterAbstract<AuthFilter> {

	private transient Query query;

	public AuthFilter() {
		super(null, Source.REQUEST, false, null);
	}

	@Override
	final public String getDescription() {
		return "Auth filter";
	}

	@Override
	public void writeXmlConfig(XmlWriter xmlWriter) throws SAXException {
	}

	@Override
	final public String getCacheKey(SchemaField defaultField,
			Analyzer analyzer, AbstractSearchRequest request)
			throws ParseException {
		StringBuilder sb = new StringBuilder(getDescription());
		sb.append(" - ");
		sb.append(request.getUser());
		sb.append(" - ");
		Collection<String> groups = request.getGroups();
		if (groups != null)
			for (String group : groups) {
				sb.append(group);
				sb.append('|');
			}
		return sb.toString();
	}

	private Query getQuery(AbstractSearchRequest request)
			throws ParseException, IOException {
		if (query != null)
			return query;

		AuthManager auth = request.getConfig().getAuthManager();
		String user = request.getUser();
		if (user == null)
			user = "";
		Collection<String> groups = request.getGroups();

		BooleanQuery booleanQuery = new BooleanQuery(true);
		String field = auth.getUserAllowField();
		if (!StringUtils.isEmpty(field))
			booleanQuery
					.add(new TermQuery(new Term(field, user)), Occur.SHOULD);
		field = auth.getUserDenyField();
		if (!StringUtils.isEmpty(field))
			booleanQuery.add(new TermQuery(new Term(field, user)),
					Occur.MUST_NOT);
		if (groups != null) {
			field = auth.getGroupAllowField();
			if (!StringUtils.isEmpty(field))
				for (String group : groups)
					booleanQuery.add(new TermQuery(new Term(field, group)),
							Occur.SHOULD);
			field = auth.getGroupDenyField();
			if (!StringUtils.isEmpty(field))
				for (String group : groups)
					booleanQuery.add(new TermQuery(new Term(field, group)),
							Occur.MUST_NOT);
		}
		query = booleanQuery;
		return query;
	}

	@Override
	public FilterHits getFilterHits(ReaderLocal reader,
			SchemaField defaultField, Analyzer analyzer,
			AbstractSearchRequest request, Timer timer) throws ParseException,
			IOException {
		Query query = getQuery(request);
		FilterHits filterHits = new FilterHits(query, isNegative(), reader,
				timer);
		return filterHits;
	}

	@Override
	public AuthFilter duplicate() {
		return new AuthFilter();
	}

	@Override
	public void copyTo(FilterAbstract<?> selectedItem) {
		if (!(selectedItem instanceof AuthFilter))
			throw new RuntimeException("Wrong filter type "
					+ selectedItem.getClass().getName());
		super.copyTo(selectedItem);
		AuthFilter copyTo = (AuthFilter) selectedItem;
		copyTo.query = null;
	}

	@Override
	public void setFromServlet(ServletTransaction transaction) {
	}

	@Override
	public void setParam(String params) throws SearchLibException {
	}
}
