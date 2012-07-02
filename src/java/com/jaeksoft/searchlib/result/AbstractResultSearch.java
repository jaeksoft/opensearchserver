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

import com.jaeksoft.searchlib.collapse.CollapseAbstract;
import com.jaeksoft.searchlib.facet.FacetList;
import com.jaeksoft.searchlib.join.JoinResult;
import com.jaeksoft.searchlib.render.Render;
import com.jaeksoft.searchlib.render.RenderCSV;
import com.jaeksoft.searchlib.render.RenderSearchJson;
import com.jaeksoft.searchlib.render.RenderSearchXml;
import com.jaeksoft.searchlib.request.SearchRequest;

public abstract class AbstractResultSearch extends
		AbstractResult<SearchRequest> {

	transient protected CollapseAbstract collapse;
	protected FacetList facetList;
	private ResultScoreDoc[] docs;
	protected int numFound;
	protected float maxScore;
	protected int collapsedDocCount;
	private ResultDocument[] resultDocuments;
	private JoinResult[] joinResults;

	protected AbstractResultSearch(SearchRequest searchRequest) {
		super(searchRequest);
		this.resultDocuments = ResultDocument.EMPTY_ARRAY;
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

	protected void setDocuments(ResultDocument[] resultDocuments) {
		this.resultDocuments = resultDocuments == null ? ResultDocument.EMPTY_ARRAY
				: resultDocuments;
	}

	protected void setJoinResults(JoinResult[] joinResults) {
		this.joinResults = joinResults == null ? JoinResult.EMPTY_ARRAY
				: joinResults;
	}

	private Integer getDocumentPos(int pos) {
		if (pos < request.getStart())
			return null;
		if (pos >= request.getEnd())
			return null;
		if (pos >= getDocLength())
			return null;
		return pos - request.getStart();
	}

	public ResultDocument getDocument(int pos) {
		Integer docPos = getDocumentPos(pos);
		return docPos == null ? null : resultDocuments[docPos];
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

	public ResultDocument[] getDocuments() {
		return resultDocuments;
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
		return ((ResultScoreDocCollapse) rsc).collapseCount;
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
		if (resultDocuments != null) {
			sb.append(' ');
			sb.append(resultDocuments.length);
			sb.append("resultDocuments.");
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
