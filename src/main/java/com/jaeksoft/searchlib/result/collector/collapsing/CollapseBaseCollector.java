/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2012-2014 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.result.collector.collapsing;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.lucene.util.OpenBitSet;

import com.jaeksoft.searchlib.result.collector.AbstractBaseCollector;
import com.jaeksoft.searchlib.result.collector.CollapseDocInterface;
import com.jaeksoft.searchlib.result.collector.DocIdInterface;

public class CollapseBaseCollector extends
		AbstractBaseCollector<CollapseCollectorInterface> implements
		CollapseDocInterface, CollapseCollectorInterface, DocIdInterface {

	final int[] sourceIds;
	protected int totalCollapseCount = 0;
	protected final int[] ids;
	protected final int[][] collapseDocsArray;
	protected final int[] collapseCounts;
	protected int currentPos;
	protected int maxDoc;
	protected OpenBitSet bitSet;

	public CollapseBaseCollector(DocIdInterface sourceCollector, int size,
			boolean collectDocArray) {
		this.sourceIds = sourceCollector.getIds();
		this.totalCollapseCount = 0;
		this.collapseDocsArray = collectDocArray ? new int[size][] : null;
		this.collapseCounts = new int[size];
		this.ids = new int[size];
		this.currentPos = 0;
		this.maxDoc = sourceCollector.getMaxDoc();
		this.bitSet = null;
	}

	private CollapseBaseCollector(CollapseBaseCollector src) {
		this.sourceIds = src.sourceIds;
		this.totalCollapseCount = src.totalCollapseCount;
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
	public CollapseBaseCollector duplicate(AbstractBaseCollector<?> base) {
		return new CollapseBaseCollector((CollapseBaseCollector) base);
	}

	@Override
	final public int collectDoc(final int sourcePos) {
		collapseCounts[currentPos] = 0;
		ids[currentPos] = sourceIds[sourcePos];
		int pos = currentPos;
		currentPos++;
		return pos;
	}

	@Override
	final public void endCollection() {
	}

	@Override
	final public void collectCollapsedDoc(final int sourcePos,
			final int collapsePos) {
		totalCollapseCount++;
		collapseCounts[collapsePos]++;
		if (collapseDocsArray == null)
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
		ids[pos1] = ids[pos2];
		ids[pos2] = id;

		int colCount = collapseCounts[pos1];
		collapseCounts[pos1] = collapseCounts[pos2];
		collapseCounts[pos2] = colCount;

		if (collapseDocsArray != null) {
			int[] colArray = collapseDocsArray[pos1];
			collapseDocsArray[pos1] = collapseDocsArray[pos2];
			collapseDocsArray[pos2] = colArray;
		}
	}

	@Override
	public int[] getIds() {
		return ids;
	}

	@Override
	public int getSize() {
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
	public int[] getCollapseCounts() {
		return collapseCounts;
	}

	@Override
	public int[] getCollapsedDocs(int pos) {
		if (collapseDocsArray == null)
			return null;
		return collapseDocsArray[pos];
	}

	@Override
	public int getMaxDoc() {
		return maxDoc;
	}

}
