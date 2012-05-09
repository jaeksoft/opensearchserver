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

import org.apache.lucene.search.ScoreDoc;

final public class ResultScoreDoc {

	public int doc;

	public float score;

	public int collapseCount;

	public ResultScoreDoc(ScoreDoc scoreDoc) {
		this.score = scoreDoc.score;
		this.doc = scoreDoc.doc;
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

	// public void loadStringIndex(StringIndex[] stringIndexArray) {
	// if (stringIndexValues != null)
	// return;
	// int i = 0;
	// stringIndexValues = new String[stringIndexArray.length];
	// for (StringIndex stringIndex : stringIndexArray) {
	// if (stringIndex == null)
	// stringIndexValues[i] = null;
	// else
	// stringIndexValues[i] = stringIndex.lookup[stringIndex.order[doc]];
	// i++;
	// }
	// }

	// final public void loadCollapseTerm(StringIndex stringIndex) {
	// if (collapseTerm != null)
	// return;
	// collapseTerm = stringIndex.lookup[stringIndex.order[doc]];
	// }

	final public static ResultScoreDoc[] appendResultScoreDocArray(
			ResultSearchSingle resultSingle,
			ResultScoreDoc[] oldResultScoreDocs, ScoreDoc[] scoreDocs, int rows) {
		if (rows > scoreDocs.length)
			rows = scoreDocs.length;
		ResultScoreDoc[] resultScoreDocs = new ResultScoreDoc[rows];
		int i = 0;
		if (oldResultScoreDocs != null)
			for (ResultScoreDoc rsc : oldResultScoreDocs)
				resultScoreDocs[i++] = rsc;
		while (i < rows)
			resultScoreDocs[i] = new ResultScoreDoc(scoreDocs[i++]);
		return resultScoreDocs;
	}

	final public static ResultScoreDoc[] appendLeftScoreDocArray(
			ResultSearchSingle resultSingle,
			ResultScoreDoc[] oldResultScoreDocs, ScoreDoc[] scoreDocs, int start) {
		if (start >= scoreDocs.length)
			return oldResultScoreDocs;
		ResultScoreDoc[] resultScoreDocs = new ResultScoreDoc[oldResultScoreDocs.length
				+ scoreDocs.length - start];
		int i = 0;
		for (ResultScoreDoc resultScoreDoc : oldResultScoreDocs)
			resultScoreDocs[i++] = resultScoreDoc;
		for (int j = start; j < scoreDocs.length; j++)
			resultScoreDocs[i++] = new ResultScoreDoc(scoreDocs[j]);
		return resultScoreDocs;
	}

	// final public static ResultScoreDoc[] appendResultScoreDocArray(
	// ResultSearchSingle resultSingle,
	// ResultScoreDoc[] oldResultScoreDocs, ScoreDoc[] scoreDocs,
	// int rows, StringIndex collapseStringIndex) {
	// if (collapseStringIndex == null)
	// return appendResultScoreDocArray(resultSingle, oldResultScoreDocs,
	// scoreDocs, rows);
	// int l = oldResultScoreDocs != null ? oldResultScoreDocs.length : 0;
	// ResultScoreDoc[] resultScoreDocs = appendResultScoreDocArray(
	// resultSingle, oldResultScoreDocs, scoreDocs, rows);
	// for (int i = l; i < resultScoreDocs.length; i++)
	// resultScoreDocs[i].loadCollapseTerm(collapseStringIndex);
	// return resultScoreDocs;
	// }
}
