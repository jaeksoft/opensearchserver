/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2008 Emmanuel Keller / Jaeksoft
 * 
 * http://www.open-search-server.com
 * 
 * This file is part of Jaeksoft OpenSearchServer.
 *
 * Jaeksoft OpenSearchServer is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * Jaeksoft OpenSearchServer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Jaeksoft OpenSearchServer. 
 *  If not, see <http://www.gnu.org/licenses/>.
 **/

package com.jaeksoft.searchlib.result;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.FieldCache.StringIndex;

import com.jaeksoft.searchlib.util.External;

public class ResultScoreDoc implements Externalizable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5961891131296766298L;

	public transient ResultSingle resultSingle;

	public String indexName;

	public int doc;

	public float score;

	public String collapseTerm;

	public transient int collapseCount;

	public String[] sortValues;

	public ResultScoreDoc() {
	}

	public ResultScoreDoc(String indexName, ResultSingle resultSingle,
			ScoreDoc scoreDoc) {
		this.score = scoreDoc.score;
		this.doc = scoreDoc.doc;
		this.indexName = indexName;
		this.resultSingle = resultSingle;
		this.collapseTerm = null;
		this.collapseCount = 0;
	}

	public void loadCollapseTerm(StringIndex stringIndex) {
		if (collapseTerm != null)
			return;
		collapseTerm = stringIndex.lookup[stringIndex.order[doc]];
	}

	public void loadSortValues(StringIndex[] sortStringIndexArray) {
		if (sortValues != null)
			return;
		int i = 0;
		sortValues = new String[sortStringIndexArray.length];
		for (StringIndex stringIndex : sortStringIndexArray) {
			if (stringIndex == null)
				sortValues[i] = null;
			else
				sortValues[i] = stringIndex.lookup[stringIndex.order[doc]];
			i++;
		}
	}

	public String[] getSortValues() {
		return sortValues;
	}

	public static ResultScoreDoc[] appendResultScoreDocArray(String indexName,
			ResultSingle resultSingle, ResultScoreDoc[] oldResultScoreDocs,
			ScoreDoc[] scoreDocs, int rows) {
		if (rows > scoreDocs.length)
			rows = scoreDocs.length;
		ResultScoreDoc[] resultScoreDocs = new ResultScoreDoc[rows];
		int i = 0;
		if (oldResultScoreDocs != null)
			for (ResultScoreDoc rsc : oldResultScoreDocs)
				resultScoreDocs[i++] = rsc;
		while (i < rows)
			resultScoreDocs[i] = new ResultScoreDoc(indexName, resultSingle,
					scoreDocs[i++]);
		return resultScoreDocs;
	}

	public static ResultScoreDoc[] appendResultScoreDocArray(String indexName,
			ResultSingle resultSingle, ResultScoreDoc[] oldResultScoreDocs,
			ScoreDoc[] scoreDocs, int rows, StringIndex collapseFieldStringIndex) {
		if (collapseFieldStringIndex == null)
			return appendResultScoreDocArray(indexName, resultSingle,
					oldResultScoreDocs, scoreDocs, rows);
		int l = oldResultScoreDocs != null ? oldResultScoreDocs.length : 0;
		ResultScoreDoc[] resultScoreDocs = appendResultScoreDocArray(indexName,
				resultSingle, oldResultScoreDocs, scoreDocs, rows);
		for (int i = l; i < resultScoreDocs.length; i++)
			resultScoreDocs[i].loadCollapseTerm(collapseFieldStringIndex);
		return resultScoreDocs;
	}

	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {
		resultSingle = null;
		collapseCount = 0;
		indexName = External.readUTF(in);
		doc = in.readInt();
		score = in.readFloat();
		collapseTerm = External.readUTF(in);
		sortValues = External.readStringArray(in);
	}

	public void writeExternal(ObjectOutput out) throws IOException {
		External.writeUTF(indexName, out);
		out.writeInt(doc);
		out.writeFloat(score);
		External.writeUTF(collapseTerm, out);
		External.writeStringArray(sortValues, out);
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		if (indexName != null) {
			sb.append("Index: ");
			sb.append(indexName);
			sb.append('.');
		}
		sb.append(" DocId: ");
		sb.append(doc);
		sb.append(" Score: ");
		sb.append(score);
		return sb.toString();
	}
}
