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

import org.w3c.dom.NamedNodeMap;

public class SizeFragmenter extends FragmenterAbstract {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private int maxFragmentSize;

	/**
	 * 
	 * @param fragmenter
	 */
	private SizeFragmenter(SizeFragmenter fragmenter) {
		maxFragmentSize = fragmenter.maxFragmentSize;
	}

	public FragmenterAbstract newInstance() {
		return new SizeFragmenter(this);
	}

	public void check(String originalText) {
		int pos = 0;
		int len = originalText.length();
		while ((len - pos) > maxFragmentSize) {
			pos += maxFragmentSize;
			addSplit(pos);
		}
	}

	@Override
	protected void setAttributes(NamedNodeMap attr) {
		maxFragmentSize = Integer.parseInt(attr.getNamedItem("maxFragmentSize")
				.getNodeValue());
	}

}
