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

import java.io.IOException;
import java.io.Serializable;

import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.FieldCache.StringIndex;

import com.jaeksoft.searchlib.schema.Field;

public class ResultScoreDoc implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5961891131296766298L;

	public transient ResultSingle resultSingle;

	public int doc;

	public float score;

	public ResultDocument resultDocument;

	public String collapseTerm;

	public int collapseCount;

	public ResultScoreDoc(ResultSingle resultSingle, ScoreDoc scoreDoc,
			ResultDocument resultDoc, String collapseTerm) {
		this.score = scoreDoc.score;
		this.doc = scoreDoc.doc;
		this.resultDocument = resultDoc;
		this.resultSingle = resultSingle;
		this.collapseTerm = collapseTerm;
		this.collapseCount = 0;
	}

	public ResultScoreDoc(ResultSingle resultSingle, ScoreDoc scoreDoc,
			String collapseTerm) {
		this(resultSingle, scoreDoc, null, null);
	}

	public ResultScoreDoc(ResultSingle resultSingle, ScoreDoc scoreDoc) {
		this(resultSingle, scoreDoc, null);
	}

	/**
	 * Create an populate a new ResultScoreDoc[]
	 * 
	 * @param resultSearch
	 * @param scoreDocs
	 * @return populated ResultScoreDoc array
	 */
	public static ResultScoreDoc[] newResultScoreDocArray(
			ResultSingle resultSingle, ScoreDoc[] scoreDocs) {
		ResultScoreDoc[] resultScoreDocs = new ResultScoreDoc[scoreDocs.length];
		int i = 0;
		for (ScoreDoc scoreDoc : scoreDocs)
			resultScoreDocs[i++] = new ResultScoreDoc(resultSingle, scoreDoc);
		return resultScoreDocs;
	}

	public static ResultScoreDoc[] newResultScoreDocArray(
			ResultSingle resultSingle, ScoreDoc[] scoreDocs, Field collapseField)
			throws IOException {
		if (collapseField == null)
			return newResultScoreDocArray(resultSingle, scoreDocs);
		StringIndex stringIndex = resultSingle.getReader().getStringIndex(
				collapseField.getName());
		ResultScoreDoc[] resultScoreDocs = new ResultScoreDoc[scoreDocs.length];
		int i = 0;
		for (ScoreDoc scoreDoc : scoreDocs) {
			String collapseTerm = stringIndex.lookup[stringIndex.order[scoreDoc.doc]];
			resultScoreDocs[i++] = new ResultScoreDoc(resultSingle, scoreDoc,
					collapseTerm);
		}
		return resultScoreDocs;

	}
}
