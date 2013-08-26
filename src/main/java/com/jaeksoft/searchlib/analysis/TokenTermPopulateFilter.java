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

package com.jaeksoft.searchlib.analysis;

import java.io.IOException;
import java.util.Collection;

import org.apache.lucene.analysis.TokenStream;

import com.jaeksoft.searchlib.analysis.filter.AbstractTermFilter;

public class TokenTermPopulateFilter extends AbstractTermFilter {

	private final Collection<TokenTerm> tokenTerms;

	protected TokenTermPopulateFilter(Collection<TokenTerm> tokenTerms,
			TokenStream input) {
		super(input);
		this.tokenTerms = tokenTerms;
	}

	@Override
	public final boolean incrementToken() throws IOException {
		if (!input.incrementToken())
			return false;
		tokenTerms.add(new TokenTerm(termAtt, posIncrAtt, offsetAtt, typeAtt));
		return true;
	}

}
