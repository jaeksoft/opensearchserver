/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008-2015 Emmanuel Keller / Jaeksoft
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

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.collapse.CollapseFunctionField;
import com.jaeksoft.searchlib.facet.FacetList;
import com.jaeksoft.searchlib.facet.FacetListExecutor;
import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.index.DocSetHits;
import com.jaeksoft.searchlib.index.ReaderAbstract;
import com.jaeksoft.searchlib.join.JoinList;
import com.jaeksoft.searchlib.join.JoinResult;
import com.jaeksoft.searchlib.query.ParseException;
import com.jaeksoft.searchlib.request.AbstractLocalSearchRequest;
import com.jaeksoft.searchlib.request.SearchFilterRequest;
import com.jaeksoft.searchlib.result.collector.CollapseDocInterface;
import com.jaeksoft.searchlib.result.collector.DocIdInterface;
import com.jaeksoft.searchlib.result.collector.JoinDocInterface;
import com.jaeksoft.searchlib.schema.FieldValue;
import com.jaeksoft.searchlib.sort.SortFieldList;
import com.jaeksoft.searchlib.sort.SorterAbstract;
import com.jaeksoft.searchlib.util.Timer;
import com.jaeksoft.searchlib.webservice.query.document.IndexDocumentResult;

public class ResultSearchSingle extends AbstractResultSearch<AbstractLocalSearchRequest> {

	transient private DocSetHits docSetHits;
	transient private final LinkedHashSet<String> fieldNameSet;

	private final ResultDocument[] resultDocuments;

	/**
	 * The constructor executes the request using the searcher provided and
	 * computes the facets.
	 * 
	 * @param searcher
	 * @param request
	 * @throws IOException
	 * @throws ParseException
	 * @throws SyntaxError
	 * @throws SearchLibException
	 * @throws ClassNotFoundException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	public ResultSearchSingle(ReaderAbstract reader, AbstractLocalSearchRequest searchRequest)
			throws IOException, ParseException, SyntaxError, SearchLibException, InstantiationException,
			IllegalAccessException, ClassNotFoundException {
		super(reader, searchRequest);

		docSetHits = reader.searchDocSet(searchRequest, timer);
		numFound = docSetHits.getNumFound();

		DocIdInterface notCollapsedDocs = docSetHits.getCollector(DocIdInterface.class);
		CollapseDocInterface collapsedDocs = null;

		JoinResult[] joinResults = null;

		SortFieldList sortFieldList = searchRequest.getSortFieldList();

		// Are we doing join
		if (searchRequest.isJoin()) {
			Timer joinTimer = new Timer(timer, "join");
			JoinList joinList = searchRequest.getJoinList();
			joinResults = new JoinResult[joinList.size()];
			Timer t = new Timer(joinTimer, "join - apply");
			notCollapsedDocs = joinList.apply(searchRequest, reader, notCollapsedDocs, joinResults, t);
			t.getDuration();
			t = new Timer(joinTimer, "join - sort");
			if (sortFieldList != null) {
				SorterAbstract sorter = sortFieldList.getSorter(notCollapsedDocs, reader);
				if (sorter != null)
					sorter.quickSort(t);
				sortFieldList = null;
			}
			t.getDuration();
			numFound = notCollapsedDocs.getSize();
			if (this.facetList == null)
				this.facetList = new FacetList();
			for (JoinResult joinResult : joinResults)
				joinResult.populate(this.facetList);
			joinTimer.getDuration();
		}

		// Handling sorting
		if (sortFieldList != null && !(request instanceof SearchFilterRequest)) {
			SorterAbstract sorter = sortFieldList.getSorter(notCollapsedDocs, reader);
			if (sorter != null)
				sorter.quickSort(timer);
		}

		// Are we doing collapsing ?
		if (collapse != null) {
			collapsedDocs = collapse.collapse(reader, notCollapsedDocs, timer);
			collapsedDocCount = collapsedDocs == null ? 0 : collapsedDocs.getCollapsedCount();
			Collection<CollapseFunctionField> functionFields = request.getCollapseFunctionFields();
			if (functionFields != null)
				for (CollapseFunctionField functionField : functionFields)
					functionField.prepareExecute(request, reader, collapsedDocs);
		}

		// We compute facet
		if (searchRequest.isFacet())
			new FacetListExecutor(searchRequest.getConfig(), reader, notCollapsedDocs, collapsedDocs,
					searchRequest.getFacetFieldList(), facetList, timer);

		// No collapsing
		if (collapsedDocs == null) {
			if (notCollapsedDocs != null)
				setDocs(notCollapsedDocs);
			else
				setDocs(docSetHits.getCollector(DocIdInterface.class));
		} else {
			// With collapsing
			setDocs(collapsedDocs);
			// Update joinResults
			if (joinResults != null)
				for (JoinResult joinResult : joinResults)
					joinResult.setJoinDocInterface(collapsedDocs.getCollector(JoinDocInterface.class));
		}

		if (joinResults != null)
			setJoinResults(joinResults);

		maxScore = request.isScoreRequired() ? docSetHits.getMaxScore() : 0;

		fieldNameSet = new LinkedHashSet<String>();
		searchRequest.getReturnFieldList().populate(fieldNameSet);
		searchRequest.getSnippetFieldList().populate(fieldNameSet);

		int rows = docs.getSize() - request.getStart();
		rows = Math.min(rows, request.getRows());
		if (rows <= 0) {
			resultDocuments = null;
			return;
		}
		resultDocuments = new ResultDocument[rows];
		int pos = request.getStart();
		for (int i = 0; i < rows; i++)
			resultDocuments[i] = getLazyDocument(pos++, timer);
	}

	/**
	 * Returns the searcher used to build the result.
	 * 
	 * @return Searcher
	 */
	@Override
	public ReaderAbstract getReader() {
		return reader;
	}

	/**
	 * 
	 * @return DocSetHits.
	 */
	public DocSetHits getDocSetHits() {
		return this.docSetHits;
	}

	@Override
	public ResultDocument getDocument(int pos, final Timer timer) throws SearchLibException {
		pos = pos - request.getStart();
		if (resultDocuments == null || pos < 0 || pos >= resultDocuments.length)
			return null;
		return resultDocuments[pos];
	}

	public ResultDocument getLazyDocument(final int pos, final Timer timer) throws SearchLibException {
		if (docs == null || pos < 0 || pos >= docs.getSize())
			return null;
		try {
			int docId = docs.getIds()[pos];
			float score = scores != null ? scores.getScores()[pos] : 0;
			if (!(docs instanceof CollapseDocInterface)) {
				return new ResultDocument(request, fieldNameSet, docId, reader, score, null, 0, timer);
			}
			int[] collapsedDocs = ((CollapseDocInterface) docs).getCollapsedDocs(pos);
			ResultDocument resultDocument = new ResultDocument(request, fieldNameSet, docId, reader, score, null,
					collapsedDocs == null ? 0 : collapsedDocs.length, timer);
			Collection<CollapseFunctionField> functionFields = request.getCollapseFunctionFields();
			if (functionFields != null && collapsedDocs != null)
				for (CollapseFunctionField functionField : functionFields)
					resultDocument.addFunctionField(functionField, reader, pos, timer);
			if (request.getCollapseMax() > 0)
				return resultDocument;
			if (collapsedDocs != null) {
				for (int doc : collapsedDocs) {
					ResultDocument rd = new ResultDocument(request, fieldNameSet, doc, reader, 0, null, 0, timer);
					resultDocument.addCollapsedDocument(rd);
					for (String field : fieldNameSet) {
						FieldValue fieldValue = resultDocument.getReturnFields().get(field);
						if (fieldValue != null && fieldValue.getValuesCount() > 0)
							continue;
						resultDocument.addReturnedFields(rd.getReturnFields().get(field));
					}
				}
			}
			return resultDocument;
		} catch (IOException e) {
			throw new SearchLibException(e);
		} catch (ParseException e) {
			throw new SearchLibException(e);
		} catch (SyntaxError e) {
			throw new SearchLibException(e);
		} catch (java.text.ParseException e) {
			throw new SearchLibException(e);
		} catch (InstantiationException e) {
			throw new SearchLibException(e);
		} catch (IllegalAccessException e) {
			throw new SearchLibException(e);
		}
	}

	@Override
	public void populate(List<IndexDocumentResult> indexDocuments) throws IOException, SearchLibException {
		throw new SearchLibException("Method not available");
	}

}
