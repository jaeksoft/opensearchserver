/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008-2012 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.collapse;

import java.io.IOException;
import java.util.List;

import org.apache.lucene.search.FieldCache.StringIndex;

import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.index.DocSetHits;
import com.jaeksoft.searchlib.index.ReaderLocal;
import com.jaeksoft.searchlib.query.ParseException;
import com.jaeksoft.searchlib.request.SearchRequest;
import com.jaeksoft.searchlib.result.ResultScoreDoc;
import com.jaeksoft.searchlib.result.ResultScoreDocCollapse;
import com.jaeksoft.searchlib.util.Timer;

public abstract class CollapseAbstract {

	private int collapsedDocCount;
	private transient int collapseMax;
	private transient String collapseField;
	private transient CollapseParameters.Mode collapseMode;
	private transient CollapseParameters.Type collapseType;
	protected transient SearchRequest searchRequest;
	private transient ResultScoreDoc[] collapsedDoc;

	protected CollapseAbstract(SearchRequest searchRequest) {
		this.searchRequest = searchRequest;
		this.collapseField = searchRequest.getCollapseField();
		this.collapseMax = searchRequest.getCollapseMax();
		this.collapseMode = searchRequest.getCollapseMode();
		this.collapseType = searchRequest.getCollapseType();
		this.collapsedDocCount = 0;
		this.collapsedDoc = ResultScoreDocCollapse.EMPTY_ARRAY;
	}

	protected abstract void collapse(ResultScoreDoc[] fetchedDocs,
			int fetchLength, StringIndex collapseStringIndex, Timer timer);

	public void run(ResultScoreDoc[] fetchedDocs, int fetchLength,
			StringIndex collapseStringIndex, Timer timer) throws IOException {

		collapsedDoc = null;

		if (fetchedDocs == null)
			return;

		if (fetchLength > fetchedDocs.length)
			fetchLength = fetchedDocs.length;

		collapse(fetchedDocs, fetchLength, collapseStringIndex, timer);
	}

	public int getDocCount() {
		return this.collapsedDocCount;
	}

	/**
	 * @param collapsedDocCount
	 *            the collapsedDocCount to set
	 */
	final protected void setCollapsedDocCount(int collapsedDocCount) {
		this.collapsedDocCount = collapsedDocCount;
	}

	/**
	 * @param collapsedDoc
	 *            the collapsedDoc to set
	 */
	final protected void setCollapsedDoc(ResultScoreDoc[] collapsedDoc) {
		this.collapsedDoc = collapsedDoc;
	}

	final protected void setCollapsedDoc(List<ResultScoreDoc> collapsedList) {
		if (collapsedList == null) {
			setCollapsedDoc((ResultScoreDoc[]) null);
			return;
		}
		this.collapsedDoc = new ResultScoreDoc[collapsedList.size()];
		collapsedList.toArray(this.collapsedDoc);
	}

	protected String getCollapseField() {
		return collapseField;
	}

	public ResultScoreDoc[] getCollapsedDoc() {
		return collapsedDoc;
	}

	public CollapseParameters.Mode getCollapseMode() {
		return collapseMode;
	}

	public CollapseParameters.Type getCollapseType() {
		return collapseType;
	}

	protected int getCollapsedDocsLength() {
		if (collapsedDoc == null)
			return 0;
		return collapsedDoc.length;
	}

	/**
	 * @return the collapseMax
	 */
	public int getCollapseMax() {
		return collapseMax;
	}

	public static CollapseAbstract newInstance(SearchRequest searchRequest) {
		CollapseParameters.Mode mode = searchRequest.getCollapseMode();
		if (mode == CollapseParameters.Mode.ADJACENT)
			return new CollapseAdjacent(searchRequest);
		else if (mode == CollapseParameters.Mode.CLUSTER)
			return new CollapseCluster(searchRequest);
		return null;
	}

	private ResultScoreDoc[] collapseFromDocSetHit(DocSetHits docSetHits,
			int searchRows, int end, StringIndex collapseFieldStringIndex,
			Timer timer) throws IOException {
		int lastRows = 0;
		int rows = end;
		int i = 0;
		Timer iterationTimer = new Timer(timer,
				"Optimized collapse iteration from DocSetHit");
		while (getCollapsedDocsLength() < end) {
			i++;
			ResultScoreDoc[] docs = docSetHits.getPriorityDocs(rows,
					iterationTimer);
			if (docs.length == lastRows)
				break;
			if (rows > docs.length)
				rows = docs.length;
			run(docs, rows, collapseFieldStringIndex, iterationTimer);
			lastRows = rows;
			rows += searchRows;
		}
		iterationTimer.end("Optimized collapse iteration from DocSetHit: " + i);
		return getCollapsedDoc();
	}

	private ResultScoreDoc[] collapseFromAllDocs(ResultScoreDoc[] docs,
			int searchRows, int end, StringIndex collapseFieldStringIndex,
			Timer timer) throws IOException {
		int lastRows = 0;
		int rows = end;
		int i = 0;
		Timer iterationTimer = new Timer(timer,
				"Optimized collapse iteration from all docs");
		while (getCollapsedDocsLength() < end) {
			i++;
			if (rows > docs.length)
				rows = docs.length;
			if (lastRows == rows)
				break;
			run(docs, rows, collapseFieldStringIndex, iterationTimer);
			lastRows = rows;
			rows += searchRows;
		}
		iterationTimer.end("Optimized collapse iteration from all docs: " + i);
		return getCollapsedDoc();
	}

	/**
	 * Fetch new documents until collapsed results is complete.
	 * 
	 * @throws IOException
	 * @throws SyntaxError
	 * @throws ParseException
	 */
	private ResultScoreDoc[] collapseOptimized(ReaderLocal reader,
			ResultScoreDoc[] allDocs, DocSetHits docSetHits, Timer timer)
			throws IOException, ParseException, SyntaxError {

		int searchRows = searchRequest.getRows();
		int end = searchRequest.getEnd();
		StringIndex collapseFieldStringIndex = reader.getStringIndex(
				searchRequest.getCollapseField(), timer);

		if (allDocs != null)
			return collapseFromAllDocs(allDocs, searchRows, end,
					collapseFieldStringIndex, timer);
		else
			return collapseFromDocSetHit(docSetHits, searchRows, end,
					collapseFieldStringIndex, timer);

	}

	private ResultScoreDoc[] collapseFull(ReaderLocal reader,
			ResultScoreDoc[] allDocs, DocSetHits docSetHits, Timer timer)
			throws IOException, ParseException, SyntaxError {

		if (allDocs == null)
			allDocs = docSetHits.getAllDocs(timer);

		StringIndex collapseFieldStringIndex = reader.getStringIndex(
				searchRequest.getCollapseField(), timer);
		run(allDocs, allDocs.length, collapseFieldStringIndex, timer);
		return getCollapsedDoc();
	}

	final public ResultScoreDoc[] collapse(ReaderLocal reader,
			ResultScoreDoc[] docs, DocSetHits docSetHits, Timer timer)
			throws IOException, ParseException, SyntaxError {
		Timer collapseTimer = new Timer(timer, "collapse "
				+ collapseMode.getLabel() + " " + collapseType.getLabel());
		try {
			if (collapseType == CollapseParameters.Type.OPTIMIZED)
				return collapseOptimized(reader, docs, docSetHits, timer);
			if (collapseType == CollapseParameters.Type.FULL)
				return collapseFull(reader, docs, docSetHits, timer);
			return null;
		} finally {
			collapseTimer.duration();
		}
	}

}
