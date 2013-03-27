/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2012 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.util;

public class Geospatial {

	private static final double MIN_LAT = Math.toRadians(-90d); // -PI/2
	private static final double MAX_LAT = Math.toRadians(90d); // PI/2
	private static final double MIN_LON = Math.toRadians(-180d); // -PI
	private static final double MAX_LON = Math.toRadians(180d); // PI

	public static class Location {

		final public double latitude;
		final public double longitude;

		public Location(double lat, double lon) {
			this.latitude = lat;
			this.longitude = lon;
		}

	}

	public final static double EARTH_RADIUS_KM = 6371;
	public final static double EARTH_RADIUS_MILES = 3959;

	/**
	 * Return the distance in KM between two points passed as latitude and
	 * longitude in Radians
	 * 
	 * @param lon1
	 * @param lat1
	 * @param lon2
	 * @param lat2
	 * @return
	 */
	public final static double distance(Location loc1, Location loc2,
			double radius) {
		double x = (loc2.longitude - loc1.longitude)
				* Math.cos((loc1.latitude + loc2.latitude) / 2);
		double y = (loc2.latitude - loc1.latitude);
		double d = Math.sqrt(x * x + y * y) * radius;
		return d;
	}

	/**
	 * <p>
	 * Part of this code was originally published at <a
	 * href="http://JanMatuschek.de/LatitudeLongitudeBoundingCoordinates#Java">
	 * http://JanMatuschek.de/LatitudeLongitudeBoundingCoordinates#Java</a>.
	 * </p>
	 * 
	 * @author Jan Philip Matuschek
	 * @version 22 September 2010
	 * 
	 * @param loc
	 * @param distance
	 * @param radius
	 * @return
	 */
	public final static Location[] boundingCoordinates(Location loc,
			double distance, double radius) {

		if (radius < 0d || distance < 0d)
			throw new IllegalArgumentException();

		// angular distance in radians on a great circle
		double radDist = distance / radius;

		double minLat = loc.latitude - radDist;
		double maxLat = loc.latitude + radDist;

		double minLon, maxLon;
		if (minLat > MIN_LAT && maxLat < MAX_LAT) {
			double deltaLon = Math.asin(Math.sin(radDist)
					/ Math.cos(loc.latitude));
			minLon = loc.longitude - deltaLon;
			if (minLon < MIN_LON)
				minLon += 2d * Math.PI;
			maxLon = loc.longitude + deltaLon;
			if (maxLon > MAX_LON)
				maxLon -= 2d * Math.PI;
		} else {
			// a pole is within the distance
			minLat = Math.max(minLat, MIN_LAT);
			maxLat = Math.min(maxLat, MAX_LAT);
			minLon = MIN_LON;
			maxLon = MAX_LON;
		}

		return new Location[] { new Location(minLat, minLon),
				new Location(maxLat, maxLon) };
	}

}
