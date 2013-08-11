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

import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.request.AbstractSearchRequest;
import com.jaeksoft.searchlib.request.BoostQuery;

public class BoostQueriesController extends AbstractQueryController {

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
		reload();
	}

	public boolean isSelected() {
		return selectedBoostQuery != null;
	}

	public boolean isNotSelected() {
		return !isSelected();
	}

	@Command
	public void onCancel() throws SearchLibException {
		reset();
		reload();
	}

	@Command
	public void onSave() throws SearchLibException {
		AbstractSearchRequest request = (AbstractSearchRequest) getRequest();
		request.setBoostingQuery(selectedBoostQuery, currentBoostQuery);
		onCancel();
	}

	@Command
	public void onRemove(@BindingParam("boostQuery") BoostQuery boostQuery)
			throws SearchLibException {
		AbstractSearchRequest request = (AbstractSearchRequest) getRequest();
		request.setBoostingQuery(boostQuery, null);
		onCancel();
	}
}
