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

import com.jaeksoft.searchlib.index.FieldCacheIndex;
import com.jaeksoft.searchlib.index.ReaderAbstract;
import com.jaeksoft.searchlib.join.JoinItem.JoinType;
import com.jaeksoft.searchlib.join.JoinItem.OuterCollector;
import com.jaeksoft.searchlib.result.collector.DocIdInterface;
import com.jaeksoft.searchlib.result.collector.ScoreInterface;
import com.jaeksoft.searchlib.sort.AscStringIndexSorter;
import com.jaeksoft.searchlib.sort.SorterAbstract.NoCollectorException;
import com.jaeksoft.searchlib.util.StringUtils;
import com.jaeksoft.searchlib.util.Timer;

public class JoinUtils {

	final public static DocIdInterface join(final DocIdInterface docs,
			FieldCacheIndex doc1StringIndex, DocIdInterface docs2,
			FieldCacheIndex doc2StringIndex, int joinResultSize,
			final int joinResultPos, Timer timer, JoinType joinType,
			OuterCollector outerCollector, ReaderAbstract foreignReader)
			throws NoCollectorException {

		if (docs.getSize() == 0 && outerCollector == null)
			return docs;

		if (docs2.getSize() == 0)
			return joinType == JoinType.INNER ? docs2 : docs;

		JoinDocCollector docs1 = JoinUtils.getCollector(docs, joinResultSize);
		docs1.getForeignReaders()[joinResultPos] = foreignReader;

		Timer t = new Timer(timer, "copy & sort local documents");
		new AscStringIndexSorter(docs1, doc1StringIndex, false).quickSort(t);
		t.getDuration();

		t = new Timer(timer, "copy & sort foreign documents");
		docs2 = (DocIdInterface) docs2.duplicate();
		ScoreInterface scoreDocs2 = docs2.getCollector(ScoreInterface.class);
		float scores2[] = scoreDocs2 != null ? scoreDocs2.getScores() : null;

		new AscStringIndexSorter(docs2, doc2StringIndex, false).quickSort(t);
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

		// / Duplicate on JoinCollector also made reduction
		return (DocIdInterface) docs1.duplicate();
	}

	final private static JoinDocCollector getCollector(
			final DocIdInterface docs, final int joinResultSize) {
		JoinDocCollector base = new JoinDocCollector(docs, joinResultSize);
		ScoreInterface scoreInterface = docs.getCollector(ScoreInterface.class);
		if (scoreInterface != null)
			new JoinScoreCollector(base, scoreInterface);
		return base;
	}

	final private static void outerJoin(JoinDocCollector docs1,
			FieldCacheIndex doc1StringIndex, DocIdInterface docs2,
			FieldCacheIndex doc2StringIndex, float scores2[], int joinResultPos) {
		float score2 = 1.0F;
		int i1 = 0;
		int i2 = 0;
		int[] ids1 = docs1.getCollector(DocIdInterface.class).getIds();
		int[] ids2 = docs2.getCollector(DocIdInterface.class).getIds();
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
				// No more outer content, we can leave
				if (i2 == ids2.length)
					break;
			} else {
				if (scores2 != null)
					score2 = scores2[i2];
				docs1.setForeignDoc(i1, joinResultPos, id2, score2);
				i1++;
			}
		}
	}

	final private static void innerJoin(JoinDocCollector docs1,
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
			final int id1 = ids1[i1];
			final int id2 = ids2[i2];
			final String v1 = doc1StringIndex.lookup[doc1StringIndex.order[id1]];
			final String v2 = doc2StringIndex.lookup[doc2StringIndex.order[id2]];
			final int c = StringUtils.compareNullString(v1, v2);
			if (c < 0) {
				ids1[i1] = -1;
				i1++;
			} else if (c > 0) {
				if (outerCollector != null && lastOuter != id2
						&& lastInner != id2) {
					outerCollector.collect(id2, v2);
					lastOuter = id2;
				}
				i2++;
				if (i2 == ids2.length)
					while (i1 != ids1.length)
						ids1[i1++] = -1;
			} else {
				if (scores2 != null)
					score2 = scores2[i2];
				docs1.setForeignDoc(i1, joinResultPos, id2, score2);
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
}
