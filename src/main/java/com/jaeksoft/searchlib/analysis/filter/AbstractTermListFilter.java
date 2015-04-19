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

import org.apache.lucene.analysis.TokenStream;

public abstract class AbstractTermListFilter extends AbstractTermFilter {

	protected String[] wordQueue = null;

	private String currentTerm = null;

	private int currentPos = 0;

	private final String token_type;

	protected AbstractTermListFilter(String token_type, TokenStream input) {
		super(input);
		this.token_type = token_type;
	}

	protected boolean popToken() {
		if (currentTerm != null) {
			createToken(currentTerm);
			currentTerm = null;
			return true;
		}
		if (wordQueue == null)
			return false;
		if (currentPos == wordQueue.length)
			return false;
		createToken(wordQueue[currentPos++], 0, offsetAtt.startOffset(),
				offsetAtt.endOffset(), token_type, flagsAtt.getFlags());
		return true;
	}

	protected abstract String[] createTokens(String term);

	@Override
	public boolean incrementToken() throws IOException {
		for (;;) {
			if (popToken())
				return true;
			if (!input.incrementToken())
				return false;
			currentTerm = termAtt.toString();
			wordQueue = createTokens(currentTerm);
			currentPos = 0;
		}
	}
}
