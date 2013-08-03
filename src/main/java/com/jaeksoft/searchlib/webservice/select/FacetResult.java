/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2011-2013 Emmanuel Keller / Jaeksoft
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
package com.jaeksoft.searchlib.webservice.select;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.jaeksoft.searchlib.facet.FacetItem;
import com.jaeksoft.searchlib.facet.FacetList;
import com.jaeksoft.searchlib.result.AbstractResultSearch;

@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
public class FacetResult {

	@XmlElement(name = "fieldName")
	public String fieldName;

	@XmlElement(name = "terms")
	public List<FacetFieldItem> terms;

	public FacetResult() {
		terms = null;
		fieldName = null;

	}

	public FacetResult(AbstractResultSearch result, String field) {
		terms = new ArrayList<FacetFieldItem>();
		FacetList facetList = result.getFacetList();
		fieldName = field;
		for (FacetItem facet : facetList.getByField(field.trim()))
			terms.add(new FacetFieldItem(facet.getCount(), facet.getTerm()));
	}

	private FacetResult(JSONObject json) throws JSONException {
		fieldName = json.getString("fieldName");
		terms = new ArrayList<FacetFieldItem>(0);
		addTerm(json.optJSONArray("terms"));
		addTerm(json.optJSONObject("terms"));
	}

	private void addTerm(JSONArray array) throws JSONException {
		if (array == null)
			return;
		for (int i = 0; i < array.length(); i++)
			addTerm(array.getJSONObject(i));
	}

	private void addTerm(JSONObject json) throws JSONException {
		if (json == null)
			return;
		terms.add(new FacetFieldItem(json));
	}

	public static void add(JSONObject json, List<FacetResult> facets)
			throws JSONException {
		if (json == null)
			return;
		facets.add(new FacetResult(json));
	}

	public static void add(JSONArray array, List<FacetResult> facets)
			throws JSONException {
		if (array == null)
			return;
		for (int i = 0; i < array.length(); i++)
			add(array.getJSONObject(i), facets);
	}
}