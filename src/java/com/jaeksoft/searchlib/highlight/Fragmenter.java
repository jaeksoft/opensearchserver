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

import java.io.Serializable;

import org.apache.lucene.analysis.Token;

public class Fragmenter implements
		org.apache.lucene.search.highlight.Fragmenter, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private int fragmentNumber;
	private String separator;
	private int maxFragmentSize;

	protected int currentFragmentSize;

	/**
	 * 
	 * @param fragmentNumber
	 * @param separator
	 */
	public Fragmenter(int fragmentNumber, String separator, int maxFragmentSize) {
		this.fragmentNumber = fragmentNumber;
		this.separator = separator;
		this.currentFragmentSize = 0;
		this.maxFragmentSize = maxFragmentSize;
	}

	/**
	 * 
	 * @param fragmenter
	 */
	protected Fragmenter(Fragmenter fragmenter) {
		this.fragmentNumber = fragmenter.fragmentNumber;
		this.separator = fragmenter.separator;
		this.currentFragmentSize = 0;
		this.maxFragmentSize = fragmenter.maxFragmentSize;
	}

	public Fragmenter newFragmenter() {
		return new Fragmenter(this);
	}

	public int getFragmentNumber() {
		return this.fragmentNumber;
	}

	public String getSeparator() {
		return this.separator;
	}

	@Override
	public boolean isNewFragment(Token token) {
		int l = token.termLength();
		currentFragmentSize += l;
		if (currentFragmentSize < maxFragmentSize)
			return false;
		currentFragmentSize = l;
		return true;
	}

	@Override
	public void start(String originalText) {
		currentFragmentSize = 0;
	}

}
