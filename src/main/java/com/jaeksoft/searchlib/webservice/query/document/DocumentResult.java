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
package com.jaeksoft.searchlib.webservice.query.document;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.jaeksoft.searchlib.SearchLibException;
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
	public final Integer collapseCount;

	@XmlElement(name = "field")
	public final List<FieldValueList> fields;

	@XmlElement(name = "snippet")
	public final List<SnippetValueList> snippets;

	@XmlElement(name = "function")
	public final List<FunctionFieldValue> functions;

	@XmlElement(name = "position")
	public final List<Position> positions;

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
	}

	public DocumentResult(ResultDocument resultDocument,
			Integer collapseDocCount, Integer position, Float docScore) {
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
		functions = resultDocument.getFunctionFieldValues();
		positions = resultDocument.getPositions();
		collapseCount = collapseDocCount;
		pos = position;
		score = docScore;
	}

	public final static void populateDocumentList(
			ResultDocumentsInterface<?> result, List<DocumentResult> documents)
			throws SearchLibException {
		int start = result.getRequestStart();
		int end = result.getDocumentCount() + result.getRequestStart();
		for (int i = start; i < end; i++) {
			ResultDocument resultDocument = result.getDocument(i, null);
			int collapseDocCount = result.getCollapseCount(i);
			float docScore = result.getScore(i);
			DocumentResult documentResult = new DocumentResult(resultDocument,
					collapseDocCount, i, docScore);
			documents.add(documentResult);
		}
	}
}
