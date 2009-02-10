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

import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.FieldCache.StringIndex;

import com.jaeksoft.searchlib.facet.FacetField;
import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.index.DocSetHits;
import com.jaeksoft.searchlib.index.ReaderLocal;
import com.jaeksoft.searchlib.request.Request;
import com.jaeksoft.searchlib.schema.FieldList;
import com.jaeksoft.searchlib.schema.FieldValue;

public class ResultSingle extends Result {

	private static final long serialVersionUID = -8289431499983379291L;

	transient private ReaderLocal reader;
	transient private StringIndex[] sortStringIndexArray;
	transient private DocSetHits docSetHits;

	/**
	 * The constructor executes the request using the searcher provided and
	 * computes the facets.
	 * 
	 * @param searcher
	 * @param request
	 * @throws IOException
	 * @throws ParseException
	 * @throws SyntaxError
	 */
	public ResultSingle(ReaderLocal reader, Request request)
			throws IOException, ParseException, SyntaxError {
		super(request);
		this.reader = reader;
		docSetHits = reader.searchDocSet(request);
		numFound = docSetHits.getDocNumFound();
		maxScore = docSetHits.getMaxScore();
		for (FacetField facetField : request.getFacetFieldList())
			this.facetList.add(facetField.getFacetInstance(this));
		sortStringIndexArray = request.getSortList()
				.newStringIndexArray(reader);

		// Are we doing collapsing ?
		if (collapse.isActive())
			fetchUntilCollapse();
		else
			loadDocs(request.getEnd());
		if (request.isWithDocument())
			loadDocuments();
	}

	public void loadDocs(int end) throws IOException {
		if (end <= getDocLength())
			return;
		setDocs(ResultScoreDoc.newResultScoreDocArray(this, docSetHits
				.getScoreDocs(end), collapse.getCollapseField()));
	}

	/**
	 * Returns the searcher used to build the result.
	 * 
	 * @return Searcher
	 */
	public ReaderLocal getReader() {
		return reader;
	}

	/**
	 * 
	 * @return DocSetHits.
	 */
	public DocSetHits getDocSetHits() {
		return this.docSetHits;
	}

	/**
	 * Fetch new documents until collapsed results is complete.
	 * 
	 * @throws IOException
	 */
	private void fetchUntilCollapse() throws IOException {
		int end = this.request.getEnd();
		int lastRows = 0;
		int rows = end;
		while (collapse.getCollapsedDocsLength() < end) {
			ScoreDoc[] scoreDocs = docSetHits.getScoreDocs(rows);
			if (scoreDocs.length == lastRows)
				break;
			collapse.run(ResultScoreDoc.newResultScoreDocArray(this, scoreDocs,
					collapse.getCollapseField()), end);
			lastRows = scoreDocs.length;
			rows += request.getRows();
		}
		setDocs(collapse.getCollapsedDoc());
	}

	private void loadDocuments() throws IOException, ParseException,
			SyntaxError {
		if (request.isDelete())
			return;
		int start = request.getStart();
		int end = request.getEnd();
		ResultScoreDoc[] scoreDocs = getDocs();
		int length = scoreDocs.length;
		if (end > length)
			end = length;
		for (int i = start; i < end; i++) {
			ResultScoreDoc scoreDoc = scoreDocs[i];
			FieldList<FieldValue> documentFields = reader.getDocumentFields(
					scoreDoc.doc, request.getDocumentFieldList());
			scoreDoc.resultDocument = new ResultDocument(request, scoreDoc.doc,
					reader, documentFields);
		}
	}

	public void loadSortValues(ResultScoreDoc doc, String[] values) {
		int i = 0;
		for (StringIndex stringIndex : sortStringIndexArray)
			values[i++] = stringIndex.lookup[stringIndex.order[doc.doc]];
	}

}
