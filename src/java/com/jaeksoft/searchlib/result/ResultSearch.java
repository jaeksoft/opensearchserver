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
import org.apache.lucene.search.FieldCache.StringIndex;

import com.jaeksoft.searchlib.collapse.CollapseSearch;
import com.jaeksoft.searchlib.facet.Facet;
import com.jaeksoft.searchlib.facet.FacetField;
import com.jaeksoft.searchlib.facet.FacetSearch;
import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.index.DocSetHits;
import com.jaeksoft.searchlib.index.ReaderLocal;
import com.jaeksoft.searchlib.request.Request;

public class ResultSearch extends Result<CollapseSearch> {

	private static final long serialVersionUID = -8289431499983379291L;

	transient private ReaderLocal reader;
	transient private StringIndex[] sortStringIndexArray;
	transient private DocSetHits docs;

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
	public ResultSearch(ReaderLocal reader, Request request)
			throws IOException, ParseException, SyntaxError {
		super(request);
		this.reader = reader;
		docs = reader.searchDocSet(request);
		numFound = docs.getDocNumFound();
		maxScore = docs.getMaxScore();
		for (FacetField facetField : request.getFacetFieldList())
			this.facetList.add(facetField.getFacetInstance(this));
		sortStringIndexArray = request.getSortList()
				.newStringIndexArray(reader);
		if (request.getCollapseField() != null) {
			this.collapse = new CollapseSearch(this);
			if (docs.getDocNumFound() > 0 && request.getCollapseActive()) {
				this.collapse.run();
				if (request.getCollapseMax() > 0) {
					fetchUntilCollapse();
					return;
				}
			}
		}
		fetchedDocs = ResultScoreDoc.newResultScoreDocArray(this, docs
				.getScoreDocs(request.getEnd()));
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
	 * To set the searcher. Useful when the result has been serialized.
	 * 
	 * @param searcher
	 * @throws IOException
	 */
	public void setReader(ReaderLocal reader) throws IOException {
		this.reader = reader;
		if (this.request.getFacetFieldList().size() > 0)
			for (Facet facet : this.getFacetList())
				((FacetSearch) facet).setReader(reader);
		if (this.collapse != null)
			this.collapse.setResult(this);
	}

	/**
	 * 
	 * @return DocSetHits.
	 */
	public DocSetHits getDocSetHits() {
		return this.docs;
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
		while (fetchedDocs.length < end) {
			int length = docs.getScoreDocs(rows).length;
			if (length == lastRows)
				break;
			fetchedDocs = collapse.run();
			lastRows = length;
			rows += request.getRows();
		}
	}

	@Override
	public DocumentResult documents() throws IOException, ParseException {
		if (documentResult != null)
			return documentResult;
		if (request.isDelete())
			return null;
		int start = request.getStart();
		int end = request.getEnd();
		int length = getFetchedDocs().length;
		if (end > length)
			end = length;
		for (int pos = start; pos < end; pos++)
			request.addDocId(reader, fetchedDocs[pos].doc);
		documentResult = reader.documents(request);
		return documentResult;
	}

	public void loadSortValues(ResultScoreDoc doc, String[] values) {
		int i = 0;
		for (StringIndex stringIndex : sortStringIndexArray)
			values[i++] = stringIndex.lookup[stringIndex.order[doc.doc]];
	}

	@Override
	public float getMaxScore() {
		return this.docs.getMaxScore();
	}

	@Override
	public int getNumFound() {
		return this.docs.getDocNumFound();
	}

}
