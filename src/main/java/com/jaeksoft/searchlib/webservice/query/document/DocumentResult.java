/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2011-2014 Emmanuel Keller / Jaeksoft
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.result.AbstractResultSearch;
import com.jaeksoft.searchlib.result.ResultDocument;
import com.jaeksoft.searchlib.result.ResultDocumentsInterface;
import com.jaeksoft.searchlib.schema.FieldValue;
import com.jaeksoft.searchlib.snippet.SnippetFieldValue;

@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
@JsonInclude(Include.NON_EMPTY)
public class DocumentResult {

	@XmlAttribute
	public final Integer pos;

	@XmlAttribute
	public final Float score;

	@XmlAttribute
	public final Float distance;

	@XmlAttribute
	public final Integer collapseCount;

	@XmlAttribute
	public final String joinParameter;

	@XmlElement(name = "field")
	public final List<FieldValueList> fields;

	@XmlElement(name = "snippet")
	public final List<SnippetValueList> snippets;

	@XmlElement(name = "function")
	public final List<FunctionFieldValue> functions;

	@XmlElement(name = "position")
	public final List<Position> positions;

	@XmlElement(name = "join")
	public final List<DocumentResult> joins;

	public static class Position {
		public final int start;
		public final int end;

		public Position() {
			start = 0;
			end = 0;
		}

		public Position(int start, int end) {
			this.start = start;
			this.end = end;
		}

		public Position(OffsetAttribute offsetAtt) {
			this.start = offsetAtt.startOffset();
			this.end = offsetAtt.endOffset();
		}
	}

	public DocumentResult() {
		fields = null;
		snippets = null;
		functions = null;
		positions = null;
		collapseCount = null;
		pos = null;
		score = null;
		distance = null;
		joins = null;
		joinParameter = null;
	}

	public DocumentResult(ResultDocument resultDocument,
			Integer collapseDocCount, Integer position, Float docScore,
			Float docDistance, List<ResultDocument> joinResultDocuments) {

		Map<String, FieldValue> returnFields = resultDocument.getReturnFields();
		fields = MapUtils.isEmpty(returnFields) ? null
				: new ArrayList<FieldValueList>(returnFields.size());
		if (returnFields != null)
			for (FieldValue fiedValue : returnFields.values())
				fields.add(new FieldValueList(fiedValue));

		Map<String, SnippetFieldValue> snippetFields = resultDocument
				.getSnippetFields();
		snippets = MapUtils.isEmpty(snippetFields) ? null
				: new ArrayList<SnippetValueList>(snippetFields.size());
		if (snippetFields != null)
			for (SnippetFieldValue snippetFiedValue : snippetFields.values())
				snippets.add(new SnippetValueList(snippetFiedValue));

		this.joinParameter = resultDocument.getJoinParameter();
		joins = CollectionUtils.isEmpty(joinResultDocuments) ? null
				: new ArrayList<DocumentResult>(joinResultDocuments.size());
		if (joinResultDocuments != null) {
			for (ResultDocument joinResultDocument : joinResultDocuments)
				joins.add(new DocumentResult(joinResultDocument, null, null,
						null, null, null));
		}
		functions = resultDocument.getFunctionFieldValues();
		positions = resultDocument.getPositions();
		collapseCount = collapseDocCount;
		pos = position;
		score = docScore;
		distance = docDistance;

	}

	public final static void populateDocumentList(
			ResultDocumentsInterface<?> result, List<DocumentResult> documents)
			throws SearchLibException {
		int start = result.getRequestStart();
		int end = result.getDocumentCount() + result.getRequestStart();
		AbstractResultSearch resultSearch = result instanceof AbstractResultSearch ? (AbstractResultSearch) result
				: null;
		for (int i = start; i < end; i++) {
			ResultDocument resultDocument = result.getDocument(i, null);
			int collapseDocCount = result.getCollapseCount(i);
			float docScore = result.getScore(i);
			Float docDistance = result.getDistance(i);
			List<ResultDocument> joinResultDocuments = resultSearch == null ? null
					: resultSearch.getJoinDocumentList(i, null);
			DocumentResult documentResult = new DocumentResult(resultDocument,
					collapseDocCount, i, docScore, docDistance,
					joinResultDocuments);
			documents.add(documentResult);
		}
	}
}
