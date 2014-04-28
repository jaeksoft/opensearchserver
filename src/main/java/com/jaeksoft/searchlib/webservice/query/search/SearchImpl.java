/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2011-2014 Emmanuel Keller / Jaeksoft
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

import javax.ws.rs.core.UriInfo;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.ClientFactory;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.request.AbstractSearchRequest;
import com.jaeksoft.searchlib.request.RequestTypeEnum;
import com.jaeksoft.searchlib.request.SearchFieldRequest;
import com.jaeksoft.searchlib.request.SearchPatternRequest;
import com.jaeksoft.searchlib.result.AbstractResultSearch;
import com.jaeksoft.searchlib.user.Role;
import com.jaeksoft.searchlib.webservice.CommonResult;
import com.jaeksoft.searchlib.webservice.query.CommonQuery;
import com.jaeksoft.searchlib.webservice.query.QueryTemplateResultList;

public class SearchImpl extends CommonQuery implements RestSearch {

	@Override
	public QueryTemplateResultList searchTemplateList(UriInfo uriInfo,
			String index, String login, String key) {
		return super.queryTemplateList(uriInfo, index, login, key,
				RequestTypeEnum.SearchRequest,
				RequestTypeEnum.SearchFieldRequest);
	}

	@Override
	public SearchTemplateResult searchTemplateGet(UriInfo uriInfo,
			String index, String login, String key, String template) {
		AbstractSearchRequest searchRequest = (AbstractSearchRequest) super
				.queryTemplateGet(uriInfo, index, login, key, template,
						RequestTypeEnum.SearchRequest,
						RequestTypeEnum.SearchFieldRequest);
		return new SearchTemplateResult(searchRequest);
	}

	@Override
	public CommonResult searchTemplateDelete(UriInfo uriInfo, String index,
			String login, String key, String template) {
		return queryTemplateDelete(uriInfo, index, login, key, template,
				RequestTypeEnum.SearchRequest,
				RequestTypeEnum.SearchFieldRequest);
	}

	@Override
	public SearchResult searchPatternTemplate(UriInfo uriInfo, String index,
			String login, String key, String template, SearchPatternQuery query) {
		try {
			SearchPatternRequest searchRequest = (SearchPatternRequest) super
					.queryTemplateGet(uriInfo, index, login, key, template,
							RequestTypeEnum.SearchRequest);
			if (query != null)
				query.apply(searchRequest);
			return new SearchResult(
					(AbstractResultSearch) client.request(searchRequest));
		} catch (SearchLibException e) {
			throw new CommonServiceException(e);
		}
	}

	@Override
	public CommonResult searchPatternTemplateSet(UriInfo uriInfo, String index,
			String login, String key, String template, SearchPatternQuery query) {
		Client client = getLoggedClient(uriInfo, index, login, key,
				Role.INDEX_UPDATE);
		SearchPatternRequest searchRequest = new SearchPatternRequest(client);
		return queryTemplateSet(client, index, login, key, template, query,
				searchRequest);
	}

	@Override
	public SearchResult searchFieldTemplate(UriInfo uriInfo, String index,
			String login, String key, String template, SearchFieldQuery query) {
		try {
			SearchFieldRequest searchRequest = (SearchFieldRequest) super
					.queryTemplateGet(uriInfo, index, login, key, template,
							RequestTypeEnum.SearchFieldRequest);
			if (query != null)
				query.apply(searchRequest);
			return new SearchResult(
					(AbstractResultSearch) client.request(searchRequest));
		} catch (SearchLibException e) {
			throw new CommonServiceException(e);
		}
	}

	@Override
	public CommonResult searchFieldTemplateSet(UriInfo uriInfo, String index,
			String login, String key, String template, SearchFieldQuery query) {
		Client client = getLoggedClient(uriInfo, index, login, key,
				Role.INDEX_UPDATE);
		SearchFieldRequest searchRequest = new SearchFieldRequest(client);
		return queryTemplateSet(client, index, login, key, template, query,
				searchRequest);
	}

	@Override
	public SearchResult searchPattern(UriInfo uriInfo, String index,
			String login, String key, SearchPatternQuery query) {
		try {
			Client client = getLoggedClientAnyRole(uriInfo, index, login, key,
					Role.GROUP_INDEX);
			ClientFactory.INSTANCE.properties.checkApi();
			SearchPatternRequest searchRequest = new SearchPatternRequest(
					client);
			if (query != null)
				query.apply(searchRequest);
			return new SearchResult(
					(AbstractResultSearch) client.request(searchRequest));
		} catch (InterruptedException e) {
			throw new CommonServiceException(e);
		} catch (IOException e) {
			throw new CommonServiceException(e);
		} catch (SearchLibException e) {
			throw new CommonServiceException(e);
		}
	}

	@Override
	public SearchResult searchField(UriInfo uriInfo, String index,
			String login, String key, SearchFieldQuery query) {
		try {
			Client client = getLoggedClientAnyRole(uriInfo, index, login, key,
					Role.GROUP_INDEX);
			ClientFactory.INSTANCE.properties.checkApi();
			SearchFieldRequest searchRequest = new SearchFieldRequest(client);
			if (query != null)
				query.apply(searchRequest);
			return new SearchResult(
					(AbstractResultSearch) client.request(searchRequest));
		} catch (InterruptedException e) {
			throw new CommonServiceException(e);
		} catch (IOException e) {
			throw new CommonServiceException(e);
		} catch (SearchLibException e) {
			throw new CommonServiceException(e);
		}
	}

}
