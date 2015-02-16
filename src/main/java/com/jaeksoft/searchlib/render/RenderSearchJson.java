/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C)2011-2015 Emmanuel Keller / Jaeksoft
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

import java.util.ArrayList;
import java.util.Map;

import org.json.simple.JSONObject;

import com.jaeksoft.searchlib.request.AbstractSearchRequest;
import com.jaeksoft.searchlib.result.AbstractResultSearch;

public class RenderSearchJson
		extends
		AbstractRenderDocumentsJson<AbstractSearchRequest, AbstractResultSearch> {

	private boolean indent;

	public RenderSearchJson(AbstractResultSearch result, boolean jsonIndent) {
		super(result);
		this.indent = jsonIndent;
	}

	@SuppressWarnings("unchecked")
	private void renderFacets(JSONObject jsonResponse) throws Exception {
		Map<String, Map<String, Long>> facetResults = result.getFacetResults();
		if (facetResults == null)
			return;
		ArrayList<JSONObject> jsonFacetingList = new ArrayList<JSONObject>();
		for (Map.Entry<String, Map<String, Long>> entry : facetResults
				.entrySet()) {
			JSONObject jsonFaceting = new JSONObject();
			String facetField = entry.getKey();
			jsonFaceting.put("fieldName", facetField);
			ArrayList<JSONObject> jsonFacetList = new ArrayList<JSONObject>();
			renderFacet(entry.getValue(), jsonFacetList);
			jsonFaceting.put("facet", jsonFacetList);
			jsonFacetingList.add(jsonFaceting);
		}
		jsonResponse.put("faceting", jsonFacetingList);
	}

	@SuppressWarnings("unchecked")
	private void renderFacet(Map<String, Long> terms,
			ArrayList<JSONObject> jsonFacetList) throws Exception {
		for (Map.Entry<String, Long> term : terms.entrySet()) {
			JSONObject jsonFacet = new JSONObject();
			jsonFacet.put("name", term.getKey());
			jsonFacet.put("value", term.getValue());
			jsonFacetList.add(jsonFacet);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void render() throws Exception {
		JSONObject jsonResponse = new JSONObject();
		renderPrefix(jsonResponse, request.getQueryParsed());
		renderDocuments(jsonResponse);
		renderFacets(jsonResponse);
		JSONObject json = new JSONObject();
		json.put("response", jsonResponse);
		if (indent)
			writer.println(new org.json.JSONObject(json.toJSONString())
					.toString(4));
		else
			writer.println(json);

	}
}
