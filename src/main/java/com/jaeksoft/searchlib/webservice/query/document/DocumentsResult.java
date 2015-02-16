/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2011-2015 Emmanuel Keller / Jaeksoft
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
package com.jaeksoft.searchlib.webservice.query.document;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.result.AbstractResultSearch;
import com.jaeksoft.searchlib.result.ResultDocument;
import com.jaeksoft.searchlib.result.ResultDocumentsInterface;
import com.opensearchserver.client.v2.search.DocumentResult2;
import com.opensearchserver.client.v2.search.SnippetField2;

@XmlRootElement(name = "result")
@XmlAccessorType(XmlAccessType.FIELD)
@JsonInclude(Include.NON_EMPTY)
public class DocumentsResult {

	@XmlElement(name = "document")
	final public List<DocumentResult2> documents;

	@XmlElement(name = "indexDocument")
	final public List<IndexDocumentResult> indexDocuments;

	@XmlElement(name = "k")
	final public String[] uniqueKeys;

	public DocumentsResult() {
		documents = null;
		indexDocuments = null;
		uniqueKeys = null;
	}

	public DocumentsResult(ResultDocumentsInterface<?> result,
			boolean indexDocument) throws SearchLibException, IOException {
		uniqueKeys = null;
		if (indexDocument) {
			documents = null;
			indexDocuments = new ArrayList<IndexDocumentResult>(1);
			result.populate(indexDocuments);
		} else {
			documents = populateDocumentList(result,
					new ArrayList<DocumentResult2>(1));
			indexDocuments = null;
		}
	}

	public DocumentsResult(String[] uniqueKeys) {
		this.documents = null;
		this.indexDocuments = null;
		this.uniqueKeys = uniqueKeys;
	}

	public final static DocumentResult2 newDocumentResult(
			ResultDocument resultDocument, Integer collapseDocCount,
			Long position, Float docScore, Float docDistance,
			List<ResultDocument> joinResultDocuments) {

		DocumentResult2 documentResult = new DocumentResult2();
		Map<String, List<String>> returnFields = resultDocument
				.getReturnFields();
		Map<String, List<String>> fields = MapUtils.isEmpty(returnFields) ? null
				: new LinkedHashMap<String, List<String>>();
		if (returnFields != null) {
			for (Map.Entry<String, List<String>> entry : returnFields
					.entrySet()) {
				String field = entry.getKey().intern();
				List<String> valueList = entry.getValue();
				if (valueList != null) {
					List<String> values = fields.get(field);
					if (values == null) {
						values = new ArrayList<String>(valueList.size());
						fields.put(field, values);
					}
					values.addAll(valueList);
				}
			}
		}
		documentResult.setFields(fields);

		Map<String, SnippetField2> snippetFields = resultDocument
				.getSnippetFields();
		Map<String, SnippetField2> snippets = MapUtils.isEmpty(snippetFields) ? null
				: new LinkedHashMap<String, SnippetField2>();
		if (snippetFields != null) {
			for (Map.Entry<String, SnippetField2> entry : snippetFields
					.entrySet()) {
				String field = entry.getKey().intern();
				SnippetField2 valueSnippet = entry.getValue();
				if (valueSnippet != null) {
					SnippetField2 snippetField = new SnippetField2();
					snippetField.setHighlighted(valueSnippet.highlighted);
					snippetField.setValues(new ArrayList<String>(
							valueSnippet.values));
					snippets.put(field, snippetField);
				}
			}
			documentResult.setSnippets(snippets);
		}

		documentResult.setJoinParameter(resultDocument.getJoinParameter());
		if (!CollectionUtils.isEmpty(joinResultDocuments)) {
			List<DocumentResult2> joins = new ArrayList<DocumentResult2>(
					joinResultDocuments.size());
			for (ResultDocument joinResultDocument : joinResultDocuments)
				joins.add(new DocumentResult2().setFields(joinResultDocument
						.getReturnFields()));
			documentResult.setJoins(joins);
		}
		documentResult.setFunctions(resultDocument.getFunctionFieldValues());
		documentResult.setPositions(resultDocument.getPositions());
		documentResult.collapseCount = collapseDocCount;
		documentResult.pos = position;
		documentResult.score = docScore;
		documentResult.distance = docDistance;

		return documentResult;
	}

	public final static List<DocumentResult2> populateDocumentList(
			ResultDocumentsInterface<?> result, List<DocumentResult2> documents)
			throws SearchLibException {
		long start = result.getRequestStart();
		long end = result.getDocumentCount() + result.getRequestStart();
		AbstractResultSearch resultSearch = result instanceof AbstractResultSearch ? (AbstractResultSearch) result
				: null;
		for (long i = start; i < end; i++) {
			ResultDocument resultDocument = result.getDocument(i, null);
			int collapseDocCount = result.getCollapseCount(i);
			float docScore = result.getScore(i);
			Float docDistance = result.getDistance(i);
			List<ResultDocument> joinResultDocuments = resultSearch == null ? null
					: resultSearch.getJoinDocumentList(i, null);
			DocumentResult2 documentResult = newDocumentResult(resultDocument,
					collapseDocCount, i, docScore, docDistance,
					joinResultDocuments);
			documents.add(documentResult);
		}
		return documents;
	}
}
