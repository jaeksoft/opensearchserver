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

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.request.RequestTypeEnum;
import com.jaeksoft.searchlib.request.SearchRequest;

public class SearchRequestController extends AbstractQueryController {

	public SearchRequestController() throws SearchLibException {
		super();
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -2891366720618162654L;

	@Override
	protected void reset() throws SearchLibException {
	}

	public SearchRequest getRequest() throws SearchLibException {
		return (SearchRequest) getRequest(RequestTypeEnum.SearchRequest);
	}
}
