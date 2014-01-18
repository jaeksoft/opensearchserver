/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2013-2014 Emmanuel Keller / Jaeksoft
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

import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.jaeksoft.searchlib.index.ReaderAbstract;
import com.jaeksoft.searchlib.util.DomUtils;
import com.jaeksoft.searchlib.util.Geospatial;
import com.jaeksoft.searchlib.util.XmlWriter;
import com.jaeksoft.searchlib.web.ServletTransaction;

public class GeoParameters {

	public static enum CoordUnit {

		DEGREES("Degrees"),

		RADIANS("Radians");

		final private String label;

		private CoordUnit(String label) {
			this.label = label;
		}

		@Override
		final public String toString() {
			return label;
		}

		@JsonIgnore
		final public static CoordUnit find(final String name) {
			for (CoordUnit unit : values())
				if (unit.name().equals(name))
					return unit;
			return null;
		}
	}

	public static enum DistanceReturn {

		NO_DISTANCE, DISTANCE_KM, DISTANCE_MILES;

		final public static DistanceReturn find(final String name) {
			for (DistanceReturn item : values())
				if (item.name().equals(name))
					return item;
			return NO_DISTANCE;
		}

		@JsonIgnore
		final public static double getRadius(final DistanceReturn distanceReturn) {
			if (distanceReturn == null)
				return Geospatial.EARTH_RADIUS_KM;
			switch (distanceReturn) {
			case NO_DISTANCE:
			case DISTANCE_KM:
				return Geospatial.EARTH_RADIUS_KM;
			case DISTANCE_MILES:
				return Geospatial.EARTH_RADIUS_MILES;
			}
			return Geospatial.EARTH_RADIUS_KM;
		}

	}

	private String latitudeField;

	private String longitudeField;

	private double latitude;

	private double longitude;

	private CoordUnit coordUnit;

	private DistanceReturn distanceReturn;

	public GeoParameters() {
		latitudeField = null;
		longitudeField = null;
		latitude = 0;
		longitude = 0;
		coordUnit = CoordUnit.DEGREES;
		distanceReturn = DistanceReturn.NO_DISTANCE;
	}

	/**
	 * @return the latitudeField
	 */
	public String getLatitudeField() {
		return latitudeField;
	}

	/**
	 * @param latitudeField
	 *            the latitudeField to set
	 */
	public void setLatitudeField(String latitudeField) {
		this.latitudeField = latitudeField;
	}

	/**
	 * @return the longitudeField
	 */
	public String getLongitudeField() {
		return longitudeField;
	}

	/**
	 * @param longitudeField
	 *            the longitudeField to set
	 */
	public void setLongitudeField(String longitudeField) {
		this.longitudeField = longitudeField;
	}

	/**
	 * @return the latitude
	 */
	public double getLatitude() {
		return latitude;
	}

	public double getLatitudeRadian() {
		return coordUnit == CoordUnit.RADIANS ? latitude : Math
				.toRadians(latitude);
	}

	/**
	 * @param latitude
	 *            the latitude to set
	 */
	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	/**
	 * @return the longitude
	 */
	public double getLongitude() {
		return longitude;
	}

	public double getLongitudeRadian() {
		return coordUnit == CoordUnit.RADIANS ? longitude : Math
				.toRadians(longitude);
	}

	/**
	 * @param longitude
	 *            the longitude to set
	 */
	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	/**
	 * @return the coordUnit
	 */
	public CoordUnit getCoordUnit() {
		return coordUnit;
	}

	/**
	 * @param coordUnit
	 *            the coordUnit to set
	 */
	public void setCoordUnit(CoordUnit coordUnit) {
		this.coordUnit = coordUnit;
	}

	public void setFromServlet(ServletTransaction transaction) {
		String q = transaction.getParameterString("geo.lon");
		if (q != null)
			longitude = Double.parseDouble(q);
		q = transaction.getParameterString("geo.lat");
		if (q != null)
			latitude = Double.parseDouble(q);
		q = transaction.getParameterString("geo.field.lon");
		if (q != null)
			longitudeField = q;
		q = transaction.getParameterString("geo.field.lat");
		if (q != null)
			latitudeField = q;
		q = transaction.getParameterString("geo.unit");
		if (q != null)
			coordUnit = CoordUnit.find(q);
		q = transaction.getParameterString("geo.distance");
		if (q != null)
			distanceReturn = DistanceReturn.find(q);
	}

	public void set(GeoParameters geoParams) {
		this.coordUnit = geoParams.coordUnit;
		this.latitude = geoParams.latitude;
		this.latitudeField = geoParams.latitudeField;
		this.longitude = geoParams.longitude;
		this.longitudeField = geoParams.longitudeField;
		this.distanceReturn = geoParams.distanceReturn;
	}

	public void set(Node geoNode) {
		this.coordUnit = CoordUnit.find(DomUtils.getAttributeText(geoNode,
				"coordUnit"));
		this.latitude = DomUtils.getAttributeDouble(geoNode, "latitude");
		this.longitude = DomUtils.getAttributeDouble(geoNode, "longitude");
		this.latitudeField = DomUtils
				.getAttributeText(geoNode, "latitudeField");
		this.longitudeField = DomUtils.getAttributeText(geoNode,
				"longitudeField");
		this.distanceReturn = DomUtils.getAttributeEnum(geoNode,
				"distanceReturn", DistanceReturn.values(),
				DistanceReturn.NO_DISTANCE);
	}

	public void writeXmlConfig(XmlWriter xmlWriter, String nodeName)
			throws SAXException {
		xmlWriter.startElement(nodeName, "coordUnit", coordUnit.name(),
				"latitudeField", latitudeField, "longitudeField",
				longitudeField, "latitude", Double.toString(latitude),
				"longitude", Double.toString(longitude), "distanceReturn",
				distanceReturn.name());
		xmlWriter.endElement();
	}

	public GeoDistance getGeoDistance(ReaderAbstract reader, Double radius)
			throws IOException {
		return new GeoDistance(this, reader, radius);
	}

	/**
	 * @return the distanceReturn
	 */
	public DistanceReturn getDistanceReturn() {
		return distanceReturn;
	}

	/**
	 * @param distanceReturn
	 *            the distanceReturn to set
	 */
	public void setDistanceReturn(DistanceReturn distanceReturn) {
		this.distanceReturn = distanceReturn;
	}

}
