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

package com.jaeksoft.searchlib.scoring;

import java.text.DecimalFormat;
import java.text.ParseException;

import org.apache.lucene.search.function.DocValues;

import com.jaeksoft.searchlib.index.FieldCacheIndex;

public class DecimalDocValue extends DocValues {

	private final FieldCacheIndex stringIndex;
	private final DecimalFormat decimalFormat;

	public DecimalDocValue(final FieldCacheIndex stringIndex,
			final DecimalFormat decimalFormat) {
		this.stringIndex = stringIndex;
		this.decimalFormat = decimalFormat;
	}

	@Override
	final public float floatVal(final int doc) {
		try {
			String s = stringIndex.lookup[stringIndex.order[doc]];
			if (s == null)
				return 0;
			return decimalFormat.parse(s).floatValue();
		} catch (ParseException e) {
			return 0;
		}
	}

	@Override
	final public String toString(final int doc) {
		StringBuilder sb = new StringBuilder("decimal(");
		sb.append(floatVal(doc));
		sb.append(')');
		return sb.toString();
	}

}
