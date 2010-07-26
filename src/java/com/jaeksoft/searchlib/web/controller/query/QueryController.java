/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2008-2010 Emmanuel Keller / Jaeksoft
 * 
 * http://www.open-search-server.com
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
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Set;

import javax.xml.transform.TransformerConfigurationException;

import org.apache.lucene.queryParser.ParseException;
import org.xml.sax.SAXException;
import org.zkoss.zul.Messagebox;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.request.SearchRequest;
import com.jaeksoft.searchlib.request.SearchRequestMap;
import com.jaeksoft.searchlib.result.Result;
import com.jaeksoft.searchlib.user.User;
import com.jaeksoft.searchlib.web.controller.AlertController;
import com.jaeksoft.searchlib.web.controller.CommonController;
import com.jaeksoft.searchlib.web.controller.PushEvent;
import com.jaeksoft.searchlib.web.controller.ScopeAttribute;

public class QueryController extends CommonController {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3182630816725436838L;

	private String selectedRequestName;

	public QueryController() throws SearchLibException {
		super();
	}

	@Override
	protected void reset() throws SearchLibException {
		selectedRequestName = null;
		setRequest(null);
		setResult(null);
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

	public String getRequestApiCall() throws SearchLibException,
			UnsupportedEncodingException {
		Client client = getClient();
		if (client == null)
			return null;
		SearchRequest request = getRequest();
		if (request == null)
			return null;
		String url = getBaseUrl() + "/search?use="
				+ URLEncoder.encode(client.getIndexName(), "UTF-8");
		if (selectedRequestName != null)
			url += "&qt=" + URLEncoder.encode(selectedRequestName, "UTF-8");
		String q = request.getQueryString();
		if (q == null || q.length() == 0)
			q = "*:*";
		url += "&q=" + URLEncoder.encode(q, "UTF-8");
		User user = getLoggedUser();
		if (user != null)
			url += "&" + user.getApiCallParameters();
		return url;
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
		SearchRequestMap searchRequestMap = client.getSearchRequestMap();
		Set<String> set = searchRequestMap.getNameList();
		if (selectedRequestName == null
				|| searchRequestMap.get(selectedRequestName) == null) {
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
		reloadPage();
	}

	public void onSaveRequest() throws SearchLibException,
			TransformerConfigurationException, IOException, SAXException {
		if (!isSchemaRights())
			throw new SearchLibException("Not allowed");
		Client client = getClient();
		SearchRequest request = getRequest();
		client.getSearchRequestMap().put(request);
		client.saveRequests();
		setSelectedRequest(request.getRequestName());
		PushEvent.REQUEST_LIST_CHANGED.publish(client);
	}

	private class RemoveAlert extends AlertController {

		private String selectedRequest;

		public RemoveAlert(String selectedRequest) throws InterruptedException {
			super("Please, confirm you want to remove the request: "
					+ selectedRequest, Messagebox.CANCEL | Messagebox.YES,
					Messagebox.QUESTION);
			this.selectedRequest = selectedRequest;
		}

		@Override
		public void onYes() throws SearchLibException {
			Client client = getClient();
			client.getSearchRequestMap().remove(selectedRequest);
			client.saveRequests();
			PushEvent.REQUEST_LIST_CHANGED.publish(client);
		}

	}

	public void onRemove() throws SearchLibException,
			TransformerConfigurationException, IOException, SAXException,
			InterruptedException {
		if (!isSchemaRights())
			throw new SearchLibException("Not allowed");
		new RemoveAlert(getSelectedRequest());
	}

	public void onSearch() throws IOException, ParseException, SyntaxError,
			URISyntaxException, ClassNotFoundException, SearchLibException,
			InterruptedException, InstantiationException,
			IllegalAccessException {
		SearchRequest request = getRequest();

		if (request.getQueryString() == null)
			request.setQueryString("*:*");

		request.reset();
		setResult(getClient().search(request));
		reloadPage();
	}

	@Override
	protected void eventRequestListChange() throws SearchLibException {
		reloadPage();
	}

	@Override
	protected void eventSchemaChange() throws SearchLibException {
		reset();
		reloadPage();
	}

}
