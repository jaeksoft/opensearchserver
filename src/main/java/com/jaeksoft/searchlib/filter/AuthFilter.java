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
import java.util.Collection;

import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.analysis.PerFieldAnalyzer;
import com.jaeksoft.searchlib.authentication.AuthManager;
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
			PerFieldAnalyzer analyzer, AbstractSearchRequest request)
			throws ParseException {
		StringBuilder sb = new StringBuilder(getDescription());
		sb.append(" - ");
		if (request == null)
			return sb.toString();
		Collection<String> users = request.getUsers();
		if (users != null) {
			for (String user : users) {
				sb.append(user);
				sb.append('|');
			}
		}
		sb.append(" - ");
		Collection<String> groups = request.getGroups();
		if (groups != null) {
			for (String group : groups) {
				sb.append(group);
				sb.append('|');
			}
		}
		return sb.toString();
	}

	private Query getQuery(AbstractSearchRequest request, AuthManager auth)
			throws ParseException, IOException {
		if (query != null)
			return query;

		Collection<String> users = request.getUsers();
		Collection<String> groups = request.getGroups();

		BooleanQuery booleanQuery = new BooleanQuery(true);
		String field;

		if (users != null) {
			field = auth.getUserAllowField();
			if (!StringUtils.isEmpty(field))
				for (String user : users)
					booleanQuery.add(new TermQuery(new Term(field, user)),
							Occur.SHOULD);
			field = auth.getUserDenyField();
			if (!StringUtils.isEmpty(field))
				for (String user : users)
					booleanQuery.add(new TermQuery(new Term(field, user)),
							Occur.MUST_NOT);
		}

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

		// Logging.info("SECURE QUERY: " + booleanQuery.toString());

		query = booleanQuery;
		return query;
	}

	@Override
	public FilterHits getFilterHits(SchemaField defaultField,
			PerFieldAnalyzer analyzer, AbstractSearchRequest request,
			Timer timer) throws ParseException, IOException, SearchLibException {
		AuthManager auth = request.getConfig().getAuthManager();
		Query query = getQuery(request, auth);
		return new FilterHits(
				getResult(request.getConfig(), query, null, timer),
				isNegative(), timer);
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
	final public void setFromServlet(final ServletTransaction transaction,
			final String prefix) {
	}

	@Override
	final public void setParam(final String params) throws SearchLibException {
	}

	@Override
	public void reset() {
		query = null;
	}
}
