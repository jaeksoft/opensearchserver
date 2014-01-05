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

package com.jaeksoft.searchlib.result.collector;

import java.io.IOException;
import java.text.ParseException;

import com.jaeksoft.searchlib.geo.GeoDistance;
import com.jaeksoft.searchlib.geo.GeoParameters;
import com.jaeksoft.searchlib.index.ReaderAbstract;
import com.jaeksoft.searchlib.util.array.FloatBufferedArray;

public class DistanceCollector extends AbstractDocSetHitCollector {

	private final FloatBufferedArray distancesBuffer;

	private float[] distances;

	private final GeoDistance geoDistance;

	public DistanceCollector(final DocSetHitCollector base,
			final ReaderAbstract reader, final GeoParameters geoParams)
			throws IOException {
		super(base);
		this.distancesBuffer = new FloatBufferedArray(base.getMaxDoc());
		this.geoDistance = geoParams.getGeoDistance(reader, null);
		distances = null;
	}

	@Override
	final public void collectDoc(int doc) throws IOException {
		parent.collectDoc(doc);
		if (geoDistance != null)
			try {
				distancesBuffer.add((float) geoDistance.getDistance(doc));
			} catch (ParseException e) {
				throw new IOException(e);
			}
	}

	@Override
	public void swap(int a, int b) {
		parent.swap(a, b);
		float dist1 = distances[a];
		float dist2 = distances[b];
		distances[b] = dist1;
		distances[a] = dist2;
	}

	@Override
	public void endCollection() {
		distances = distancesBuffer.getFinalArray();
	}

	@Override
	public int getSize() {
		if (distances == null)
			return 0;
		return distances.length;
	}

}
