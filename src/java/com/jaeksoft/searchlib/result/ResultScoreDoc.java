/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008-2010 Emmanuel Keller / Jaeksoft
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

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.apache.lucene.search.FieldCache.StringIndex;
import org.apache.lucene.search.ScoreDoc;

import com.jaeksoft.searchlib.util.External;

public class ResultScoreDoc implements Externalizable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5961891131296766298L;

	public transient ResultSingle resultSingle;

	public int doc;

	public float score;

	public String collapseTerm;

	public transient int collapseCount;

	public String[] sortValues;

	public String[] facetValues;

	public ResultScoreDoc() {
	}

	public ResultScoreDoc(ResultSingle resultSingle, ScoreDoc scoreDoc) {
		this.score = scoreDoc.score;
		this.doc = scoreDoc.doc;
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

	public void loadFacetValues(StringIndex[] facetStringIndexArray) {
		if (facetValues != null)
			return;
		int i = 0;
		facetValues = new String[facetStringIndexArray.length];
		for (StringIndex stringIndex : facetStringIndexArray) {
			if (stringIndex == null)
				facetValues[i] = null;
			else
				facetValues[i] = stringIndex.lookup[stringIndex.order[doc]];
			i++;
		}
	}

	public String[] getSortValues() {
		return sortValues;
	}

	public String[] getFacetValues() {
		return facetValues;
	}

	public static ResultScoreDoc[] appendResultScoreDocArray(
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
			resultScoreDocs[i] = new ResultScoreDoc(resultSingle,
					scoreDocs[i++]);
		return resultScoreDocs;
	}

	public static ResultScoreDoc[] appendResultScoreDocArray(
			ResultSingle resultSingle, ResultScoreDoc[] oldResultScoreDocs,
			ScoreDoc[] scoreDocs, int rows, StringIndex collapseFieldStringIndex) {
		if (collapseFieldStringIndex == null)
			return appendResultScoreDocArray(resultSingle, oldResultScoreDocs,
					scoreDocs, rows);
		int l = oldResultScoreDocs != null ? oldResultScoreDocs.length : 0;
		ResultScoreDoc[] resultScoreDocs = appendResultScoreDocArray(
				resultSingle, oldResultScoreDocs, scoreDocs, rows);
		for (int i = l; i < resultScoreDocs.length; i++)
			resultScoreDocs[i].loadCollapseTerm(collapseFieldStringIndex);
		return resultScoreDocs;
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {
		resultSingle = null;
		collapseCount = 0;
		doc = in.readInt();
		score = in.readFloat();
		collapseTerm = External.readUTF(in);
		sortValues = External.readStringArray(in);
		facetValues = External.readStringArray(in);
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeInt(doc);
		out.writeFloat(score);
		External.writeUTF(collapseTerm, out);
		External.writeStringArray(sortValues, out);
		External.writeStringArray(facetValues, out);
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
}
