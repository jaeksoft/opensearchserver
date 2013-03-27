/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008-2009 Emmanuel Keller / Jaeksoft
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

import com.jaeksoft.searchlib.result.collector.DocIdInterface;
import com.jaeksoft.searchlib.result.collector.ScoreDocInterface;

public abstract class AbstractScoreSorter extends SorterAbstract {

	protected float[] scores;

	protected AbstractScoreSorter(DocIdInterface collector) {
		super(collector);
		if (collector instanceof ScoreDocInterface)
			scores = ((ScoreDocInterface) collector).getScores();
		else
			throw new RuntimeException("Wrong collector " + collector);
	}

	@Override
	public boolean isScore() {
		return true;
	}

	@Override
	final public String toString(int pos) {
		StringBuffer sb = new StringBuffer("Score: ");
		sb.append(scores[pos]);
		return sb.toString();
	}

}
