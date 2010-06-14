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
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.FieldCache.StringIndex;

import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.index.DocSetHits;
import com.jaeksoft.searchlib.index.ReaderLocal;
import com.jaeksoft.searchlib.request.SearchRequest;
import com.jaeksoft.searchlib.result.ResultScoreDoc;
import com.jaeksoft.searchlib.result.ResultSingle;

public class CollapseOptimized extends CollapseAdjacent {

	protected CollapseOptimized(SearchRequest searchRequest) {
		super(searchRequest);
	}

	/**
	 * Fetch new documents until collapsed results is complete.
	 * 
	 * @throws IOException
	 * @throws SyntaxError
	 * @throws ParseException
	 */
	@Override
	public ResultScoreDoc[] collapse(ResultSingle resultSingle)
			throws IOException, ParseException, SyntaxError {

		ReaderLocal reader = resultSingle.getReader();
		DocSetHits docSetHits = resultSingle.getDocSetHits();

		int end = searchRequest.getEnd();
		int lastRows = 0;
		int rows = end;
		StringIndex collapseFieldStringIndex = reader
				.getStringIndex(searchRequest.getCollapseField());
		ResultScoreDoc[] resultScoreDocs = null;
		String indexName = reader.getName();
		while (getCollapsedDocsLength() < end) {
			ScoreDoc[] scoreDocs = docSetHits.getScoreDocs(rows);
			if (scoreDocs.length == lastRows)
				break;
			if (rows > scoreDocs.length)
				rows = scoreDocs.length;
			resultScoreDocs = ResultScoreDoc.appendResultScoreDocArray(
					indexName, resultSingle, resultScoreDocs, scoreDocs, rows,
					collapseFieldStringIndex);
			run(resultScoreDocs, rows);
			lastRows = rows;
			rows += searchRequest.getRows();
		}
		return getCollapsedDoc();
	}
}
