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

package com.jaeksoft.searchlib.geo;

import java.io.IOException;
import java.text.NumberFormat;
import java.text.ParseException;

import com.jaeksoft.searchlib.analysis.filter.DegreesRadiansFilter;
import com.jaeksoft.searchlib.index.FieldCacheIndex;
import com.jaeksoft.searchlib.index.ReaderAbstract;
import com.jaeksoft.searchlib.util.Geospatial;

public class GeoDistance {

	private final FieldCacheIndex stringIndexLatitude;

	private final FieldCacheIndex stringIndexLongitude;

	private final double latitudeRadians;

	private final double longitudeRadians;

	private final double radius;

	private final NumberFormat numberFormat = DegreesRadiansFilter
			.getRadiansFormat();

	GeoDistance(GeoParameters geoParams, ReaderAbstract reader, Double radius)
			throws IOException {
		stringIndexLatitude = reader.getStringIndex(geoParams
				.getLatitudeField());
		stringIndexLongitude = reader.getStringIndex(geoParams
				.getLongitudeField());
		latitudeRadians = geoParams.getLatitudeRadian();
		longitudeRadians = geoParams.getLongitudeRadian();
		this.radius = radius == null ? Geospatial.EARTH_RADIUS_KM : radius;
	}

	final public double getDistance(final int doc) throws ParseException {
		double lat = numberFormat.parse(
				stringIndexLatitude.lookup[stringIndexLatitude.order[doc]])
				.doubleValue();
		double lon = numberFormat.parse(
				stringIndexLongitude.lookup[stringIndexLongitude.order[doc]])
				.doubleValue();
		return Geospatial.distance(latitudeRadians, longitudeRadians, lat, lon,
				radius);
	}
}
