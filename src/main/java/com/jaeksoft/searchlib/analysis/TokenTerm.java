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

import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.FlagsAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;

public class TokenTerm {

	public final String term;
	public final int start;
	public final int end;
	public final int increment;
	public final String type;
	public final int flags;

	public TokenTerm(String term, int start, int end, int increment,
			String type, int flags) {
		this.term = term;
		this.start = start;
		this.end = end;
		this.increment = increment;
		this.type = type;
		this.flags = flags;
	}

	public TokenTerm(String term, TokenTerm tt, String type, int flags) {
		this.term = term;
		this.start = tt.start;
		this.end = tt.end;
		this.increment = tt.increment;
		this.type = type;
		this.flags = flags;
	}

	/**
	 * Merge many token together
	 * 
	 * @param tokenTerms
	 */
	public TokenTerm(final Collection<TokenTerm> tokenTerms) {
		int start = Integer.MAX_VALUE;
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
		this.type = null;
		this.start = start;
		this.end = end;
		this.increment = increment;
		this.flags = 0;
	}

	public TokenTerm(final CharTermAttribute termAtt,
			final PositionIncrementAttribute posIncrAtt,
			final OffsetAttribute offsetAtt, final TypeAttribute typeAtt,
			final FlagsAttribute flagsAtt) {
		this.term = termAtt != null ? termAtt.toString() : null;
		this.start = offsetAtt != null ? offsetAtt.startOffset() : 0;
		this.end = offsetAtt != null ? offsetAtt.endOffset() : 0;
		this.increment = posIncrAtt != null ? posIncrAtt.getPositionIncrement()
				: 0;
		this.type = typeAtt != null ? typeAtt.type() : null;
		this.flags = flagsAtt != null ? flagsAtt.getFlags() : null;
	}
}
