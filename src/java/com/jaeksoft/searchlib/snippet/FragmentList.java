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

import java.util.ArrayList;
import java.util.List;

public class FragmentList {

	private List<Fragment> fragments;

	private Fragment lastAddedFragment;

	private int totalSize;

	protected FragmentList() {
		fragments = new ArrayList<Fragment>();
		lastAddedFragment = null;
		totalSize = 0;
	}

	protected Fragment addOriginalText(String originalText, int vectorOffset,
			boolean newValue) {
		totalSize += originalText.length();
		Fragment fragment = new Fragment(lastAddedFragment, originalText,
				vectorOffset, newValue);
		fragments.add(fragment);
		lastAddedFragment = fragment;
		return fragment;
	}

	protected int getTotalSize() {
		return totalSize;
	}

	final private static String lastRight(String text, int max) {
		int len = text.length();
		if (max >= len)
			return text;
		int pos = len - max;
		while (pos < len)
			if (Character.isWhitespace(text.charAt(pos)))
				break;
			else
				pos++;
		if (pos == len)
			pos = len - max;
		return text.substring(pos);
	}

	final private static String firstLeft(String text, int max) {
		if (max >= text.length())
			return text;
		int pos = max;
		while (pos > 0)
			if (Character.isWhitespace(text.charAt(pos)))
				break;
			else
				pos--;
		if (pos == 0)
			return null;
		return text.substring(0, pos);
	}

	/**
	 * Append the next fragment. Return TRUE is the snippet is not at the
	 * required size. Return FALSE is the snippet is large enough.
	 * 
	 * @param fragment
	 * @param maxLength
	 * @param snippet
	 * @param separator
	 * @return
	 */
	final private static boolean leftAppend(Fragment fragment, int maxLength,
			StringBuffer snippet, String separator, String[] tags) {
		int maxLeft = maxLength - snippet.length();
		if (maxLeft < 0)
			return false; // The snippet is complete
		String text = fragment.getFinalText(tags);
		String appendText = firstLeft(text, maxLeft);
		if (appendText == null)
			return true; // Nothing to append
		if (snippet.length() > 0)
			if (fragment.isEdge())
				snippet.append(separator);
		snippet.append(appendText);
		if (appendText.length() == text.length())
			return true; // We have added all the fragment
		// The fragment has been truncated to fit, the snippet is large enough
		snippet.append(separator);
		return false;
	}

	final private static boolean rightAppend(Fragment fragment, int maxLength,
			StringBuffer snippet, String separator, String[] tags) {
		int maxLeft = maxLength - snippet.length();
		if (maxLeft < 0)
			return false;
		String text = fragment.getFinalText(tags);
		String appendText = lastRight(text, maxLeft);
		if (appendText == null)
			return true;
		if (snippet.length() > 0)
			if (fragment.isEdge())
				snippet.insert(0, separator);
		snippet.insert(0, appendText);
		if (appendText.length() == text.length())
			return true;
		snippet.insert(0, separator);
		return false;
	}

	/**
	 * Build a snippet starting from originalFragment (highlighted or not)
	 * 
	 * @param maxLength
	 * @param separator
	 * @param iterator
	 * @param originalFragment
	 * @return
	 */
	final protected StringBuffer getSnippet(int maxLength, String separator,
			String[] tags, Fragment originalFragment) {

		StringBuffer snippet = new StringBuffer();
		if (!leftAppend(originalFragment, maxLength, snippet, separator, tags))
			return snippet;
		// First add next fragment (highlighted or not)
		Fragment fragment = originalFragment.next();
		while (fragment != null) {
			if (!leftAppend(fragment, maxLength, snippet, separator, tags))
				return snippet;
			fragment = fragment.next();
		}
		// Then previous fragment (highlighted or not)
		fragment = originalFragment.previous();
		while (fragment != null) {
			if (!rightAppend(fragment, maxLength, snippet, separator, tags))
				return snippet;
			fragment = originalFragment.previous();
		}
		return snippet;

	}

	public int size() {
		return fragments.size();
	}

	public Fragment first() {
		if (fragments.size() == 0)
			return null;
		return fragments.get(0);
	}

}
