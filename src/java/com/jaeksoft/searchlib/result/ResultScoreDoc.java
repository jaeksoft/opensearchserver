/**   
 * License Agreement for Jaeksoft SearchLib Community
 *
 * Copyright (C) 2008 Emmanuel Keller / Jaeksoft
 * 
 * http://www.jaeksoft.com
 * 
 * This file is part of Jaeksoft SearchLib Community.
 *
 * Jaeksoft SearchLib Community is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * Jaeksoft SearchLib Community is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Jaeksoft SearchLib Community. 
 *  If not, see <http://www.gnu.org/licenses/>.
 **/

package com.jaeksoft.searchlib.result;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.FieldCache.StringIndex;

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

	public static ResultScoreDoc[] appendResultScoreDocArray(String indexName,
			ResultSingle resultSingle, ResultScoreDoc[] oldResultScoreDocs,
			ScoreDoc[] scoreDocs) {
		ResultScoreDoc[] resultScoreDocs = new ResultScoreDoc[scoreDocs.length];
		int i = 0;
		if (oldResultScoreDocs != null)
			for (ResultScoreDoc rsc : oldResultScoreDocs)
				resultScoreDocs[i++] = rsc;
		while (i < scoreDocs.length)
			resultScoreDocs[i] = new ResultScoreDoc(indexName, resultSingle,
					scoreDocs[i++]);
		return resultScoreDocs;
	}

	public static ResultScoreDoc[] appendResultScoreDocArray(String indexName,
			ResultSingle resultSingle, ResultScoreDoc[] oldResultScoreDocs,
			ScoreDoc[] scoreDocs, StringIndex collapseFieldStringIndex) {
		if (collapseFieldStringIndex == null)
			return appendResultScoreDocArray(indexName, resultSingle,
					oldResultScoreDocs, scoreDocs);
		int l = oldResultScoreDocs != null ? oldResultScoreDocs.length : 0;
		ResultScoreDoc[] resultScoreDocs = appendResultScoreDocArray(indexName,
				resultSingle, oldResultScoreDocs, scoreDocs);
		for (int i = l; i < resultScoreDocs.length; i++)
			resultScoreDocs[i].loadCollapseTerm(collapseFieldStringIndex);
		return resultScoreDocs;
	}

	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {
		resultSingle = null;
		collapseCount = 0;
		if (in.readBoolean())
			indexName = in.readUTF();
		doc = in.readInt();
		score = in.readFloat();
		if (in.readBoolean())
			collapseTerm = in.readUTF();
	}

	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeBoolean(indexName != null);
		if (indexName != null)
			out.writeUTF(indexName);
		out.writeInt(doc);
		out.writeFloat(score);
		out.writeBoolean(collapseTerm != null);
		if (collapseTerm != null)
			out.writeUTF(collapseTerm);
	}
}
