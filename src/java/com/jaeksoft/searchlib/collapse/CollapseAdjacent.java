/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2010 Emmanuel Keller / Jaeksoft
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

import org.apache.lucene.util.OpenBitSet;

import com.jaeksoft.searchlib.request.SearchRequest;
import com.jaeksoft.searchlib.result.ResultScoreDoc;

public abstract class CollapseAdjacent extends CollapseAbstract {

	protected CollapseAdjacent(SearchRequest searchRequest) {
		super(searchRequest);
	}

	@Override
	protected void collapse(ResultScoreDoc[] fetchedDocs, int fetchLength) {

		OpenBitSet collapsedSet = new OpenBitSet(fetchLength);

		String lastTerm = null;
		int adjacent = 0;
		setCollapsedDocCount(0);
		for (int i = 0; i < fetchLength; i++) {
			String term = fetchedDocs[i].collapseTerm;
			if (term != null && term.equals(lastTerm)) {
				if (++adjacent >= getCollapseMax())
					collapsedSet.set(i);
			} else {
				lastTerm = term;
				adjacent = 0;
			}
		}

		int collapsedDocCount = (int) collapsedSet.cardinality();

		ResultScoreDoc[] collapsedDoc = new ResultScoreDoc[fetchLength
				- collapsedDocCount];

		int currentPos = 0;
		ResultScoreDoc collapseDoc = null;
		for (int i = 0; i < fetchLength; i++) {
			if (!collapsedSet.get(i)) {
				collapseDoc = fetchedDocs[i];
				collapseDoc.collapseCount = 0;
				collapsedDoc[currentPos++] = collapseDoc;
			} else {
				collapseDoc.collapseCount++;
			}
		}

		setCollapsedDocCount(collapsedDocCount);
		setCollapsedDoc(collapsedDoc);

	}

}
