/**   
 * License Agreement for Jaeksoft Simport java.io.IOException;

import org.apache.lucene.search.FieldCache.StringIndex;

import com.jaeksoft.searchlib.result.ResultSearch;
 SearchLib Community.
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

import org.apache.lucene.search.FieldCache.StringIndex;

import com.jaeksoft.searchlib.result.ResultSearch;

public class CollapseSearch extends Collapse<ResultSearch> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4471977653574049055L;
	transient private StringIndex stringIndex;
	private int[] fetchedDoc;

	public CollapseSearch(ResultSearch result) throws IOException {
		super(result);
		this.setResult(result);
	}

	@Override
	public void setResult(ResultSearch result) throws IOException {
		super.setResult(result);
		this.stringIndex = result.getReader().getStringIndex(
				result.getRequest().getCollapseField().getName());
	}

	@Override
	protected void prepare() {
		this.fetchedDoc = this.result.getFetchedDoc();
	}

	@Override
	protected void reduce() {
		int[] sortFetchDocs = this.result.getFetchedDoc();
		int[] newCollapsedDoc = new int[sortFetchDocs.length
				- this.getDocCount()];
		int[] newCollapseCount = new int[newCollapsedDoc.length];

		int currentPos = 0;
		int collapsePos = 0;
		for (int i = 0; i < sortFetchDocs.length; i++) {
			if (!this.collapsedSet.get(i)) {
				collapsePos = currentPos;
				newCollapseCount[currentPos] = 0;
				newCollapsedDoc[currentPos++] = i;
			} else {
				newCollapseCount[collapsePos]++;
			}
		}

		this.collapsedDoc = newCollapsedDoc;
		this.collapseCount = newCollapseCount;
	}

	@Override
	protected String getTerm(int pos) {
		return this.stringIndex.lookup[stringIndex.order[this.fetchedDoc[pos]]];
	}
}
