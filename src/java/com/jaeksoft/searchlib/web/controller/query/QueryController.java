/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2008-2009 Emmanuel Keller / Jaeksoft
 * 
 * http://www.jaeksoft.com
 * 
 * This file is part of Jaeksoft OpenSearchServer.
 *
 * Jaeksoft OpenSearchServer is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * Jaeksoft OpenSearchServer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Jaeksoft OpenSearchServer. 
 *  If not, see <http://www.gnu.org/licenses/>.
 **/

package com.jaeksoft.searchlib.web.controller.query;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.queryParser.ParseException;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.request.SearchRequest;
import com.jaeksoft.searchlib.result.Result;
import com.jaeksoft.searchlib.web.controller.CommonController;
import com.jaeksoft.searchlib.web.controller.ScopeAttribute;

public class QueryController extends CommonController {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3182630816725436838L;

	private String selectedRequestName = null;

	public QueryController() throws SearchLibException {
		super();
	}

	public SearchRequest getRequest() throws SearchLibException {
		SearchRequest request = (SearchRequest) getAttribute(ScopeAttribute.QUERY_SEARCH_REQUEST);
		if (request != null)
			return request;
		Client client = getClient();
		if (client == null)
			return null;
		request = client.getNewSearchRequest();
		setRequest(request);
		return request;
	}

	public void setRequest(SearchRequest request) {
		setAttribute(ScopeAttribute.QUERY_SEARCH_REQUEST, request);
	}

	public void setSelectedRequest(String requestName) {
		this.selectedRequestName = requestName;
	}

	public String getSelectedRequest() {
		return selectedRequestName;
	}

	public Set<String> getRequests() throws SearchLibException {
		Client client = getClient();
		if (client == null)
			return null;
		Map<String, SearchRequest> map = client.getSearchRequestMap();
		if (map == null)
			return null;
		Set<String> set = map.keySet();
		if (selectedRequestName == null || map.get(selectedRequestName) == null) {
			Iterator<String> it = set.iterator();
			if (it.hasNext())
				setSelectedRequest(it.next());
		}
		return set;
	}

	public boolean getResultExists() {
		return getResult() != null;
	}

	public Result getResult() {
		return (Result) getAttribute(ScopeAttribute.QUERY_SEARCH_RESULT);
	}

	public void setResult(Result result) {
		setAttribute(ScopeAttribute.QUERY_SEARCH_RESULT, result);
	}

	public void onLoadRequest() throws SearchLibException {
		setRequest(getClient().getNewSearchRequest(selectedRequestName));
		reloadDesktop();
	}

	public void onSearch() throws IOException, ParseException, SyntaxError,
			URISyntaxException, ClassNotFoundException, SearchLibException,
			InterruptedException {
		SearchRequest request = getRequest();
		request.reset();
		setResult(getClient().search(request));
		reloadDesktop();
	}
}
