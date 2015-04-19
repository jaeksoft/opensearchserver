/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008-2014 Emmanuel Keller / Jaeksoft
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

import org.apache.lucene.index.memory.MemoryIndex;
import org.apache.lucene.search.Query;

import com.jaeksoft.searchlib.analysis.CompiledAnalyzer;
import com.jaeksoft.searchlib.util.StringUtils;

public class Fragment {

	private String originalText;

	private String highlightedText;

	private String finalText;

	private boolean inSnippet;

	protected int vectorOffset;

	private boolean isEdge;

	private Fragment previousFragment;

	private Fragment nextFragment;

	private double searchScore;

	private double score;

	protected Fragment(Fragment previousFragment, String originalText,
			int vectorOffset, boolean isEdge) {
		this.originalText = originalText;
		this.highlightedText = null;
		this.finalText = null;
		this.inSnippet = false;
		this.vectorOffset = vectorOffset;
		this.isEdge = isEdge;
		this.previousFragment = previousFragment;
		this.nextFragment = null;
		if (previousFragment != null)
			previousFragment.nextFragment = this;
	}

	protected boolean isHighlighted() {
		return highlightedText != null;
	}

	/**
	 * True if getFinalText or getOriginalText has been called
	 * 
	 * @return
	 */
	protected boolean isInSnippet() {
		return inSnippet;
	}

	/**
	 * True if this fragment is the first fragment of a new value (multivalue
	 * suport)
	 * 
	 * @return
	 */
	protected boolean isEdge() {
		return isEdge;
	}

	public void setEdge(boolean isEdge) {
		this.isEdge = isEdge;
	}

	protected void setHighlightedText(String highlightedText) {
		this.highlightedText = highlightedText;
	}

	protected String getOriginalText() {
		return originalText;
	}

	protected String getFinalText(String[] allowedTags) {
		inSnippet = true;
		if (finalText != null)
			return finalText;
		if (highlightedText != null && searchScore > 0) {
			finalText = StringUtils.removeTag(highlightedText, allowedTags);
		} else
			finalText = originalText;
		return finalText;
	}

	protected String getFinalText(String[] allowedTags, int maxLength) {
		String text = getFinalText(allowedTags);
		if (maxLength > text.length())
			return text;
		int pos = maxLength;
		while (pos-- > 0)
			if (Character.isWhitespace(text.indexOf(pos)))
				break;
		if (pos == 0)
			pos = maxLength;
		return text.substring(0, pos);
	}

	public final double searchScore(final String fieldName,
			final CompiledAnalyzer analyzer, final Query query) {
		searchScore = 0;
		if (query == null || analyzer == null)
			return 0;
		MemoryIndex index = new MemoryIndex();
		index.addField(fieldName, originalText, analyzer);
		searchScore = index.search(query);
		return searchScore;
	}

	private final void finalizeScore(final double maxSearchScore,
			final int maxLength) {
		double sizeScore = (originalText.length() < maxLength) ? (double) originalText
				.length() / (double) maxLength
				: 1;
		score = (searchScore / maxSearchScore) * sizeScore;
	}

	/**
	 * Returns the fragment which have the higher score. If the fragments has
	 * the same score, the fragment1 is returned.
	 * 
	 * @param fragment1
	 * @param fragment2
	 * @return
	 */
	public static final Fragment bestScore(Fragment fragment1,
			Fragment fragment2, final double maxSearchScore, final int maxLength) {
		if (fragment2 == null)
			return fragment1;
		fragment2.finalizeScore(maxSearchScore, maxLength);
		if (fragment1 == null)
			return fragment2;
		return fragment2.score > fragment1.score ? fragment2 : fragment1;
	}

	public final Fragment next() {
		return nextFragment;
	}

	public final Fragment previous() {
		return previousFragment;
	}

	/**
	 * Find the next highlighted fragment
	 * 
	 * @param iterator
	 * @return
	 */
	final static protected Fragment findNextHighlightedFragment(
			Fragment fragment) {
		while (fragment != null) {
			if (fragment.isHighlighted() && !fragment.isInSnippet())
				return fragment;
			fragment = fragment.next();
		}
		return null;
	}

	/**
	 * Remove the Fragment from the chained list (updating previous and next)
	 */
	public void removeFromList() {
		if (previousFragment != null)
			previousFragment.nextFragment = nextFragment;
		if (nextFragment != null)
			nextFragment.previousFragment = previousFragment;
	}

}
