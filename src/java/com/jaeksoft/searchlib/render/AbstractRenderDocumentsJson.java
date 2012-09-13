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
import java.util.ArrayList;

import org.json.simple.JSONObject;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.query.ParseException;
import com.jaeksoft.searchlib.request.AbstractRequest;
import com.jaeksoft.searchlib.request.RequestInterfaces;
import com.jaeksoft.searchlib.request.ReturnField;
import com.jaeksoft.searchlib.request.ReturnFieldList;
import com.jaeksoft.searchlib.request.SearchRequest;
import com.jaeksoft.searchlib.result.AbstractResult;
import com.jaeksoft.searchlib.result.ResultDocument;
import com.jaeksoft.searchlib.result.ResultDocumentsInterface;
import com.jaeksoft.searchlib.schema.FieldValueItem;
import com.jaeksoft.searchlib.snippet.SnippetField;
import com.jaeksoft.searchlib.snippet.SnippetFieldList;

public abstract class AbstractRenderDocumentsJson<T1 extends AbstractRequest, T2 extends AbstractResult<T1>>
		extends AbstractRenderJson<T1, T2> {

	private final ResultDocumentsInterface<?> resultDocs;

	protected AbstractRenderDocumentsJson(T2 result) {
		super(result);
		resultDocs = result instanceof ResultDocumentsInterface ? (ResultDocumentsInterface<?>) result
				: null;
	}

	@SuppressWarnings("unchecked")
	final protected void renderDocuments(JSONObject jsonResponse)
			throws IOException, ParseException, SyntaxError, SearchLibException {
		int start = resultDocs.getRequestStart();
		int end = resultDocs.getDocumentCount() + start;
		JSONObject jsonResult = new JSONObject();
		ArrayList<JSONObject> resultArrayList = new ArrayList<JSONObject>();
		jsonResult.put("numFound", resultDocs.getNumFound());
		jsonResult.put("collapsedDocCount", resultDocs.getCollapsedDocCount());
		jsonResult.put("start", start);
		jsonResult.put("rows", resultDocs.getRequestRows());
		jsonResult.put("maxScore", resultDocs.getMaxScore());
		jsonResult.put("time", result.getTimer().duration());
		for (int i = start; i < end; i++)
			this.renderDocument(request, i, jsonResult, resultArrayList);
		jsonResult.put("doc", resultArrayList);
		jsonResponse.put("result", jsonResult);
	}

	@SuppressWarnings("unchecked")
	private void renderDocument(AbstractRequest abstractRequest, int pos,
			JSONObject jsonResult, ArrayList<JSONObject> resultArrayList)
			throws IOException, ParseException, SyntaxError, SearchLibException {
		JSONObject jsonDoc = new JSONObject();
		ArrayList<JSONObject> jsonFieldList = new ArrayList<JSONObject>();
		ArrayList<JSONObject> jsonSnippetList = new ArrayList<JSONObject>();
		jsonDoc.put("score", resultDocs.getScore(pos));
		jsonDoc.put("pos", pos);
		ResultDocument doc = resultDocs.getDocument(pos, renderingTimer);
		ReturnFieldList returnFieldList = null;
		SnippetFieldList snippetFieldList = null;
		if (abstractRequest instanceof SearchRequest) {
			returnFieldList = ((SearchRequest) abstractRequest)
					.getReturnFieldList();
			snippetFieldList = ((SearchRequest) abstractRequest)
					.getSnippetFieldList();
		} else if (abstractRequest instanceof RequestInterfaces.ReturnedFieldInterface) {
			returnFieldList = ((RequestInterfaces.ReturnedFieldInterface) abstractRequest)
					.getReturnFieldList();
		}
		if (returnFieldList != null)
			for (ReturnField field : returnFieldList) {
				renderField(doc, field, jsonFieldList);
				jsonDoc.put("field", jsonFieldList);
			}
		if (snippetFieldList != null)
			for (SnippetField field : snippetFieldList) {
				renderSnippetValue(doc, field, jsonSnippetList);
				jsonDoc.put("snippet", jsonSnippetList);
			}

		int cc = resultDocs.getCollapseCount(pos);
		if (cc > 0)
			jsonDoc.put("collapseCount", cc);

		resultArrayList.add(jsonDoc);
	}

	@SuppressWarnings("unchecked")
	private void renderField(ResultDocument doc, ReturnField field,
			ArrayList<JSONObject> jsonFieldList) throws IOException {
		String fieldName = field.getName();
		FieldValueItem[] values = doc.getValueArray(field);
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

}
