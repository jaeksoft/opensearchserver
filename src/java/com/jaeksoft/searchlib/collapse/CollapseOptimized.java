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

import org.apache.lucene.search.FieldCache.StringIndex;

import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.index.DocSetHits;
import com.jaeksoft.searchlib.index.ReaderLocal;
import com.jaeksoft.searchlib.query.ParseException;
import com.jaeksoft.searchlib.request.SearchRequest;
import com.jaeksoft.searchlib.result.ResultScoreDoc;
import com.jaeksoft.searchlib.util.Timer;

public class CollapseOptimized extends CollapseAdjacent {

	protected CollapseOptimized(SearchRequest searchRequest) {
		super(searchRequest);
	}

	private ResultScoreDoc[] collapseFromDocSetHit(DocSetHits docSetHits,
			int searchRows, int end, StringIndex collapseFieldStringIndex,
			Timer timer) throws IOException {
		int lastRows = 0;
		int rows = end;
		int i = 0;
		Timer iterationTimer = new Timer(timer, "Optimized collapse iteration");
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
		iterationTimer.end("Optimized collapse iteration:" + i);
		return getCollapsedDoc();
	}

	private ResultScoreDoc[] collapseFromAllDocs(ResultScoreDoc[] docs,
			int searchRows, int end, StringIndex collapseFieldStringIndex,
			Timer timer) throws IOException {
		int lastRows = 0;
		int rows = end;
		while (getCollapsedDocsLength() < end) {
			if (rows > docs.length)
				rows = docs.length;
			if (lastRows == rows)
				break;
			run(docs, rows, collapseFieldStringIndex, timer);
			lastRows = rows;
			rows += searchRows;
		}
		return getCollapsedDoc();
	}

	/**
	 * Fetch new documents until collapsed results is complete.
	 * 
	 * @throws IOException
	 * @throws SyntaxError
	 * @throws ParseException
	 */
	@Override
	public ResultScoreDoc[] collapse(ReaderLocal reader,
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
}
