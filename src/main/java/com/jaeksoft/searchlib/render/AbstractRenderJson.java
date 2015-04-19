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

package com.jaeksoft.searchlib.render;

import java.io.IOException;
import java.io.PrintWriter;

import org.json.simple.JSONObject;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.query.ParseException;
import com.jaeksoft.searchlib.request.AbstractRequest;
import com.jaeksoft.searchlib.result.AbstractResult;
import com.jaeksoft.searchlib.web.ServletTransaction;

public abstract class AbstractRenderJson<T1 extends AbstractRequest, T2 extends AbstractResult<T1>>
		extends AbstractRender<T1, T2> {

	protected PrintWriter writer;

	protected AbstractRenderJson(T2 result) {
		super(result);

	}

	public abstract void render() throws Exception;

	@SuppressWarnings("unchecked")
	protected void renderPrefix(JSONObject jsonResponse, String queryString)
			throws ParseException, SyntaxError, SearchLibException, IOException {
		JSONObject jsonHeader = new JSONObject();
		jsonHeader.put("status", 0);
		jsonHeader.put("query", queryString);
		jsonResponse.put("header", jsonHeader);

	}

	@Override
	final public void render(ServletTransaction servletTransaction)
			throws Exception {
		servletTransaction.setResponseContentType("application/json");
		writer = servletTransaction.getWriter("UTF-8");
		render();
	}

	protected void renderTimers() {
		result.getTimer().writeXml(writer, request.getTimerMinTime(),
				request.getTimerMaxDepth());
	}

}
