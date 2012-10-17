/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.snippet;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Iterator;
import java.util.TreeSet;

import org.w3c.dom.NamedNodeMap;

import com.jaeksoft.searchlib.Logging;

public abstract class FragmenterAbstract implements Externalizable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1443188121132664991L;

	private transient TreeSet<Integer> splitPos;

	private transient int originalTextLength;

	protected FragmenterAbstract() {
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
			lastFragment = fragments.addOriginalText(
					originalText.substring(pos, nextSplitPos), vectorOffset,
					lastFragment == null);
			pos = nextSplitPos;
		}
		if (pos < originalText.length())
			lastFragment = fragments.addOriginalText(
					originalText.substring(pos), vectorOffset,
					lastFragment == null);
		if (lastFragment != null)
			lastFragment.setEdge(true);
	}

	protected abstract FragmenterAbstract newInstance();

	final protected static NoFragmenter NOFRAGMENTER = new NoFragmenter();

	final static protected FragmenterAbstract newInstance(String className)
			throws InstantiationException, IllegalAccessException {
		if (className == null || className.length() == 0)
			return NOFRAGMENTER;
		try {
			FragmenterAbstract fragmenter = (FragmenterAbstract) Class.forName(
					"com.jaeksoft.searchlib.snippet." + className)
					.newInstance();
			return fragmenter;
		} catch (ClassNotFoundException e) {
			Logging.warn(e);
			return NOFRAGMENTER;
		}
	}

	protected abstract void check(String originalText);

	@Override
	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
	}

}
