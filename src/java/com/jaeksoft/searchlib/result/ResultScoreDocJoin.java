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

package com.jaeksoft.searchlib.result;

import org.apache.lucene.search.FieldCache.StringIndex;

import com.jaeksoft.searchlib.sort.AscStringIndexSorter;
import com.jaeksoft.searchlib.util.StringUtils;
import com.jaeksoft.searchlib.util.Timer;

final public class ResultScoreDocJoin extends ResultScoreDoc implements
		ResultScoreDocJoinInterface {

	protected static final ResultScoreDocJoin[] EMPTY_ARRAY = new ResultScoreDocJoin[0];

	protected int[] foreignDocIds;

	private ResultScoreDocJoin(ResultScoreDoc rsd, int joinResultSize) {
		super(rsd);
		this.foreignDocIds = new int[joinResultSize];
	}

	private ResultScoreDocJoin(ResultScoreDocJoin doc) {
		super(doc);
		this.foreignDocIds = doc.foreignDocIds;
	}

	@Override
	final public int[] getForeignDocIds() {
		return foreignDocIds;
	}

	@Override
	final public void setForeignDocId(int pos, int doc) {
		foreignDocIds[pos] = doc;
	}

	final public static ResultScoreDoc[] copyJoin(ResultScoreDoc[] docs,
			int joinResultSize) {
		ResultScoreDoc[] newDocs = new ResultScoreDocJoin[docs.length];
		if (docs.length == 0)
			return newDocs;
		int i = 0;
		ResultScoreDoc refDoc = docs[0];
		if (refDoc instanceof ResultScoreDocJoin) {
			for (ResultScoreDoc doc : docs)
				newDocs[i++] = new ResultScoreDocJoin((ResultScoreDocJoin) doc);
		} else if (refDoc instanceof ResultScoreDocCollapseJoin) {
			for (ResultScoreDoc doc : docs)
				newDocs[i++] = new ResultScoreDocCollapseJoin(
						(ResultScoreDocCollapseJoin) doc);
		} else {
			for (ResultScoreDoc doc : docs)
				newDocs[i++] = new ResultScoreDocJoin(doc, joinResultSize);
		}
		return newDocs;
	}

	final public static ResultScoreDoc[] join(ResultScoreDoc[] docs,
			StringIndex doc1StringIndex, ResultScoreDoc[] docs2,
			StringIndex doc2StringIndex, int joinResultSize, int joinResultPos,
			Timer timer) {
		if (docs.length == 0 || docs2.length == 0)
			return ResultScoreDocJoin.EMPTY_ARRAY;

		Timer t = new Timer(timer, "copy & sort local documents");
		ResultScoreDoc[] docs1 = copyJoin(docs, joinResultSize);
		new AscStringIndexSorter(doc1StringIndex).sort(docs1);
		t.duration();

		t = new Timer(timer, "copy & sort foreign documents");
		docs2 = copy(docs2);
		new AscStringIndexSorter(doc2StringIndex).sort(docs2);
		t.duration();

		t = new Timer(timer, "join operation");
		int i1 = 0;
		int i2 = 0;
		while (i1 != docs1.length) {
			ResultScoreDoc doc1 = docs1[i1];
			ResultScoreDoc doc2 = docs2[i2];
			String t1 = doc1StringIndex.lookup[doc1StringIndex.order[doc1.doc]];
			String t2 = doc2StringIndex.lookup[doc2StringIndex.order[doc2.doc]];
			int c = StringUtils.compareNullString(t1, t2);
			if (c < 0) {
				docs1[i1] = null;
				i1++;
			} else if (c > 0) {
				i2++;
				if (i2 == docs2.length) {
					while (i1 != docs1.length)
						docs1[i1++] = null;
				}
			} else {
				((ResultScoreDocJoinInterface) doc1).setForeignDocId(
						joinResultPos, doc2.doc);
				i1++;
			}
		}
		t.duration();

		return copyValid(docs1);
	}

	final private static ResultScoreDoc[] copyValid(ResultScoreDoc[] docs) {
		int i = 0;
		for (ResultScoreDoc doc : docs)
			if (doc != null)
				i++;
		ResultScoreDoc[] newDocs = new ResultScoreDocJoin[i];
		i = 0;
		for (ResultScoreDoc doc : docs)
			if (doc != null)
				newDocs[i++] = doc;
		return newDocs;
	}

	@Override
	public ResultScoreDoc getForeignDoc(int pos) {
		return new ResultScoreDoc(foreignDocIds[pos], score);
	}

	@Override
	public ResultScoreDocCollapse newCollapseInstance() {
		return new ResultScoreDocCollapseJoin(this);
	}
}
