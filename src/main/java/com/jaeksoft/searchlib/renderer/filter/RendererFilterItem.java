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

package com.jaeksoft.searchlib.renderer.filter;

import java.util.Collection;

public class RendererFilterItem {

	private final String queryName;

	private final String query;

	private final Collection<String> terms;

	private final String label;

	RendererFilterItem(Collection<String> terms, String label) {
		this.queryName = null;
		this.query = null;
		this.terms = terms;
		this.label = label;
	}

	RendererFilterItem(String queryName, String query, String label) {
		this.queryName = queryName;
		this.query = query;
		this.label = label;
		this.terms = null;
	}

	/**
	 * @return the terms
	 */
	public Collection<String> getTerms() {
		return terms;
	}

	/**
	 * @return the label
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * @return the label
	 */
	public String getQuery() {
		return query;
	}

	public boolean isQuery() {
		return query != null;
	}

	public boolean isTerms() {
		return terms != null;
	}

	/**
	 * @return the queryName
	 */
	public String getQueryName() {
		return queryName;
	}

}
