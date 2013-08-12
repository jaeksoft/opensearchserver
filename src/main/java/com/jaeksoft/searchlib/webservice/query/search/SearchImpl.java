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

package com.jaeksoft.searchlib.webservice.query.search;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.ClientFactory;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.analysis.LanguageEnum;
import com.jaeksoft.searchlib.collapse.CollapseParameters;
import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.query.ParseException;
import com.jaeksoft.searchlib.request.AbstractSearchRequest;
import com.jaeksoft.searchlib.request.RequestTypeEnum;
import com.jaeksoft.searchlib.result.AbstractResultSearch;
import com.jaeksoft.searchlib.user.Role;
import com.jaeksoft.searchlib.webservice.query.CommonQuery;
import com.jaeksoft.searchlib.webservice.query.QueryTemplateResultList;
import com.jaeksoft.searchlib.webservice.query.search.SearchQueryAbstract.OperatorEnum;

public class SearchImpl extends CommonQuery implements RestSearch, SoapSearch {

	@Override
	public QueryTemplateResultList searchTemplateList(String index,
			String login, String key) {
		return super.queryTemplateList(index, login, key,
				RequestTypeEnum.SearchRequest,
				RequestTypeEnum.SearchFieldRequest);
	}

	@Override
	public SearchTemplateResult searchTemplateGet(String index, String login,
			String key, String template) {
		AbstractSearchRequest searchRequest = (AbstractSearchRequest) super
				.searchTemplateGet(index, login, key, template,
						RequestTypeEnum.SearchRequest,
						RequestTypeEnum.SearchFieldRequest);
		return new SearchTemplateResult(searchRequest);
	}

	@Override
	public SearchResult search(String index, String login, String key,
			String template, String query, Integer start, Integer rows,
			LanguageEnum lang, OperatorEnum operator, String collapseField,
			Integer collapseMax, CollapseParameters.Mode collapseMode,
			CollapseParameters.Type collapseType, List<String> filter,
			List<String> negativeFilter, List<String> sort,
			List<String> returnedField, List<String> snippetField,
			List<String> facet, List<String> facetCollapse,
			List<String> facetMulti, List<String> facetMultiCollapse,
			List<String> filterParams, List<String> joinParams,
			Boolean enableLog, List<String> customLog) {
		try {
			Client client = getLoggedClient(index, login, key, Role.INDEX_QUERY);
			ClientFactory.INSTANCE.properties.checkApi();
			AbstractSearchRequest searchRequest = getSearchRequest(client,
					template, query, start, rows, lang, operator,
					collapseField, collapseMax, collapseMode, collapseType,
					filter, negativeFilter, sort, returnedField, snippetField,
					facet, facetCollapse, facetMulti, facetMultiCollapse,
					filterParams, joinParams, enableLog, customLog);
			return new SearchResult(
					(AbstractResultSearch) client.request(searchRequest));
		} catch (SearchLibException e) {
			throw new CommonServiceException(e);
		} catch (SyntaxError e) {
			throw new CommonServiceException(e);
		} catch (IOException e) {
			throw new CommonServiceException(e);
		} catch (InstantiationException e) {
			throw new CommonServiceException(e);
		} catch (IllegalAccessException e) {
			throw new CommonServiceException(e);
		} catch (ClassNotFoundException e) {
			throw new CommonServiceException(e);
		} catch (ParseException e) {
			throw new CommonServiceException(e);
		} catch (URISyntaxException e) {
			throw new CommonServiceException(e);
		} catch (InterruptedException e) {
			throw new CommonServiceException(e);
		}
	}

	@Override
	@POST
	@Consumes({ "application/json", "application/xml" })
	@Produces({ "application/json", "application/xml" })
	@Path("/{template_name}")
	public SearchResult searchTemplate(@PathParam("index_name") String index,
			@QueryParam("login") String login, @QueryParam("key") String key,
			@PathParam("template_name") String template,
			SearchPatternQuery query) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@POST
	@Consumes({ "application/json", "application/xml" })
	@Produces({ "application/json", "application/xml" })
	@Path("/")
	public SearchResult searchTemplate(@PathParam("index_name") String index,
			@QueryParam("login") String login, @QueryParam("key") String key,
			@PathParam("template_name") String template, SearchFieldQuery query) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@POST
	@Consumes({ "application/json", "application/xml" })
	@Produces({ "application/json", "application/xml" })
	@Path("/")
	public SearchResult search(@PathParam("index_name") String index,
			@QueryParam("login") String login, @QueryParam("key") String key,
			SearchPatternQuery query) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@POST
	@Consumes({ "application/json", "application/xml" })
	@Produces({ "application/json", "application/xml" })
	@Path("/")
	public SearchResult search(@PathParam("index_name") String index,
			@QueryParam("login") String login, @QueryParam("key") String key,
			SearchFieldQuery query) {
		// TODO Auto-generated method stub
		return null;
	}

}
