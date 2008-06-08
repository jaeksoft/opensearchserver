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

import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;

import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.WhitespaceTokenizer;

public abstract class Fragmenter implements
		org.apache.lucene.search.highlight.Fragmenter, Serializable {

	private int fragmentNumber;
	private String separator;
	private int maxSize;

	public Fragmenter(int fragmentNumber, int maxSize, String separator) {
		this.fragmentNumber = fragmentNumber;
		this.maxSize = maxSize;
		this.separator = separator;
	}

	protected Fragmenter(Fragmenter fragmenter) {
		this.fragmentNumber = fragmenter.fragmentNumber;
		this.maxSize = fragmenter.maxSize;
		this.separator = fragmenter.separator;
	}

	public abstract Fragmenter newFragmenter();

	public int getFragmentNumber() {
		return this.fragmentNumber;
	}

	public int getMaxSize() {
		return this.maxSize;
	}

	public String getSeparator() {
		return this.separator;
	}

	public String format(String textFragment, int spaceLeft) throws IOException {
		if (textFragment.length() < spaceLeft)
			return textFragment;
		WhitespaceTokenizer wt = new WhitespaceTokenizer(new StringReader(
				textFragment));
		Token token;
		StringBuffer frag = new StringBuffer();
		int l = 0;
		while ((token = wt.next()) != null) {
			int termLength = token.termLength();
			l += termLength;
			if (l > spaceLeft)
				break;
			if (l != termLength)
				frag.append(" ");
			frag.append(token.termBuffer(), 0, termLength);
		}
		return frag.toString();
	}

	public String getSnippet(String[] fragments) throws IOException {
		if (fragments == null || fragments.length == 0)
			return null;
		int spaceLeft = this.maxSize;
		String hl = null;
		for (String fragment : fragments) {
			if (fragment == null)
				continue;
			String fr = this.format(fragment, spaceLeft);
			if (hl == null)
				hl = fr;
			else
				hl += this.separator + fr;
			if (fr.length() != fragment.length())
				break;
			spaceLeft -= fr.length();
		}
		return hl;
	}
}
