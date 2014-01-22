package com.jaeksoft.searchlib.result.collector.join;

import com.jaeksoft.searchlib.index.FieldCacheIndex;
import com.jaeksoft.searchlib.join.JoinItem.JoinType;
import com.jaeksoft.searchlib.join.JoinItem.OuterCollector;
import com.jaeksoft.searchlib.result.collector.DocIdInterface;
import com.jaeksoft.searchlib.result.collector.ScoreInterface;
import com.jaeksoft.searchlib.sort.AscStringIndexSorter;
import com.jaeksoft.searchlib.util.StringUtils;
import com.jaeksoft.searchlib.util.Timer;

public class JoinUtils {

	final public static DocIdInterface join(final DocIdInterface docs,
			FieldCacheIndex doc1StringIndex, DocIdInterface docs2,
			FieldCacheIndex doc2StringIndex, int joinResultSize,
			final int joinResultPos, Timer timer, boolean factorScore,
			JoinType joinType, OuterCollector outerCollector) {

		if (docs.getSize() == 0 && outerCollector == null)
			return docs;

		if (docs2.getSize() == 0)
			return joinType == JoinType.INNER ? docs2 : docs;

		JoinDocCollectorInterface docs1 = JoinUtils.getCollector(docs,
				joinResultSize);

		Timer t = new Timer(timer, "copy & sort local documents");
		new AscStringIndexSorter(docs1, doc1StringIndex).quickSort(t);
		t.getDuration();

		t = new Timer(timer, "copy & sort foreign documents");
		docs2 = (DocIdInterface) docs2.duplicate();
		ScoreInterface scoreDocs2 = docs2.getCollector(ScoreInterface.class);
		float scores2[] = null;
		if (scoreDocs2 != null && factorScore) {
			if (factorScore)
				scores2 = scoreDocs2.getScores();

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

		docs1 = (JoinDocCollectorInterface) docs1.duplicate();
		return docs1.getCollector(DocIdInterface.class);
	}

	final private static JoinDocCollectorInterface getCollector(
			final DocIdInterface docs, final int joinResultSize) {
		JoinDocCollector base = new JoinDocCollector(docs, joinResultSize);
		JoinDocCollectorInterface last = base;
		ScoreInterface scoreInterface = docs.getCollector(ScoreInterface.class);
		if (scoreInterface != null)
			last = new JoinScoreCollector(base, scoreInterface, joinResultSize);
		return last;
	}

	final private static void outerJoin(JoinDocCollectorInterface docs1,
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

	final private static void innerJoin(JoinDocCollectorInterface docs1,
			FieldCacheIndex doc1StringIndex, DocIdInterface docs2,
			FieldCacheIndex doc2StringIndex, float scores2[],
			int joinResultPos, OuterCollector outerCollector) {
		float score2 = 1.0F;
		int i1 = 0;
		int i2 = 0;
		int lastOuter = -1;
		int lastInner = -1;
		int[] ids1 = docs1.getCollector(DocIdInterface.class).getIds();
		int[] ids2 = docs2.getCollector(DocIdInterface.class).getIds();
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

}
