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

package com.jaeksoft.searchlib.result.collector.collapsing;

import org.apache.commons.lang.ArrayUtils;

import com.jaeksoft.searchlib.result.collector.AbstractBaseCollector;
import com.jaeksoft.searchlib.result.collector.AbstractExtendsCollector;
import com.jaeksoft.searchlib.result.collector.DistanceInterface;
import com.jaeksoft.searchlib.util.array.FloatBufferedArray;

public class CollapseDistanceCollector
		extends
		AbstractExtendsCollector<CollapseCollectorInterface, CollapseBaseCollector>
		implements CollapseCollectorInterface, DistanceInterface {

	final private float[] sourceDistances;
	final private FloatBufferedArray distanceCollector;

	private float[] distances;
	final private float maxDistance;
	private int currentPos;

	public CollapseDistanceCollector(final CollapseBaseCollector base,
			final DistanceInterface distanceInterface,
			final boolean collectDocArray) {
		super(base);
		this.sourceDistances = distanceInterface.getDistances();
		distanceCollector = new FloatBufferedArray(this.sourceDistances.length);
		maxDistance = 0;
		currentPos = 0;
		distances = null;
	}

	private CollapseDistanceCollector(final CollapseBaseCollector base,
			CollapseDistanceCollector src) {
		super(base);
		this.sourceDistances = null;
		this.distanceCollector = null;

		this.distances = ArrayUtils.clone(src.distances);
		this.maxDistance = src.maxDistance;
		this.currentPos = src.currentPos;
	}

	@Override
	public CollapseDistanceCollector duplicate(AbstractBaseCollector<?> base) {
		parent.duplicate(base);
		return new CollapseDistanceCollector((CollapseBaseCollector) base, this);
	}

	@Override
	final public int collectDoc(final int sourcePos) {
		int pos = parent.collectDoc(sourcePos);
		if (pos != currentPos)
			throw new RuntimeException("Internal position issue: " + pos
					+ " - " + currentPos);
		currentPos++;
		float dist = sourceDistances[sourcePos];
		distanceCollector.add(dist);
		return pos;
	}

	@Override
	final public void collectCollapsedDoc(final int sourcePos,
			final int collapsePos) {
		parent.collectCollapsedDoc(sourcePos, collapsePos);
	}

	@Override
	final public void endCollection() {
		parent.endCollection();
		distances = distanceCollector.getFinalArray();
	}

	@Override
	final public void swap(final int pos1, final int pos2) {
		parent.swap(pos1, pos2);
		float dist = distances[pos1];
		distances[pos1] = distances[pos2];
		distances[pos2] = dist;
	}

	@Override
	final public float getMaxDistance() {
		return maxDistance;
	}

	@Override
	final public float[] getDistances() {
		return distances;
	}

	@Override
	final public int getSize() {
		if (distances == null)
			return 0;
		return distances.length;
	}

}
