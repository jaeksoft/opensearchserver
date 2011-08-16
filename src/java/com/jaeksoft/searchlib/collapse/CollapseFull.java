/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008-2010 Emmanuel Keller / Jaeksoft
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

import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.FieldCache.StringIndex;
import org.apache.lucene.search.ScoreDoc;

import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.index.DocSetHits;
import com.jaeksoft.searchlib.index.ReaderLocal;
import com.jaeksoft.searchlib.request.SearchRequest;
import com.jaeksoft.searchlib.result.ResultScoreDoc;
import com.jaeksoft.searchlib.result.ResultSingle;

public class CollapseFull extends CollapseAdjacent {

	protected CollapseFull(SearchRequest searchRequest) {
		super(searchRequest);
	}

	@Override
	public ResultScoreDoc[] collapse(ResultSingle resultSingle)
			throws IOException, ParseException, SyntaxError {
		// TODO Auto-generated method stub
		ReaderLocal reader = resultSingle.getReader();
		DocSetHits docSetHits = resultSingle.getDocSetHits();
		int allRows = docSetHits.getDocNumFound();
		ScoreDoc[] scoreDocs = docSetHits.getScoreDocs(allRows);
		StringIndex collapseFieldStringIndex = reader
				.getStringIndex(searchRequest.getCollapseField());
		ResultScoreDoc[] resultScoreDocs = ResultScoreDoc
				.appendResultScoreDocArray(resultSingle, null, scoreDocs,
						allRows, collapseFieldStringIndex);
		run(resultScoreDocs, allRows);
		return getCollapsedDoc();
	}

}
