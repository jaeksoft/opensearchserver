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

import java.util.Collection;

public class TokenTerm {

	public final String term;
	public final int start;
	public final int end;
	public final int increment;

	public TokenTerm(String term, int start, int end, int increment) {
		this.term = term;
		this.start = start;
		this.end = end;
		this.increment = increment;
	}

	public TokenTerm(String term, TokenTerm tt) {
		this.term = term;
		this.start = tt.start;
		this.end = tt.end;
		this.increment = tt.increment;
	}

	/**
	 * Merge many token together
	 * 
	 * @param tokenTerms
	 */
	public TokenTerm(Collection<TokenTerm> tokenTerms) {
		int start = 0;
		int end = 0;
		int increment = 0;
		for (TokenTerm tokenTerm : tokenTerms) {
			increment += tokenTerm.increment;
			if (tokenTerm.start < start)
				start = tokenTerm.start;
			if (tokenTerm.end > end)
				end = tokenTerm.end;
		}
		this.term = null;
		this.start = start;
		this.end = end;
		this.increment = increment;
	}

}
