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

import com.jaeksoft.searchlib.collapse.CollapseSearch;
import com.jaeksoft.searchlib.facet.Facet;
import com.jaeksoft.searchlib.facet.FacetField;
import com.jaeksoft.searchlib.facet.FacetSearch;
import com.jaeksoft.searchlib.index.DocSetHits;
import com.jaeksoft.searchlib.index.ReaderLocal;
import com.jaeksoft.searchlib.request.Request;

public class ResultSearch extends Result<CollapseSearch> {

	private static final long serialVersionUID = -8289431499983379291L;
	transient private ReaderLocal reader;
	private DocSetHits docs;

	/**
	 * The constructor executes the request using the searcher provided and
	 * computes the facets.
	 * 
	 * @param searcher
	 * @param request
	 * @throws IOException
	 */
	public ResultSearch(ReaderLocal reader, Request request) throws IOException {
		super(request);
		this.reader = reader;
		this.docs = reader.searchDocSet(request);
		for (FacetField facetField : request.getFacetFieldList())
			this.facetList.add(new FacetSearch(this, facetField));
		if (request.getCollapseField() != null) {
			this.collapse = new CollapseSearch(this);
			if (this.docs.getDocNumFound() > 0 && request.getCollapseActive())
				this.collapse.run();
			if (request.getCollapseMax() > 0) {
				fetchUntilCollapse();
				return;
			}
		}
		docs.getHits(this.request.getEnd());
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
	 * Set the DocSetHits.
	 * 
	 * @param docs
	 */
	public void setDocSetHits(DocSetHits docs) {
		this.docs = docs;
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
		while (collapse.getDocs().length < end) {
			docs.getHits(rows);
			if (docs.getSortFetchDocs().length == lastRows)
				break;
			collapse.run();
			lastRows = docs.getSortFetchDocs().length;
			rows += request.getRows();
		}
	}

	/**
	 * Returns the internal fetched document array.
	 */
	@Override
	public int[] getFetchedDoc() {
		return docs.getSortFetchDocs();
	}

	/**
	 * Return the internal document array (fetched or collapsed).
	 */
	@Override
	public int[] getDocs() {
		if (request.getCollapseActive())
			return this.collapse.getDocs();
		else
			return this.getFetchedDoc();
	}

	@Override
	public DocumentResult documents() throws IOException {
		if (documentResult != null)
			return documentResult;
		if (request.isDelete())
			return null;
		int start = request.getStart();
		int end = request.getEnd();
		if (end > getDocs().length)
			end = getDocs().length;
		for (int pos = start; pos < end; pos++)
			request.addDocId(reader, getDocId(pos));
		documentResult = reader.documents(request);
		return documentResult;
	}

	@Override
	public float getScore(int pos) {
		if (request.getCollapseActive())
			return this.docs.getScore(this.collapse.getDocs()[pos]);
		else
			return this.docs.getScore(pos);
	}

	public int getDocId(int pos) {
		if (request.getCollapseActive())
			return this.docs.getDocId(this.collapse.getDocs()[pos]);
		else
			return this.docs.getDocId(pos);
	}

	public int[] getUnsortedDocFound() {
		return this.docs.getCollectedDocs();
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
