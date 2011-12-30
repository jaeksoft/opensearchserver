/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C)2011 Emmanuel Keller / Jaeksoft
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
import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.queryParser.ParseException;
import org.json.JSONObject;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.facet.Facet;
import com.jaeksoft.searchlib.facet.FacetField;
import com.jaeksoft.searchlib.facet.FacetItem;
import com.jaeksoft.searchlib.facet.FacetList;
import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.request.SearchRequest;
import com.jaeksoft.searchlib.result.Result;
import com.jaeksoft.searchlib.result.ResultDocument;
import com.jaeksoft.searchlib.schema.Field;
import com.jaeksoft.searchlib.schema.FieldValueItem;
import com.jaeksoft.searchlib.snippet.SnippetField;
import com.jaeksoft.searchlib.web.ServletTransaction;

public class RenderJson implements Render {

	private Result result;
	private SearchRequest searchRequest;
	private StringBuffer stringBuffer;
	private String JSON_COLON = ":";
	private String JSON_OPENING_BRACE = "{";
	private String JSON_CLOSING_BRACE = "}";
	private String JSON_OPENING_SQUARE_BRACKET = "[";
	private String JSON_CLOSING_SQUARE_BRACKET = "]";

	private String JSON_COMMA = ",";

	public RenderJson(Result result) {
		this.result = result;
		this.searchRequest = result.getSearchRequest();
		this.stringBuffer = new StringBuffer();

	}

	private void renderPrefix() throws ParseException, SyntaxError,
			SearchLibException, IOException {
		stringBuffer.append(JSON_OPENING_BRACE);
		stringBuffer.append(JSONObject.quote("response") + JSON_COLON
				+ JSON_OPENING_BRACE);
		stringBuffer.append(JSONObject.quote("header") + JSON_COLON
				+ JSON_OPENING_BRACE);
		stringBuffer.append(JSONObject.quote("status") + JSON_COLON);
		stringBuffer.append(0 + JSON_COMMA);
		stringBuffer.append(JSONObject.quote("query") + JSON_COLON);
		stringBuffer.append(JSONObject.quote(StringEscapeUtils
				.escapeXml(searchRequest.getQueryParsed())));
		stringBuffer.append(JSON_CLOSING_BRACE + JSON_COMMA);
	}

	private void renderSuffix() {
		stringBuffer.append(JSON_CLOSING_BRACE + JSON_CLOSING_BRACE);
	}

	private void renderDocuments() throws CorruptIndexException, IOException,
			ParseException, SyntaxError {
		SearchRequest searchRequest = result.getSearchRequest();
		int start = searchRequest.getStart();
		int end = result.getDocumentCount() + searchRequest.getStart();

		stringBuffer.append(JSONObject.quote("result") + JSON_COLON
				+ JSON_OPENING_BRACE);
		stringBuffer.append(JSONObject.quote("numFound") + JSON_COLON);
		stringBuffer.append(result.getNumFound() + JSON_COMMA);
		stringBuffer.append(JSONObject.quote("collapsedDocCount") + JSON_COLON);
		stringBuffer.append(result.getCollapseDocCount() + JSON_COMMA);
		stringBuffer.append(JSONObject.quote("start") + JSON_COLON);
		stringBuffer.append(searchRequest.getStart() + JSON_COMMA);
		stringBuffer.append(JSONObject.quote("rows") + JSON_COLON);
		stringBuffer.append(searchRequest.getRows() + JSON_COMMA);
		stringBuffer.append(JSONObject.quote("maxScore") + JSON_COLON);
		stringBuffer.append(result.getMaxScore() + JSON_COMMA);
		stringBuffer.append(JSONObject.quote("time") + JSON_COLON);
		stringBuffer.append(searchRequest.getFinalTime() + JSON_COMMA);
		stringBuffer.append(JSONObject.quote("doc") + JSON_COLON
				+ JSON_OPENING_SQUARE_BRACKET);
		for (int i = start; i < end; i++)
			this.renderDocument(i, end);
		stringBuffer.append(JSON_CLOSING_SQUARE_BRACKET);
		stringBuffer.append(JSON_CLOSING_BRACE);

	}

	private void renderDocument(int pos, int end) throws CorruptIndexException,
			IOException, ParseException, SyntaxError {

		stringBuffer.append(JSON_OPENING_BRACE);
		stringBuffer.append(JSONObject.quote("score") + JSON_COLON);
		stringBuffer.append(result.getScore(pos) + JSON_COMMA);
		stringBuffer.append(JSONObject.quote("pos") + JSON_COLON);
		stringBuffer.append(pos);
		ResultDocument doc = result.getDocument(pos);
		if (searchRequest.getReturnFieldList().size() > 0) {
			stringBuffer.append(JSON_COMMA + JSONObject.quote("field")
					+ JSON_COLON + JSON_OPENING_SQUARE_BRACKET);
			for (Field field : searchRequest.getReturnFieldList())
				renderField(doc, field);
			stringBuffer.append(JSON_CLOSING_SQUARE_BRACKET);

		}
		if (searchRequest.getSnippetFieldList().size() > 0) {
			stringBuffer.append(JSON_COMMA + JSONObject.quote("snippet")
					+ JSON_COLON + JSON_OPENING_SQUARE_BRACKET);
			for (SnippetField field : searchRequest.getSnippetFieldList())
				renderSnippetValue(doc, field);
			if (pos == searchRequest.getSnippetFieldList().size() - 1) {
				stringBuffer.append(JSON_CLOSING_SQUARE_BRACKET);
			} else {
				stringBuffer.append(JSON_CLOSING_SQUARE_BRACKET + JSON_COMMA);
			}
		}
		int cc = result.getCollapseCount(pos);
		if (cc > 0) {
			stringBuffer.append(JSONObject.quote("collapseCount") + JSON_COLON);
			stringBuffer.append(cc + JSON_COMMA);
		}
		if (pos == end - 1) {
			stringBuffer.append(JSON_CLOSING_BRACE);
		} else {
			stringBuffer.append(JSON_CLOSING_BRACE + JSON_COMMA);
		}
	}

	private void renderField(ResultDocument doc, Field field)
			throws CorruptIndexException, IOException {
		String fieldName = field.getName();
		List<FieldValueItem> values = doc.getValueList(field);
		if (values == null)
			return;
		for (FieldValueItem v : values) {
			stringBuffer.append(JSON_OPENING_BRACE);
			stringBuffer.append(JSONObject.quote("name") + JSON_COLON);
			stringBuffer.append(JSONObject.quote(fieldName) + JSON_COMMA);
			Float b = v.getBoost();
			if (b != null) {
				stringBuffer.append(JSONObject.quote("boost") + JSON_COLON);
				stringBuffer.append(b + JSON_COMMA);

			}
			stringBuffer.append(JSONObject.quote("value") + JSON_COLON);
			stringBuffer.append(JSONObject.quote(v.getValue()));
			stringBuffer.append(JSON_CLOSING_BRACE + JSON_COMMA);
		}
	}

	private void renderSnippetValue(ResultDocument doc, SnippetField field)
			throws IOException {
		String fieldName = field.getName();
		FieldValueItem[] snippets = doc.getSnippetArray(field);
		if (snippets == null)
			return;
		boolean highlighted = doc.isHighlighted(field.getName());
		for (FieldValueItem snippet : snippets) {
			stringBuffer.append(JSON_OPENING_BRACE);
			stringBuffer.append(JSONObject.quote("name") + JSON_COLON);
			stringBuffer.append(JSONObject.quote(fieldName) + JSON_COMMA);
			if (highlighted) {
				stringBuffer.append(JSONObject.quote("highlighted")
						+ JSON_COLON);
				stringBuffer.append(JSONObject.quote("yes") + JSON_COMMA);
			}
			stringBuffer.append(JSONObject.quote("value") + JSON_COLON);
			stringBuffer.append(JSONObject.quote(snippet.getValue()));
			stringBuffer.append(JSON_CLOSING_BRACE + JSON_COMMA);
		}
	}

	private void renderFacet(Facet facet, int num) throws Exception {
		FacetField facetField = facet.getFacetField();
		int k = 0;
		if (facet.getArray().length > 0) {
			if (num == 0) {
				stringBuffer.append(JSONObject.quote(facetField.getName())
						+ JSON_COLON);
			} else {
				stringBuffer.append(JSON_COMMA
						+ JSONObject.quote(facetField.getName()) + JSON_COLON);
			}

			stringBuffer.append(JSON_OPENING_SQUARE_BRACKET);
			for (FacetItem facetItem : facet) {
				stringBuffer.append(JSON_OPENING_BRACE);
				stringBuffer.append(JSONObject.quote("name") + JSON_COLON);
				stringBuffer.append(JSONObject.quote(StringEscapeUtils
						.escapeXml(facetItem.getTerm())) + JSON_COMMA);
				stringBuffer.append(JSONObject.quote("count") + JSON_COLON);
				stringBuffer.append(facetItem.getCount());
				if (k == facet.getArray().length - 1) {
					stringBuffer.append(JSON_CLOSING_BRACE);
				} else {
					stringBuffer.append(JSON_CLOSING_BRACE + JSON_COMMA);
				}

				k++;
			}

			stringBuffer.append(JSON_CLOSING_SQUARE_BRACKET);
		}
	}

	private void renderFacets() throws Exception {
		FacetList facetList = result.getFacetList();
		if (facetList == null)
			return;
		stringBuffer.append(JSON_COMMA + JSONObject.quote("faceting")
				+ JSON_COLON + JSON_OPENING_BRACE);
		int num = 0;
		for (Facet facet : facetList) {
			renderFacet(facet, num);
			num++;
		}
		stringBuffer.append(JSON_CLOSING_BRACE);
	}

	@Override
	public void render(ServletTransaction servletTransaction) throws Exception {
		servletTransaction.setResponseContentType("application/json");
		render(servletTransaction.getWriter("UTF-8"), "json");
	}

	private void render(PrintWriter writer, String format) throws Exception {
		if ("json".equalsIgnoreCase(format)) {
			renderPrefix();
			renderDocuments();
			renderFacets();
			renderSuffix();
			JSONObject json = new JSONObject(stringBuffer.toString());
			writer.println(json.toString(4));

		}

	}
}
