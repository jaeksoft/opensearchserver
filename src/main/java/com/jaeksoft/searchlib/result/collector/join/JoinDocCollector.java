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

package com.jaeksoft.searchlib.result.collector.join;

import java.io.IOException;
import java.util.Arrays;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.lucene.util.OpenBitSet;

import com.jaeksoft.searchlib.index.ReaderAbstract;
import com.jaeksoft.searchlib.result.collector.AbstractBaseCollector;
import com.jaeksoft.searchlib.result.collector.DocIdCollector;
import com.jaeksoft.searchlib.result.collector.DocIdInterface;
import com.jaeksoft.searchlib.result.collector.JoinDocInterface;

public class JoinDocCollector extends
		AbstractBaseCollector<JoinCollectorInterface> implements
		JoinCollectorInterface, JoinDocInterface, DocIdInterface {

	private final int maxDoc;
	private final ReaderAbstract[] foreignReaders;
	final int[] srcIds;
	final int joinResultSize;
	private final int[][] foreignDocIdsArray;
	private OpenBitSet bitSet = null;

	private final static int[][] EMPTY = new int[0][0];

	JoinDocCollector() {
		srcIds = ArrayUtils.EMPTY_INT_ARRAY;
		foreignDocIdsArray = EMPTY;
		joinResultSize = 0;
		foreignReaders = null;
		maxDoc = 0;
	}

	JoinDocCollector(DocIdInterface docs, int joinResultSize) {
		this.srcIds = ArrayUtils.clone(docs.getIds());
		this.foreignDocIdsArray = new int[srcIds.length][];
		if (docs instanceof JoinDocCollector)
			((JoinDocCollector) docs).copyForeignDocIdsArray(this);
		this.joinResultSize = joinResultSize;
		this.foreignReaders = new ReaderAbstract[joinResultSize];
		this.maxDoc = docs.getMaxDoc();
	}

	private void copyForeignDocIdsArray(final JoinDocCollector joinDocCollector) {
		if (foreignDocIdsArray == null)
			return;
		int i = 0;
		for (int[] ids : foreignDocIdsArray)
			joinDocCollector.foreignDocIdsArray[i++] = ids;
	}

	/**
	 * Copy only the valid item (other than -1)
	 * 
	 * @param src
	 */
	private JoinDocCollector(final JoinDocCollector src) {
		this.maxDoc = src.maxDoc;
		this.joinResultSize = src.joinResultSize;
		this.srcIds = new int[validSize(src.srcIds)];
		this.foreignDocIdsArray = new int[this.srcIds.length][];
		this.foreignReaders = new ReaderAbstract[joinResultSize];
		int i1 = 0;
		int i2 = 0;
		for (int id : src.srcIds) {
			if (id != -1) {
				srcIds[i1] = id;
				foreignDocIdsArray[i1++] = ArrayUtils
						.clone(src.foreignDocIdsArray[i2]);
			}
			i2++;
		}
		System.arraycopy(src.getForeignReaders(), 0, foreignReaders, 0,
				foreignReaders.length);
	}

	@Override
	public JoinDocCollector duplicate(final AbstractBaseCollector<?> base) {
		return new JoinDocCollector((JoinDocCollector) base);
	}

	protected static final int validSize(final int[] ids) {
		int i = 0;
		for (int id : ids)
			if (id != -1)
				i++;
		return i;
	}

	final public static int[][] copyForeignDocIdsArray(
			final int[][] foreignDocIdsArray) {
		int[][] neworeignDocIdsArray = new int[foreignDocIdsArray.length][];
		int i = 0;
		for (int[] foreignIds : foreignDocIdsArray)
			neworeignDocIdsArray[i++] = ArrayUtils.clone(foreignIds);
		return neworeignDocIdsArray;
	}

	final public static void swap(final int[][] foreignDocIdsArray,
			final int pos1, final int pos2) {
		int[] foreignDocIds = foreignDocIdsArray[pos1];
		foreignDocIdsArray[pos1] = foreignDocIdsArray[pos2];
		foreignDocIdsArray[pos2] = foreignDocIds;
	}

	@Override
	final public void doSwap(final int pos1, final int pos2) {
		int id = srcIds[pos1];
		srcIds[pos1] = srcIds[pos2];
		srcIds[pos2] = id;
		swap(foreignDocIdsArray, pos1, pos2);
	}

	@Override
	final public void doSetForeignDoc(final int pos, final int joinResultPos,
			final int foreignDocId, float foreignScore) {
		int[] foreignDocIds = foreignDocIdsArray[pos];
		if (foreignDocIds == null) {
			foreignDocIds = new int[joinResultSize];
			Arrays.fill(foreignDocIds, -1);
			foreignDocIdsArray[pos] = foreignDocIds;
		}
		foreignDocIds[joinResultPos] = foreignDocId;
	}

	final void setForeignDoc(final int pos, final int joinResultPos,
			final int foreignDocId, float foreignScore) {
		lastCollector.doSetForeignDoc(pos, joinResultPos, foreignDocId,
				foreignScore);
	}

	final public static int getForeignDocIds(final int[][] foreignDocIdsArray,
			int pos, int joinPosition) {
		int[] foreignDocIds = foreignDocIdsArray[pos];
		if (foreignDocIds == null)
			return -1;
		if (joinPosition >= foreignDocIds.length)
			return -1;
		return foreignDocIds[joinPosition];
	}

	@Override
	final public int getForeignDocId(final int pos, final int joinPosition) {
		return getForeignDocIds(foreignDocIdsArray, pos, joinPosition);
	}

	final public static DocIdInterface getDocIdInterface(int maxDoc,
			int joinPosition, JoinDocCollector joinDocColletor)
			throws IOException {
		DocIdCollector docIdCollector = new DocIdCollector(maxDoc,
				joinDocColletor.srcIds.length);
		for (int[] foreinDocs : joinDocColletor.getForeignDocIdsArray())
			docIdCollector.collectDoc(foreinDocs[joinPosition]);
		return docIdCollector;
	}

	@Override
	final public int[][] getForeignDocIdsArray() {
		return foreignDocIdsArray;
	}

	@Override
	public ReaderAbstract[] getForeignReaders() {
		return foreignReaders;
	}

	@Override
	public int getSize() {
		return srcIds.length;
	}

	@Override
	public int getMaxDoc() {
		return maxDoc;
	}

	@Override
	public int[] getIds() {
		return srcIds;
	}

	@Override
	public OpenBitSet getBitSet() {
		if (bitSet != null)
			return bitSet;
		bitSet = new OpenBitSet(maxDoc);
		for (int id : srcIds)
			bitSet.fastSet(id);
		return bitSet;
	}

}
