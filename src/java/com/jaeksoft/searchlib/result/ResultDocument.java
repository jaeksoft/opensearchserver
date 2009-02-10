/**   
 * License Agreement for Jaeksoft SearchLib Community
 *
 * Copyright (C) 2008 Emmanuel Keller / Jaeksoft
 * 
 * http://www.jaeksoft.com
 * 
 * This file is part of Jaeksoft SearchLib Community.
 *
 * Jaeksoft SearchLib Community is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * Jaeksoft SearchLib Community is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Jaeksoft SearchLib Community. 
 *  If not, see <http://www.gnu.org/licenses/>.
 **/

package com.jaeksoft.searchlib.result;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.queryParser.ParseException;

import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.highlight.HighlightField;
import com.jaeksoft.searchlib.highlight.HighlightFieldValue;
import com.jaeksoft.searchlib.index.ReaderLocal;
import com.jaeksoft.searchlib.request.Request;
import com.jaeksoft.searchlib.schema.Field;
import com.jaeksoft.searchlib.schema.FieldList;
import com.jaeksoft.searchlib.schema.FieldValue;

public class ResultDocument implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6099412341625264882L;

	private FieldList<FieldValue> returnFields;
	private FieldList<HighlightFieldValue> highlightFields;

	public ResultDocument(Request request, int docId, ReaderLocal reader,
			FieldList<FieldValue> fieldsValues) throws IOException,
			ParseException, SyntaxError {
		returnFields = new FieldList<FieldValue>();
		highlightFields = new FieldList<HighlightFieldValue>();

		for (Field field : request.getReturnFieldList())
			returnFields.add(fieldsValues.get(field));

		for (HighlightField field : request.getHighlightFieldList()) {
			List<String> snippets = new ArrayList<String>();
			boolean highlighted = field.getSnippets(request, docId, reader,
					fieldsValues.get(field).getValueArray(), snippets);
			HighlightFieldValue fieldValue = new HighlightFieldValue(field,
					snippets, highlighted);
			highlightFields.add(fieldValue);
		}
	}

	public String[] getValueArray(Field field) {
		return returnFields.get(field).getValueArray();
	}

	public List<String> getValueList(Field field) {
		return getValueList(field.getName());
	}

	public String[] getValueArray(String fieldName) {
		return returnFields.get(fieldName).getValueArray();
	}

	public List<String> getValueList(String fieldName) {
		FieldValue fieldValue = returnFields.get(fieldName);
		if (fieldValue == null)
			return null;
		return fieldValue.getValueList();
	}

	public String getValue(Field field, int pos) {
		String[] values = getValueArray(field);
		if (values == null)
			return null;
		if (pos >= values.length)
			return null;
		return values[pos];
	}

	public String getValue(String fieldName, int pos) {
		FieldValue field = returnFields.get(fieldName);
		if (field == null)
			return null;
		return getValue(field, pos);
	}

	public String[] getSnippetArray(HighlightField field) {
		return highlightFields.get(field).getValueArray();
	}

	public List<String> getSnippetValue(HighlightField field) {
		return highlightFields.get(field).getValueList();
	}

	public String[] getSnippetArray(String fieldName) {
		return highlightFields.get(fieldName).getValueArray();
	}

	public List<String> getSnippetList(String fieldName) {
		HighlightFieldValue highlightFieldValue = highlightFields
				.get(fieldName);
		if (highlightFieldValue == null)
			return null;
		return highlightFieldValue.getValueList();
	}

	public List<String> getSnippetList(Field field) {
		return getSnippetList(field.getName());
	}

	public String getSnippet(String fieldName, int pos) {
		String[] values = getSnippetArray(fieldName);
		if (values == null)
			return null;
		if (pos >= values.length)
			return null;
		return values[pos];
	}

	public boolean isHighlighted(String fieldName) {
		return highlightFields.get(fieldName).isHighlighted();
	}
}
