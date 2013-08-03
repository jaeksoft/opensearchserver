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
package com.jaeksoft.searchlib.webservice.update;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.jaeksoft.searchlib.result.ResultDocument;
import com.jaeksoft.searchlib.schema.FieldValue;
import com.jaeksoft.searchlib.snippet.SnippetFieldValue;
import com.jaeksoft.searchlib.webservice.select.FieldValueList;
import com.jaeksoft.searchlib.webservice.select.SnippetValueList;

@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
public class DocumentResult {

	@XmlAttribute
	public int pos;

	@XmlAttribute
	public float score;

	@XmlAttribute
	public int collapseCount;

	@XmlElement(name = "field")
	public List<FieldValueList> fields;

	@XmlElement(name = "snippet")
	public List<SnippetValueList> snippets;

	public DocumentResult() {
		fields = null;
		snippets = null;
		collapseCount = 0;
		pos = 0;
		score = 0;
	}

	public DocumentResult(ResultDocument resultDocument, int collapseDocCount,
			int position, float docScore) {
		fields = new ArrayList<FieldValueList>(0);
		for (FieldValue fiedValue : resultDocument.getReturnFields().values())
			fields.add(new FieldValueList(fiedValue));
		snippets = new ArrayList<SnippetValueList>(0);
		for (SnippetFieldValue snippetFiedValue : resultDocument
				.getSnippetFields().values()) {
			boolean highlighted = resultDocument.isHighlighted(snippetFiedValue
					.getName());
			snippets.add(new SnippetValueList(highlighted, snippetFiedValue));
		}
		collapseCount = collapseDocCount;
		pos = position;
		score = docScore;
	}

	public DocumentResult(JSONObject json) throws JSONException {
		fields = new ArrayList<FieldValueList>(0);
		setReturnedField(json.optJSONObject("field"));
		setReturnedField(json.optJSONArray("field"));
		snippets = new ArrayList<SnippetValueList>(0);
		setSnippetField(json.optJSONObject("snippet"));
		setSnippetField(json.optJSONArray("snippet"));
		pos = json.getInt("@pos");
		collapseCount = json.getInt("@collapseCount");
		score = (float) json.getDouble("@score");
	}

	private void setReturnedField(JSONArray array) throws JSONException {
		if (array == null)
			return;
		for (int i = 0; i < array.length(); i++)
			setReturnedField(array.getJSONObject(i));
	}

	private void setReturnedField(JSONObject json) throws JSONException {
		if (json == null)
			return;
		if (fields == null)
			fields = new ArrayList<FieldValueList>(1);
		FieldValueList.addFieldValue(json, fields);
	}

	private void setSnippetField(JSONArray array) throws JSONException {
		if (array == null)
			return;
		for (int i = 0; i < array.length(); i++)
			setSnippetField(array.getJSONObject(i));
	}

	private void setSnippetField(JSONObject json) throws JSONException {
		if (json == null)
			return;
		if (snippets == null)
			snippets = new ArrayList<SnippetValueList>(1);
		SnippetValueList.addSnippetValue(json, snippets);
	}

	public static void add(JSONObject json, List<DocumentResult> documents)
			throws JSONException {
		if (json == null)
			return;
		documents.add(new DocumentResult(json));
	}

	public static void add(JSONArray array, List<DocumentResult> documents)
			throws JSONException {
		if (array == null)
			return;
		for (int i = 0; i < array.length(); i++)
			DocumentResult.add(array.getJSONObject(i), documents);
	}

	@XmlTransient
	public List<FieldValueList> getReturnFields() {
		return fields;
	}

	public FieldValueList getReturnField(String name) {
		return FieldValueList.getField(fields, name);
	}

	@XmlTransient
	public List<SnippetValueList> getSnippetFields() {
		return snippets;
	}

	public FieldValueList getSnippetField(String name) {
		return FieldValueList.getField(snippets, name);
	}

	@XmlTransient
	public float getScore() {
		return score;
	}

	@XmlTransient
	public int getCollapseCount() {
		return collapseCount;
	}

	@XmlTransient
	public int getPos() {
		return pos;
	}
}
