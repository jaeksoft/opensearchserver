/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2011 Emmanuel Keller / Jaeksoft
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

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.apache.lucene.util.AttributeSource;

public abstract class AbstractTermFilter extends TokenFilter {

	protected AttributeSource.State current = null;

	protected PositionIncrementAttribute posIncrAtt = null;

	protected OffsetAttribute offsetAtt = null;

	protected TermAttribute termAtt = null;

	protected AbstractTermFilter(TokenStream input) {
		super(input);
		termAtt = (TermAttribute) addAttribute(TermAttribute.class);
		posIncrAtt = (PositionIncrementAttribute) addAttribute(PositionIncrementAttribute.class);
		offsetAtt = (OffsetAttribute) addAttribute(OffsetAttribute.class);
	}

	protected final void createToken(String term, int posInc, int startOff,
			int endOff) {
		restoreState(current);
		termAtt.setTermBuffer(term);
		posIncrAtt.setPositionIncrement(posInc);
		offsetAtt.setOffset(startOff, endOff);
	}

	protected final void createToken(String term) {
		createToken(term, posIncrAtt.getPositionIncrement(),
				offsetAtt.startOffset(), offsetAtt.endOffset());
	}

	protected final String getTerm() {
		final char[] buffer = termAtt.termBuffer();
		final int length = termAtt.termLength();
		char[] text = new char[length];
		int i = 0;
		for (char c : buffer)
			text[i++] = c;
		return new String(text);
	}

}
