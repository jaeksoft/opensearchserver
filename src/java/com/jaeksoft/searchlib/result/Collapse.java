/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2008 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.result;

import java.io.IOException;

import org.apache.lucene.util.OpenBitSet;

import com.jaeksoft.searchlib.request.SearchRequest;

public class Collapse {

	private int collapsedDocCount;
	private transient int collapseMax;
	private transient String collapseField;
	private transient boolean collapseActive;
	protected transient OpenBitSet collapsedSet;
	private transient ResultScoreDoc[] collapsedDoc;

	protected Collapse(SearchRequest searchRequest) {
		this.collapseField = searchRequest.getCollapseField();
		this.collapseMax = searchRequest.getCollapseMax();
		this.collapseActive = searchRequest.getCollapseActive();
		this.collapsedDocCount = 0;
		this.collapsedDoc = null;
	}

	protected void run(ResultScoreDoc[] fetchedDocs, int fetchLength)
			throws IOException {

		collapsedDoc = null;

		if (fetchedDocs == null)
			return;

		if (fetchLength > fetchedDocs.length)
			fetchLength = fetchedDocs.length;

		collapsedSet = new OpenBitSet(fetchLength);

		String lastTerm = null;
		int adjacent = 0;
		collapsedDocCount = 0;
		for (int i = 0; i < fetchLength; i++) {
			String term = fetchedDocs[i].collapseTerm;
			if (term != null && term.equals(lastTerm)) {
				if (++adjacent >= collapseMax)
					collapsedSet.set(i);
			} else {
				lastTerm = term;
				adjacent = 0;
			}
		}
		collapsedDocCount = (int) collapsedSet.cardinality();

		collapsedDoc = new ResultScoreDoc[fetchLength - collapsedDocCount];

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
	}

	protected OpenBitSet getBitSet() {
		return this.collapsedSet;
	}

	protected int getDocCount() {
		return this.collapsedDocCount;
	}

	protected String getCollapseField() {
		return collapseField;
	}

	protected ResultScoreDoc[] getCollapsedDoc() {
		return collapsedDoc;
	}

	protected boolean isActive() {
		if (!collapseActive)
			return false;
		if (collapseField == null)
			return false;
		if (collapseMax == 0)
			return false;
		return true;
	}

	protected int getCollapsedDocsLength() {
		if (collapsedDoc == null)
			return 0;
		return collapsedDoc.length;
	}

}
