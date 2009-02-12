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

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Iterator;
import java.util.TreeSet;

import org.w3c.dom.NamedNodeMap;

public abstract class FragmenterAbstract implements Externalizable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1443188121132664991L;

	private transient TreeSet<Integer> splitPos;

	private transient int originalTextLength;

	public FragmenterAbstract() {
		splitPos = null;
	}

	protected abstract void setAttributes(NamedNodeMap attr);

	protected void addSplit(int pos) {
		if (pos >= originalTextLength)
			return;
		if (pos == 0)
			return;
		splitPos.add(pos);
	}

	final protected void getFragments(String originalText,
			FragmentList fragments, int vectorOffset) {
		originalTextLength = originalText.length();
		if (splitPos == null)
			splitPos = new TreeSet<Integer>();
		splitPos.clear();
		check(originalText);
		Iterator<Integer> splitIterator = splitPos.iterator();
		int pos = 0;
		Fragment lastFragment = null;
		while (splitIterator.hasNext()) {
			int nextSplitPos = splitIterator.next();
			lastFragment = fragments.addOriginalText(originalText.substring(
					pos, nextSplitPos), vectorOffset, lastFragment == null);
			pos = nextSplitPos;
		}
		if (pos < originalText.length())
			lastFragment = fragments.addOriginalText(originalText
					.substring(pos), vectorOffset, lastFragment == null);
		lastFragment.setEdge(true);
	}

	final static protected FragmenterAbstract newInstance(String className)
			throws InstantiationException, IllegalAccessException,
			ClassNotFoundException {
		if (className == null || className.length() == 0)
			className = "NoFragmenter";
		FragmenterAbstract fragmenter = (FragmenterAbstract) Class.forName(
				"com.jaeksoft.searchlib.highlight." + className).newInstance();
		return fragmenter;
	}

	protected abstract void check(String originalText);

	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {
	}

	public void writeExternal(ObjectOutput out) throws IOException {
	}

}
