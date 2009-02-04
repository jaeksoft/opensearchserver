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

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

public class FragmentList {

	private List<Fragment> fragments;

	protected FragmentList() {
		fragments = new ArrayList<Fragment>();
	}

	protected void addOriginalText(String originalText) {
		fragments.add(new Fragment(originalText));
	}

	protected ListIterator<Fragment> iterator() {
		return fragments.listIterator();
	}

	private Fragment findFirstHighlight(ListIterator<Fragment> iterator) {
		while (iterator.hasNext()) {
			Fragment fragment = iterator.next();
			if (fragment.isHighlighted())
				return fragment;
		}
		return null;
	}

	final private static String firstLeft(String text, int max) {
		if (max > text.length())
			return text;
		int pos = max;
		while (pos-- > 0) {
			if (Character.isWhitespace(text.charAt(pos)))
				break;
		}
		if (pos == 0)
			pos = max;
		return text.substring(0, pos);
	}

	final private static boolean leftAppend(Fragment fragment, int maxLength,
			StringBuffer snippet, String separator) {
		int maxLeft = maxLength - snippet.length();
		String text = fragment.getFinalText();
		String appendText = firstLeft(text, maxLeft);
		snippet.append(appendText);
		if (appendText.length() == text.length())
			return true;
		snippet.append(separator);
		return false;
	}

	final private static boolean rightAppend(Fragment fragment, int maxLength,
			StringBuffer snippet, String separator) {
		return false;
	}

	final protected String getSnippet(int maxLength, String separator) {
		ListIterator<Fragment> iterator = iterator();
		// Find first highlighted fragment
		Fragment fragment = findFirstHighlight(iterator);
		if (fragment == null) {
			// Or taking fist fragment
			iterator = iterator();
			fragment = iterator.next();
			if (fragment == null)
				return null;
		}
		StringBuffer snippet = new StringBuffer();
		if (!leftAppend(fragment, maxLength, snippet, separator))
			return snippet.toString();
		// First add next fragment (highlighted or not)
		while (iterator.hasNext())
			if (!leftAppend(iterator.next(), maxLength, snippet, separator))
				return snippet.toString();
		// Then previous fragment (highlighted or not)
		while (iterator.hasPrevious())
			if (!rightAppend(iterator.previous(), maxLength, snippet, separator))
				return snippet.toString();
		return snippet.toString();
	}
}
