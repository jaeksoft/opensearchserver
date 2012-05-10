/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2010-2012 Emmanuel Keller / Jaeksoft
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
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.lucene.search.FieldCache.StringIndex;

import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.index.DocSetHits;
import com.jaeksoft.searchlib.index.ReaderLocal;
import com.jaeksoft.searchlib.query.ParseException;
import com.jaeksoft.searchlib.request.SearchRequest;
import com.jaeksoft.searchlib.result.ResultScoreDoc;

public class CollapseCluster extends CollapseAbstract {

	protected CollapseCluster(SearchRequest searchRequest) {
		super(searchRequest);
	}

	@Override
	protected void collapse(ResultScoreDoc[] fetchedDocs, int fetchLength,
			StringIndex collapseStringIndex) {

		Map<String, ResultScoreDoc> collapsedDocMap = new LinkedHashMap<String, ResultScoreDoc>();
		ResultScoreDoc collapseDoc;
		for (int i = 0; i < fetchLength; i++) {
			ResultScoreDoc fetchedDoc = fetchedDocs[i];
			String term = collapseStringIndex.lookup[collapseStringIndex.order[fetchedDoc.doc]];
			if (term != null
					&& ((collapseDoc = collapsedDocMap.get(term)) != null)) {
				collapseDoc.collapseCount++;
			} else {
				collapsedDocMap.put(term, fetchedDoc);
			}
		}

		ResultScoreDoc[] collapsedDocs = new ResultScoreDoc[collapsedDocMap
				.size()];
		collapsedDocMap.values().toArray(collapsedDocs);

		setCollapsedDocCount(fetchLength - collapsedDocs.length);
		setCollapsedDoc(collapsedDocs);

	}

	@Override
	public ResultScoreDoc[] collapse(ReaderLocal reader,
			ResultScoreDoc[] allDocs, DocSetHits docSetHits)
			throws IOException, ParseException, SyntaxError {
		if (allDocs == null)
			allDocs = docSetHits.getAllDocs();
		StringIndex collapseFieldStringIndex = reader
				.getStringIndex(searchRequest.getCollapseField());
		run(allDocs, allDocs.length, collapseFieldStringIndex);
		return getCollapsedDoc();
	}

}
