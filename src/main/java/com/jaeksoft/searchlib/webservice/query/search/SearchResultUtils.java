/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2011-2015 Emmanuel Keller / Jaeksoft
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
package com.jaeksoft.searchlib.webservice.query.search;

import java.io.IOException;

import org.icepdf.core.tag.query.DocumentResult;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.facet.FacetField;
import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.query.ParseException;
import com.jaeksoft.searchlib.request.AbstractSearchRequest;
import com.jaeksoft.searchlib.result.AbstractResultSearch;
import com.jaeksoft.searchlib.webservice.CommonServices;
import com.opensearchserver.client.v2.search.FacetResult2;
import com.opensearchserver.client.v2.search.SearchResult2;

public class SearchResultUtils {

	public SearchResult2 getSearchResult(AbstractResultSearch result) {
		try {
			SearchResult2 searchResult = new SearchResult2();
			AbstractSearchRequest searchRequest = result.getRequest();
			searchResult.setQuery(searchRequest.getQueryParsed());
			searchResult.setStart(searchRequest.getStart());
			searchResult.setRows(searchRequest.getRows());
			searchResult.setNumFound(result.getNumFound());
			searchResult.setCollapsedDocCount(result.getCollapsedDocCount());
			searchResult.setTime(result.getTimer().tempDuration());
			searchResult.setMaxScore(result.getMaxScore());

			DocumentResult.populateDocumentList(result, documents);

			if (searchRequest.getFacetFieldList().size() > 0)
				for (FacetField FacetField : searchRequest.getFacetFieldList())
					searchResult.addFact(new FacetResult2(result, FacetField
							.getName()));

		} catch (ParseException e) {
			throw new CommonServices.CommonServiceException(e);
		} catch (SyntaxError e) {
			throw new CommonServices.CommonServiceException(e);
		} catch (SearchLibException e) {
			throw new CommonServices.CommonServiceException(e);
		} catch (IOException e) {
			throw new CommonServices.CommonServiceException(e);
		}
	}
}
