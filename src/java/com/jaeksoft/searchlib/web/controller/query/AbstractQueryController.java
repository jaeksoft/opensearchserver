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

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.request.AbstractRequest;
import com.jaeksoft.searchlib.request.RequestTypeEnum;
import com.jaeksoft.searchlib.result.AbstractResult;
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

	final protected AbstractRequest getRequest(RequestTypeEnum type)
			throws SearchLibException {
		AbstractRequest request = getAbstractRequest();
		if (request == null)
			return null;
		if (request.getType() != type)
			return null;
		return request;
	}

	final public AbstractRequest getAbstractRequest() throws SearchLibException {
		return (AbstractRequest) ScopeAttribute.QUERY_REQUEST.get(this);
	}

	protected AbstractResult<?> getAbstractResult() {
		return (AbstractResult<?>) ScopeAttribute.QUERY_SEARCH_RESULT.get(this);
	}

	protected AbstractResult<?> getResult(RequestTypeEnum type) {
		AbstractResult<?> result = getAbstractResult();
		if (result == null)
			return null;
		if (result.getRequest().getType() != type)
			return null;
		return result;
	}

	private boolean isResult(RequestTypeEnum type) {
		AbstractResult<?> result = getAbstractResult();
		if (result == null)
			return false;
		return result.getRequest().getType() == type;
	}

	public boolean isResultSearch() {
		return isResult(RequestTypeEnum.SearchRequest);
	}

	public boolean isResultSpellCheck() {
		return isResult(RequestTypeEnum.SpellCheckRequest);
	}

	public boolean isResultMoreLikeThis() {
		return isResult(RequestTypeEnum.MoreLikeThisRequest);
	}

	@Override
	public void eventQueryEditResult(AbstractResult<?> result) {
		reloadPage();
	}

	@Override
	public void eventQueryEditRequest(AbstractRequest request) {
		reloadPage();
	}

}
