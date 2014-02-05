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

package com.jaeksoft.searchlib.result.collector.docsethit;

import java.io.IOException;

import org.apache.commons.lang3.ArrayUtils;

import com.jaeksoft.searchlib.geo.GeoParameters;
import com.jaeksoft.searchlib.geo.GeoParameters.DistanceReturn;
import com.jaeksoft.searchlib.index.ReaderAbstract;
import com.jaeksoft.searchlib.index.docvalue.DocValueInterface;
import com.jaeksoft.searchlib.index.docvalue.DocValueType;
import com.jaeksoft.searchlib.result.collector.AbstractBaseCollector;
import com.jaeksoft.searchlib.result.collector.AbstractExtendsCollector;
import com.jaeksoft.searchlib.result.collector.DistanceInterface;
import com.jaeksoft.searchlib.util.Geospatial;
import com.jaeksoft.searchlib.util.array.FloatBufferedArray;

public class DistanceCollector
		extends
		AbstractExtendsCollector<DocSetHitCollectorInterface, DocSetHitBaseCollector>
		implements DocSetHitCollectorInterface, DistanceInterface {

	private final FloatBufferedArray distancesBuffer;

	private float[] distances;

	private float minDistance;

	private float maxDistance;

	private final double radius;
	private final DocValueInterface latitudeProvider;
	private final DocValueInterface longitudeProvider;
	private final double latitude;
	private final double longitude;

	float currentDistance;
	private int currentDoc;

	public DistanceCollector(final DocSetHitBaseCollector base,
			final ReaderAbstract reader, final GeoParameters geoParams)
			throws IOException {
		super(base);
		this.distancesBuffer = new FloatBufferedArray(base.getMaxDoc());
		radius = DistanceReturn.getRadius(geoParams.getDistanceReturn());
		latitudeProvider = reader.getDocValueInterface(
				geoParams.getLatitudeField(), DocValueType.RADIANS);
		longitudeProvider = reader.getDocValueInterface(
				geoParams.getLongitudeField(), DocValueType.RADIANS);
		latitude = geoParams.getLatitudeRadian();
		longitude = geoParams.getLongitudeRadian();
		distances = null;
		currentDistance = 0;
		currentDoc = -1;
		maxDistance = 0;
		minDistance = Float.MAX_VALUE;
	}

	private DistanceCollector(final DocSetHitBaseCollector base,
			final DistanceCollector src) {
		super(base);
		distancesBuffer = null;
		distances = ArrayUtils.clone(src.distances);
		radius = src.radius;
		latitudeProvider = null;
		longitudeProvider = null;
		latitude = src.latitude;
		longitude = src.longitude;
	}

	@Override
	public DistanceCollector duplicate(final AbstractBaseCollector<?> base) {
		parent.duplicate(base);
		return new DistanceCollector((DocSetHitBaseCollector) base, this);
	}

	@Override
	final public void collectDoc(final int doc) throws IOException {
		parent.collectDoc(doc);
		currentDoc = doc;
		double dist = Geospatial.distance(latitudeProvider.getFloat(doc),
				longitudeProvider.getFloat(doc), latitude, longitude, radius);
		currentDistance = (float) dist;
		if (currentDistance > maxDistance)
			maxDistance = currentDistance;
		if (currentDistance < minDistance)
			minDistance = currentDistance;
		distancesBuffer.add(currentDistance);
	}

	@Override
	final public void doSwap(final int a, final int b) {
		parent.doSwap(a, b);
		float dist1 = distances[a];
		float dist2 = distances[b];
		distances[b] = dist1;
		distances[a] = dist2;
	}

	@Override
	final public void endCollection() {
		parent.endCollection();
		distances = distancesBuffer.getFinalArray();
	}

	final public class DocValue implements DocValueInterface {

		@Override
		final public float getFloat(final int doc) {
			if (currentDoc != doc)
				throw new RuntimeException("Unexpected doc value: " + doc + "/"
						+ currentDoc);
			return currentDistance;
		}

	}

	final public DocValueInterface getDocValue() {
		return new DocValue();
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

}
