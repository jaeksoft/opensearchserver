/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2012-2013 Emmanuel Keller / Jaeksoft
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

import java.io.IOException;
import java.util.Arrays;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.lucene.util.OpenBitSet;

import com.jaeksoft.searchlib.index.FieldCacheIndex;
import com.jaeksoft.searchlib.join.JoinItem.JoinType;
import com.jaeksoft.searchlib.join.JoinItem.OuterCollector;
import com.jaeksoft.searchlib.sort.AscStringIndexSorter;
import com.jaeksoft.searchlib.util.StringUtils;
import com.jaeksoft.searchlib.util.Timer;

public class JoinDocCollector implements JoinDocInterface {

	public final static JoinDocCollector EMPTY = new JoinDocCollector();

	protected final int maxDoc;
	protected final int[] ids;
	protected final int[][] foreignDocIdsArray;
	protected OpenBitSet bitSet;
	protected final int joinResultSize;

	protected JoinDocCollector() {
		maxDoc = 0;
		ids = new int[0];
		foreignDocIdsArray = new int[0][0];
		bitSet = null;
		joinResultSize = 0;
	}

	public JoinDocCollector(DocIdInterface docs, int joinResultSize) {
		this.bitSet = null;
		this.maxDoc = docs.getMaxDoc();
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

	protected JoinDocCollector(int joinResultSize, int idsLength, int maxDoc) {
		this.bitSet = null;
		this.joinResultSize = joinResultSize;
		this.ids = new int[idsLength];
		this.foreignDocIdsArray = new int[idsLength][];
		this.maxDoc = maxDoc;
	}

	/**
	 * Copy only the valid item (other than -1)
	 * 
	 * @param src
	 */
	protected JoinDocCollector(JoinDocCollector src) {
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
	public DocIdInterface duplicate() {
		return new JoinDocCollector(this);
	}

	@Override
	public void swap(int pos1, int pos2) {
		int id = ids[pos1];
		ids[pos1] = ids[pos2];
		ids[pos2] = id;
		swap(foreignDocIdsArray, pos1, pos2);
	}

	@Override
	public int[] getIds() {
		return ids;
	}

	@Override
	public int getSize() {
		return ids.length;
	}

	@Override
	public OpenBitSet getBitSet() {
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
	public void setForeignDocId(int pos, int joinResultPos, int foreignDocId,
			float foreignScore) {
		int[] foreignDocIds = foreignDocIdsArray[pos];
		if (foreignDocIds == null) {
			foreignDocIds = new int[joinResultSize];
			Arrays.fill(foreignDocIds, -1);
		}
		foreignDocIds[joinResultPos] = foreignDocId;
		foreignDocIdsArray[pos] = foreignDocIds;
	}

	final public static int getForeignDocIds(int[][] foreignDocIdsArray,
			int pos, int joinPosition) {
		int[] foreignDocIds = foreignDocIdsArray[pos];
		if (foreignDocIds == null)
			return -1;
		if (joinPosition >= foreignDocIds.length)
			return -1;
		return foreignDocIds[joinPosition];
	}

	@Override
	public int getForeignDocIds(int pos, int joinPosition) {
		return getForeignDocIds(foreignDocIdsArray, pos, joinPosition);
	}

	final public static DocIdInterface getDocIdInterface(int maxDoc,
			int joinPosition, JoinDocCollector joinDocColletor)
			throws IOException {
		DocIdCollector docIdCollector = new DocIdCollector(maxDoc,
				joinDocColletor.getIds().length);
		for (int[] foreinDocs : joinDocColletor.getForeignDocIdsArray())
			docIdCollector.collect(foreinDocs[joinPosition]);
		return docIdCollector;
	}

	final private static void innerJoin(JoinDocInterface docs1,
			FieldCacheIndex doc1StringIndex, DocIdInterface docs2,
			FieldCacheIndex doc2StringIndex, float scores2[],
			int joinResultPos, OuterCollector outerCollector) {
		float score2 = 1.0F;
		int i1 = 0;
		int i2 = 0;
		int lastOuter = -1;
		int lastInner = -1;
		int[] ids1 = docs1.getIds();
		int[] ids2 = docs2.getIds();
		while (i1 != ids1.length) {
			int id1 = ids1[i1];
			int id2 = ids2[i2];
			String t1 = doc1StringIndex.lookup[doc1StringIndex.order[id1]];
			String t2 = doc2StringIndex.lookup[doc2StringIndex.order[id2]];
			int c = StringUtils.compareNullString(t1, t2);
			if (c < 0) {
				ids1[i1] = -1;
				i1++;
			} else if (c > 0) {
				if (outerCollector != null && lastOuter != id2
						&& lastInner != id2) {
					outerCollector.collect(id2, t2);
					lastOuter = id2;
				}
				i2++;
				if (i2 == ids2.length)
					while (i1 != ids1.length)
						ids1[i1++] = -1;
			} else {
				if (scores2 != null)
					score2 = scores2[i2];
				docs1.setForeignDocId(i1, joinResultPos, id2, score2);
				lastInner = id2;
				i1++;
			}
		}
		if (outerCollector != null) {
			while (i2 != ids2.length) {
				int id2 = ids2[i2++];
				if (id2 != lastInner)
					outerCollector.collect(id2,
							doc2StringIndex.lookup[doc2StringIndex.order[id2]]);
			}
		}
	}

	final private static void outerJoin(JoinDocInterface docs1,
			FieldCacheIndex doc1StringIndex, DocIdInterface docs2,
			FieldCacheIndex doc2StringIndex, float scores2[], int joinResultPos) {
		float score2 = 1.0F;
		int i1 = 0;
		int i2 = 0;
		int[] ids1 = docs1.getIds();
		int[] ids2 = docs2.getIds();
		while (i1 != ids1.length) {
			int id1 = ids1[i1];
			int id2 = ids2[i2];
			String t1 = doc1StringIndex.lookup[doc1StringIndex.order[id1]];
			String t2 = doc2StringIndex.lookup[doc2StringIndex.order[id2]];
			int c = StringUtils.compareNullString(t1, t2);
			if (c < 0) {
				i1++;
			} else if (c > 0) {
				i2++;
				if (i2 == ids2.length)
					while (i1 != ids1.length)
						ids1[i1++] = -1;
			} else {
				if (scores2 != null)
					score2 = scores2[i2];
				docs1.setForeignDocId(i1, joinResultPos, id2, score2);
				i1++;
			}
		}
	}

	final public static DocIdInterface join(DocIdInterface docs,
			FieldCacheIndex doc1StringIndex, DocIdInterface docs2,
			FieldCacheIndex doc2StringIndex, int joinResultSize,
			int joinResultPos, Timer timer, boolean factorScore,
			JoinType joinType, OuterCollector outerCollector) {

		DocIdInterface emptyDocs = docs instanceof ScoreDocInterface ? JoinScoreDocCollector.EMPTY
				: JoinDocCollector.EMPTY;

		if (docs.getSize() == 0 && outerCollector == null)
			return emptyDocs;

		if (docs2.getSize() == 0)
			return joinType == JoinType.INNER ? emptyDocs : docs;

		Timer t = new Timer(timer, "copy & sort local documents");
		JoinDocInterface docs1 = (docs instanceof ScoreDocInterface) ? new JoinScoreDocCollector(
				(ScoreDocInterface) docs, joinResultSize)
				: new JoinDocCollector(docs, joinResultSize);
		new AscStringIndexSorter(docs1, doc1StringIndex).quickSort(t);
		t.getDuration();

		t = new Timer(timer, "copy & sort foreign documents");
		docs2 = docs2.duplicate();
		float scores2[] = null;
		if (docs2 instanceof ScoreDocInterface && factorScore) {
			if (factorScore)
				scores2 = ((ScoreDocInterface) docs2).getScores();

		}
		new AscStringIndexSorter(docs2, doc2StringIndex).quickSort(t);
		t.getDuration();

		t = new Timer(timer, "join operation");

		switch (joinType) {
		case INNER:
			innerJoin(docs1, doc1StringIndex, docs2, doc2StringIndex, scores2,
					joinResultPos, outerCollector);
			break;
		case OUTER:
			outerJoin(docs1, doc1StringIndex, docs2, doc2StringIndex, scores2,
					joinResultPos);
			break;
		}

		t.getDuration();

		return docs1.duplicate();
	}

	@Override
	public int[][] getForeignDocIdsArray() {
		return foreignDocIdsArray;
	}

}
