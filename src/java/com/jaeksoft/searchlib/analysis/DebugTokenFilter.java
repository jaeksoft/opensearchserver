/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2010 Emmanuel Keller / Jaeksoft
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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.apache.lucene.util.AttributeSource;

public class DebugTokenFilter extends org.apache.lucene.analysis.TokenFilter {

	private ClassFactory classFactory;
	private TermAttribute termAtt;
	private PositionIncrementAttribute posIncrAtt;
	private OffsetAttribute offsetAtt;
	private List<String> termList;
	private List<AttributeSource.State> stateList;
	private Iterator<AttributeSource.State> cacheIterator;

	protected DebugTokenFilter(ClassFactory classFactory, TokenStream input) {
		super(input);
		this.classFactory = classFactory;
		termList = new ArrayList<String>();
		stateList = new ArrayList<AttributeSource.State>();
		cacheIterator = null;
		this.termAtt = (TermAttribute) addAttribute(TermAttribute.class);
		this.posIncrAtt = (PositionIncrementAttribute) addAttribute(PositionIncrementAttribute.class);
		this.offsetAtt = (OffsetAttribute) addAttribute(OffsetAttribute.class);
	}

	@Override
	public final boolean incrementToken() throws IOException {
		if (cacheIterator == null) {
			while (input.incrementToken()) {
				stateList.add(captureState());
				termList.add(termAtt.term() + " [" + offsetAtt.startOffset()
						+ ',' + offsetAtt.endOffset() + " - "
						+ posIncrAtt.getPositionIncrement() + ']');
			}
			cacheIterator = stateList.iterator();
			return false;
		}
		if (cacheIterator.hasNext()) {
			restoreState((AttributeSource.State) cacheIterator.next());
			return true;
		}
		return false;
	}

	@Override
	public void reset() throws IOException {
		super.reset();
		cacheIterator = stateList.iterator();
	}

	public List<String> getTokenList() {
		return termList;
	}

	public ClassFactory getClassFactory() {
		return classFactory;
	}
}
