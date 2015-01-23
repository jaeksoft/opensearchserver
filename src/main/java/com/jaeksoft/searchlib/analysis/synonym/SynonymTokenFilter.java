/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2009-2015 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.analysis.synonym;

import org.apache.lucene.analysis.TokenStream;

import com.jaeksoft.searchlib.analysis.filter.AbstractTermListFilter;

public class SynonymTokenFilter extends AbstractTermListFilter {

	private SynonymMap synonymMap = null;

	private final static String TOKEN_TYPE = "synonym";

	public SynonymTokenFilter(TokenStream input, SynonymMap synonymMap) {
		super(TOKEN_TYPE, input);
		this.synonymMap = synonymMap;
	}

	@Override
	protected final String[] createTokens(String term) {
		return synonymMap.getSynonyms(term);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Create token: " + this);
		return sb.toString();
	}
}
