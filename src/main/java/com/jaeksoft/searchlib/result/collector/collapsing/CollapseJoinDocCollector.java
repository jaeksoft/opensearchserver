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

import com.jaeksoft.searchlib.index.ReaderAbstract;
import com.jaeksoft.searchlib.result.collector.AbstractBaseCollector;
import com.jaeksoft.searchlib.result.collector.AbstractExtendsCollector;
import com.jaeksoft.searchlib.result.collector.JoinDocInterface;
import com.jaeksoft.searchlib.result.collector.join.JoinDocCollector;

public class CollapseJoinDocCollector
		extends
		AbstractExtendsCollector<CollapseCollectorInterface, CollapseBaseCollector>
		implements CollapseCollectorInterface, JoinDocInterface {

	protected final int[][] sourceForeignDocIdsArray;
	protected final int[][] foreignDocIdsArray;
	protected final ReaderAbstract[] foreignReaders;

	private int currentPos;

	public CollapseJoinDocCollector(final CollapseBaseCollector base,
			final JoinDocInterface sourceCollector) {
		super(base);
		sourceForeignDocIdsArray = sourceCollector.getForeignDocIdsArray();
		foreignDocIdsArray = new int[sourceForeignDocIdsArray.length][];
		foreignReaders = sourceCollector.getForeignReaders();
		currentPos = 0;
	}

	private CollapseJoinDocCollector(final CollapseBaseCollector base,
			final CollapseJoinDocCollector src) {
		super(base);
		sourceForeignDocIdsArray = src.sourceForeignDocIdsArray;
		foreignDocIdsArray = JoinDocCollector
				.copyForeignDocIdsArray(src.foreignDocIdsArray);
		foreignReaders = src.getForeignReaders();
		currentPos = src.currentPos;
	}

	@Override
	public CollapseJoinDocCollector duplicate(
			final AbstractBaseCollector<?> base) {
		parent.duplicate(base);
		return new CollapseJoinDocCollector((CollapseBaseCollector) base, this);
	}

	@Override
	final public void doSwap(final int pos1, final int pos2) {
		parent.doSwap(pos1, pos2);
		JoinDocCollector.swap(foreignDocIdsArray, pos1, pos2);
	}

	@Override
	final public int getForeignDocId(final int pos, final int joinPosition) {
		return JoinDocCollector.getForeignDocIds(foreignDocIdsArray, pos,
				joinPosition);
	}

	@Override
	final public int[][] getForeignDocIdsArray() {
		return foreignDocIdsArray;
	}

	@Override
	final public int collectDoc(final int sourcePos) {
		int pos = parent.collectDoc(sourcePos);
		if (pos != currentPos)
			throw new RuntimeException("Internal position issue: " + pos
					+ " - " + currentPos);
		int[] foreignDocIds = sourceForeignDocIdsArray[sourcePos];
		foreignDocIdsArray[currentPos++] = foreignDocIds;
		return pos;

	}

	@Override
	final public void collectCollapsedDoc(final int sourcePos,
			final int collapsePos) {
		parent.collectCollapsedDoc(sourcePos, collapsePos);
	}

	@Override
	final public void endCollection() {
		parent.endCollection();
	}

	@Override
	final public int getSize() {
		if (foreignDocIdsArray == null)
			return 0;
		return currentPos;
	}

	@Override
	final public ReaderAbstract[] getForeignReaders() {
		return foreignReaders;
	}

}
