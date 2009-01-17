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

import com.jaeksoft.searchlib.result.ResultGroup;
import com.jaeksoft.searchlib.result.ResultScoreDoc;
import com.jaeksoft.searchlib.result.ResultSearch;

public class CollapseGroup extends Collapse<ResultGroup> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7860659405139297551L;

	transient private ResultScoreDoc[] fetchedDoc;

	public CollapseGroup(ResultGroup result) throws IOException {
		super(result);
	}

	@Override
	protected void prepare() throws IOException {
		fetchedDoc = (ResultScoreDoc[]) result.getFetchedDocs();
		for (ResultSearch result : this.result.getResultList())
			result.getCollapse().prepare();
	}

	@Override
	protected ResultScoreDoc[] reduce() {
		ResultScoreDoc[] newCollapsedDoc = new ResultScoreDoc[fetchedDoc.length
				- this.getDocCount()];
		int[] newCollapseCount = new int[newCollapsedDoc.length];

		int currentPos = 0;
		int collapsePos = 0;
		for (int i = 0; i < fetchedDoc.length; i++) {
			if (!collapsedSet.get(i)) {
				collapsePos = currentPos;
				newCollapseCount[currentPos] = 0;
				newCollapsedDoc[currentPos] = fetchedDoc[i];
				currentPos++;
			} else {
				newCollapseCount[collapsePos]++;
			}
		}

		this.collapseCount = newCollapseCount;
		return newCollapsedDoc;
	}

	@Override
	protected String getTerm(ResultScoreDoc scoreDoc) throws IOException {
		return scoreDoc.resultSearch.getCollapse().getTerm(scoreDoc);
	}

}
