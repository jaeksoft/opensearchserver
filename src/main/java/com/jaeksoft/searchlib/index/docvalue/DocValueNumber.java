/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2014 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.index.docvalue;

import java.text.NumberFormat;
import java.text.ParseException;

import com.jaeksoft.searchlib.index.FieldCacheIndex;

public abstract class DocValueNumber extends DocValueStringIndex {

	private final NumberFormat numberFormat;

	protected DocValueNumber(final FieldCacheIndex stringIndex,
			final NumberFormat numberFormat) {
		super(stringIndex);
		this.numberFormat = numberFormat;
	}

	@Override
	final public float getFloat(final int doc) {
		try {
			String s = stringIndex.lookup[stringIndex.order[doc]];
			if (s == null)
				return 0;
			return numberFormat.parse(s).floatValue();
		} catch (ParseException e) {
			return 0;
		}
	}
}
