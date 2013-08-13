/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2013 Emmanuel Keller / Jaeksoft
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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.jaeksoft.searchlib.request.AbstractSearchRequest;
import com.jaeksoft.searchlib.request.SearchPatternRequest;

@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
@JsonInclude(Include.NON_NULL)
public class SearchPatternQuery extends SearchQueryAbstract {

	public String patternSearchQuery;
	public String patternSnippetQuery;

	public SearchPatternQuery() {
		patternSearchQuery = null;
		patternSnippetQuery = null;
	}

	public SearchPatternQuery(SearchPatternRequest request) {
		super(request);
		patternSearchQuery = request.getPatternQuery();
		patternSnippetQuery = request.getSnippetPatternQuery();
	}

	@Override
	public void apply(AbstractSearchRequest request) {
		super.apply(request);
		SearchPatternRequest patternRequest = (SearchPatternRequest) request;
		if (patternSearchQuery != null)
			patternRequest.setPatternQuery(patternSearchQuery);
		if (patternSnippetQuery != null)
			patternRequest.setSnippetPatternQuery(patternSnippetQuery);
	}

}
