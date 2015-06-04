/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2014 Emmanuel Keller / Jaeksoft
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

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.request.AbstractSearchRequest;
import com.jaeksoft.searchlib.request.RequestTypeEnum;
import com.jaeksoft.searchlib.request.SearchFieldRequest;
import com.jaeksoft.searchlib.request.SearchPatternRequest;
import com.jaeksoft.searchlib.result.AbstractResultSearch;
import com.jaeksoft.searchlib.webservice.query.CommonQuery;

@JsonInclude(Include.NON_NULL)
@XmlAccessorType(XmlAccessType.FIELD)
public class SearchQueryBatch {

	public enum Mode {
		/**
		 * All queries are executed
		 */
		all,

		/**
		 * The batch is stopped when a query found a result
		 */
		first,

		/**
		 * The behavior is managed by the batchAction parameter for each query
		 */
		manual
	}

	final public Mode mode;

	@XmlElements({
			@XmlElement(name = "SearchField", type = SearchFieldQuery.class),
			@XmlElement(name = "SearchPattern", type = SearchPatternQuery.class),
			@XmlElement(name = "SearchFieldTemplate", type = SearchFieldTemplateQuery.class),
			@XmlElement(name = "SearchPatternTemplate", type = SearchPatternTemplateQuery.class) })
	@JsonTypeInfo(use = Id.NAME, property = "type")
	@JsonSubTypes({
			@JsonSubTypes.Type(value = SearchFieldQuery.class, name = "SearchField"),
			@JsonSubTypes.Type(value = SearchPatternQuery.class, name = "SearchPattern"),
			@JsonSubTypes.Type(value = SearchFieldTemplateQuery.class, name = "SearchFieldTemplate"),
			@JsonSubTypes.Type(value = SearchPatternTemplateQuery.class, name = "SearchPatternTemplate") })
	final public List<SearchQueryAbstract> queries;

	public SearchQueryBatch() {
		queries = null;
		mode = Mode.all;
	}

	@XmlAccessorType(XmlAccessType.FIELD)
	@JsonInclude(Include.NON_NULL)
	public static class SearchFieldTemplateQuery extends SearchFieldQuery {

		public final String template;

		public SearchFieldTemplateQuery() {
			template = null;
		}

		public AbstractSearchRequest getNewRequest(Client client)
				throws SearchLibException {
			return (AbstractSearchRequest) CommonQuery.getNewRequest(client,
					template, RequestTypeEnum.SearchFieldRequest);
		}
	}

	@XmlAccessorType(XmlAccessType.FIELD)
	@JsonInclude(Include.NON_NULL)
	public static class SearchPatternTemplateQuery extends SearchPatternQuery {

		public final String template;

		public SearchPatternTemplateQuery() {
			template = null;
		}

		public AbstractSearchRequest getNewRequest(Client client)
				throws SearchLibException {
			return (AbstractSearchRequest) CommonQuery.getNewRequest(client,
					template, RequestTypeEnum.SearchRequest);
		}
	}

	public List<SearchResult> result(Client client) throws SearchLibException {
		if (queries == null)
			return null;
		List<SearchResult> searchResults = new ArrayList<SearchResult>(
				queries.size());
		for (SearchQueryAbstract query : queries) {
			AbstractSearchRequest searchRequest = null;
			if (query instanceof SearchFieldTemplateQuery)
				searchRequest = ((SearchFieldTemplateQuery) query)
						.getNewRequest(client);
			else if (query instanceof SearchPatternTemplateQuery)
				searchRequest = ((SearchPatternTemplateQuery) query)
						.getNewRequest(client);
			else if (query instanceof SearchFieldQuery)
				searchRequest = new SearchFieldRequest(client);
			else if (query instanceof SearchPatternQuery)
				searchRequest = new SearchPatternRequest(client);
			query.apply(searchRequest);
			SearchResult searchResult = new SearchResult(
					(AbstractResultSearch<?>) client.request(searchRequest));
			searchResults.add(searchResult);
			if (mode != null) {
				switch (mode) {
				case all:
					break;
				case first:
					if (searchResult.numFound > 0)
						return searchResults;
					break;
				case manual:
					if (query.batchAction != null) {
						switch (query.batchAction) {
						case CONTINUE:
							break;
						case STOP_IF_FOUND:
							if (searchResult.numFound > 0)
								return searchResults;
							break;
						}
					}
					break;
				}
			}
		}
		return searchResults;
	}
}
