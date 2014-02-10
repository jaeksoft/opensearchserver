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

package com.jaeksoft.searchlib.sort;

import com.jaeksoft.searchlib.result.collector.CollectorInterface;
import com.jaeksoft.searchlib.result.collector.JoinScoreInterface;

public abstract class AbstractJoinScoreSorter extends SorterAbstract {

	final protected float[][] foreignScoresArray;
	final protected int joinPosition;
	final protected int pos1null;
	final protected int pos2null;

	protected AbstractJoinScoreSorter(final CollectorInterface collector,
			final int joinPosition, final String name, final boolean nullFirst)
			throws NoCollectorException {
		super(collector);
		JoinScoreInterface joinScoreCollector = collector
				.getCollector(JoinScoreInterface.class);
		if (joinScoreCollector == null)
			throw new NoCollectorException("Wrong collector ", collector);
		this.joinPosition = joinPosition;
		foreignScoresArray = joinScoreCollector.getForeignDocScoreArray();
		pos1null = nullFirst ? -1 : 1;
		pos2null = nullFirst ? 1 : -1;
	}

	@Override
	final public boolean isScore() {
		return true;
	}

	@Override
	final public boolean isDistance() {
		return false;
	}

	@Override
	public String toString(final int pos) {
		StringBuilder sb = new StringBuilder("StringIndex: ");
		sb.append(foreignScoresArray[pos][joinPosition]);
		return sb.toString();
	}

}
