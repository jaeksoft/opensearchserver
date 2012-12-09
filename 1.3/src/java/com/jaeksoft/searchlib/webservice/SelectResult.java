/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2011-2012 Emmanuel Keller / Jaeksoft
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
package com.jaeksoft.searchlib.webservice;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.ws.WebServiceException;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.facet.FacetField;
import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.query.ParseException;
import com.jaeksoft.searchlib.request.SearchRequest;
import com.jaeksoft.searchlib.result.AbstractResultSearch;
import com.jaeksoft.searchlib.result.ResultDocument;

@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
public class SelectResult {

	@XmlElement(name = "documentResult")
	public List<DocumentResult> documentResults;

	@XmlElement(name = "spellCheck")
	public List<SpellChecking> spellCheck;

	@XmlElement(name = "facet")
	public List<FacetResult> facet;

	@XmlElement(name = "query")
	public String query;

	@XmlElement(name = "rows")
	public int rows;

	@XmlElement(name = "start")
	public int start;

	@XmlElement(name = "numFound")
	public int numFound;

	@XmlElement(name = "time")
	public long time;

	@XmlElement(name = "collapsedDocCount")
	public long collapsedDocCount;

	@XmlElement(name = "maxScore")
	public float maxScore;

	public SelectResult() {
		documentResults = null;
		query = null;
		rows = 0;
		start = 0;
		numFound = 0;
		time = 0;
		collapsedDocCount = 0;
		maxScore = 0;

	}

	public SelectResult(AbstractResultSearch result) {
		try {
			SearchRequest searchRequest = result.getRequest();
			documentResults = new ArrayList<DocumentResult>();
			spellCheck = new ArrayList<SpellChecking>();
			facet = new ArrayList<FacetResult>();
			query = searchRequest.getQueryParsed();
			start = searchRequest.getStart();
			rows = searchRequest.getRows();
			numFound = result.getNumFound();
			collapsedDocCount = result.getCollapsedDocCount();
			time = result.getTimer().tempDuration();
			maxScore = result.getMaxScore();
			int end = result.getDocumentCount() + searchRequest.getStart();
			for (int i = start; i < end; i++) {
				ResultDocument resultDocument = result.getDocument(i);
				int collapseDocCount = result.getCollapseCount(i);
				float docScore = result.getScore(i);
				DocumentResult documentResult = new DocumentResult(
						resultDocument, collapseDocCount, i, docScore);
				documentResults.add(documentResult);

			}

			// TODO Add Spellcheck result
			// if (searchRequest.getSpellCheckFieldList().size() > 0)
			// spellCheck.add(new SpellChecking(result, searchRequest
			// .getQueryString()));

			if (searchRequest.getFacetFieldList().size() > 0)
				for (FacetField FacetField : searchRequest.getFacetFieldList())
					facet.add(new FacetResult(result, FacetField.getName()));

		} catch (ParseException e) {
			throw new WebServiceException(e);
		} catch (SyntaxError e) {
			throw new WebServiceException(e);
		} catch (SearchLibException e) {
			throw new WebServiceException(e);
		} catch (IOException e) {
			throw new WebServiceException(e);
		}
	}
}
