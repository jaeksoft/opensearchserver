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
import com.jaeksoft.searchlib.result.collector.DistanceInterface;

public abstract class AbstractDistanceSorter extends SorterAbstract {

	final protected float[] distances;

	protected AbstractDistanceSorter(final CollectorInterface collector) {
		super(collector);
		DistanceInterface distanceCollector = collector
				.getCollector(DistanceInterface.class);
		if (distanceCollector == null)
			throw new RuntimeException("Wrong collector " + collector);
		distances = distanceCollector.getDistances();
	}

	@Override
	final public boolean isDistance() {
		return true;
	}

	@Override
	final public boolean isScore() {
		return false;
	}

	@Override
	public String toString(final int pos) {
		StringBuilder sb = new StringBuilder("Distance: ");
		sb.append(distances[pos]);
		return sb.toString();
	}

}
