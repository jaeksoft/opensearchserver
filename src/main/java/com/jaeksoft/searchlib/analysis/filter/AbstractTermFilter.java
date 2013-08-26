/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2011-2013 Emmanuel Keller / Jaeksoft
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
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;

import com.jaeksoft.searchlib.analysis.TokenTerm;

public abstract class AbstractTermFilter extends TokenFilter {

	protected PositionIncrementAttribute posIncrAtt = null;

	protected OffsetAttribute offsetAtt = null;

	protected CharTermAttribute termAtt = null;

	protected TypeAttribute typeAtt = null;

	protected AbstractTermFilter(TokenStream input) {
		super(input);
		termAtt = (CharTermAttribute) addAttribute(CharTermAttribute.class);
		posIncrAtt = (PositionIncrementAttribute) addAttribute(PositionIncrementAttribute.class);
		offsetAtt = (OffsetAttribute) addAttribute(OffsetAttribute.class);
		typeAtt = (TypeAttribute) addAttribute(TypeAttribute.class);
	}

	protected final boolean createToken(String term, int posInc, int startOff,
			int endOff, String type) {
		if (term == null)
			return false;
		if (term.length() == 0)
			return false;
		termAtt.setEmpty();
		termAtt.append(term);
		posIncrAtt.setPositionIncrement(posInc);
		offsetAtt.setOffset(startOff, endOff);
		typeAtt.setType(type);
		return true;
	}

	protected final boolean createToken(String term) {
		return createToken(term, posIncrAtt.getPositionIncrement(),
				offsetAtt.startOffset(), offsetAtt.endOffset(), typeAtt.type());
	}

	protected final boolean createToken(TokenTerm tokenTerm) {
		return createToken(tokenTerm.term, tokenTerm.increment,
				tokenTerm.start, tokenTerm.end, tokenTerm.type);

	}

}
