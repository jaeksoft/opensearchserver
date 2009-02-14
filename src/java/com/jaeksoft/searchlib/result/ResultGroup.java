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
import java.net.URISyntaxException;

import org.apache.lucene.queryParser.ParseException;

import com.jaeksoft.searchlib.facet.Facet;
import com.jaeksoft.searchlib.facet.FacetField;
import com.jaeksoft.searchlib.facet.FacetGroup;
import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.index.IndexGroup;
import com.jaeksoft.searchlib.request.DocumentsRequest;
import com.jaeksoft.searchlib.request.SearchRequest;
import com.jaeksoft.searchlib.sort.SorterInterface;

public class ResultGroup extends Result {

	private transient SorterInterface sorter;
	private transient ResultScoreDoc[] notCollapsedDocs;

	public ResultGroup(SearchRequest searchRequest) throws IOException {
		super(searchRequest);
		notCollapsedDocs = null;
		for (FacetField facetField : searchRequest.getFacetFieldList())
			this.facetList.addObject(new FacetGroup(facetField));
		sorter = searchRequest.getSortList().getSorter();
	}

	private void addResult(Result result) throws IOException {
		synchronized (this) {
			numFound += result.getNumFound();
			if (facetList != null) {
				for (Facet facet : facetList) {
					FacetGroup facetGroup = (FacetGroup) facet;
					facetGroup.append(result.getFacetList());
				}
			}
		}
	}

	/**
	 * Return true if more pertinent results are available
	 * 
	 * @param result
	 * @param start
	 * @param rows
	 * @return
	 * @throws IOException
	 */
	public void populate(Result result) throws IOException {

		synchronized (this) {

			if (result.getSearchRequest().getStart() == 0)
				addResult(result);

			if (result.getNumFound() == 0)
				return;

			if (result.getMaxScore() > maxScore)
				maxScore = result.getMaxScore();

			if (notCollapsedDocs == null || notCollapsedDocs.length == 0)
				this.setDoc(result);
			else
				insertAndSort(result);

			setDocs(notCollapsedDocs);
		}
	}

	private void setDoc(Result result) {
		ResultScoreDoc[] resultScoreDocs = result.getDocs();
		if (resultScoreDocs == null)
			return;

		int start = result.searchRequest.getStart();
		int end = result.searchRequest.getEnd();
		if (end > resultScoreDocs.length)
			end = resultScoreDocs.length;

		notCollapsedDocs = new ResultScoreDoc[end - start];
		for (int i = 0; i < notCollapsedDocs.length; i++)
			notCollapsedDocs[i] = resultScoreDocs[start + i];
	}

	/**
	 * Concat�nation d'un HitGroup et d'un Result Tri�
	 * 
	 * @param resultSearch
	 * @param start
	 * @param rows
	 */
	private void insertAndSort(Result result) {

		int start = result.searchRequest.getStart();
		int end = result.searchRequest.getEnd();
		ResultScoreDoc[] insertFetchedDocs = result.getDocs();
		int length = insertFetchedDocs.length;
		if (end > length)
			end = length;
		if (start >= end)
			return;
		ResultScoreDoc[] newDocs = new ResultScoreDoc[notCollapsedDocs.length
				+ (end - start)];
		int iOld = 0;
		int iResult = start;
		int n = 0;
		for (;;) {
			if (sorter.isBefore(insertFetchedDocs[iResult],
					notCollapsedDocs[iOld])) {
				newDocs[n++] = insertFetchedDocs[iResult];
				if (++iResult == end) {
					for (int i = iOld; i < notCollapsedDocs.length; i++)
						newDocs[n++] = notCollapsedDocs[i];
					break;
				}
			} else {
				newDocs[n++] = notCollapsedDocs[iOld];
				if (++iOld == notCollapsedDocs.length) {
					for (int i = iResult; i < end; i++)
						newDocs[n++] = insertFetchedDocs[i];
					break;
				}
			}
		}
		notCollapsedDocs = newDocs;
	}

	public void setFinalDocs() throws IOException {
		if (notCollapsedDocs == null)
			return;
		if (!collapse.isActive())
			setDocs(notCollapsedDocs);
		else {
			collapse.run(notCollapsedDocs, notCollapsedDocs.length);
			setDocs(collapse.getCollapsedDoc());
			collapsedDocCount = collapse.getDocCount();
		}
	}

	/*
	 * TODO REMOVE public float getScoreGoal() { int end = request.getEnd(); if
	 * (end == 0) return 0; if (this.getDocs().length < end) return 0; return
	 * getScore(end - 1); }
	 */

	@Override
	public float getMaxScore() {
		return this.maxScore;
	}

	@Override
	public int getNumFound() {
		return this.numFound;
	}

	public void expungeFacet() {
		if (facetList == null)
			return;
		for (Facet facet : facetList)
			((FacetGroup) facet).expunge();
	}

	public void loadDocuments(IndexGroup indexGroup) throws ParseException,
			SyntaxError, IOException, URISyntaxException,
			ClassNotFoundException {
		DocumentsRequest documentsRequest = new DocumentsRequest(this);
		ResultDocuments resultDocuments = indexGroup
				.documents(documentsRequest);
		setDocuments(resultDocuments);
	}

}
