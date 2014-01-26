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

import com.jaeksoft.searchlib.result.collector.CollectorInterface;
import com.jaeksoft.searchlib.result.collector.JoinDocInterface;
import com.jaeksoft.searchlib.util.StringUtils;

public abstract class AbstractJoinStringIndexSorter extends
		AbstractStringIndexSorter {

	final protected int[][] foreignDocIdsArray;
	final protected int joinPosition;

	public AbstractJoinStringIndexSorter(final CollectorInterface collector,
			final int joinPosition, final String name, final boolean nullFirst)
			throws IOException {
		super(collector, null, nullFirst);
		JoinDocInterface joinDocCollector = collector
				.getCollector(JoinDocInterface.class);
		if (joinDocCollector == null)
			throw new IOException("Unable to apply sort on non-joined query");
		this.joinPosition = joinPosition;
		stringIndex = joinDocCollector.getForeignReaders()[joinPosition]
				.getStringIndex(name);
		if (stringIndex == null)
			throw new IOException(StringUtils.fastConcat(
					"No string index found for the foreign field: ", name));
		foreignDocIdsArray = joinDocCollector.getForeignDocIdsArray();
	}

	@Override
	public String toString(final int pos) {
		StringBuilder sb = new StringBuilder("StringIndex: ");
		sb.append(stringIndex.lookup[stringIndex.order[foreignDocIdsArray[pos][joinPosition]]]);
		return sb.toString();
	}
}
