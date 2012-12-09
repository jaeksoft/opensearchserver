/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2011-2012 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.webservice;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import javax.naming.NamingException;
import javax.xml.ws.WebServiceException;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.ClientCatalog;
import com.jaeksoft.searchlib.ClientFactory;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.analysis.LanguageEnum;
import com.jaeksoft.searchlib.collapse.CollapseParameters;
import com.jaeksoft.searchlib.facet.FacetField;
import com.jaeksoft.searchlib.facet.FacetFieldList;
import com.jaeksoft.searchlib.filter.FilterAbstract;
import com.jaeksoft.searchlib.filter.FilterList;
import com.jaeksoft.searchlib.filter.QueryFilter;
import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.query.ParseException;
import com.jaeksoft.searchlib.request.ReturnField;
import com.jaeksoft.searchlib.request.ReturnFieldList;
import com.jaeksoft.searchlib.request.SearchRequest;
import com.jaeksoft.searchlib.result.AbstractResultSearch;
import com.jaeksoft.searchlib.snippet.SnippetField;
import com.jaeksoft.searchlib.snippet.SnippetFieldList;
import com.jaeksoft.searchlib.sort.SortField;
import com.jaeksoft.searchlib.sort.SortFieldList;

public class SelectImpl extends CommonServicesImpl implements Select {

	@Override
	public SelectResult search(String q, String qt, String use, String login,
			String key, int start, int rows, LanguageEnum lang,
			String collapseField, int collapseMax,
			CollapseParameters.Mode collapseMode,
			CollapseParameters.Type collapseType, List<String> filterQuery,
			List<String> filterQueryNegetive, List<String> sort,
			List<String> returnField, boolean withDocs, List<String> highlight,
			List<String> facet, List<String> facetCollapse,
			List<String> facetMulti, List<String> facetMultiCollapse,
			boolean moreLikeThis, String mltDocQuery, int mltMinWordLen,
			int mltMaxWordLen, int mltMinDocFeq, int mltMinTermFreq,
			String mltStopWords, List<String> customLogs, boolean log,
			boolean delete) {
		try {
			ClientFactory.INSTANCE.properties.checkApi();
			Client client = ClientCatalog.getClient(use);
			if (isLogged(use, login, key)) {
				SearchRequest searchRequest = getSearchRequest(client, q, qt,
						use, start, rows, lang, collapseField, collapseMax,
						collapseMode, collapseType, filterQuery,
						filterQueryNegetive, sort, returnField, withDocs,
						highlight, facet, facetCollapse, facetMulti,
						facetMultiCollapse, moreLikeThis, mltDocQuery,
						mltMinWordLen, mltMaxWordLen, mltMinDocFeq,
						mltMinTermFreq, mltStopWords, customLogs, log, delete);

				return new SelectResult(
						(AbstractResultSearch) client.request(searchRequest));
			} else
				throw new WebServiceException("Bad Credential");

		} catch (SearchLibException e) {
			throw new WebServiceException(e);
		} catch (NamingException e) {
			throw new WebServiceException(e);
		} catch (SyntaxError e) {
			throw new WebServiceException(e);
		} catch (IOException e) {
			throw new WebServiceException(e);
		} catch (InstantiationException e) {
			throw new WebServiceException(e);
		} catch (IllegalAccessException e) {
			throw new WebServiceException(e);
		} catch (ClassNotFoundException e) {
			throw new WebServiceException(e);
		} catch (ParseException e) {
			throw new WebServiceException(e);
		} catch (URISyntaxException e) {
			throw new WebServiceException(e);
		} catch (InterruptedException e) {
			throw new WebServiceException(e);
		}
	}

	private SearchRequest getSearchRequest(Client client, String q, String qt,
			String use, int start, int rows, LanguageEnum lang,
			String collapseField, int collapseMax,
			CollapseParameters.Mode collapseMode,
			CollapseParameters.Type collapseType, List<String> filterQuery,
			List<String> filterQueryNegetive, List<String> sort,
			List<String> returnField, boolean withDocs, List<String> highlight,
			List<String> facet, List<String> facetCollapse,
			List<String> facetMulti, List<String> facetMultiCollapse,
			boolean MoreLikeThis, String mltDocQuery, int mltMinWordLen,
			int mltMaxWordLen, int mltMinDocFeq, int mltMinTermFreq,
			String mltStopWords, List<String> customLogs, boolean log,
			boolean delete) throws SearchLibException, SyntaxError,
			IOException, InstantiationException, IllegalAccessException,
			ClassNotFoundException, ParseException, URISyntaxException,
			InterruptedException {
		SearchRequest searchRequest = (SearchRequest) client.getNewRequest(qt);
		searchRequest.setQueryString(q);
		searchRequest.setStart(start);
		Boolean b;
		if (rows != 0)
			searchRequest.setRows(rows);
		else
			searchRequest.setRows(searchRequest.getRows());
		if (lang != null)
			searchRequest.setLang(lang);
		if (collapseField != null && !collapseField.equals(""))
			searchRequest.setCollapseField(collapseField);
		if (collapseMax != 0)
			searchRequest.setCollapseMax(collapseMax);
		if (collapseMode != null)
			searchRequest.setCollapseMode(collapseMode);
		if (collapseType != null)
			searchRequest.setCollapseType(collapseType);
		if (filterQuery != null && filterQuery.size() > 0) {
			FilterList fl = searchRequest.getFilterList();
			for (String value : filterQuery)
				if (value != null && !value.equals(""))
					if (value.trim().length() > 0)
						fl.add(new QueryFilter(value, false,
								FilterAbstract.Source.REQUEST, null));
		}

		if (filterQueryNegetive != null && filterQueryNegetive.size() > 0) {
			FilterList fl = searchRequest.getFilterList();
			for (String value : filterQueryNegetive)
				if (value != null)
					if (value.trim().length() > 0)
						fl.add(new QueryFilter(value, true,
								FilterAbstract.Source.REQUEST, null));
		}
		if (sort != null && sort.size() > 0) {
			SortFieldList sortFieldList = searchRequest.getSortFieldList();
			for (String value : sort)
				if (value != null && !value.equals(""))
					sortFieldList.put(new SortField(value));
		}
		if (returnField != null && returnField.size() > 0) {
			ReturnFieldList rf = searchRequest.getReturnFieldList();
			for (String value : returnField)
				if (value != null)
					if (value.trim().length() > 0)
						rf.put(new ReturnField(client.getSchema()
								.getFieldList().get(value).getName()));
		}
		if (highlight != null && highlight.size() > 0) {
			SnippetFieldList snippetFields = searchRequest
					.getSnippetFieldList();
			for (String value : highlight)
				if (value != null && !value.equals(""))
					snippetFields.put(new SnippetField(client.getSchema()
							.getFieldList().get(value).getName()));
		}
		if (facet != null && facet.size() > 0) {
			FacetFieldList facetList = searchRequest.getFacetFieldList();
			for (String value : facet)
				if (value != null && !value.equals(""))
					facetList.put(FacetField.buildFacetField(value, false,
							false));
		}
		if (facetCollapse != null && facetCollapse.size() > 0) {
			FacetFieldList facetList = searchRequest.getFacetFieldList();
			for (String value : facetCollapse)
				if (value != null && !value.equals(""))
					facetList.put(FacetField
							.buildFacetField(value, false, true));
		}
		if (facetMulti != null && facetMulti.size() > 0) {
			FacetFieldList facetList = searchRequest.getFacetFieldList();
			for (String value : facetMulti)
				if (value != null && !value.equals(""))
					facetList.put(FacetField
							.buildFacetField(value, true, false));
		}
		if (facetMultiCollapse != null && facetMultiCollapse.size() > 0) {
			FacetFieldList facetList = searchRequest.getFacetFieldList();
			for (String value : facetMultiCollapse)
				if (value != null && !value.equals(""))
					facetList
							.put(FacetField.buildFacetField(value, true, true));
		}

		// TODO MoreLikeThis request

		// if ((b = MoreLikeThis) != null)
		// searchRequest.setMoreLikeThis(b);
		// if (mltDocQuery != null && !mltDocQuery.equals(""))
		// searchRequest.setMoreLikeThisDocQuery(mltDocQuery);
		//
		// if ((i = mltMinWordLen) != null)
		// searchRequest.setMoreLikeThisMinWordLen(i);
		//
		// if ((i = mltMaxWordLen) != null)
		// searchRequest.setMoreLikeThisMaxWordLen(i);
		//
		// if ((i = mltMinDocFeq) != null)
		// searchRequest.setMoreLikeThisMinDocFreq(i);
		//
		// if ((i = mltMinTermFreq) != null)
		// searchRequest.setMoreLikeThisMinTermFreq(i);
		//
		// if (mltStopWords != null && !mltStopWords.equals(""))
		// searchRequest.setMoreLikeThisStopWords(mltStopWords);

		if ((b = log) != null)
			searchRequest.setLogReport(true);

		if (searchRequest.isLogReport()) {
			for (String logString : customLogs) {
				if (logString != null && logString.equals(""))
					searchRequest.addCustomLog(logString);
			}
		}

		if ((b = delete) != null && b) {
			client.deleteDocuments(searchRequest);

		}
		return searchRequest;
	}
}
