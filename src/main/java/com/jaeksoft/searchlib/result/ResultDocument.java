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
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.collapse.CollapseFunctionField;
import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.index.ReaderAbstract;
import com.jaeksoft.searchlib.index.ReaderInterface;
import com.jaeksoft.searchlib.query.ParseException;
import com.jaeksoft.searchlib.request.AbstractLocalSearchRequest;
import com.jaeksoft.searchlib.request.ReturnField;
import com.jaeksoft.searchlib.result.collector.CollapseDocInterface;
import com.jaeksoft.searchlib.result.collector.DocIdInterface;
import com.jaeksoft.searchlib.schema.AbstractField;
import com.jaeksoft.searchlib.schema.FieldValue;
import com.jaeksoft.searchlib.schema.FieldValueItem;
import com.jaeksoft.searchlib.schema.FieldValueOriginEnum;
import com.jaeksoft.searchlib.snippet.SnippetField;
import com.jaeksoft.searchlib.snippet.SnippetFieldValue;
import com.jaeksoft.searchlib.util.Timer;
import com.jaeksoft.searchlib.webservice.query.document.DocumentResult.Position;
import com.jaeksoft.searchlib.webservice.query.document.FunctionFieldValue;

public class ResultDocument {

	final private LinkedHashMap<String, FieldValue> returnFields;
	final private LinkedHashMap<String, SnippetFieldValue> snippetFields;
	final private int docId;
	final private List<FunctionFieldValue> functionFieldValue;
	final private List<Position> positions;
	final private String joinParameter;
	final private List<ResultDocument> collapsedDocuments;
	final private float score;

	public ResultDocument(final AbstractLocalSearchRequest searchRequest, final LinkedHashSet<String> fieldSet,
			final int docId, final ReaderInterface reader, final float score, final String joinParameter,
			final int collapsedDocumentCount, final Timer timer)
					throws IOException, ParseException, SyntaxError, SearchLibException {

		this.docId = docId;

		returnFields = new LinkedHashMap<String, FieldValue>();
		snippetFields = new LinkedHashMap<String, SnippetFieldValue>();
		functionFieldValue = new ArrayList<FunctionFieldValue>(0);
		positions = new ArrayList<Position>(0);
		collapsedDocuments = collapsedDocumentCount == 0 ? null : new ArrayList<ResultDocument>(0);

		this.joinParameter = joinParameter;
		this.score = score;

		if (docId < 0)
			return;

		Timer mainTimer = new Timer(timer, "ResultDocument");

		Timer t = new Timer(mainTimer, "returnField(s)");

		Map<String, FieldValue> documentFields = reader.getDocumentFields(docId, fieldSet, t);

		for (ReturnField field : searchRequest.getReturnFieldList()) {
			String fieldName = field.getName();
			FieldValue fieldValue = documentFields.get(fieldName);
			if (fieldValue != null)
				returnFields.put(fieldName, fieldValue);
		}

		t.end(null);

		t = new Timer(mainTimer, "snippetField(s)");

		for (SnippetField field : searchRequest.getSnippetFieldList()) {
			String fieldName = field.getName();
			field.initSearchTerms(searchRequest);
			List<FieldValueItem> snippets = new ArrayList<FieldValueItem>();
			boolean isHighlighted = false;
			FieldValue fieldValue = documentFields.get(fieldName);
			if (fieldValue != null)
				isHighlighted = field.getSnippets(docId, reader, fieldValue.getValueList(), snippets, t);
			SnippetFieldValue snippetFieldValue = new SnippetFieldValue(fieldName, snippets, isHighlighted);
			snippetFields.put(fieldName, snippetFieldValue);
		}

		t.end(null);

		mainTimer.end(null);
	}

	public ResultDocument(LinkedHashSet<String> fieldSet, int docId, ReaderInterface reader, float score,
			String joinParameter, Timer timer) throws IOException, ParseException, SyntaxError, SearchLibException {
		this.docId = docId;
		returnFields = reader.getDocumentFields(docId, fieldSet, timer);
		snippetFields = new LinkedHashMap<String, SnippetFieldValue>();
		collapsedDocuments = null;
		positions = new ArrayList<Position>(0);
		functionFieldValue = null;
		this.score = score;
		this.joinParameter = joinParameter;
	}

	public ResultDocument(Integer docId) {
		this.docId = docId;
		returnFields = new LinkedHashMap<String, FieldValue>();
		snippetFields = new LinkedHashMap<String, SnippetFieldValue>();
		collapsedDocuments = null;
		positions = new ArrayList<Position>(0);
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

	public Map<String, FieldValue> getReturnFields() {
		return returnFields;
	}

	public Map<String, SnippetFieldValue> getSnippetFields() {
		return snippetFields;
	}

	public List<FieldValueItem> getValues(AbstractField<?> field) {
		if (field == null)
			return null;
		return getValues(field.getName());
	}

	public List<FieldValueItem> getValues(String fieldName) {
		if (fieldName == null)
			return null;
		FieldValue fieldValue = returnFields.get(fieldName);
		if (fieldValue == null)
			return null;
		return fieldValue.getValueList();
	}

	public String getValueContent(AbstractField<?> field, int pos) {
		List<FieldValueItem> values = getValues(field);
		if (values == null)
			return null;
		if (pos >= values.size())
			return null;
		return values.get(pos).getValue();
	}

	public String getValueContent(String fieldName, int pos) {
		FieldValue field = returnFields.get(fieldName);
		if (field == null)
			return null;
		return getValueContent(field, pos);
	}

	final public List<FieldValueItem> getSnippetValues(SnippetField field) {
		if (field == null)
			return null;
		return getSnippetValues(field.getName());
	}

	final public List<FieldValueItem> getSnippetValue(SnippetField field) {
		if (field == null)
			return null;
		return getSnippetValues(field.getName());
	}

	final public List<FieldValueItem> getSnippetValues(String fieldName) {
		SnippetFieldValue fieldValue = snippetFields.get(fieldName);
		if (fieldValue == null)
			return null;
		return fieldValue.getValueList();
	}

	public List<FieldValueItem> getSnippetList(String fieldName) {
		SnippetFieldValue snippetFieldValue = snippetFields.get(fieldName);
		if (snippetFieldValue == null)
			return null;
		return snippetFieldValue.getValueList();
	}

	final public List<FieldValueItem> getSnippetList(AbstractField<?> field) {
		if (field == null)
			return null;
		return getSnippetList(field.getName());
	}

	final public FieldValueItem getSnippet(String fieldName, int pos) {
		List<FieldValueItem> values = getSnippetValues(fieldName);
		if (values == null)
			return null;
		if (pos >= values.size())
			return null;
		return values.get(pos);
	}

	final public String getSnippetContent(String fieldName, int pos) {
		FieldValueItem fieldValue = getSnippet(fieldName, pos);
		if (fieldValue == null)
			return null;
		return fieldValue.getValue();
	}

	final public boolean isHighlighted(String fieldName) {
		return snippetFields.get(fieldName).isHighlighted();
	}

	public void appendIfStringDoesNotExist(ResultDocument rd) {
		for (FieldValue newFieldValue : rd.returnFields.values()) {
			String fieldName = newFieldValue.getName();
			FieldValue fieldValue = returnFields.get(fieldName);
			if (fieldValue == null)
				returnFields.put(fieldName, fieldValue);
			else
				fieldValue.addIfStringDoesNotExist(newFieldValue.getValueList());
		}
		for (SnippetFieldValue newFieldValue : rd.snippetFields.values()) {
			String fieldName = newFieldValue.getName();
			SnippetFieldValue fieldValue = snippetFields.get(fieldName);
			if (fieldValue == null)
				snippetFields.put(fieldName, fieldValue);
			else
				fieldValue.addIfStringDoesNotExist(newFieldValue.getValueList());
		}
	}

	public void addReturnedField(FieldValueOriginEnum origin, String field, String value) {
		FieldValue fieldValue = returnFields.get(field);
		if (fieldValue == null) {
			fieldValue = new FieldValue(field);
			returnFields.put(field, fieldValue);
		}
		fieldValue.addValues(new FieldValueItem(origin, value));
	}

	public void addReturnedFields(FieldValue newFieldValue) {
		if (newFieldValue == null)
			return;
		if (newFieldValue.getValuesCount() == 0)
			return;
		String field = newFieldValue.getName();
		FieldValue fieldValue = returnFields.get(field);
		if (fieldValue == null) {
			fieldValue = new FieldValue(field);
			returnFields.put(field, fieldValue);
		}
		fieldValue.addValues(newFieldValue.getValueList());
	}

	public void addPosition(Position position) {
		positions.add(position);
	}

	public void addCollapsedDocument(ResultDocument document) {
		collapsedDocuments.add(document);
	}

	public List<ResultDocument> getCollapsedDocuments() {
		return collapsedDocuments;
	}

	public void addFunctionField(CollapseFunctionField functionField, ReaderAbstract reader, int pos, Timer timer)
			throws IOException, java.text.ParseException, InstantiationException, IllegalAccessException {
		functionFieldValue.add(new FunctionFieldValue(functionField, functionField.executeByPos(pos)));
	}

	public List<FunctionFieldValue> getFunctionFieldValues() {
		return functionFieldValue;
	}

	final public static int[] getDocIds(DocIdInterface docs) {
		if (docs == null)
			return null;
		return docs.getIds();
	}

	final public static int getCollapseCount(DocIdInterface docs, int pos) {
		if (docs == null)
			return 0;
		if (!(docs instanceof CollapseDocInterface))
			return 0;
		return ((CollapseDocInterface) docs).getCollapseCounts()[pos];
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

	public List<Position> getPositions() {
		return positions;
	}

	public void addPositions(Collection<Position> positions) {
		if (positions != null)
			this.positions.addAll(positions);
	}

}
