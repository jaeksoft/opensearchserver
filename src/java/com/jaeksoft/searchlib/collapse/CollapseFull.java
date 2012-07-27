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

public class CollapseFull extends CollapseAdjacent {

	protected CollapseFull(SearchRequest searchRequest) {
		super(searchRequest);
	}

	@Override
	public ResultScoreDoc[] collapse(ReaderLocal reader,
			ResultScoreDoc[] allDocs, DocSetHits docSetHits, Timer timer)
			throws IOException, ParseException, SyntaxError {

		if (allDocs == null)
			allDocs = docSetHits.getAllDocs(timer);

		StringIndex collapseFieldStringIndex = reader
				.getStringIndex(searchRequest.getCollapseField());
		run(allDocs, allDocs.length, collapseFieldStringIndex, timer);
		return getCollapsedDoc();
	}

}
