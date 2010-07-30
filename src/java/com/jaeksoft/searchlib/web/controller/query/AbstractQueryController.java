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

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.request.SearchRequest;
import com.jaeksoft.searchlib.result.Result;
import com.jaeksoft.searchlib.web.controller.CommonController;
import com.jaeksoft.searchlib.web.controller.ScopeAttribute;

public abstract class AbstractQueryController extends CommonController {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1943618096637337561L;

	public AbstractQueryController() throws SearchLibException {
		super();
	}

	public SearchRequest getRequest() throws SearchLibException {
		return (SearchRequest) ScopeAttribute.QUERY_SEARCH_REQUEST.get(this);
	}

	public boolean getResultExists() {
		return getResult() != null;
	}

	public Result getResult() {
		return (Result) ScopeAttribute.QUERY_SEARCH_RESULT.get(this);
	}

	@Override
	protected void eventQueryEditResult(Result result) {
		reloadPage();
	}

	@Override
	protected void eventQueryEditRequest(SearchRequest request) {
		reloadPage();
	}

}
