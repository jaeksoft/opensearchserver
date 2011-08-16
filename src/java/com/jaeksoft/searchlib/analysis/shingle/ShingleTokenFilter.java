/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2010 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.analysis.shingle;

import java.io.IOException;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.apache.lucene.util.AttributeSource;

public class ShingleTokenFilter extends TokenFilter {

	private TermAttribute termAtt;

	private AttributeSource.State current = null;

	private PositionIncrementAttribute posIncrAtt = null;

	private OffsetAttribute offsetAtt = null;

	private ShingleQueue[] shingles;

	public ShingleTokenFilter(TokenStream tokenStream, String tokenSeparator,
			int minShingleSize, int maxShingleSize) {
		super(tokenStream);
		this.termAtt = (TermAttribute) addAttribute(TermAttribute.class);
		this.posIncrAtt = (PositionIncrementAttribute) addAttribute(PositionIncrementAttribute.class);
		this.offsetAtt = (OffsetAttribute) addAttribute(OffsetAttribute.class);
		shingles = new ShingleQueue[maxShingleSize - minShingleSize + 1];
		for (int i = 0; i < shingles.length; i++)
			shingles[i] = new ShingleQueue(tokenSeparator, maxShingleSize - i);
	}

	private ShingleQueue isToken() {
		for (ShingleQueue shingle : shingles)
			if (shingle.isFull())
				return shingle;
		return null;
	}

	private final boolean createToken(String term, int posInc, int startOff,
			int endOff) {
		restoreState(current);
		termAtt.setTermBuffer(term);
		posIncrAtt.setPositionIncrement(posInc);
		offsetAtt.setOffset(startOff, endOff);
		return true;
	}

	private final boolean createToken(ShingleQueue shingle) {
		if (!createToken(shingle.getTerm(), shingle.getPositionIncrement(),
				shingle.getStartOffset(), shingle.getEndOffset()))
			return false;
		shingle.pop();
		return true;
	}

	private final void addToken(ShingleToken shingleToken) {
		for (ShingleQueue shingle : shingles)
			shingle.addToken(shingleToken);
	}

	@Override
	public final boolean incrementToken() throws IOException {
		current = captureState();
		for (;;) {
			ShingleQueue shingle = isToken();
			if (shingle != null)
				return createToken(shingle);
			if (!input.incrementToken())
				return false;
			addToken(new ShingleToken(termAtt.term(),
					posIncrAtt.getPositionIncrement(), offsetAtt.startOffset(),
					offsetAtt.endOffset()));
		}
	}

}
