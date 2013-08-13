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
package com.jaeksoft.searchlib.webservice.query.search;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.jaeksoft.searchlib.request.SearchFieldRequest;

@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
@JsonInclude(Include.NON_NULL)
public class SearchFieldQuery extends SearchQueryAbstract {

	final public List<SearchField> searchFields;

	public SearchFieldQuery() {
		searchFields = null;
	}

	@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
	@JsonInclude(Include.NON_NULL)
	public static class SearchField {

		final public String field;
		final public Boolean phrase;
		final public Float boost;

		public SearchField() {
			field = null;
			phrase = null;
			boost = null;
		}

		public SearchField(
				com.jaeksoft.searchlib.request.SearchField searchField) {
			field = searchField.getField();
			phrase = searchField.isPhrase();
			boost = searchField.getBoost();
		}

		@JsonIgnore
		protected com.jaeksoft.searchlib.request.SearchField newSearchField() {
			return new com.jaeksoft.searchlib.request.SearchField(field,
					phrase, boost);
		}
	}

	private static List<SearchField> newSearchFields(
			Collection<com.jaeksoft.searchlib.request.SearchField> searchFieldCollection) {
		if (searchFieldCollection == null)
			return null;
		if (searchFieldCollection.size() == 0)
			return null;
		List<SearchField> searchFields = new ArrayList<SearchField>(
				searchFieldCollection.size());
		for (com.jaeksoft.searchlib.request.SearchField searchField : searchFieldCollection)
			searchFields.add(new SearchField(searchField));
		return searchFields;
	}

	public SearchFieldQuery(SearchFieldRequest request) {
		super(request);
		searchFields = newSearchFields(request.getSearchFields());
	}

	public void apply(SearchFieldRequest request) {
		super.apply(request);
		if (searchFields != null)
			for (SearchField searchField : searchFields)
				request.add(searchField.newSearchField());

	}
}
