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

import com.jaeksoft.searchlib.result.collector.AbstractBaseCollector;
import com.jaeksoft.searchlib.result.collector.DocIdCollector;
import com.jaeksoft.searchlib.result.collector.DocIdInterface;
import com.jaeksoft.searchlib.result.collector.JoinDocInterface;

public class JoinDocCollector extends
		AbstractBaseCollector<JoinDocCollectorInterface> implements
		JoinDocCollectorInterface, JoinDocInterface, DocIdInterface {

	private final int maxDoc;
	private final int[] ids;
	private final int[][] foreignDocIdsArray;
	private OpenBitSet bitSet;
	private final int joinResultSize;

	JoinDocCollector() {
		maxDoc = 0;
		ids = new int[0];
		foreignDocIdsArray = new int[0][0];
		bitSet = null;
		joinResultSize = 0;
	}

	JoinDocCollector(DocIdInterface docs, int joinResultSize) {
		this.maxDoc = docs.getMaxDoc();
		this.bitSet = null;
		this.ids = ArrayUtils.clone(docs.getIds());
		this.foreignDocIdsArray = new int[ids.length][];
		if (docs instanceof JoinDocCollector)
			((JoinDocCollector) docs).copyForeignDocIdsArray(this);
		this.joinResultSize = joinResultSize;
	}

	private void copyForeignDocIdsArray(JoinDocCollector joinDocCollector) {
		int i = 0;
		if (foreignDocIdsArray == null)
			return;
		for (int[] ids : foreignDocIdsArray)
			joinDocCollector.foreignDocIdsArray[i++] = ids;
	}

	JoinDocCollector(int joinResultSize, int idsLength, int maxDoc) {
		this.maxDoc = maxDoc;
		this.bitSet = null;
		this.joinResultSize = joinResultSize;
		this.ids = new int[idsLength];
		this.foreignDocIdsArray = new int[idsLength][];
	}

	/**
	 * Copy only the valid item (other than -1)
	 * 
	 * @param src
	 */
	JoinDocCollector(final JoinDocCollector src) {
		this(src.joinResultSize, validSize(src.ids), src.maxDoc);
		int i1 = 0;
		int i2 = 0;
		for (int id : src.ids) {
			if (id != -1) {
				ids[i1] = id;
				foreignDocIdsArray[i1++] = ArrayUtils
						.clone(src.foreignDocIdsArray[i2]);
			}
			i2++;
		}
	}

	@Override
	public JoinDocCollector duplicate(AbstractBaseCollector<?> base) {
		return new JoinDocCollector((JoinDocCollector) base);
	}

	protected static final int validSize(int[] ids) {
		int i = 0;
		for (int id : ids)
			if (id != -1)
				i++;
		return i;
	}

	final public static int[][] copyForeignDocIdsArray(
			int[][] foreignDocIdsArray) {
		int[][] neworeignDocIdsArray = new int[foreignDocIdsArray.length][];
		int i = 0;
		for (int[] foreignIds : foreignDocIdsArray)
			neworeignDocIdsArray[i++] = ArrayUtils.clone(foreignIds);
		return neworeignDocIdsArray;
	}

	final public static void swap(int[][] foreignDocIdsArray, int pos1, int pos2) {
		int[] foreignDocIds = foreignDocIdsArray[pos1];
		foreignDocIdsArray[pos1] = foreignDocIdsArray[pos2];
		foreignDocIdsArray[pos2] = foreignDocIds;
	}

	@Override
	final public void swap(final int pos1, final int pos2) {
		int id = ids[pos1];
		ids[pos1] = ids[pos2];
		ids[pos2] = id;
		swap(foreignDocIdsArray, pos1, pos2);
	}

	@Override
	final public int[] getIds() {
		return ids;
	}

	@Override
	final public int getSize() {
		return ids.length;
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
	final public void setForeignDocId(final int pos, final int joinResultPos,
			final int foreignDocId, float foreignScore) {
		int[] foreignDocIds = foreignDocIdsArray[pos];
		if (foreignDocIds == null) {
			foreignDocIds = new int[joinResultSize];
			Arrays.fill(foreignDocIds, -1);
		}
		foreignDocIds[joinResultPos] = foreignDocId;
		foreignDocIdsArray[pos] = foreignDocIds;
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
	final public int getForeignDocIds(final int pos, final int joinPosition) {
		return getForeignDocIds(foreignDocIdsArray, pos, joinPosition);
	}

	final public static DocIdInterface getDocIdInterface(int maxDoc,
			int joinPosition, JoinDocCollector joinDocColletor)
			throws IOException {
		DocIdCollector docIdCollector = new DocIdCollector(maxDoc,
				joinDocColletor.getIds().length);
		for (int[] foreinDocs : joinDocColletor.getForeignDocIdsArray())
			docIdCollector.collectDoc(foreinDocs[joinPosition]);
		return docIdCollector;
	}

	@Override
	final public int[][] getForeignDocIdsArray() {
		return foreignDocIdsArray;
	}

	@Override
	final public int getMaxDoc() {
		return maxDoc;
	}

}
