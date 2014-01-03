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

package com.jaeksoft.searchlib.scoring;

import java.text.DecimalFormat;
import java.text.ParseException;

import org.apache.lucene.search.function.DocValues;

import com.jaeksoft.searchlib.index.FieldCacheIndex;
import com.jaeksoft.searchlib.util.Geospatial;
import com.jaeksoft.searchlib.util.Geospatial.Location;

public class DistanceDocValue extends DocValues {

	private final Location location;
	private final FieldCacheIndex latitudeSi;
	private final FieldCacheIndex longitudeSi;
	private final DecimalFormat decimalFormat;
	private final double radius;

	public DistanceDocValue(final Location location, final double radius,
			final FieldCacheIndex latitudeSi,
			final FieldCacheIndex longitudeSi, final DecimalFormat decimalFormat) {
		this.location = location;
		this.radius = radius;
		this.latitudeSi = latitudeSi;
		this.longitudeSi = longitudeSi;
		this.decimalFormat = decimalFormat;
	}

	@Override
	final public float floatVal(final int doc) {
		try {
			float lat = decimalFormat.parse(
					latitudeSi.lookup[latitudeSi.order[doc]]).floatValue();
			float lon = decimalFormat.parse(
					longitudeSi.lookup[longitudeSi.order[doc]]).floatValue();
			return (float) Geospatial.distance(location.latitude,
					location.longitude, lat, lon, radius);
		} catch (ParseException e) {
			return 0;
		}
	}

	@Override
	final public String toString(final int doc) {
		StringBuilder sb = new StringBuilder("distance(");
		sb.append(floatVal(doc));
		sb.append(')');
		return sb.toString();
	}

}
