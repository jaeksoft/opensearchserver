/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C)2011-2012 Emmanuel Keller / Jaeksoft
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

import org.json.simple.JSONObject;

import com.jaeksoft.searchlib.facet.Facet;
import com.jaeksoft.searchlib.facet.FacetField;
import com.jaeksoft.searchlib.facet.FacetItem;
import com.jaeksoft.searchlib.facet.FacetList;
import com.jaeksoft.searchlib.request.SearchRequest;
import com.jaeksoft.searchlib.result.AbstractResultSearch;

public class RenderSearchJson extends
		AbstractRenderDocumentsJson<SearchRequest, AbstractResultSearch> {

	private boolean indent;

	public RenderSearchJson(AbstractResultSearch result, boolean jsonIndent) {
		super(result);
		this.indent = jsonIndent;
	}

	@SuppressWarnings("unchecked")
	private void renderFacets(JSONObject jsonResponse) throws Exception {
		FacetList facetList = result.getFacetList();
		if (facetList == null)
			return;
		ArrayList<JSONObject> jsonFacetingList = new ArrayList<JSONObject>();
		for (Facet facet : facetList) {
			JSONObject jsonFaceting = new JSONObject();
			FacetField facetField = facet.getFacetField();
			jsonFaceting.put("fieldName", facetField.getName());
			ArrayList<JSONObject> jsonFacetList = new ArrayList<JSONObject>();
			renderFacet(facet, jsonFacetList);
			jsonFaceting.put("facet", jsonFacetList);
			jsonFacetingList.add(jsonFaceting);
		}
		jsonResponse.put("faceting", jsonFacetingList);
	}

	@SuppressWarnings("unchecked")
	private void renderFacet(Facet facet, ArrayList<JSONObject> jsonFacetList)
			throws Exception {
		for (FacetItem facetItem : facet) {
			JSONObject jsonFacet = new JSONObject();
			jsonFacet.put("name", facetItem.getTerm());
			jsonFacet.put("value", facetItem.getCount());
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
