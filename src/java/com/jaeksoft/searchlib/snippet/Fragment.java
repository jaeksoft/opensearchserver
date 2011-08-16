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

public class Fragment {

	private String originalText;

	private String highlightedText;

	private boolean inSnippet;

	protected int vectorOffset;

	private boolean isEdge;

	protected Fragment(String originalText, int vectorOffset, boolean isEdge) {
		this.originalText = originalText;
		this.highlightedText = null;
		this.inSnippet = false;
		this.vectorOffset = vectorOffset;
		this.isEdge = isEdge;
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

	protected String getFinalText() {
		inSnippet = true;
		if (highlightedText != null)
			return highlightedText;
		return originalText;
	}

	protected String getFinalText(int maxLength) {
		String text = getFinalText();
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

}
