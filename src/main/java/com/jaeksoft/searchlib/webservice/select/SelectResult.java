/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2011-2013 Emmanuel Keller / Jaeksoft
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
package com.jaeksoft.searchlib.webservice.select;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.ws.WebServiceException;

import org.json.JSONException;
import org.json.JSONObject;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.facet.FacetField;
import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.query.ParseException;
import com.jaeksoft.searchlib.request.AbstractSearchRequest;
import com.jaeksoft.searchlib.result.AbstractResultSearch;
import com.jaeksoft.searchlib.result.ResultDocument;
import com.jaeksoft.searchlib.webservice.CommonResult;

@XmlRootElement(name = "result")
@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
public class SelectResult extends CommonResult {

	@XmlElement(name = "document")
	public List<DocumentResult> documents;

	@XmlElement(name = "facet")
	public List<FacetResult> facets;

	@XmlElement
	public String query;

	@XmlAttribute
	public int rows;

	@XmlAttribute
	public int start;

	@XmlAttribute
	public int numFound;

	@XmlAttribute
	public long time;

	@XmlAttribute
	public long collapsedDocCount;

	@XmlAttribute
	public float maxScore;

	public SelectResult() {
		documents = null;
		query = null;
		rows = 0;
		start = 0;
		numFound = 0;
		time = 0;
		collapsedDocCount = 0;
		maxScore = 0;
	}

	public SelectResult(JSONObject json) throws JSONException {
		JSONObject jsonResult = json.getJSONObject("result");
		numFound = jsonResult.getInt("@numFound");
		documents = new ArrayList<DocumentResult>(0);
		facets = new ArrayList<FacetResult>(0);
		DocumentResult.add(jsonResult.optJSONObject("document"), documents);
		DocumentResult.add(jsonResult.optJSONArray("document"), documents);
		FacetResult.add(jsonResult.optJSONObject("facet"), facets);
		FacetResult.add(jsonResult.optJSONArray("facet"), facets);
	}

	public SelectResult(AbstractResultSearch result) {
		super(true, null);
		try {
			AbstractSearchRequest searchRequest = result.getRequest();
			documents = new ArrayList<DocumentResult>();
			facets = new ArrayList<FacetResult>();
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
				documents.add(documentResult);

			}

			if (searchRequest.getFacetFieldList().size() > 0)
				for (FacetField FacetField : searchRequest.getFacetFieldList())
					facets.add(new FacetResult(result, FacetField.getName()));

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

	@XmlTransient
	public int getNumFound() {
		return numFound;
	}

	@XmlTransient
	public List<DocumentResult> getDocuments() {
		return documents;
	}

	@XmlTransient
	public List<FacetResult> getFacets() {
		return facets;
	}

	@XmlTransient
	public String getQuery() {
		return query;
	}

	@XmlTransient
	public int getRows() {
		return rows;
	}

	@XmlTransient
	public int getStart() {
		return start;
	}

	@XmlTransient
	public long getTime() {
		return time;
	}

	@XmlTransient
	public long getCollapsedDocCount() {
		return collapsedDocCount;
	}

	@XmlTransient
	public float getMaxScore() {
		return maxScore;
	}

}
