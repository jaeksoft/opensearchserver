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

import org.apache.commons.lang3.ArrayUtils;

import com.jaeksoft.searchlib.result.collector.AbstractBaseCollector;
import com.jaeksoft.searchlib.result.collector.AbstractExtendsCollector;
import com.jaeksoft.searchlib.result.collector.CollapseDistanceInterface;
import com.jaeksoft.searchlib.result.collector.DistanceInterface;
import com.jaeksoft.searchlib.util.array.FloatBufferedArrayFactory;
import com.jaeksoft.searchlib.util.array.FloatBufferedArrayInterface;

public class CollapseDistanceCollector
		extends
		AbstractExtendsCollector<CollapseCollectorInterface, CollapseBaseCollector>
		implements CollapseCollectorInterface, CollapseDistanceInterface {

	final private float[] sourceDistances;
	final private FloatBufferedArrayInterface distanceCollector;

	private float[] distances;
	private float[][] collapsedDistances;
	final private float maxDistance;
	final private float minDistance;
	private int currentPos;

	public CollapseDistanceCollector(final CollapseBaseCollector base,
			final DistanceInterface distanceInterface) {
		super(base);
		this.sourceDistances = distanceInterface.getDistances();
		distanceCollector = FloatBufferedArrayFactory.INSTANCE
				.newInstance(this.sourceDistances.length);
		maxDistance = 0;
		minDistance = Float.MAX_VALUE;
		currentPos = 0;
		distances = null;
		collapsedDistances = new float[base.getIds().length][];
	}

	private CollapseDistanceCollector(final CollapseBaseCollector base,
			CollapseDistanceCollector src) {
		super(base);
		this.sourceDistances = null;
		this.distanceCollector = null;

		this.distances = ArrayUtils.clone(src.distances);
		this.maxDistance = src.maxDistance;
		this.minDistance = src.minDistance;
		this.currentPos = src.currentPos;

		this.collapsedDistances = new float[src.collapsedDistances.length][];
		int i = 0;
		for (float[] collDistanceArray : src.collapsedDistances)
			this.collapsedDistances[i++] = ArrayUtils.clone(collDistanceArray);
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
		if (collapsedDistances == null)
			return;
		if (collapsedDistances[collapsePos] == null)
			collapsedDistances[collapsePos] = new float[] { sourceDistances[sourcePos] };
		else
			collapsedDistances[collapsePos] = ArrayUtils
					.add(collapsedDistances[collapsePos],
							sourceDistances[sourcePos]);

	}

	@Override
	final public void endCollection() {
		parent.endCollection();
		distances = distanceCollector.getFinalArray();
	}

	@Override
	final public void doSwap(final int pos1, final int pos2) {
		parent.doSwap(pos1, pos2);
		float dist = distances[pos1];
		distances[pos1] = distances[pos2];
		distances[pos2] = dist;
		if (collapsedDistances != null) {
			float[] colArray = collapsedDistances[pos1];
			collapsedDistances[pos1] = collapsedDistances[pos2];
			collapsedDistances[pos2] = colArray;
		}
	}

	@Override
	final public float getMaxDistance() {
		return maxDistance;
	}

	@Override
	final public float getMinDistance() {
		return minDistance;
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

	@Override
	public float[] getCollapsedDistances(int pos) {
		if (collapsedDistances == null)
			return null;
		return collapsedDistances[pos];
	}

}
