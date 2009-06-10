/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2008-2009 Emmanuel Keller / Jaeksoft
 * 
 * http://www.open-search-server.com
 * 
 * This file is part of Jaeksoft OpenSearchServer.
 *
 * Jaeksoft OpenSearchServer is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * Jaeksoft OpenSearchServer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Jaeksoft OpenSearchServer. 
 *  If not, see <http://www.gnu.org/licenses/>.
 **/

package com.jaeksoft.searchlib.result;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.queryParser.ParseException;

import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.index.ReaderLocal;
import com.jaeksoft.searchlib.request.DocumentsRequest;
import com.jaeksoft.searchlib.schema.Field;
import com.jaeksoft.searchlib.schema.FieldList;
import com.jaeksoft.searchlib.schema.FieldValue;
import com.jaeksoft.searchlib.snippet.SnippetField;
import com.jaeksoft.searchlib.snippet.SnippetFieldValue;
import com.jaeksoft.searchlib.util.External;

public class ResultDocument implements Externalizable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6099412341625264882L;

	private FieldList<FieldValue> returnFields;
	private FieldList<SnippetFieldValue> snippetFields;

	public ResultDocument() {
	}

	public ResultDocument(DocumentsRequest documentsRequest, int doc,
			ReaderLocal reader) throws IOException, ParseException, SyntaxError {

		FieldList<FieldValue> documentFields = reader.getDocumentFields(doc,
				documentsRequest.getDocumentFieldList());

		returnFields = new FieldList<FieldValue>();
		snippetFields = new FieldList<SnippetFieldValue>();

		for (Field field : documentsRequest.getReturnFieldList())
			returnFields.add(documentFields.get(field));

		for (SnippetField field : documentsRequest.getSnippetFieldList()) {
			List<String> snippets = new ArrayList<String>();
			boolean isSnippet = field.getSnippets(doc, reader, documentFields
					.get(field).getValueArray(), snippets);
			SnippetFieldValue fieldValue = new SnippetFieldValue(field,
					snippets, isSnippet);
			snippetFields.add(fieldValue);
		}
	}

	public FieldList<FieldValue> getReturnFields() {
		return returnFields;
	}

	public FieldList<SnippetFieldValue> getSnippetFields() {
		return snippetFields;
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

	public String[] getSnippetArray(SnippetField field) {
		return snippetFields.get(field).getValueArray();
	}

	public List<String> getSnippetValue(SnippetField field) {
		return snippetFields.get(field).getValueList();
	}

	public String[] getSnippetArray(String fieldName) {
		return snippetFields.get(fieldName).getValueArray();
	}

	public List<String> getSnippetList(String fieldName) {
		SnippetFieldValue snippetFieldValue = snippetFields.get(fieldName);
		if (snippetFieldValue == null)
			return null;
		return snippetFieldValue.getValueList();
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
		return snippetFields.get(fieldName).isHighlighted();
	}

	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {
		returnFields = External.readObject(in);
		snippetFields = External.readObject(in);
	}

	public void writeExternal(ObjectOutput out) throws IOException {
		External.writeObject(returnFields, out);
		External.writeObject(snippetFields, out);
	}

}
