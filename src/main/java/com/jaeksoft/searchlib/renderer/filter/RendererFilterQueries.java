/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2014-2015 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.renderer.filter;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections.CollectionUtils;

import com.jaeksoft.searchlib.facet.FacetCounter;
import com.jaeksoft.searchlib.query.ParseException;
import com.jaeksoft.searchlib.request.AbstractSearchRequest;
import com.jaeksoft.searchlib.util.StringUtils;

public class RendererFilterQueries {

	final Map<String, Set<String>> filterTerms;
	final Map<String, String> filterQueries;

	public RendererFilterQueries() {
		filterTerms = new TreeMap<String, Set<String>>();
		filterQueries = new TreeMap<String, String>();
	}

	public Set<String> getTermSet(String fieldName) {
		Set<String> queries = filterTerms.get(fieldName);
		if (queries != null)
			return queries;
		queries = new TreeSet<String>();
		filterTerms.put(fieldName, queries);
		return queries;
	}

	public Set<String> getTermsFilterSet() {
		return filterTerms.keySet();
	}

	public boolean isEmpty() {
		for (String filterQuery : filterQueries.values())
			if (!StringUtils.isEmpty(filterQuery))
				return false;
		for (Set<String> termSet : filterTerms.values())
			if (!termSet.isEmpty())
				return false;
		return true;
	}

	public boolean isEmpty(String fieldName) {
		Set<String> queries = filterTerms.get(fieldName);
		if (queries == null)
			return true;
		return queries.isEmpty();
	}

	private void addTerm(String fieldName, String[] values) {
		if (values == null)
			return;
		Set<String> queries = getTermSet(fieldName);
		for (String value : values)
			queries.add(value);
	}

	private void removeTerm(String fieldName, String[] values) {
		if (values == null)
			return;
		Set<String> queries = getTermSet(fieldName);
		for (String value : values)
			queries.remove(value);
	}

	private void setQuery(String fieldName, String query) {
		if (StringUtils.isEmpty(query))
			filterQueries.remove(fieldName);
		else
			filterQueries.put(fieldName, query);
	}

	public void applyServletRequest(HttpServletRequest servletRequest) {
		if (servletRequest.getParameter("fqc") != null) {
			filterQueries.clear();
			filterTerms.clear();
		}
		Enumeration<String> en = servletRequest.getParameterNames();
		if (en != null) {
			while (en.hasMoreElements()) {
				String parm = en.nextElement();
				if (parm.startsWith("fqa."))
					addTerm(parm.substring(4),
							servletRequest.getParameterValues(parm));
				if (parm.startsWith("fqr."))
					removeTerm(parm.substring(4),
							servletRequest.getParameterValues(parm));
				if (parm.startsWith("fq."))
					setQuery(parm.substring(4),
							servletRequest.getParameter(parm));
			}
		}
	}

	public void applyToSearchRequest(AbstractSearchRequest searchRequest)
			throws ParseException {
		for (Map.Entry<String, Set<String>> entry : filterTerms.entrySet()) {
			String fieldName = entry.getKey();
			Set<String> queries = entry.getValue();
			if (CollectionUtils.isEmpty(queries))
				continue;
			if (queries.size() == 1) {
				searchRequest.addTermFilter(fieldName, queries.iterator()
						.next(), false);
				continue;
			}
			StringBuilder sb = new StringBuilder(fieldName);
			sb.append(":(");
			boolean bFirst = true;
			for (String query : queries) {
				if (bFirst)
					bFirst = false;
				else
					sb.append(" OR ");
				sb.append('"');
				sb.append(StringUtils.replace(query, "\"", "\\\""));
				sb.append('"');
			}
			sb.append(')');
			searchRequest.addFilter(sb.toString(), false);
		}
		for (String query : filterQueries.values())
			searchRequest.addFilter(query, false);
	}

	private boolean contains(String fieldName, String query) {
		if (query == null)
			return false;
		String q = filterQueries.get(fieldName);
		if (q == null)
			return false;
		return q.equals(query);
	}

	private boolean containsAny(String fieldName, String term) {
		if (term == null)
			return false;
		Set<String> termSet = filterTerms.get(fieldName);
		if (termSet == null)
			return false;
		return termSet.contains(term);
	}

	private boolean containsAny(String fieldName, Collection<String> terms) {
		if (terms == null)
			return false;
		Set<String> termSet = filterTerms.get(fieldName);
		if (termSet == null)
			return false;
		for (String term : terms)
			if (termSet.contains(term))
				return true;
		return false;
	}

	public boolean contains(String fieldName, RendererFilterItem filterItem) {
		if (filterItem.isQuery())
			return contains(fieldName, filterItem.getQuery());
		if (filterItem.isTerms())
			return containsAny(fieldName, filterItem.getTerms());
		return false;
	}

	public boolean contains(String fieldName,
			Map.Entry<String, FacetCounter> facetItem) {
		return containsAny(fieldName, facetItem.getKey());
	}

	private StringBuilder append(StringBuilder sb, boolean current,
			String fieldName, String term) throws UnsupportedEncodingException {
		sb.append("&amp;");
		if (current)
			sb.append("fqr.");
		else
			sb.append("fqa.");
		sb.append(URLEncoder.encode(fieldName, "UTF-8"));
		sb.append('=');
		sb.append(URLEncoder.encode(term, "UTF-8"));
		return sb;
	}

	private String getFilterParamQuery(boolean current, String fieldName,
			String query) throws UnsupportedEncodingException {
		if (query == null)
			return StringUtils.EMPTY;
		return StringUtils.fastConcat("&amp;fq.",
				URLEncoder.encode(fieldName, "UTF-8"), '=',
				URLEncoder.encode(query, "UTF-8"));
	}

	public String getFilterClearParam(String fieldName)
			throws UnsupportedEncodingException {
		return StringUtils.fastConcat("&amp;fqc.",
				URLEncoder.encode(fieldName, "UTF-8"));
	}

	private String getFilterParam(boolean current, String fieldName,
			Collection<String> terms) throws UnsupportedEncodingException {
		if (terms == null)
			return StringUtils.EMPTY;
		StringBuilder sb = new StringBuilder();
		for (String term : terms)
			append(sb, current, fieldName, term);
		return sb.toString();
	}

	public String getFilterParamTerm(boolean current, String fieldName,
			String term) throws UnsupportedEncodingException {
		if (term == null)
			return StringUtils.EMPTY;
		return append(new StringBuilder(), current, fieldName, term).toString();
	}

	public String getFilterParam(boolean current, String fieldName,
			RendererFilterItem filterItem) throws UnsupportedEncodingException {
		if (filterItem.isQuery())
			return getFilterParamQuery(current, fieldName,
					filterItem.getQuery());
		if (filterItem.isTerms())
			return getFilterParam(current, fieldName, filterItem.getTerms());
		return StringUtils.EMPTY;
	}

	public String getFilterParam(boolean current, String fieldName,
			Map.Entry<String, FacetCounter> facetItem)
			throws UnsupportedEncodingException {
		return getFilterParamTerm(current, fieldName, facetItem.getKey());
	}
}
