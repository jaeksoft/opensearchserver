/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2008-2009 Emmanuel Keller / Jaeksoft
 * 
 * http://www.jaeksoft.com
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

import java.io.IOException;

import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.FieldCache.StringIndex;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.facet.FacetField;
import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.index.DocSetHits;
import com.jaeksoft.searchlib.index.ReaderLocal;
import com.jaeksoft.searchlib.request.DocumentsRequest;
import com.jaeksoft.searchlib.request.SearchRequest;

public class ResultSingle extends Result {

	private static final long serialVersionUID = -8289431499983379291L;

	transient private ReaderLocal reader;
	transient private StringIndex[] sortStringIndexArray;
	transient private DocSetHits docSetHits;

	public ResultSingle() {
	}

	/**
	 * The constructor executes the request using the searcher provided and
	 * computes the facets.
	 * 
	 * @param searcher
	 * @param request
	 * @throws IOException
	 * @throws ParseException
	 * @throws SyntaxError
	 * @throws SearchLibException
	 */
	public ResultSingle(ReaderLocal reader, SearchRequest searchRequest)
			throws IOException, ParseException, SyntaxError, SearchLibException {
		super(searchRequest);

		this.reader = reader;
		docSetHits = reader.searchDocSet(searchRequest);
		numFound = docSetHits.getDocNumFound();
		maxScore = docSetHits.getMaxScore();
		for (FacetField facetField : searchRequest.getFacetFieldList())
			this.facetList.addObject(facetField.getFacet(this));

		ResultScoreDoc[] docs;
		// Are we doing collapsing ?
		if (collapse.isActive()) {
			docs = fetchUntilCollapse();
			collapsedDocCount = collapse.getDocCount();
		} else
			docs = fetchWithoutCollapse();

		if (searchRequest.isWithSortValues()) {
			sortStringIndexArray = searchRequest.getSortList()
					.newStringIndexArray(reader);
			if (sortStringIndexArray != null)
				for (ResultScoreDoc doc : docs)
					doc.loadSortValues(sortStringIndexArray);
		}

		setDocs(docs);

		if (searchRequest.isWithDocument())
			setDocuments(reader.documents(new DocumentsRequest(this)));

		if (debug != null)
			debug.setInfo(this);

	}

	private ResultScoreDoc[] fetchWithoutCollapse() throws IOException,
			ParseException, SyntaxError {
		int end = searchRequest.getEnd();
		String collapseField = searchRequest.getCollapseField();
		StringIndex collapseFieldStringIndex = (collapseField != null) ? reader
				.getStringIndex(collapseField) : null;
		return ResultScoreDoc.appendResultScoreDocArray(reader.getName(), this,
				getDocs(), docSetHits.getScoreDocs(end),
				collapseFieldStringIndex);
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
	 * @throws SyntaxError
	 * @throws ParseException
	 */
	private ResultScoreDoc[] fetchUntilCollapse() throws IOException,
			ParseException, SyntaxError {
		int end = searchRequest.getEnd();
		int lastRows = 0;
		int rows = end;
		StringIndex collapseFieldStringIndex = reader
				.getStringIndex(searchRequest.getCollapseField());
		ResultScoreDoc[] resultScoreDocs = null;
		String indexName = reader.getName();
		while (collapse.getCollapsedDocsLength() < end) {
			ScoreDoc[] scoreDocs = docSetHits.getScoreDocs(rows);
			if (scoreDocs.length == lastRows)
				break;
			resultScoreDocs = ResultScoreDoc.appendResultScoreDocArray(
					indexName, this, resultScoreDocs, scoreDocs,
					collapseFieldStringIndex);
			collapse.run(resultScoreDocs, end);
			lastRows = scoreDocs.length;
			rows += searchRequest.getRows();
		}
		return collapse.getCollapsedDoc();
	}

}
