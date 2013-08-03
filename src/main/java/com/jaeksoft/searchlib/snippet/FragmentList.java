/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008-2012 Emmanuel Keller / Jaeksoft
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

import java.util.Collection;

public class FragmentList {

	private Fragment firstFragment;

	private Fragment lastFragment;

	private int totalSize;

	private int size;

	protected FragmentList() {
		firstFragment = null;
		lastFragment = null;
		totalSize = 0;
		size = 0;
	}

	protected Fragment addOriginalText(String originalText, int vectorOffset,
			boolean newValue) {
		totalSize += originalText.length();
		lastFragment = new Fragment(lastFragment, originalText, vectorOffset,
				newValue);
		if (firstFragment == null)
			firstFragment = lastFragment;
		size++;
		return lastFragment;
	}

	protected int getTotalSize() {
		return totalSize;
	}

	public int size() {
		return size;
	}

	public Fragment first() {
		return firstFragment;
	}

	private void remove(Fragment fragment) {
		if (fragment == firstFragment) {
			firstFragment = fragment.next();
			return;
		}
		if (fragment == lastFragment)
			lastFragment = fragment.previous();
		fragment.removeFromList();
	}

	public void remove(Collection<Fragment> fragments) {
		for (Fragment fragment : fragments)
			remove(fragment);
	}

}
