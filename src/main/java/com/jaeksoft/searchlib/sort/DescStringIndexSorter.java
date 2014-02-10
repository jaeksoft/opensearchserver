/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2012-2014 Keller / Jaeksoft
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

package com.jaeksoft.searchlib.sort;

import com.jaeksoft.searchlib.index.FieldCacheIndex;
import com.jaeksoft.searchlib.result.collector.CollectorInterface;

public class DescStringIndexSorter extends AbstractStringIndexSorter {

	public DescStringIndexSorter(final CollectorInterface collector,
			final FieldCacheIndex stringIndex, final boolean nullFirst)
			throws NoCollectorException {
		super(collector, stringIndex, nullFirst);
	}

	@Override
	final public int compare(final int pos1, final int pos2) {
		int ord1 = stringIndex.order[ids[pos1]];
		int ord2 = stringIndex.order[ids[pos2]];
		if (ord1 == 0)
			return ord2 == 0 ? 0 : pos1null;
		if (ord2 == 0)
			return pos2null;
		return ord2 - ord1;
	}
}
