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
import java.util.ArrayList;

import com.jaeksoft.searchlib.collapse.CollapseGroup;
import com.jaeksoft.searchlib.facet.Facet;
import com.jaeksoft.searchlib.facet.FacetField;
import com.jaeksoft.searchlib.facet.FacetGroup;
import com.jaeksoft.searchlib.request.Request;

public class ResultGroup extends Result<CollapseGroup> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7403200579165999208L;
	private ArrayList<ResultSearch> resultList;
	private ResultSearch[] resultsFetch;
	private int[] fetchedDoc;
	private int numFound;
	private float maxScore;

	public ResultGroup(Request request) throws IOException {
		super(request);
		this.resultList = new ArrayList<ResultSearch>();
		this.fetchedDoc = new int[0];
		this.resultsFetch = null;
		this.numFound = 0;
		this.maxScore = 0;
		for (FacetField facetField : request.getFacetFieldList())
			this.facetList.add(new FacetGroup(facetField));
		if (request.getCollapseField() != null)
			this.collapse = new CollapseGroup(this);
	}

	private ResultSearch[] getResults() {
		if (this.collapse != null)
			return this.collapse.getResults();
		else
			return resultsFetch;
	}

	@Override
	public int[] getDocs() {
		if (this.collapse != null)
			return this.collapse.getDocs();
		else
			return this.fetchedDoc;
	}

	@Override
	public DocumentResult documents() throws IOException {
		if (documentResult != null)
			return documentResult;
		DocumentsGroup docsGroup = new DocumentsGroup(request);
		int start = request.getStart();
		int end = request.getEnd();
		for (int pos = start; pos < end; pos++) {
			if (pos >= getDocs().length)
				break;
			ResultSearch resultSearch = this.getResults()[pos];
			docsGroup.add(resultSearch.getReader(), resultSearch
					.getDocId(getDocs()[pos]));
		}
		documentResult = docsGroup.documents();
		return documentResult;
	}

	@Override
	public float getScore(int pos) {
		return this.getResults()[pos].getScore(this.getDocs()[pos]);
	}

	@Override
	public int[] getFetchedDoc() {
		return this.fetchedDoc;
	}

	public ResultSearch[] getResultsFetch() {
		return resultsFetch;
	}

	public void addResult(ResultSearch result) throws IOException {
		synchronized (this) {
			this.resultList.add(result);
			this.numFound += result.getNumFound();
			if (this.facetList != null)
				for (Facet facet : this.facetList)
					((FacetGroup) facet).run(result.getFacetList());
		}
	}

	public int populate(ResultSearch result, int start, int rows)
			throws IOException {

		synchronized (this) {

			if (result.getNumFound() == 0)
				return 0;

			int r = 0;

			if (this.fetchedDoc == null || this.fetchedDoc.length == 0)
				r = this.setDoc(result, start + rows);
			else
				r = this.insertAndSort(result, start, rows);

			if (result.getMaxScore() > this.maxScore)
				this.maxScore = result.getMaxScore();
			return r;
		}
	}

	private int setDoc(ResultSearch result, int end) {
		int[] resultDoc = result.getFetchedDoc();
		if (resultDoc == null)
			return 0;
		if (end > resultDoc.length)
			end = resultDoc.length;
		this.fetchedDoc = new int[end];
		this.resultsFetch = new ResultSearch[end];
		for (int i = 0; i < end; i++) {
			this.fetchedDoc[i] = i;
			this.resultsFetch[i] = result;
		}
		return end;
	}

	/**
	 * Concat�nation d'un HitGroup et d'un Result Tri� en fonction du score
	 * 
	 * @param resultSearch
	 * @param start
	 * @param rows
	 * @return the number of rows
	 */
	private int insertAndSort(ResultSearch resultSearch, int start, int rows) {
		int end = start + rows;
		if (end > resultSearch.getFetchedDoc().length)
			end = resultSearch.getFetchedDoc().length;
		if (start >= end)
			return 0;
		int[] newDoc = new int[this.fetchedDoc.length + (end - start)];
		ResultSearch[] newResults = new ResultSearch[newDoc.length];
		int iOld = 0;
		int iResult = start;
		int n = 0;
		for (;;) {
			if (resultSearch.getScore(iResult) > this.resultsFetch[iOld]
					.getScore(this.fetchedDoc[iOld])) {
				newDoc[n] = iResult;
				newResults[n] = resultSearch;
				n++;
				if (++iResult == end) {
					for (int i = iOld; i < this.fetchedDoc.length; i++) {
						newDoc[n] = this.fetchedDoc[i];
						newResults[n] = this.resultsFetch[i];
						n++;
					}
					break;
				}
			} else {
				newResults[n] = this.resultsFetch[iOld];
				newDoc[n] = this.fetchedDoc[iOld];
				n++;
				if (++iOld == this.fetchedDoc.length) {
					for (int i = iResult; i < end; i++) {
						newDoc[n] = i;
						newResults[n] = resultSearch;
						n++;
					}
					break;
				}
			}
		}
		this.fetchedDoc = newDoc;
		this.resultsFetch = newResults;
		return end - start;
	}

	public ArrayList<ResultSearch> getResultList() {
		return this.resultList;
	}

	public float getScoreGoal() {
		int end = request.getEnd();
		if (this.getDocs().length < end)
			return 0;
		return getScore(end - 1);
	}

	@Override
	public float getMaxScore() {
		return this.maxScore;
	}

	@Override
	public int getNumFound() {
		return this.numFound;
	}

}
