/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2012-2014 Emmanuel Keller / Jaeksoft
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

import java.io.IOException;

import com.jaeksoft.searchlib.index.FieldCacheIndex;
import com.jaeksoft.searchlib.result.collector.CollectorInterface;
import com.jaeksoft.searchlib.result.collector.JoinDocInterface;

public abstract class AbstractJoinStringIndexSorter extends AbstractDocIdSorter {

	final protected FieldCacheIndex stringIndex;
	final protected int[] foreignDocIds;

	public AbstractJoinStringIndexSorter(final CollectorInterface collector,
			final int joinPosition) throws IOException {
		super(collector);
		JoinDocInterface joinDocCollector = collector
				.getCollector(JoinDocInterface.class);
		if (joinDocCollector == null)
			throw new IOException("Unable to apply sort on non-joined query");
		stringIndex = joinDocCollector.getFieldCacheIndexArray()[joinPosition];
		foreignDocIds = joinDocCollector.getForeignDocIdsArray()[joinPosition];
	}

	@Override
	public String toString(final int pos) {
		StringBuilder sb = new StringBuilder("StringIndex: ");
		sb.append(stringIndex.lookup[stringIndex.order[foreignDocIds[pos]]]);
		return sb.toString();
	}
}
