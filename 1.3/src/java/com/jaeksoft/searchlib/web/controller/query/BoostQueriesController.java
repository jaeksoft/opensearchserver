/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2012 Emmanuel Keller / Jaeksoft
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

import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.ForwardEvent;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.request.BoostQuery;
import com.jaeksoft.searchlib.request.SearchRequest;

public class BoostQueriesController extends AbstractQueryController {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7985584537309902023L;

	private BoostQuery currentBoostQuery;

	private BoostQuery selectedBoostQuery;

	public BoostQueriesController() throws SearchLibException {
		super();
		reset();
	}

	@Override
	protected void reset() throws SearchLibException {
		currentBoostQuery = new BoostQuery("", 1.0f);
		selectedBoostQuery = null;
	}

	/**
	 * @return the currentBoostQuery
	 */
	public BoostQuery getCurrentBoostQuery() {
		return currentBoostQuery;
	}

	/**
	 * @return the selectedBoostQuery
	 */
	public BoostQuery getSelectedBoostQuery() {
		return selectedBoostQuery;
	}

	/**
	 * @param selectedBoostQuery
	 *            the selectedBoostQuery to set
	 * @throws SearchLibException
	 */
	public void setSelectedBoostQuery(BoostQuery selectedBoostQuery)
			throws SearchLibException {
		this.selectedBoostQuery = selectedBoostQuery;
		currentBoostQuery.copyFrom(selectedBoostQuery);
		reloadPage();
	}

	public boolean isSelected() {
		return selectedBoostQuery != null;
	}

	public boolean isNotSelected() {
		return !isSelected();
	}

	public void onCancel() throws SearchLibException {
		reset();
		reloadPage();
	}

	public void onSave() throws SearchLibException {
		SearchRequest request = (SearchRequest) getRequest();
		request.setBoostingQuery(selectedBoostQuery, currentBoostQuery);
		onCancel();
	}

	public void onRemove(Event event) throws SearchLibException {
		if (event instanceof ForwardEvent)
			event = ((ForwardEvent) event).getOrigin();
		BoostQuery boostQuery = (BoostQuery) event.getTarget().getAttribute(
				"boostQuery");
		SearchRequest request = (SearchRequest) getRequest();
		request.setBoostingQuery(boostQuery, null);
		onCancel();
	}
}
