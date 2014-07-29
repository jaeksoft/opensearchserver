/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2011-2014 Emmanuel Keller / Jaeksoft
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
import javax.xml.bind.annotation.XmlTransient;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.jaeksoft.searchlib.request.AbstractRequest;
import com.jaeksoft.searchlib.request.SearchFieldRequest;

@XmlAccessorType(XmlAccessType.FIELD)
@JsonInclude(Include.NON_NULL)
public class SearchFieldQuery extends SearchQueryAbstract {

	final public List<SearchField> searchFields;

	public SearchFieldQuery() {
		searchFields = null;
	}

	@XmlAccessorType(XmlAccessType.FIELD)
	@JsonInclude(Include.NON_NULL)
	public static class SearchField {

		public static enum Mode {
			PATTERN("Pattern"), TERM("Term"), PHRASE("Phrase"), TERM_AND_PHRASE(
					"Term & phrase");

			@XmlTransient
			private final String label;

			private Mode(String label) {
				this.label = label;
			}

			public final static Mode find(String text) {
				for (Mode mode : values())
					if (mode.name().equalsIgnoreCase(text))
						return mode;
				return Mode.PATTERN;
			}

			@XmlTransient
			public final String getLabel() {
				return label;
			}
		}

		final public String field;
		final public Mode mode;
		final public Double boost;
		final public Double phraseBoost;
		final public Integer phraseSlop;

		public SearchField() {
			field = null;
			mode = null;
			boost = null;
			phraseBoost = null;
			phraseSlop = null;
		}

		public SearchField(
				com.jaeksoft.searchlib.request.SearchField searchField) {
			field = searchField.getField();
			mode = searchField.getMode();
			boost = searchField.getTermBoost();
			phraseBoost = searchField.getPhraseBoost();
			phraseSlop = searchField.getPhraseSlop();
		}

		@JsonIgnore
		protected com.jaeksoft.searchlib.request.SearchField newSearchField() {
			return new com.jaeksoft.searchlib.request.SearchField(field, mode,
					boost, phraseBoost, phraseSlop);
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

	@Override
	public void apply(AbstractRequest request) {
		super.apply(request);
		SearchFieldRequest fieldRequest = (SearchFieldRequest) request;
		if (searchFields != null)
			for (SearchField searchField : searchFields)
				fieldRequest.add(searchField.newSearchField());

	}
}
