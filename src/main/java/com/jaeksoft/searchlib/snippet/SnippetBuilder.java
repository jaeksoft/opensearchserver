/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2013 Emmanuel Keller / Jaeksoft
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
import java.util.Collection;
import java.util.List;

public class SnippetBuilder {

	private final StringBuilder snippet;

	private final List<Fragment> fragments;

	public SnippetBuilder(final int maxLength, final String separator,
			final String[] tags, final Fragment originalFragment) {
		snippet = new StringBuilder();
		fragments = new ArrayList<Fragment>(0);
		if (!leftAppend(originalFragment, maxLength, separator, tags))
			return;
		// First add next fragment (highlighted or not)
		Fragment fragment = originalFragment.next();
		while (fragment != null) {
			if (!leftAppend(fragment, maxLength, separator, tags))
				return;
			fragment = fragment.next();
		}
		// Then previous fragment (highlighted or not)
		fragment = originalFragment.previous();
		while (fragment != null) {
			if (!rightAppend(fragment, maxLength, separator, tags))
				return;
			fragment = fragment.previous();
		}
	}

	final private static String lastRight(final String text, final int max) {
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

	final private static String firstLeft(final String text, final int max) {
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
	final private boolean leftAppend(final Fragment fragment,
			final int maxLength, final String separator, final String[] tags) {
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
		fragments.add(fragment);
		if (appendText.length() == text.length())
			return true; // We have added all the fragment
		// The fragment has been truncated to fit, the snippet is large enough
		snippet.append(separator);
		return false;
	}

	final private boolean rightAppend(final Fragment fragment,
			final int maxLength, final String separator, final String[] tags) {
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
		fragments.add(fragment);
		if (appendText.length() == text.length())
			return true;
		snippet.insert(0, separator);
		return false;
	}

	final public int length() {
		return snippet.length();
	}

	@Override
	final public String toString() {
		return snippet.toString();
	}

	final public Collection<Fragment> getFragments() {
		return fragments;
	}
}
