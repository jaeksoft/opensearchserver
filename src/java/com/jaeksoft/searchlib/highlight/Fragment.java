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

public class Fragment {

	private String originalText;

	private String highlightedText;

	protected Fragment(String originalText) {
		this.originalText = originalText;
		this.highlightedText = null;
	}

	protected boolean isHighlighted() {
		return highlightedText != null;
	}

	protected void setHighlightedText(String highlightedText) {
		this.highlightedText = highlightedText;
	}

	protected String getOriginalText() {
		return originalText;
	}

	protected String getFinalText() {
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
