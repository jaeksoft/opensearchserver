/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2012 Emmanuel Keller / Jaeksoft
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
import java.util.Set;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;

public class TermSetTokenFilter extends org.apache.lucene.analysis.TokenFilter {

	private TermAttribute termAtt;
	private Set<String> termSet;

	protected TermSetTokenFilter(Set<String> termSet, TokenStream input) {
		super(input);
		this.termSet = termSet;
		this.termAtt = (TermAttribute) addAttribute(TermAttribute.class);
	}

	@Override
	public final boolean incrementToken() throws IOException {
		while (input.incrementToken())
			termSet.add(termAtt.term());
		return false;
	}

}
