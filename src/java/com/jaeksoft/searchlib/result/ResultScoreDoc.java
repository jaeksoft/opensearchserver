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

import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.FieldCache.StringIndex;

import com.jaeksoft.searchlib.schema.Field;

public class ResultScoreDoc {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5961891131296766298L;

	public transient ResultSearch resultSearch;

	public transient int doc;

	public float score;

	public String collapseTerm;

	public int collapseCount;

	public ResultScoreDoc(ResultSearch resultSearch, ScoreDoc scoreDoc,
			String collapseTerm) {
		this.score = scoreDoc.score;
		this.doc = scoreDoc.doc;
		this.resultSearch = resultSearch;
		this.collapseTerm = collapseTerm;
		this.collapseCount = 0;
	}

	public ResultScoreDoc(ResultSearch resultSearch, ScoreDoc scoreDoc) {
		this(resultSearch, scoreDoc, null);
	}

	/**
	 * Create an populate a new ResultScoreDoc[]
	 * 
	 * @param resultSearch
	 * @param scoreDocs
	 * @return populated ResultScoreDoc array
	 */
	public static ResultScoreDoc[] newResultScoreDocArray(
			ResultSearch resultSearch, ScoreDoc[] scoreDocs) {
		ResultScoreDoc[] resultScoreDocs = new ResultScoreDoc[scoreDocs.length];
		int i = 0;
		for (ScoreDoc scoreDoc : scoreDocs)
			resultScoreDocs[i++] = new ResultScoreDoc(resultSearch, scoreDoc);
		return resultScoreDocs;
	}

	public static ResultScoreDoc[] newResultScoreDocArray(
			ResultSearch resultSearch, ScoreDoc[] scoreDocs, Field collapseField)
			throws IOException {
		if (collapseField == null)
			return newResultScoreDocArray(resultSearch, scoreDocs);
		StringIndex stringIndex = resultSearch.getReader().getStringIndex(
				collapseField.getName());
		ResultScoreDoc[] resultScoreDocs = new ResultScoreDoc[scoreDocs.length];
		int i = 0;
		for (ScoreDoc scoreDoc : scoreDocs) {
			String collapseTerm = stringIndex.lookup[stringIndex.order[scoreDoc.doc]];
			resultScoreDocs[i++] = new ResultScoreDoc(resultSearch, scoreDoc,
					collapseTerm);
		}
		return resultScoreDocs;

	}
}
