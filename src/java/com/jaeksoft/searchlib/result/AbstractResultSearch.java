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

import javax.servlet.http.HttpServletRequest;

import com.jaeksoft.searchlib.collapse.CollapseAbstract;
import com.jaeksoft.searchlib.facet.FacetList;
import com.jaeksoft.searchlib.render.Render;
import com.jaeksoft.searchlib.render.RenderJsp;
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

	private final static ResultDocument[] noDocuments = new ResultDocument[0];
	private final static ResultScoreDoc[] noResultScoreDocs = new ResultScoreDoc[0];

	protected AbstractResultSearch(SearchRequest searchRequest) {
		super(searchRequest);
		this.resultDocuments = noDocuments;
		this.numFound = 0;
		this.maxScore = 0;
		this.collapsedDocCount = 0;
		this.docs = noResultScoreDocs;
		if (searchRequest.getFacetFieldList().size() > 0)
			this.facetList = new FacetList();
		collapse = CollapseAbstract.newInstance(searchRequest);
	}

	public FacetList getFacetList() {
		return this.facetList;
	}

	protected void setDocuments(ResultDocument[] resultDocuments) {
		this.resultDocuments = resultDocuments == null ? noDocuments
				: resultDocuments;
	}

	public ResultDocument getDocument(int pos) {
		if (pos < request.getStart())
			return null;
		if (pos >= request.getEnd())
			return null;
		if (pos >= getDocLength())
			return null;
		return resultDocuments[pos - request.getStart()];
	}

	public float getMaxScore() {
		return maxScore;
	}

	public int getNumFound() {
		return numFound;
	}

	protected void setDocs(ResultScoreDoc[] docs) {
		this.docs = docs == null ? noResultScoreDocs : docs;
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

	public ResultScoreDoc[] getDocs() {
		return docs;
	}

	public CollapseAbstract getCollapse() {
		return collapse;
	}

	public int getCollapseDocCount() {
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
		return docs[pos].collapseCount;
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

	public Render getRender(HttpServletRequest request) {

		Render render = null;

		String p;
		if ((p = request.getParameter("render")) != null) {
			if ("jsp".equals(p))
				render = new RenderJsp(request.getParameter("jsp"), this);
		}

		if (render == null)
			render = new RenderSearchXml(this);

		return render;
	}
}
