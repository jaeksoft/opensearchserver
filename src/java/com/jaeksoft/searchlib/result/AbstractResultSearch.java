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

import java.util.Iterator;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.collapse.CollapseAbstract;
import com.jaeksoft.searchlib.facet.FacetList;
import com.jaeksoft.searchlib.join.JoinResult;
import com.jaeksoft.searchlib.render.Render;
import com.jaeksoft.searchlib.render.RenderCSV;
import com.jaeksoft.searchlib.render.RenderSearchJson;
import com.jaeksoft.searchlib.render.RenderSearchXml;
import com.jaeksoft.searchlib.request.SearchRequest;
import com.jaeksoft.searchlib.util.Timer;

public abstract class AbstractResultSearch extends
		AbstractResult<SearchRequest> implements Iterable<ResultDocument> {

	transient protected CollapseAbstract collapse;
	protected FacetList facetList;
	protected ResultScoreDoc[] docs;
	protected int numFound;
	protected float maxScore;
	protected int collapsedDocCount;
	private JoinResult[] joinResults;

	protected AbstractResultSearch(SearchRequest searchRequest) {
		super(searchRequest);
		this.numFound = 0;
		this.maxScore = 0;
		this.collapsedDocCount = 0;
		this.docs = ResultScoreDoc.EMPTY_ARRAY;
		this.joinResults = JoinResult.EMPTY_ARRAY;
		if (searchRequest.getFacetFieldList().size() > 0)
			this.facetList = new FacetList();
		collapse = CollapseAbstract.newInstance(searchRequest);
	}

	public FacetList getFacetList() {
		return this.facetList;
	}

	protected void setJoinResults(JoinResult[] joinResults) {
		this.joinResults = joinResults == null ? JoinResult.EMPTY_ARRAY
				: joinResults;
	}

	public ResultDocument getDocument(int pos) throws SearchLibException {
		return getDocument(pos, null);
	}

	public abstract ResultDocument getDocument(int pos, Timer timer)
			throws SearchLibException;

	public class ResultDocumentIterator implements Iterator<ResultDocument> {

		private int pos;
		private int end;
		private Timer timer;

		private ResultDocumentIterator(Timer timer) {
			pos = request.getStart();
			if (pos < 0)
				pos = 0;
			end = getDocumentCount() + pos;
		}

		@Override
		public boolean hasNext() {
			return pos < end;
		}

		@Override
		public ResultDocument next() {
			try {
				return getDocument(pos++, timer);
			} catch (SearchLibException e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public void remove() {
			// TODO Auto-generated method stub
		}
	}

	public Iterator<ResultDocument> iterator(Timer timer) {
		return new ResultDocumentIterator(timer);
	}

	@Override
	public Iterator<ResultDocument> iterator() {
		return new ResultDocumentIterator(null);
	}

	public float getMaxScore() {
		return maxScore;
	}

	public int getNumFound() {
		return numFound;
	}

	protected void setDocs(ResultScoreDoc[] docs) {
		this.docs = docs == null ? ResultScoreDoc.EMPTY_ARRAY : docs;
	}

	public int getDocLength() {
		if (docs == null)
			return 0;
		return docs.length;
	}

	public int getDocumentCount() {
		int end = request.getEnd();
		int len = getDocLength();
		if (end > len)
			end = len;
		return end - request.getStart();
	}

	public JoinResult[] getJoinResult() {
		return joinResults;
	}

	public ResultScoreDoc[] getDocs() {
		return docs;
	}

	public CollapseAbstract getCollapse() {
		return collapse;
	}

	public int getCollapsedDocCount() {
		return collapsedDocCount;
	}

	public float getScore(int pos) {
		if (docs == null)
			return 0;
		return docs[pos].score;
	}

	public int getCollapseCount(int pos) {
		if (docs == null)
			return 0;
		ResultScoreDoc rsc = docs[pos];
		if (!(rsc instanceof ResultScoreDocCollapse))
			return 0;
		return ((ResultScoreDocCollapse) rsc).getCollapseCount();
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(numFound);
		sb.append(" founds.");
		if (docs != null) {
			sb.append(' ');
			sb.append(docs.length);
			sb.append(" docs.");
		}
		sb.append(" MaxScore: ");
		sb.append(maxScore);
		if (request != null) {
			sb.append(" - ");
			sb.append(request);
		}
		return sb.toString();
	}

	@Override
	protected Render getRenderXml() {
		return new RenderSearchXml(this);
	}

	@Override
	protected Render getRenderCsv() {
		return new RenderCSV(this);
	}

	@Override
	protected Render getRenderJson(boolean indent) {
		return new RenderSearchJson(this, indent);
	}

}
