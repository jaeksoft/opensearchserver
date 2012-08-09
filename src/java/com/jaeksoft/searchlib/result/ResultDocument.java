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

package com.jaeksoft.searchlib.result;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.index.ReaderLocal;
import com.jaeksoft.searchlib.query.ParseException;
import com.jaeksoft.searchlib.request.MoreLikeThisRequest;
import com.jaeksoft.searchlib.request.SearchRequest;
import com.jaeksoft.searchlib.schema.Field;
import com.jaeksoft.searchlib.schema.FieldList;
import com.jaeksoft.searchlib.schema.FieldValue;
import com.jaeksoft.searchlib.schema.FieldValueItem;
import com.jaeksoft.searchlib.snippet.SnippetField;
import com.jaeksoft.searchlib.snippet.SnippetFieldValue;
import com.jaeksoft.searchlib.util.Timer;

public class ResultDocument {

	final private FieldList<FieldValue> returnFields;
	final private FieldList<SnippetFieldValue> snippetFields;

	public ResultDocument(SearchRequest searchRequest, int docId,
			ReaderLocal reader, Timer timer) throws IOException,
			ParseException, SyntaxError, SearchLibException {

		returnFields = new FieldList<FieldValue>();
		snippetFields = new FieldList<SnippetFieldValue>();

		FieldList<FieldValue> documentFields = reader.getDocumentFields(docId,
				searchRequest.getDocumentFieldList(), timer);

		for (Field field : searchRequest.getReturnFieldList()) {
			FieldValue fieldValue = documentFields.get(field);
			if (fieldValue != null)
				returnFields.add(fieldValue);
		}

		for (SnippetField field : searchRequest.getSnippetFieldList()) {
			field.initSearchTerms(searchRequest);
			List<FieldValueItem> snippets = new ArrayList<FieldValueItem>();
			boolean isSnippet = field.getSnippets(docId, reader, documentFields
					.get(field).getValueArray(), snippets);
			SnippetFieldValue fieldValue = new SnippetFieldValue(field,
					snippets, isSnippet);
			snippetFields.add(fieldValue);
		}
	}

	public ResultDocument(MoreLikeThisRequest mltRequest, int docId,
			ReaderLocal reader, Timer timer) throws IOException,
			ParseException, SyntaxError {
		returnFields = reader.getDocumentFields(docId,
				mltRequest.getReturnFieldList(), timer);
		snippetFields = new FieldList<SnippetFieldValue>();
	}

	public FieldList<FieldValue> getReturnFields() {
		return returnFields;
	}

	public FieldList<SnippetFieldValue> getSnippetFields() {
		return snippetFields;
	}

	public FieldValueItem[] getValueArray(Field field) {
		if (field == null)
			return null;
		FieldValue fieldValue = returnFields.get(field);
		if (fieldValue == null)
			return null;
		return fieldValue.getValueArray();
	}

	public FieldValueItem[] getValueArray(String fieldName) {
		FieldValue fieldValue = returnFields.get(fieldName);
		if (fieldValue == null)
			return null;
		return fieldValue.getValueArray();
	}

	public String getValueContent(Field field, int pos) {
		FieldValueItem[] values = getValueArray(field);
		if (values == null)
			return null;
		if (pos >= values.length)
			return null;
		return values[pos].getValue();
	}

	public String getValueContent(String fieldName, int pos) {
		FieldValue field = returnFields.get(fieldName);
		if (field == null)
			return null;
		return getValueContent(field, pos);
	}

	public FieldValueItem[] getSnippetArray(SnippetField field) {
		return snippetFields.get(field).getValueArray();
	}

	public FieldValueItem[] getSnippetValue(SnippetField field) {
		return snippetFields.get(field).getValueArray();
	}

	public FieldValueItem[] getSnippetArray(String fieldName) {
		return snippetFields.get(fieldName).getValueArray();
	}

	public FieldValueItem[] getSnippetList(String fieldName) {
		SnippetFieldValue snippetFieldValue = snippetFields.get(fieldName);
		if (snippetFieldValue == null)
			return null;
		return snippetFieldValue.getValueArray();
	}

	public FieldValueItem[] getSnippetList(Field field) {
		return getSnippetList(field.getName());
	}

	public FieldValueItem getSnippet(String fieldName, int pos) {
		FieldValueItem[] values = getSnippetArray(fieldName);
		if (values == null)
			return null;
		if (pos >= values.length)
			return null;
		return values[pos];
	}

	public boolean isHighlighted(String fieldName) {
		return snippetFields.get(fieldName).isHighlighted();
	}

	public void appendIfStringDoesNotExist(ResultDocument rd) {
		for (FieldValue newFieldValue : rd.returnFields) {
			FieldValue fieldValue = returnFields.get(newFieldValue.getName());
			if (fieldValue == null)
				returnFields.add(fieldValue);
			else
				fieldValue.addIfStringDoesNotExist(newFieldValue
						.getValueArray());
		}
		for (SnippetFieldValue newFieldValue : rd.snippetFields) {
			SnippetFieldValue fieldValue = snippetFields.get(newFieldValue
					.getName());
			if (fieldValue == null)
				snippetFields.add(fieldValue);
			else
				fieldValue.addIfStringDoesNotExist(newFieldValue
						.getValueArray());
		}
	}
}
