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

public class ResultScoreDoc {

	public static final ResultScoreDoc[] EMPTY_ARRAY = new ResultScoreDoc[0];

	final public int doc;

	final public float score;

	protected ResultScoreDoc(ResultScoreDoc rsd) {
		this.score = rsd.score;
		this.doc = rsd.doc;
	}

	public ResultScoreDoc(int doc, float score) {
		this.score = score;
		this.doc = doc;
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

	public ResultScoreDocCollapse newResultScoreDocCollapse() {
		return ResultScoreDocCollapse.newInstance(this);
	}

	final protected static ResultScoreDoc[] copy(ResultScoreDoc[] docs) {
		ResultScoreDoc[] newDocs = new ResultScoreDoc[docs.length];
		if (docs.length == 0)
			return newDocs;
		int i = 0;
		for (ResultScoreDoc doc : docs)
			newDocs[i++] = new ResultScoreDoc(doc);
		return newDocs;
	}

	/**
	 * For join feature only
	 * 
	 * @param pos
	 * @return
	 */
	public ResultScoreDoc getForeignDoc(int pos) {
		return this;
	}

}
