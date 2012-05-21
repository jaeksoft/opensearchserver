/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008-2012 Emmanuel Keller / Jaeksoft
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
import org.apache.lucene.search.ScoreDoc;

import com.jaeksoft.searchlib.sort.AscStringIndexSorter;
import com.jaeksoft.searchlib.util.StringUtils;

final public class ResultScoreDoc {

	public static final ResultScoreDoc[] EMPTY_ARRAY = new ResultScoreDoc[0];

	public int doc;

	final public float score;

	public int collapseCount;

	public ResultScoreDoc(int doc, float score) {
		this.score = score;
		this.doc = doc;
		this.collapseCount = 0;
	}

	public ResultScoreDoc(ScoreDoc sc) {
		this.score = sc.score;
		this.doc = sc.doc;
		this.collapseCount = 0;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(" DocId: ");
		sb.append(doc);
		sb.append(" Score: ");
		sb.append(score);
		return sb.toString();
	}

	final private static ResultScoreDoc[] copy(ResultScoreDoc[] docs) {
		ResultScoreDoc[] newDocs = new ResultScoreDoc[docs.length];
		int i = 0;
		for (ResultScoreDoc doc : docs)
			newDocs[i++] = doc;
		return newDocs;
	}

	final private static ResultScoreDoc[] copyValid(ResultScoreDoc[] docs) {
		int i = 0;
		for (ResultScoreDoc doc : docs)
			if (doc != null)
				i++;
		ResultScoreDoc[] newDocs = new ResultScoreDoc[i];
		i = 0;
		for (ResultScoreDoc doc : docs)
			if (doc != null)
				newDocs[i++] = doc;
		return newDocs;
	}

	final public static ResultScoreDoc[] join(ResultScoreDoc[] docs,
			StringIndex doc1StringIndex, ResultScoreDoc[] docs2,
			StringIndex doc2StringIndex) {
		if (docs.length == 0 || docs2.length == 0)
			return ResultScoreDoc.EMPTY_ARRAY;
		ResultScoreDoc[] docs1 = copy(docs);
		new AscStringIndexSorter(doc1StringIndex).sort(docs1);
		docs2 = copy(docs2);
		new AscStringIndexSorter(doc2StringIndex).sort(docs2);

		int i1 = 0;
		int i2 = 0;
		while (i1 != docs1.length) {
			ResultScoreDoc doc1 = docs1[i1];
			String t1 = doc1StringIndex.lookup[doc1StringIndex.order[doc1.doc]];
			String t2 = doc2StringIndex.lookup[doc2StringIndex.order[docs2[i2].doc]];
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
				i1++;
			}
		}
		return copyValid(docs1);
	}
}
