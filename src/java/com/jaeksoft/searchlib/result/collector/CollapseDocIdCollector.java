/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2012 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.result.collector;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.lucene.util.OpenBitSet;

public class CollapseDocIdCollector implements CollapseDocInterface {

	protected final int[] sourceIds;
	protected final int collapseMax;
	protected int totalCollapseCount = 0;
	protected final int[] ids;
	protected final int[][] collapseDocsArray;
	protected final int[] collapseCounts;
	protected int currentPos;
	protected int maxDoc;
	protected OpenBitSet bitSet;

	public CollapseDocIdCollector(DocIdInterface sourceCollector, int size,
			int collapseMax) {
		this.sourceIds = sourceCollector.getIds();
		this.totalCollapseCount = 0;
		this.collapseMax = collapseMax;
		this.collapseDocsArray = new int[size][];
		this.collapseCounts = new int[size];
		this.ids = new int[size];
		this.currentPos = 0;
		this.maxDoc = sourceCollector.getMaxDoc();
		this.bitSet = null;
	}

	protected CollapseDocIdCollector(CollapseDocIdCollector src) {
		this.sourceIds = src.sourceIds;
		this.totalCollapseCount = src.totalCollapseCount;
		this.collapseMax = src.collapseMax;
		this.collapseDocsArray = new int[src.collapseDocsArray.length][];
		int i = 0;
		for (int[] collDocArray : src.collapseDocsArray)
			this.collapseDocsArray[i++] = ArrayUtils.clone(collDocArray);
		this.collapseCounts = ArrayUtils.clone(src.collapseCounts);
		this.ids = ArrayUtils.clone(src.ids);
		this.currentPos = src.currentPos;
		this.maxDoc = src.maxDoc;
		this.bitSet = null;
	}

	@Override
	public DocIdInterface duplicate() {
		return new CollapseDocIdCollector(this);
	}

	@Override
	public int collectDoc(int sourcePos) {
		collapseCounts[currentPos] = 0;
		ids[currentPos] = sourceIds[sourcePos];
		int pos = currentPos;
		currentPos++;
		return pos;
	}

	@Override
	public void collectCollapsedDoc(int sourcePos, int collapsePos) {
		totalCollapseCount++;
		int collCount = collapseCounts[collapsePos]++;
		if (collapseMax == 0 || collapseMax < collCount)
			return;
		if (collapseDocsArray[collapsePos] == null)
			collapseDocsArray[collapsePos] = new int[] { sourceIds[sourcePos] };
		else
			collapseDocsArray[collapsePos] = ArrayUtils.add(
					collapseDocsArray[collapsePos], sourceIds[sourcePos]);
	}

	@Override
	public int getCollapsedCount() {
		return totalCollapseCount;
	}

	@Override
	public void swap(int pos1, int pos2) {
		int id = ids[pos1];
		int[] colArray = collapseDocsArray[pos1];
		int colCount = collapseCounts[pos1];
		ids[pos1] = ids[pos2];
		collapseDocsArray[pos1] = collapseDocsArray[pos2];
		collapseCounts[pos1] = collapseCounts[pos2];
		ids[pos2] = id;
		collapseDocsArray[pos2] = colArray;
		collapseCounts[pos2] = colCount;
	}

	@Override
	public int[] getIds() {
		return ids;
	}

	@Override
	public int getNumFound() {
		return currentPos;
	}

	@Override
	final public OpenBitSet getBitSet() {
		if (bitSet != null)
			return bitSet;
		bitSet = new OpenBitSet(maxDoc);
		for (int id : ids)
			bitSet.fastSet(id);
		return bitSet;
	}

	@Override
	public int getMaxDoc() {
		return maxDoc;
	}

	@Override
	public int[] getCollapseCounts() {
		return collapseCounts;
	}

	@Override
	public int[] getCollapsedDocs(int pos) {
		return collapseDocsArray[pos];
	}

}
