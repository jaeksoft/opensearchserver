/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008-2013 Emmanuel Keller / Jaeksoft
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

import java.util.ArrayList;
import java.util.List;

import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.NotifyChange;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.request.NamedEntityExtractionRequest;
import com.jaeksoft.searchlib.request.RequestTypeEnum;

public class NamedEntityExtractionController extends AbstractQueryController {

	public NamedEntityExtractionController() throws SearchLibException {
		super(RequestTypeEnum.NamedEntityExtractionRequest);
	}

	@Override
	protected void reset() throws SearchLibException {
	}

	public String[] getStopWordsList() throws SearchLibException {
		Client client = getClient();
		if (client == null)
			return null;
		return client.getStopWordsManager().getList();
	}

	public List<String> getSearchRequests() throws SearchLibException {
		Client client = getClient();
		if (client == null)
			return null;
		List<String> nameList = new ArrayList<String>(0);
		client.getRequestMap().getNameList(nameList,
				RequestTypeEnum.SearchFieldRequest,
				RequestTypeEnum.SearchRequest);
		return nameList;
	}

	@Command
	@NotifyChange("*")
	public void onReturnedFieldAdd(@BindingParam("field") String field)
			throws SearchLibException {
		NamedEntityExtractionRequest request = (NamedEntityExtractionRequest) getRequest();
		if (request == null)
			return;
		request.addReturnedField(field);
	}

	@Command
	@NotifyChange("*")
	public void onReturnedFieldDelete(@BindingParam("field") String field)
			throws SearchLibException {
		NamedEntityExtractionRequest request = (NamedEntityExtractionRequest) getRequest();
		if (request == null)
			return;
		request.removeReturnedField(field);
	}

	@Command
	@NotifyChange("*")
	public void onStopWordsAdd(@BindingParam("listName") String listName,
			@BindingParam("ignoreCase") boolean ignoreCase)
			throws SearchLibException {
		NamedEntityExtractionRequest request = (NamedEntityExtractionRequest) getRequest();
		if (request == null)
			return;
		request.addStopWords(listName, ignoreCase);
	}

	@Command
	@NotifyChange("*")
	public void onStopWordsDelete(@BindingParam("listName") String listName)
			throws SearchLibException {
		NamedEntityExtractionRequest request = (NamedEntityExtractionRequest) getRequest();
		if (request == null)
			return;
		request.removeStopWords(listName);
	}

}
