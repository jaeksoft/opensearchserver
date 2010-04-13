/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2009-2010 Emmanuel Keller / Jaeksoft
 * 
 * http://www.open-search-server.com
 * 
 * This file is part of Jaeksoft OpenSearchServer.
 *
 * Jaeksoft OpenSearchServer is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * Jaeksoft OpenSearchServer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Jaeksoft OpenSearchServer. 
 *  If not, see <http://www.gnu.org/licenses/>.
 **/

package com.jaeksoft.searchlib.analysis;

import java.io.IOException;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.apache.lucene.util.AttributeSource;

public class SynonymTokenFilter extends TokenFilter {

	private TermAttribute termAtt;

	private AttributeSource.State current = null;

	private SynonymQueue synonymQueue;

	protected SynonymTokenFilter(TokenStream input, SynonymQueue synonymQueue) {
		super(input);
		this.termAtt = (TermAttribute) addAttribute(TermAttribute.class);
		this.synonymQueue = synonymQueue;
	}

	private final boolean createToken(String token) {
		if (token == null)
			return false;
		restoreState(current);
		termAtt.setTermBuffer(token);
		return true;
	}

	@Override
	public final boolean incrementToken() throws IOException {
		current = captureState();
		for (;;) {
			if (!input.incrementToken())
				return createToken(synonymQueue.popToken());
			synonymQueue.addToken(termAtt.term());
			restoreState(current);
			String synonymKey = synonymQueue.findSynonym();
			if (synonymKey != null) {
				synonymQueue.clean();
				return createToken(synonymKey);
			}
			if (synonymQueue.isFull())
				return createToken(synonymQueue.popToken());
		}
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("Create token: " + this);
		return sb.toString();
	}
}
