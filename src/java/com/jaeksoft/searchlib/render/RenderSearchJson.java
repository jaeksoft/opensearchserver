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

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONObject;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.facet.Facet;
import com.jaeksoft.searchlib.facet.FacetField;
import com.jaeksoft.searchlib.facet.FacetItem;
import com.jaeksoft.searchlib.facet.FacetList;
import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.query.ParseException;
import com.jaeksoft.searchlib.request.SearchRequest;
import com.jaeksoft.searchlib.result.AbstractResultSearch;
import com.jaeksoft.searchlib.result.ResultDocument;
import com.jaeksoft.searchlib.schema.Field;
import com.jaeksoft.searchlib.schema.FieldValueItem;
import com.jaeksoft.searchlib.snippet.SnippetField;
import com.jaeksoft.searchlib.web.ServletTransaction;

public class RenderSearchJson implements Render {

	private AbstractResultSearch result;
	private SearchRequest searchRequest;
	private String indent;

	public RenderSearchJson(AbstractResultSearch result, String jsonIndent) {
		this.result = result;
		this.searchRequest = result.getRequest();
		this.indent = jsonIndent;
	}

	@SuppressWarnings("unchecked")
	private void renderPrefix(JSONObject jsonResponse) throws ParseException,
			SyntaxError, SearchLibException, IOException {
		JSONObject jsonHeader = new JSONObject();
		jsonHeader.put("status", 0);
		jsonHeader.put("query", searchRequest.getQueryParsed());
		jsonResponse.put("header", jsonHeader);

	}

	@SuppressWarnings("unchecked")
	private void renderDocuments(JSONObject jsonResponse) throws IOException,
			ParseException, SyntaxError {
		SearchRequest searchRequest = result.getRequest();
		ArrayList<JSONObject> resultArrayList = new ArrayList<JSONObject>();
		int start = searchRequest.getStart();
		int end = result.getDocumentCount() + searchRequest.getStart();
		JSONObject jsonResult = new JSONObject();
		jsonResult.put("numFound", result.getNumFound());
		jsonResult.put("start", searchRequest.getStart());
		jsonResult.put("rows", searchRequest.getRows());
		jsonResult.put("maxScore", result.getMaxScore());
		jsonResult.put("time", searchRequest.getFinalTime());
		jsonResult.put("collapsedDocCount", result.getCollapseDocCount());
		for (int i = start; i < end; i++)
			this.renderDocument(i, jsonResult, resultArrayList);
		jsonResult.put("doc", resultArrayList);
		jsonResponse.put("result", jsonResult);
	}

	@SuppressWarnings("unchecked")
	private void renderDocument(int pos, JSONObject jsonResult,
			ArrayList<JSONObject> resultArrayList) throws IOException,
			ParseException, SyntaxError {
		JSONObject jsonDoc = new JSONObject();
		ArrayList<JSONObject> jsonFieldList = new ArrayList<JSONObject>();
		ArrayList<JSONObject> jsonSnippetList = new ArrayList<JSONObject>();
		jsonDoc.put("score", result.getScore(pos));
		jsonDoc.put("pos", searchRequest.getStart());
		ResultDocument doc = result.getDocument(pos);
		for (Field field : searchRequest.getReturnFieldList()) {
			renderField(doc, field, jsonFieldList);
			jsonDoc.put("field", jsonFieldList);
		}
		for (SnippetField field : searchRequest.getSnippetFieldList()) {
			renderSnippetValue(doc, field, jsonSnippetList);
			jsonDoc.put("snippet", jsonSnippetList);
		}

		int cc = result.getCollapseCount(pos);
		if (cc > 0)
			jsonDoc.put("collapseCount", cc);

		resultArrayList.add(jsonDoc);
	}

	@SuppressWarnings("unchecked")
	private void renderField(ResultDocument doc, Field field,
			ArrayList<JSONObject> jsonFieldList) throws IOException {
		String fieldName = field.getName();
		List<FieldValueItem> values = doc.getValueList(field);
		JSONObject jsonField = new JSONObject();
		if (values == null)
			return;
		for (FieldValueItem v : values) {
			jsonField.put("name", fieldName);
			Float b = v.getBoost();
			if (b != null)
				jsonField.put("boost", b);
			jsonField.put("value", v.getValue());
			jsonFieldList.add(jsonField);
		}
	}

	@SuppressWarnings("unchecked")
	private void renderSnippetValue(ResultDocument doc, SnippetField field,
			ArrayList<JSONObject> jsonSnippetList) throws IOException {
		String fieldName = field.getName();
		FieldValueItem[] snippets = doc.getSnippetArray(field);
		JSONObject jsonSnippet = new JSONObject();
		if (snippets == null)
			return;
		boolean highlighted = doc.isHighlighted(field.getName());
		for (FieldValueItem snippet : snippets) {
			jsonSnippet.put("name", fieldName);
			if (highlighted)
				jsonSnippet.put("highlighted", "yes");
			jsonSnippet.put("value", snippet.getValue());
			jsonSnippetList.add(jsonSnippet);
		}
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

	// @SuppressWarnings("unchecked")
	// private void renderSpellCheck(SpellCheck spellCheck,
	// ArrayList<JSONObject> jsonSpellCheckList) throws Exception {
	//
	// for (SpellCheckItem spellCheckItem : spellCheck) {
	// JSONObject jsonSpellCheck = new JSONObject();
	// jsonSpellCheck.put("name", spellCheckItem.getWord());
	// ArrayList<JSONObject> jsonSpellcheckWords = new ArrayList<JSONObject>();
	// for (SuggestionItem suggest : spellCheckItem.getSuggestions()) {
	// JSONObject jsonSpellSuggest = new JSONObject();
	// jsonSpellSuggest.put("suggest", suggest.getTerm());
	// jsonSpellSuggest.put("freq", suggest.getFreq());
	// jsonSpellcheckWords.add(jsonSpellSuggest);
	// }
	// jsonSpellCheck.put("suggestions", jsonSpellcheckWords);
	// jsonSpellCheckList.add(jsonSpellCheck);
	// }
	//
	// }

	// @SuppressWarnings("unchecked")
	// private void renderSpellChecks(JSONObject jsonResponse) throws Exception
	// {
	// List<SpellCheck> spellChecklist = result.getSpellCheckList();
	// ArrayList<JSONObject> jsonSpellCheckArray = new ArrayList<JSONObject>();
	// if (spellChecklist == null)
	// return;
	//
	// for (SpellCheck spellCheck : spellChecklist) {
	// JSONObject jsonSpellCheck = new JSONObject();
	// ArrayList<JSONObject> jsonSpellcheckList = new ArrayList<JSONObject>();
	// String fieldName = spellCheck.getFieldName();
	// jsonSpellCheck.put("fieldName", fieldName);
	// renderSpellCheck(spellCheck, jsonSpellcheckList);
	// jsonSpellCheck.put("word", jsonSpellcheckList);
	// jsonSpellCheckArray.add(jsonSpellCheck);
	// }
	// jsonResponse.put("spellcheck", jsonSpellCheckArray);
	// }

	@Override
	public void render(ServletTransaction servletTransaction) throws Exception {
		servletTransaction.setResponseContentType("application/json");
		render(servletTransaction.getWriter("UTF-8"), indent);
	}

	@SuppressWarnings("unchecked")
	private void render(PrintWriter writer, String indent) throws Exception {
		JSONObject jsonResponse = new JSONObject();
		renderPrefix(jsonResponse);
		renderDocuments(jsonResponse);
		renderFacets(jsonResponse);
		JSONObject json = new JSONObject();
		json.put("response", jsonResponse);
		if ("yes".equalsIgnoreCase(indent))
			writer.println(new org.json.JSONObject(json.toJSONString())
					.toString(4));
		else
			writer.println(json);
	}
}
