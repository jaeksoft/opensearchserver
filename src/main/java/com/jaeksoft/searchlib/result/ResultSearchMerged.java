/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2015 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.result;

import java.io.IOException;
import java.util.List;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.request.SearchMergedRequest;
import com.jaeksoft.searchlib.request.SearchMergedRequest.RemoteRequest;
import com.jaeksoft.searchlib.util.Timer;
import com.jaeksoft.searchlib.webservice.query.document.IndexDocumentResult;

public class ResultSearchMerged extends
		AbstractResultSearch<SearchMergedRequest> {

	public ResultSearchMerged(SearchMergedRequest request) {
		super(null, request);
		for (RemoteRequest remoteRequest : request.getRequests()) {

		}
	}

	@Override
	public void populate(List<IndexDocumentResult> indexDocuments)
			throws IOException, SearchLibException {
		// TODO Auto-generated method stub

	}

	@Override
	public ResultDocument getDocument(int pos, Timer timer)
			throws SearchLibException {
		// TODO Auto-generated method stub
		return null;
	}

}
