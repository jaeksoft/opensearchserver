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
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.lucene.queryParser.ParseException;

import com.jaeksoft.searchlib.collapse.CollapseGroup;
import com.jaeksoft.searchlib.facet.Facet;
import com.jaeksoft.searchlib.facet.FacetField;
import com.jaeksoft.searchlib.facet.FacetGroup;
import com.jaeksoft.searchlib.request.Request;
import com.jaeksoft.searchlib.sort.SorterInterface;

public class ResultGroup extends Result<CollapseGroup> {

	final private static Logger logger = Logger.getLogger(ResultGroup.class
			.getCanonicalName());
	/**
	 * 
	 */
	private static final long serialVersionUID = 7403200579165999208L;
	private ArrayList<ResultSearch> resultList;
	private SorterInterface sorter;

	public ResultGroup(Request request) throws IOException {
		super(request);
		this.resultList = new ArrayList<ResultSearch>();
		for (FacetField facetField : request.getFacetFieldList())
			this.facetList.add(new FacetGroup(facetField));
		if (request.getCollapseField() != null)
			this.collapse = new CollapseGroup(this);
		this.sorter = request.getSortList().getSorter();
	}

	@Override
	public DocumentResult documents() throws IOException, ParseException {
		if (documentResult != null)
			return documentResult;
		DocumentsGroup docsGroup = new DocumentsGroup(request);
		int start = request.getStart();
		int end = request.getEnd();
		for (int pos = start; pos < end; pos++) {
			if (pos >= fetchedDocs.length)
				break;
			ResultScoreDoc scoreDoc = (ResultScoreDoc) fetchedDocs[pos];
			docsGroup.add(scoreDoc);
		}
		documentResult = docsGroup.documents();
		return documentResult;
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

	/**
	 * Return true if more pertinent results are available
	 * 
	 * @param result
	 * @param start
	 * @param rows
	 * @return
	 * @throws IOException
	 */
	public void populate(ResultSearch result, int start, int rows)
			throws IOException {

		synchronized (this) {

			if (logger.isLoggable(Level.INFO))
				logger.info("Populate " + result);

			if (result.getNumFound() == 0)
				return;

			if (result.getMaxScore() > maxScore)
				maxScore = result.getMaxScore();

			if (fetchedDocs == null || fetchedDocs.length == 0)
				this.setDoc(result, start + rows);
			else
				insertAndSort(result, start, rows);

		}
	}

	private int setDoc(ResultSearch result, int end) {
		ResultScoreDoc[] resultScoreDocs = result.getFetchedDocs();
		if (resultScoreDocs == null)
			return 0;
		if (end > resultScoreDocs.length)
			end = resultScoreDocs.length;
		fetchedDocs = new ResultScoreDoc[end];
		for (int i = 0; i < end; i++)
			fetchedDocs[i] = resultScoreDocs[i];
		return end;
	}

	/**
	 * Concat�nation d'un HitGroup et d'un Result Tri�
	 * 
	 * @param resultSearch
	 * @param start
	 * @param rows
	 */
	private void insertAndSort(ResultSearch resultSearch, int start, int rows) {
		int end = start + rows;
		ResultScoreDoc[] insertFetchedDocs = resultSearch.getFetchedDocs();
		int length = insertFetchedDocs.length;
		if (end > length)
			end = length;
		if (start >= end)
			return;
		ResultScoreDoc[] newDocs = new ResultScoreDoc[fetchedDocs.length
				+ (end - start)];
		int iOld = 0;
		int iResult = start;
		int n = 0;
		for (;;) {
			if (sorter.isBefore(insertFetchedDocs[iResult], fetchedDocs[iOld])) {
				newDocs[n++] = insertFetchedDocs[iResult];
				if (++iResult == end) {
					for (int i = iOld; i < fetchedDocs.length; i++)
						newDocs[n++] = fetchedDocs[i];
					break;
				}
			} else {
				newDocs[n++] = fetchedDocs[iOld];
				if (++iOld == fetchedDocs.length) {
					for (int i = iResult; i < end; i++)
						newDocs[n++] = insertFetchedDocs[i];
					break;
				}
			}
		}
		fetchedDocs = newDocs;
	}

	public ArrayList<ResultSearch> getResultList() {
		return this.resultList;
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

}
