/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2015 Emmanuel Keller / Jaeksoft
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
package com.jaeksoft.searchlib.analysis.filter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.lucene.analysis.TokenStream;

public abstract class AbstractTermCollectFilter extends AbstractTermFilter {

	private Integer startOffset = null;

	private Integer endOffset = null;

	private final List<String> inputTermList = new ArrayList<String>(0);

	private final LinkedList<String> outputTermList = new LinkedList<String>();

	private final String token_type;

	private boolean tokensCreated = false;

	protected AbstractTermCollectFilter(String token_type, TokenStream input) {
		super(input);
		this.token_type = token_type;
	}

	protected boolean popToken() {
		if (outputTermList == null || outputTermList.isEmpty())
			return false;
		createToken(outputTermList.removeFirst(), 0, startOffset, endOffset,
				token_type, flagsAtt.getFlags());
		return true;
	}

	protected abstract void createTokens(List<String> input, List<String> output);

	@Override
	public boolean incrementToken() throws IOException {
		for (;;) {
			if (popToken())
				return true;
			if (tokensCreated)
				return false;
			while (input.incrementToken()) {
				if (startOffset == null)
					startOffset = offsetAtt.startOffset();
				inputTermList.add(termAtt.toString());
				endOffset = offsetAtt.endOffset();
			}
			createTokens(inputTermList, outputTermList);
			tokensCreated = true;
		}
	}
}
