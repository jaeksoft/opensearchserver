/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008-2015 Emmanuel Keller / Jaeksoft
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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.collapse.CollapseFunctionField;
import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.index.ReaderAbstract;
import com.jaeksoft.searchlib.index.ReaderInterface;
import com.jaeksoft.searchlib.query.ParseException;
import com.jaeksoft.searchlib.request.AbstractSearchRequest;
import com.jaeksoft.searchlib.request.ReturnField;
import com.jaeksoft.searchlib.result.collector.CollapseDocInterface;
import com.jaeksoft.searchlib.result.collector.DocIdInterface;
import com.jaeksoft.searchlib.schema.AbstractField;
import com.jaeksoft.searchlib.snippet.SnippetField;
import com.jaeksoft.searchlib.util.Timer;
import com.opensearchserver.client.common.search.result.FunctionFieldValue;
import com.opensearchserver.client.common.search.result.VectorPosition;
import com.opensearchserver.client.v2.search.SnippetField2;

public class ResultDocument {

	final private Map<String, List<String>> returnFields;
	final private Map<String, SnippetField2> snippetFields;
	final private int docId;
	final private List<FunctionFieldValue> functionFieldValue;
	final private List<VectorPosition> positions;
	final private String joinParameter;
	final private List<ResultDocument> collapsedDocuments;
	private float score;

	public ResultDocument(final AbstractSearchRequest searchRequest,
			final TreeSet<String> fieldSet, final int docId,
			final ReaderInterface reader, final float score,
			final String joinParameter, final int collapsedDocumentCount,
			final Timer timer) throws IOException, ParseException, SyntaxError,
			SearchLibException {

		this.docId = docId;

		returnFields = new HashMap<String, List<String>>();
		snippetFields = new HashMap<String, SnippetField2>();
		functionFieldValue = new ArrayList<FunctionFieldValue>(0);
		positions = new ArrayList<VectorPosition>(0);
		collapsedDocuments = collapsedDocumentCount == 0 ? null
				: new ArrayList<ResultDocument>(0);

		this.joinParameter = joinParameter;
		this.score = score;

		if (docId < 0)
			return;

		Timer mainTimer = new Timer(timer, "ResultDocument");

		Timer t = new Timer(mainTimer, "returnField(s)");

		Map<String, List<String>> documentFields = reader.getDocumentFields(
				docId, fieldSet, t);

		for (ReturnField field : searchRequest.getReturnFieldList()) {
			String fieldName = field.getName().intern();
			List<String> fieldValues = documentFields.get(fieldName);
			if (fieldValues != null)
				returnFields.put(fieldName, fieldValues);
		}

		t.end(null);

		t = new Timer(mainTimer, "snippetField(s)");

		for (SnippetField field : searchRequest.getSnippetFieldList()) {
			String fieldName = field.getName().intern();
			field.initSearchTerms(searchRequest);
			List<String> snippets = new ArrayList<String>();
			boolean isHighlighted = false;
			List<String> fieldValues = documentFields.get(fieldName);
			if (fieldValues != null)
				isHighlighted = field.getSnippets(docId, reader, fieldValues,
						snippets, t);
			snippetFields.put(fieldName, new SnippetField2()
					.setValues(snippets).setHighlighted(isHighlighted));
		}

		t.end(null);

		mainTimer.end(null);
	}

	public ResultDocument(TreeSet<String> fieldSet, int docId,
			ReaderInterface reader, float score, String joinParameter,
			Timer timer) throws IOException, ParseException, SyntaxError,
			SearchLibException {
		this.docId = docId;
		returnFields = reader.getDocumentFields(docId, fieldSet, timer);
		snippetFields = new HashMap<String, SnippetField2>();
		collapsedDocuments = null;
		positions = new ArrayList<VectorPosition>(0);
		functionFieldValue = null;
		this.score = score;
		this.joinParameter = joinParameter;
	}

	public ResultDocument(Integer docId) {
		this.docId = docId;
		returnFields = new HashMap<String, List<String>>();
		snippetFields = new HashMap<String, SnippetField2>();
		collapsedDocuments = null;
		positions = new ArrayList<VectorPosition>(0);
		functionFieldValue = null;
		joinParameter = null;
		score = 0;
	}

	public static <T> List<T> toList(Map<String, T> map) {
		List<T> list = new ArrayList<T>(0);
		for (T fv : map.values())
			list.add(fv);
		return list;
	}

	public Map<String, List<String>> getReturnFields() {
		return returnFields;
	}

	public Map<String, SnippetField2> getSnippetFields() {
		return snippetFields;
	}

	public List<String> getValues(AbstractField<?> field) {
		if (field == null)
			return null;
		return getValues(field.getName());
	}

	public List<String> getValues(String fieldName) {
		if (fieldName == null)
			return null;
		return returnFields.get(fieldName.intern());
	}

	public String getValueContent(AbstractField<?> field, int pos) {
		if (field == null)
			return null;
		return getValueContent(field.getName(), pos);
	}

	public String getValueContent(String fieldName, int pos) {
		List<String> values = returnFields.get(fieldName.intern());
		if (values == null)
			return null;
		if (pos >= values.size())
			return null;
		return values.get(pos);
	}

	final public List<String> getSnippetValues(SnippetField field) {
		if (field == null)
			return null;
		return getSnippetValues(field.getName());
	}

	final public List<String> getSnippetValues(String fieldName) {
		SnippetField2 snippetField = getSnippets(fieldName);
		if (snippetField == null)
			return null;
		return snippetField.values;
	}

	final public SnippetField2 getSnippets(String fieldName) {
		if (fieldName == null)
			return null;
		return snippetFields.get(fieldName.intern());
	}

	final public SnippetField2 getSnippets(AbstractField<?> field) {
		if (field == null)
			return null;
		return getSnippets(field.getName());
	}

	final public String getSnippet(String fieldName, int pos) {
		List<String> values = getSnippetValues(fieldName);
		if (values == null)
			return null;
		if (pos >= values.size())
			return null;
		return values.get(pos);
	}

	final public boolean isHighlighted(String fieldName) {
		SnippetField2 snippetField = getSnippets(fieldName);
		if (snippetField == null)
			return false;
		return snippetField.highlighted;
	}

	public void appendIfStringDoesNotExist(ResultDocument rd) {
		for (Map.Entry<String, List<String>> entry : rd.returnFields.entrySet()) {
			String fieldName = entry.getKey();
			List<String> fieldValues = entry.getValue();
			List<String> values = returnFields.get(fieldName);
			if (values == null)
				returnFields.put(fieldName, new ArrayList<String>(fieldValues));
			else
				for (String fieldValue : fieldValues)
					if (!values.contains(fieldValue))
						values.add(fieldValue);
		}
		for (Map.Entry<String, SnippetField2> entry : rd.snippetFields
				.entrySet()) {
			String fieldName = entry.getKey();
			SnippetField2 fieldSnippet = entry.getValue();
			SnippetField2 snippetField = snippetFields.get(fieldName);
			if (snippetField == null)
				snippetFields.put(
						fieldName,
						new SnippetField2().setHighlighted(
								fieldSnippet.highlighted).setValues(
								new ArrayList<String>(fieldSnippet.values)));
			else
				for (String fieldValue : fieldSnippet.values)
					if (!snippetField.values.contains(fieldValue))
						snippetField.values.add(fieldValue);
		}
	}

	final public void addReturnedField(String field, String value) {
		if (field == null || value == null)
			return;
		field = field.intern();
		List<String> values = returnFields.get(field);
		if (values == null) {
			values = new ArrayList<String>(1);
			returnFields.put(field, values);
		}
		values.add(value);
	}

	public void addPosition(VectorPosition position) {
		positions.add(position);
	}

	public void addCollapsedDocument(ResultDocument document) {
		collapsedDocuments.add(document);
	}

	public List<ResultDocument> getCollapsedDocuments() {
		return collapsedDocuments;
	}

	public void addFunctionField(CollapseFunctionField functionField,
			ReaderAbstract reader, long pos, Timer timer) throws IOException,
			java.text.ParseException, InstantiationException,
			IllegalAccessException {
		functionFieldValue.add(new FunctionFieldValue(functionField
				.getFunction().function, functionField.getField(),
				functionField.executeByPos(pos)));
	}

	public List<FunctionFieldValue> getFunctionFieldValues() {
		return functionFieldValue;
	}

	final public static int[] getDocIds(DocIdInterface docs) {
		if (docs == null)
			return null;
		return docs.getIds();
	}

	final public static int getCollapseCount(DocIdInterface docs, long pos) {
		if (docs == null)
			return 0;
		if (!(docs instanceof CollapseDocInterface))
			return 0;
		return ((CollapseDocInterface) docs).getCollapseCounts()[(int) pos];
	}

	public int getDocId() {
		return docId;
	}

	public float getScore() {
		return score;
	}

	public String getJoinParameter() {
		return joinParameter;
	}

	public List<VectorPosition> getPositions() {
		return positions;
	}

	public void addPositions(Collection<VectorPosition> positions) {
		if (positions != null)
			this.positions.addAll(positions);
	}

}
