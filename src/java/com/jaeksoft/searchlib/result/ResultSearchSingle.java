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

import java.io.IOException;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.facet.FacetField;
import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.index.DocSetHits;
import com.jaeksoft.searchlib.index.ReaderLocal;
import com.jaeksoft.searchlib.join.JoinList;
import com.jaeksoft.searchlib.join.JoinResult;
import com.jaeksoft.searchlib.query.ParseException;
import com.jaeksoft.searchlib.request.SearchRequest;
import com.jaeksoft.searchlib.util.Timer;

public class ResultSearchSingle extends AbstractResultSearch {

	transient private ReaderLocal reader;
	transient private DocSetHits docSetHits;

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
	public ResultSearchSingle(ReaderLocal reader, SearchRequest searchRequest)
			throws IOException, ParseException, SyntaxError,
			SearchLibException, InstantiationException, IllegalAccessException,
			ClassNotFoundException {
		super(searchRequest);

		Timer timer = request.getTimer();
		this.reader = reader;
		docSetHits = reader.searchDocSet(searchRequest, timer);
		numFound = docSetHits.getDocNumFound();
		maxScore = docSetHits.getMaxScore();

		ResultScoreDoc[] notCollapsedDocs = null;
		ResultScoreDoc[] collapsedDocs = null;

		JoinResult[] joinResults = null;

		// Are we doing join
		if (searchRequest.isJoin()) {
			Timer joinTimer = new Timer(timer, "join");
			JoinList joinList = searchRequest.getJoinList();
			joinResults = new JoinResult[joinList.size()];
			Timer t = new Timer(joinTimer, "join - apply");
			notCollapsedDocs = joinList.apply(reader,
					docSetHits.getAllDocs(joinTimer), joinResults, t);
			t.duration();
			t = new Timer(joinTimer, "join - sort");
			searchRequest.getSortList().getSorter(reader, timer)
					.sort(notCollapsedDocs, timer);
			t.duration();
			numFound = notCollapsedDocs.length;
			joinTimer.duration();
		}

		// Are we doing collapsing ?
		if (collapse != null) {
			collapsedDocs = collapse.collapse(reader, notCollapsedDocs,
					docSetHits, timer);
			collapsedDocCount = collapse.getDocCount();
		}

		// We compute facet
		if (searchRequest.isFacet()) {
			Timer facetTimer = new Timer(timer, "facet");
			for (FacetField facetField : searchRequest.getFacetFieldList()) {
				Timer t = new Timer(facetTimer, "facet - "
						+ facetField.getName() + '(' + facetField.getMinCount()
						+ ')');
				this.facetList.add(facetField.getFacet(reader, docSetHits,
						notCollapsedDocs, collapsedDocs, timer));
				t.duration();
			}
			facetTimer.duration();
		}

		// No collapsing
		if (collapsedDocs == null) {
			if (notCollapsedDocs != null)
				setDocs(notCollapsedDocs);
			else
				setDocs(docSetHits.getPriorityDocs(request.getEnd(), timer));
		} else
			setDocs(collapsedDocs);

		if (joinResults != null)
			setJoinResults(joinResults);

	}

	/**
	 * Returns the searcher used to build the result.
	 * 
	 * @return Searcher
	 */
	public ReaderLocal getReader() {
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
	public ResultDocument getDocument(int pos, Timer timer)
			throws SearchLibException {
		if (docs == null || pos < 0 || pos > docs.length)
			return null;
		try {
			ResultScoreDoc rsc = docs[pos];
			ResultDocument resultDocument = new ResultDocument(request,
					rsc.doc, reader, timer);
			if (!(rsc instanceof ResultScoreDocCollapse))
				return resultDocument;
			if (request.getCollapseMax() > 0)
				return resultDocument;
			ResultScoreDoc[] rsds = ((ResultScoreDocCollapse) rsc)
					.getCollapsedDocs();
			for (ResultScoreDoc rsd : rsds) {
				ResultDocument rd = new ResultDocument(request, rsd.doc,
						reader, timer);
				resultDocument.appendIfStringDoesNotExist(rd);
			}
			return resultDocument;
		} catch (IOException e) {
			throw new SearchLibException(e);
		} catch (ParseException e) {
			throw new SearchLibException(e);
		} catch (SyntaxError e) {
			throw new SearchLibException(e);
		}
	}
}
