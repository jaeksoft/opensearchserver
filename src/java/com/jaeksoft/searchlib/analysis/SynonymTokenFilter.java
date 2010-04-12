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

	private SynonymAttribute synonymAtt;

	protected SynonymTokenFilter(TokenStream input, SynonymMap synonymMap) {
		super(input);
		this.termAtt = (TermAttribute) addAttribute(TermAttribute.class);
		this.synonymAtt = (SynonymAttribute) addAttribute(SynonymAttribute.class);
		this.synonymAtt.checkSynonymQueue(synonymMap);
	}

	private final boolean createToken(String term) {
		if (term == null)
			return false;
		restoreState(current);
		termAtt.setTermBuffer(term);
		return true;
	}

	@Override
	public final boolean incrementToken() throws IOException {
		current = captureState();
		SynonymQueues queues = synonymAtt.getSynonymQueues();
		for (;;) {
			String insertTerm = queues.getNextInsertTerm();
			if (insertTerm != null)
				return createToken(insertTerm);
			if (!input.incrementToken())
				return createToken(queues.popToken());
			queues.addToken(termAtt.term());
			restoreState(current);
			StringBuffer synonymKey = new StringBuffer();
			SynonymQueue queue = queues.isSynonym(synonymKey);
			if (queue != null) {
				queues.setInsertTerm(queue, synonymKey.toString());
				continue;
			}
			if (queues.isFull())
				return createToken(queues.popToken());
		}
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("Create token: " + this);
		return sb.toString();
	}
}
