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

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.request.RequestTypeEnum;

public class NamedEntityExtractionController extends AbstractQueryController {

	public NamedEntityExtractionController() throws SearchLibException {
		super(RequestTypeEnum.NamedEntityExtractionRequest);
	}

	@Override
	protected void reset() throws SearchLibException {
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

}
