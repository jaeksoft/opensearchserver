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

import org.json.simple.JSONObject;

import com.jaeksoft.searchlib.request.MoreLikeThisRequest;
import com.jaeksoft.searchlib.result.ResultMoreLikeThis;

public class RenderMoreLikeThisJson extends
		AbstractRenderDocumentsJson<MoreLikeThisRequest, ResultMoreLikeThis> {
	Boolean indent = null;

	public RenderMoreLikeThisJson(ResultMoreLikeThis result, Boolean indent) {
		super(result);
		this.indent = indent;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void render() throws Exception {
		JSONObject jsonResponse = new JSONObject();
		renderPrefix(jsonResponse, request.getDocQuery());
		renderDocuments(jsonResponse);
		JSONObject json = new JSONObject();
		json.put("response", jsonResponse);
		if (indent)
			writer.println(new org.json.JSONObject(json.toJSONString())
					.toString(4));
		else
			writer.println(json);

	}
}
