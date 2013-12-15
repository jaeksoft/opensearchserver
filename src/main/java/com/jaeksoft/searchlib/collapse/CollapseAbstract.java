/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008-2013 Emmanuel Keller / Jaeksoft
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
import java.util.Collection;

import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.index.FieldCacheIndex;
import com.jaeksoft.searchlib.index.ReaderAbstract;
import com.jaeksoft.searchlib.query.ParseException;
import com.jaeksoft.searchlib.request.AbstractSearchRequest;
import com.jaeksoft.searchlib.result.collector.CollapseDocIdCollector;
import com.jaeksoft.searchlib.result.collector.CollapseDocInterface;
import com.jaeksoft.searchlib.result.collector.CollapseScoreDocCollector;
import com.jaeksoft.searchlib.result.collector.DocIdInterface;
import com.jaeksoft.searchlib.result.collector.ScoreDocInterface;
import com.jaeksoft.searchlib.util.Timer;

public abstract class CollapseAbstract {

	private transient final int collapseMax;
	private transient final String collapseField;
	private transient final CollapseParameters.Mode collapseMode;
	private transient final CollapseParameters.Type collapseType;
	private transient final Collection<CollapseFunctionField> collapseFunctionFields;
	protected transient final AbstractSearchRequest searchRequest;
	private transient CollapseDocInterface collapsedDocs;

	protected CollapseAbstract(AbstractSearchRequest searchRequest) {
		this.searchRequest = searchRequest;
		this.collapseField = searchRequest.getCollapseField();
		this.collapseMax = searchRequest.getCollapseMax();
		this.collapseMode = searchRequest.getCollapseMode();
		this.collapseType = searchRequest.getCollapseType();
		this.collapseFunctionFields = searchRequest.getCollapseFunctionFields();
		this.collapsedDocs = null;
	}

	protected abstract CollapseDocInterface collapse(DocIdInterface collector,
			int fetchLength, FieldCacheIndex collapseStringIndex, Timer timer);

	public CollapseDocInterface run(DocIdInterface collector, int fetchLength,
			FieldCacheIndex collapseStringIndex, Timer timer)
			throws IOException {

		collapsedDocs = null;

		if (collector == null)
			return null;

		int numFound = collector.getSize();
		if (fetchLength > numFound)
			fetchLength = numFound;

		collapsedDocs = collapse(collector, fetchLength, collapseStringIndex,
				timer);
		return collapsedDocs;
	}

	protected String getCollapseField() {
		return collapseField;
	}

	public DocIdInterface getCollapsedDoc() {
		return collapsedDocs;
	}

	public CollapseParameters.Mode getCollapseMode() {
		return collapseMode;
	}

	public CollapseParameters.Type getCollapseType() {
		return collapseType;
	}

	protected int getCollapsedDocsLength() {
		if (collapsedDocs == null)
			return 0;
		return collapsedDocs.getSize();
	}

	/**
	 * @return the collapseMax
	 */
	public final int getCollapseMax() {
		return collapseMax;
	}

	public boolean getCollectDocArray() {
		if (collapseMax == 0)
			return true;
		if (collapseFunctionFields == null)
			return false;
		return collapseFunctionFields.size() > 0;
	}

	public static CollapseAbstract newInstance(
			AbstractSearchRequest searchRequest) {
		CollapseParameters.Mode mode = searchRequest.getCollapseMode();
		if (mode == CollapseParameters.Mode.ADJACENT)
			return new CollapseAdjacent(searchRequest);
		else if (mode == CollapseParameters.Mode.CLUSTER)
			return new CollapseCluster(searchRequest);
		return null;
	}

	final private static int getNewFetchRows(long rows, long searchRows,
			long collapsedDocCount) {
		long fact = rows / searchRows;
		if (fact == 0)
			fact = 1;
		rows = rows + searchRows + collapsedDocCount * fact;
		if (rows > Integer.MAX_VALUE)
			rows = Integer.MAX_VALUE;
		return (int) rows;
	}

	private CollapseDocInterface collapseFromCollector(
			DocIdInterface collector, int searchRows, int end,
			FieldCacheIndex collapseFieldStringIndex, Timer timer)
			throws IOException {
		int lastRows = 0;
		int rows = end;
		int numFound = collector.getSize();
		int i = 0;
		Timer iterationTimer = new Timer(timer,
				"Optimized collapse iteration from all docs");
		while (getCollapsedDocsLength() < end) {
			i++;
			if (rows > numFound)
				rows = numFound;
			if (lastRows == rows)
				break;
			collapsedDocs = run(collector, rows, collapseFieldStringIndex,
					iterationTimer);
			lastRows = rows;
			rows = getNewFetchRows(rows, searchRows,
					collapsedDocs.getCollapsedCount());
		}
		iterationTimer.end("Optimized collapse iteration from all docs: " + i);
		return collapsedDocs;
	}

	/**
	 * Fetch new documents until collapsed results is complete.
	 * 
	 * @throws IOException
	 * @throws SyntaxError
	 * @throws ParseException
	 */
	private CollapseDocInterface collapseOptimized(ReaderAbstract reader,
			DocIdInterface collector, Timer timer) throws IOException,
			ParseException, SyntaxError {

		int searchRows = searchRequest.getRows();
		int end = searchRequest.getEnd();
		FieldCacheIndex collapseFieldStringIndex = reader
				.getStringIndex(searchRequest.getCollapseField());
		return collapseFromCollector(collector, searchRows, end,
				collapseFieldStringIndex, timer);
	}

	private CollapseDocInterface collapseFull(ReaderAbstract reader,
			DocIdInterface collector, Timer timer) throws IOException,
			ParseException, SyntaxError {
		FieldCacheIndex collapseFieldStringIndex = reader
				.getStringIndex(searchRequest.getCollapseField());
		collapsedDocs = run(collector, collector.getSize(),
				collapseFieldStringIndex, timer);
		return collapsedDocs;
	}

	final public CollapseDocInterface collapse(ReaderAbstract reader,
			DocIdInterface collector, Timer timer) throws IOException,
			ParseException, SyntaxError {
		Timer collapseTimer = new Timer(timer, "collapse "
				+ collapseMode.getLabel() + " " + collapseType.getLabel());
		try {
			if (collapseType == CollapseParameters.Type.OPTIMIZED)
				return collapseOptimized(reader, collector, timer);
			if (collapseType == CollapseParameters.Type.FULL)
				return collapseFull(reader, collector, timer);
			return null;
		} finally {
			collapseTimer.getDuration();
		}
	}

	protected static CollapseDocInterface getNewCollapseInterfaceInstance(
			DocIdInterface collector, int capacity, boolean collectDocArray) {
		if (collector instanceof ScoreDocInterface)
			return new CollapseScoreDocCollector((ScoreDocInterface) collector,
					capacity, collectDocArray);
		return new CollapseDocIdCollector(collector, capacity, collectDocArray);
	}

}
