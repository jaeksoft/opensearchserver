/**   
 * License Agreement for Jaeksoft SearchLib Community
 *
 * Copyright (C) 2008 Emmanuel Keller / Jaeksoft
 * 
 * http://www.jaeksoft.com
 * 
 * This file is part of Jaeksoft SearchLib Community.
 *
 * Jaeksoft SearchLib Community is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * Jaeksoft SearchLib Community is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Jaeksoft SearchLib Community. 
 *  If not, see <http://www.gnu.org/licenses/>.
 **/

package com.jaeksoft.searchlib.collapse;

import java.io.IOException;
import java.io.Serializable;
import java.util.BitSet;

import com.jaeksoft.searchlib.request.Request;
import com.jaeksoft.searchlib.result.ResultScoreDoc;
import com.jaeksoft.searchlib.schema.Field;

public class Collapse implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private int collapsedDocCount;
	private int collapseMax;
	private Field collapseField;
	private boolean collapseActive;
	protected BitSet collapsedSet;
	private ResultScoreDoc[] collapsedDoc;

	public Collapse(Request request) {
		this.collapseField = request.getCollapseField();
		this.collapseMax = request.getCollapseMax();
		this.collapseActive = request.getCollapseActive();
		this.collapsedDocCount = 0;
		this.collapsedDoc = null;
	}

	public void run(ResultScoreDoc[] fetchedDocs, int fetchLength)
			throws IOException {

		collapsedDoc = null;

		if (fetchedDocs == null)
			return;

		if (fetchLength > fetchedDocs.length)
			fetchLength = fetchedDocs.length;

		collapsedSet = new BitSet(fetchLength);

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
		collapsedDocCount = collapsedSet.cardinality();

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

	public BitSet getBitSet() {
		return this.collapsedSet;
	}

	public int getDocCount() {
		return this.collapsedDocCount;
	}

	public Field getCollapseField() {
		return collapseField;
	}

	public ResultScoreDoc[] getCollapsedDoc() {
		return collapsedDoc;
	}

	public boolean isActive() {
		if (!collapseActive)
			return false;
		if (collapseField == null)
			return false;
		if (collapseMax == 0)
			return false;
		return true;
	}

	public int getCount(int pos) {
		if (collapsedDoc == null)
			return 0;
		return collapsedDoc[pos].collapseCount;
	}

	public int getCollapsedDocsLength() {
		if (collapsedDoc == null)
			return 0;
		return collapsedDoc.length;
	}

}
