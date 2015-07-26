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

import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.GlobalCommand;
import org.zkoss.bind.annotation.NotifyChange;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.request.SearchMergedRequest;
import com.jaeksoft.searchlib.request.SearchMergedRequest.RemoteRequest;

public class RequestsController extends AbstractQueryController {

	private transient RemoteRequest currentRequest;
	private transient RemoteRequest selectedRequest;

	public RequestsController() throws SearchLibException {
		super(null/* RequestTypeEnum.SearchMergedRequest */);
	}

	@Override
	protected void reset() throws SearchLibException {
		selectedRequest = null;
		currentRequest = new RemoteRequest();
	}

	public RemoteRequest getCurrentRequest() {
		synchronized (this) {
			return currentRequest;
		}
	}

	@NotifyChange("*")
	public void setSelectedRequest(RemoteRequest request) {
		synchronized (this) {
			selectedRequest = request;
			currentRequest = new RemoteRequest(request);
		}
	}

	public boolean isRequestSelected() {
		synchronized (this) {
			return selectedRequest != null;
		}
	}

	public RemoteRequest getSelectedRequest() {
		synchronized (this) {
			return selectedRequest;
		}
	}

	private SearchMergedRequest getSearchMergedRequest()
			throws SearchLibException {
		return (SearchMergedRequest) getRequest();
	}

	@Command
	@NotifyChange("*")
	public void onRequestSave() throws SearchLibException {
		synchronized (this) {
			getSearchMergedRequest().save(selectedRequest, currentRequest);
			onRequestCancel();
		}
	}

	@Command
	@NotifyChange("*")
	public void onRequestRemove() throws SearchLibException {
		synchronized (this) {
			getSearchMergedRequest().save(selectedRequest, null);
			onRequestCancel();
		}
	}

	@Command
	@NotifyChange("*")
	public void onRequestCancel() throws SearchLibException {
		synchronized (this) {
			selectedRequest = null;
			currentRequest = new RemoteRequest();
		}
	}

	@Override
	@GlobalCommand
	public void eventSchemaChange(Client client) throws SearchLibException {
		reset();
	}
}
