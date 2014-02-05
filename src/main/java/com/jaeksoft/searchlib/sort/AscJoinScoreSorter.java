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

import java.io.IOException;

import com.jaeksoft.searchlib.result.collector.CollectorInterface;

public class AscJoinScoreSorter extends AbstractJoinScoreSorter {

	public AscJoinScoreSorter(final CollectorInterface collector,
			final int joinPosition, final String name, final boolean nullFirst)
			throws IOException {
		super(collector, joinPosition, name, nullFirst);
	}

	@Override
	final public int compare(final int pos1, final int pos2) {
		float[] joinScores1 = foreignScoresArray[pos1];
		float[] joinScores2 = foreignScoresArray[pos2];
		if (joinScores1 == null)
			return joinScores2 == null ? 0 : pos1null;
		if (joinScores2 == null)
			return pos2null;
		float s1 = joinScores1[joinPosition];
		float s2 = joinScores2[joinPosition];
		if (s1 > s2)
			return 1;
		else if (s1 < s2)
			return -1;
		else
			return 0;
	}
}
