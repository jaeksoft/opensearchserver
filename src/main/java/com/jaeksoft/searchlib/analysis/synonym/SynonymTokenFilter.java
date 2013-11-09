/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2009-2012 Emmanuel Keller / Jaeksoft
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

import java.io.IOException;

import org.apache.lucene.analysis.TokenStream;

import com.jaeksoft.searchlib.analysis.filter.AbstractTermFilter;

public class SynonymTokenFilter extends AbstractTermFilter {

	private SynonymMap synonymMap = null;

	private String[] wordQueue = null;

	private String currentTerm = null;

	private int currentPos = 0;

	private final static String TYPE = "synonym";

	public SynonymTokenFilter(TokenStream input, SynonymMap synonymMap) {
		super(input);
		this.synonymMap = synonymMap;
	}

	private final boolean popToken() {
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
				offsetAtt.endOffset(), TYPE, flagsAtt.getFlags());
		return true;
	}

	private final void createTokens() {
		currentTerm = termAtt.toString();
		wordQueue = synonymMap.getSynonyms(currentTerm);
		currentPos = 0;
	}

	@Override
	public final boolean incrementToken() throws IOException {
		for (;;) {
			if (popToken())
				return true;
			if (!input.incrementToken())
				return false;
			createTokens();
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Create token: " + this);
		return sb.toString();
	}
}
