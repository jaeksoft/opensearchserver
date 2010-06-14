/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2008-2010 Emmanuel Keller / Jaeksoft
 * 
 * http://www.open-search-server.com
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

package com.jaeksoft.searchlib.collapse;

import java.io.IOException;

import org.apache.lucene.queryParser.ParseException;

import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.request.SearchRequest;
import com.jaeksoft.searchlib.result.ResultScoreDoc;
import com.jaeksoft.searchlib.result.ResultSingle;

public abstract class CollapseAbstract {

	private int collapsedDocCount;
	private transient int collapseMax;
	private transient String collapseField;
	private transient CollapseMode collapseMode;
	protected transient SearchRequest searchRequest;
	private transient ResultScoreDoc[] collapsedDoc;

	protected CollapseAbstract(SearchRequest searchRequest) {
		this.searchRequest = searchRequest;
		this.collapseField = searchRequest.getCollapseField();
		this.collapseMax = searchRequest.getCollapseMax();
		this.collapseMode = searchRequest.getCollapseMode();
		this.collapsedDocCount = 0;
		this.collapsedDoc = null;
	}

	protected abstract void collapse(ResultScoreDoc[] fetchedDocs,
			int fetchLength);

	public void run(ResultScoreDoc[] fetchedDocs, int fetchLength)
			throws IOException {

		collapsedDoc = null;

		if (fetchedDocs == null)
			return;

		if (fetchLength > fetchedDocs.length)
			fetchLength = fetchedDocs.length;

		collapse(fetchedDocs, fetchLength);
	}

	public abstract ResultScoreDoc[] collapse(ResultSingle resultSingle)
			throws IOException, ParseException, SyntaxError;

	public int getDocCount() {
		return this.collapsedDocCount;
	}

	/**
	 * @param collapsedDocCount
	 *            the collapsedDocCount to set
	 */
	public void setCollapsedDocCount(int collapsedDocCount) {
		this.collapsedDocCount = collapsedDocCount;
	}

	/**
	 * @param collapsedDoc
	 *            the collapsedDoc to set
	 */
	public void setCollapsedDoc(ResultScoreDoc[] collapsedDoc) {
		this.collapsedDoc = collapsedDoc;
	}

	protected String getCollapseField() {
		return collapseField;
	}

	public ResultScoreDoc[] getCollapsedDoc() {
		return collapsedDoc;
	}

	public CollapseMode getCollapseMode() {
		return collapseMode;
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
		CollapseMode mode = searchRequest.getCollapseMode();
		if (mode == CollapseMode.COLLAPSE_FULL)
			return new CollapseFull(searchRequest);
		else if (mode == CollapseMode.COLLAPSE_OPTIMIZED)
			return new CollapseOptimized(searchRequest);
		else if (mode == CollapseMode.COLLAPSE_CLUSTER)
			return new CollapseCluster(searchRequest);
		return null;
	}

}
