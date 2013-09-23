/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008-2012 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.web.controller.query;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.transform.TransformerConfigurationException;

import org.xml.sax.SAXException;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zul.Messagebox;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.query.ParseException;
import com.jaeksoft.searchlib.request.AbstractRequest;
import com.jaeksoft.searchlib.request.AbstractSearchRequest;
import com.jaeksoft.searchlib.request.MoreLikeThisRequest;
import com.jaeksoft.searchlib.request.RequestTypeEnum;
import com.jaeksoft.searchlib.request.SpellCheckRequest;
import com.jaeksoft.searchlib.result.AbstractResult;
import com.jaeksoft.searchlib.web.AbstractServlet;
import com.jaeksoft.searchlib.web.controller.AlertController;
import com.jaeksoft.searchlib.web.controller.PushEvent;
import com.jaeksoft.searchlib.web.controller.ScopeAttribute;

public final class QueryController extends AbstractQueryController {

	private transient String requestName;

	private transient RequestTypeEnum requestType;

	private transient Entry<String, AbstractRequest> selectedRequest;

	public QueryController() throws SearchLibException {
		super();
		reset();
	}

	@Override
	protected void reset() throws SearchLibException {
		selectedRequest = null;
		requestType = RequestTypeEnum.SearchFieldRequest;
		setAttribute(ScopeAttribute.QUERY_REQUEST, null);
		setAttribute(ScopeAttribute.QUERY_RESULT, null);
	}

	public String getRequestApiCall() throws SearchLibException,
			UnsupportedEncodingException {
		Client client = getClient();
		if (client == null)
			return null;
		AbstractRequest request = getAbstractRequest();
		if (request == null)
			return null;
		StringBuffer sb = AbstractServlet.getApiUrl(getBaseUrl(), "/select",
				client, getLoggedUser());
		String requestName = request.getRequestName();
		if (requestName != null && requestName.length() > 0) {
			sb.append("&qt=");
			sb.append(URLEncoder.encode(requestName, "UTF-8"));
		}
		if (request instanceof AbstractSearchRequest) {
			String q = ((AbstractSearchRequest) request).getQueryString();
			if (q == null || q.length() == 0)
				q = "*:*";
			sb.append("&q=");
			sb.append(URLEncoder.encode(q, "UTF-8"));
		} else if (request instanceof SpellCheckRequest) {
			String q = ((SpellCheckRequest) request).getQueryString();
			sb.append("&q=");
			if (q != null)
				sb.append(URLEncoder.encode(q, "UTF-8"));
		} else if (request instanceof MoreLikeThisRequest) {
			String q = ((MoreLikeThisRequest) request).getDocQuery();
			if (q != null && q.length() > 0) {
				sb.append("&mlt.docquery=");
				sb.append(URLEncoder.encode(q, "UTF-8"));
			}
			q = ((MoreLikeThisRequest) request).getLikeText();
			if (q != null && q.length() > 0) {
				sb.append("&mlt.liketext=");
				sb.append(URLEncoder.encode(q, "UTF-8"));
			}
		}
		return sb.toString();
	}

	public void setRequest(AbstractRequest request) {
		setAttribute(ScopeAttribute.QUERY_REQUEST, request);
		PushEvent.eventEditRequest.publish(request);
	}

	private boolean isEditing(RequestTypeEnum type) throws SearchLibException {
		AbstractRequest request = getAbstractRequest();
		if (request == null)
			return false;
		return type == request.getType();
	}

	public boolean isEditing() throws SearchLibException {
		return getAbstractRequest() != null;
	}

	public boolean isNotEditing() throws SearchLibException {
		return !isEditing();
	}

	public boolean isEditingSearchPattern() throws SearchLibException {
		return isEditing(RequestTypeEnum.SearchRequest);
	}

	public boolean isNotEditingSearchPattern() throws SearchLibException {
		return !isEditingSearchPattern();
	}

	public boolean isEditingSearchField() throws SearchLibException {
		return isEditing(RequestTypeEnum.SearchFieldRequest);
	}

	public boolean isNotEditingSearchField() throws SearchLibException {
		return !isEditingSearchField();
	}

	public boolean isEditingNamedEntityExtraction() throws SearchLibException {
		return isEditing(RequestTypeEnum.NamedEntityExtractionRequest);
	}

	public boolean isEditingSpellCheck() throws SearchLibException {
		return isEditing(RequestTypeEnum.SpellCheckRequest);
	}

	public boolean isNotEditingSpellCheck() throws SearchLibException {
		return !isEditingSpellCheck();
	}

	public boolean isEditingMoreLikeThis() throws SearchLibException {
		return isEditing(RequestTypeEnum.MoreLikeThisRequest);
	}

	public boolean isNotEditingMoreLikeThis() throws SearchLibException {
		return !isEditingMoreLikeThis();
	}

	public boolean isEditingDocuments() throws SearchLibException {
		return isEditing(RequestTypeEnum.DocumentsRequest);
	}

	public boolean isNotEditingDocuments() throws SearchLibException {
		return !isEditingDocuments();
	}

	public boolean isNotRequestSelected() {
		return !isRequestSelected();
	}

	public boolean isRequestSelected() {
		return selectedRequest != null;
	}

	public Entry<String, AbstractRequest> getSelectedRequest() {
		return selectedRequest;
	}

	@NotifyChange("*")
	public void setSelectedRequest(Entry<String, AbstractRequest> entry)
			throws SearchLibException {
		selectedRequest = entry;
	}

	/**
	 * @param requestName
	 *            the requestName to set
	 */
	public void setRequestName(String requestName) {
		this.requestName = requestName;
	}

	/**
	 * @return the requestName
	 */
	public String getRequestName() {
		return requestName;
	}

	private boolean checkRequestName() throws InterruptedException,
			SearchLibException {
		if (requestName == null || requestName.length() == 0) {
			new AlertController("Please enter a name");
			return false;
		}
		Client client = getClient();
		if (client.getRequestMap().get(requestName) != null) {
			new AlertController("This name is already used.");
			return false;
		}
		return true;
	}

	private void newEdit(AbstractRequest newRequest) throws SearchLibException {
		newRequest.setRequestName(requestName);
		setRequest(newRequest);
	}

	@Command
	@NotifyChange("*")
	public void onNew() throws SearchLibException, InterruptedException,
			InstantiationException, IllegalAccessException {
		if (!checkRequestName())
			return;
		newEdit(requestType.newInstance(getClient()));
	}

	@Command
	@NotifyChange("*")
	public void onNewCopy() throws InterruptedException, SearchLibException,
			InstantiationException, IllegalAccessException {
		if (!checkRequestName())
			return;
		Client client = getClient();
		AbstractRequest request = requestType.newInstance(client);
		request.copyFrom(client.getNewRequest(selectedRequest.getKey()));
		newEdit(request);
	}

	@Command
	@NotifyChange("*")
	public void doEditQuery(@BindingParam("requestname") String requestName)
			throws SearchLibException {
		setRequest(getClient().getNewRequest(requestName));
	}

	@Command
	public void doDeleteQuery(@BindingParam("requestname") String requestName)
			throws SearchLibException, InterruptedException {
		if (!isSchemaRights())
			throw new SearchLibException("Not allowed");
		new RemoveAlert(requestName);
	}

	public Set<Entry<String, AbstractRequest>> getRequests()
			throws SearchLibException {
		Client client = getClient();
		if (client == null)
			return null;
		return client.getRequestMap().getRequests();
	}

	public void setResult(AbstractResult<?> result) {
		setAttribute(ScopeAttribute.QUERY_RESULT, result);
		PushEvent.eventEditRequestResult.publish(result);
	}

	@Command
	@NotifyChange("*")
	public void onCancel() throws SearchLibException {
		reset();
	}

	@Command
	@NotifyChange("*")
	public void onSave() throws SearchLibException,
			TransformerConfigurationException, IOException, SAXException {
		if (!isSchemaRights())
			throw new SearchLibException("Not allowed");
		Client client = getClient();
		AbstractRequest request = getAbstractRequest();
		client.getRequestMap().put(request);
		client.saveRequests();
		onCancel();
		PushEvent.eventRequestListChange.publish(client);
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
			client.getRequestMap().remove(selectedRequest);
			client.saveRequests();
			reload();
			PushEvent.eventRequestListChange.publish(client);
		}

	}

	@Command
	@NotifyChange("*")
	public void onSearch() throws IOException, ParseException, SyntaxError,
			URISyntaxException, ClassNotFoundException, SearchLibException,
			InterruptedException, InstantiationException,
			IllegalAccessException {
		AbstractRequest request = getAbstractRequest();
		request.reset();
		setResult(getClient().request(request));
	}

	/**
	 * @return the requestType
	 */
	public RequestTypeEnum getRequestType() {
		return requestType;
	}

	/**
	 * @param requestType
	 *            the requestType to set
	 */
	public void setRequestType(RequestTypeEnum requestType) {
		this.requestType = requestType;
	}

	public RequestTypeEnum[] getRequestTypeList() {
		return RequestTypeEnum.values();
	}

}
