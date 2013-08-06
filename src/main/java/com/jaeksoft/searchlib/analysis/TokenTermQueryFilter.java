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
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.TermQuery;

import com.jaeksoft.searchlib.analysis.filter.AbstractTermFilter;

public class TokenTermQueryFilter extends AbstractTermFilter {

	private final String field;

	public final List<TermQuery> queryList;

	protected TokenTermQueryFilter(String field, TokenStream input) {
		super(input);
		this.field = field;
		this.queryList = new ArrayList<TermQuery>();
	}

	@Override
	public final boolean incrementToken() throws IOException {
		current = captureState();
		while (input.incrementToken())
			queryList.add(new TermQuery(new Term(field, termAtt.toString())));
		return false;
	}
}
