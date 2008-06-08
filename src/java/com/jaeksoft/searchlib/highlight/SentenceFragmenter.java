/**   
 * License Agreement for Jaeksoft SearchLib Community
 *
 * Copyright (C) 2008 Emmanuel Keller / Jaeksoft
 * 
 * http://www.jaeksoft.com
 * 
 * This file is part of Jaeksoft SearchLib Community.
 *
 * Jaeksoft SearchLib Community is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * Jaeksoft SearchLib Community is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Jaeksoft SearchLib Community. 
 *  If not, see <http://www.gnu.org/licenses/>.
 **/

package com.jaeksoft.searchlib.highlight;

import org.apache.lucene.analysis.Token;

public class SentenceFragmenter extends Fragmenter {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4962131354275204301L;

	private int currentFragmentSize;
	private boolean bNextIsNew;

	public SentenceFragmenter(int fragmentNumber, int maxSize, String separator) {
		super(fragmentNumber, maxSize, separator);
	}

	private SentenceFragmenter(SentenceFragmenter fragmenter) {
		super(fragmenter);
	}

	@Override
	public Fragmenter newFragmenter() {
		return new SentenceFragmenter(this);
	}

	public boolean isNewFragment(Token token) {
		boolean bIsNew = false;
		char[] termBuffer = token.termBuffer();
		int termLength = token.termLength();
		if ((Character.isUpperCase(termBuffer[0])) || this.bNextIsNew) {
			this.currentFragmentSize = termLength;
			bIsNew = true;
		} else
			this.currentFragmentSize += termLength;
		this.bNextIsNew = false;
		switch (termBuffer[termLength - 1]) {
		case '.':
		case '?':
		case '!':
			this.bNextIsNew = true;
			break;
		}
		return bIsNew;
	}

	public void start(String originalText) {
		this.currentFragmentSize = 0;
	}

}
