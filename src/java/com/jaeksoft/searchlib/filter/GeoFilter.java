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

package com.jaeksoft.searchlib.filter;

import java.io.IOException;
import java.text.NumberFormat;

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Query;
import org.apache.lucene.util.Version;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.analysis.filter.DegreesRadiansFilter;
import com.jaeksoft.searchlib.index.ReaderLocal;
import com.jaeksoft.searchlib.query.ParseException;
import com.jaeksoft.searchlib.schema.SchemaField;
import com.jaeksoft.searchlib.util.Geospatial;
import com.jaeksoft.searchlib.util.Timer;
import com.jaeksoft.searchlib.util.XPathParser;
import com.jaeksoft.searchlib.util.XmlWriter;
import com.jaeksoft.searchlib.web.ServletTransaction;

public class GeoFilter extends FilterAbstract<GeoFilter> {

	public static enum Unit {

		METERS("Meters", 1000, Geospatial.EARTH_RADIUS_KM),

		KILOMETERS("Kilometers", 1, Geospatial.EARTH_RADIUS_KM),

		MILES("Miles", 1, Geospatial.EARTH_RADIUS_MILES),

		FEETS("Feets", 5280, Geospatial.EARTH_RADIUS_MILES);

		final private String label;

		final public double div;

		final public double radius;

		private Unit(String label, double div, double radius) {
			this.label = label;
			this.div = div;
			this.radius = radius;
		}

		@Override
		final public String toString() {
			return label;
		}

		final public static Unit find(String name) {
			for (Unit unit : values())
				if (unit.name().equals(name))
					return unit;
			return null;
		}

	}

	public static enum Type {

		SQUARED("Squared");

		final private String label;

		private Type(String label) {
			this.label = label;
		}

		@Override
		final public String toString() {
			return label;
		}

		final public static Type find(String name) {
			for (Type type : values())
				if (type.name().equals(name))
					return type;
			return null;
		}
	}

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

		final public static CoordUnit find(String name) {
			for (CoordUnit unit : values())
				if (unit.name().equals(name))
					return unit;
			return null;
		}
	}

	private transient Query query;

	private Unit unit;

	private Type type;

	private double distance;

	private String latitudeField;

	private String longitudeField;

	private double latitude;

	private double longitude;

	private CoordUnit coordUnit;

	public GeoFilter() {
		this(Source.REQUEST, false, null, Unit.KILOMETERS, Type.SQUARED, 0,
				null, null, 0, 0, CoordUnit.RADIANS);
	}

	public GeoFilter(Source source, boolean negative, String paramPosition,
			Unit unit, Type type, double distance, String latitudeField,
			String longitudeField, double latitude, double longitude,
			CoordUnit coordUnit) {
		super(FilterType.GEO_FILTER, source, negative, paramPosition);
		query = null;
		this.unit = unit;
		this.type = type;
		this.distance = distance;
		this.latitudeField = latitudeField;
		this.longitudeField = longitudeField;
		this.latitude = latitude;
		this.longitude = longitude;
		this.coordUnit = coordUnit;
	}

	public GeoFilter(XPathParser xpp, Node node) {
		super(FilterType.GEO_FILTER, Source.CONFIGXML, "yes".equals(XPathParser
				.getAttributeString(node, "negative")), null);
		this.unit = Unit.find(XPathParser.getAttributeString(node, "unit"));
		this.type = Type.find(XPathParser.getAttributeString(node, "type"));
		this.distance = XPathParser.getAttributeDouble(node, "distance");
		this.latitudeField = XPathParser.getAttributeString(node,
				"latitudeField");
		this.longitudeField = XPathParser.getAttributeString(node,
				"longitudeField");
		this.latitude = XPathParser.getAttributeDouble(node, "latitude");
		this.longitude = XPathParser.getAttributeDouble(node, "longitude");
		this.coordUnit = CoordUnit.find(XPathParser.getAttributeString(node,
				"coordUnit"));
	}

	@Override
	public String getDescription() {
		StringBuffer sb = new StringBuffer("Geo filter: ");
		sb.append(type);
		sb.append(" - ");
		sb.append(distance);
		sb.append(' ');
		sb.append(unit);
		sb.append(" - Fields: ");
		sb.append(latitudeField);
		sb.append('/');
		sb.append(longitudeField);
		return sb.toString();
	}

	/**
	 * @return the unit
	 */
	public Unit getUnit() {
		return unit;
	}

	/**
	 * @param unit
	 *            the unit to set
	 */
	public void setUnit(Unit unit) {
		this.unit = unit;
		this.query = null;
	}

	/**
	 * @return the type
	 */
	public Type getType() {
		return type;
	}

	/**
	 * @param type
	 *            the type to set
	 */
	public void setType(Type type) {
		this.type = type;
		this.query = null;
	}

	/**
	 * @return the distance
	 */
	public double getDistance() {
		return distance;
	}

	/**
	 * @param distance
	 *            the distance to set
	 */
	public void setDistance(double distance) {
		this.distance = distance;
		this.query = null;
	}

	@Override
	public void writeXmlConfig(XmlWriter xmlWriter) throws SAXException {
		xmlWriter.startElement("geofilter", "type", type.name(), "unit",
				unit.name(), "distance", Double.toString(distance),
				"latitudeField", latitudeField, "longitudeField",
				longitudeField, "latitude", Double.toString(latitude),
				"longitude", Double.toString(longitude), "coordUnit",
				coordUnit.name());
		xmlWriter.endElement();
	}

	@Override
	public String getCacheKey(SchemaField defaultField, Analyzer analyzer)
			throws ParseException {
		String key = "GeoFilter - "
				+ getQuery(defaultField, analyzer).toString();
		return key;
	}

	private String getQueryString() {
		double lat = coordUnit == CoordUnit.RADIANS ? latitude : Math
				.toRadians(latitude);
		double lon = coordUnit == CoordUnit.RADIANS ? longitude : Math
				.toRadians(longitude);
		Geospatial.Location loc = new Geospatial.Location(lat, lon);
		double dist = distance / unit.div;
		Geospatial.Location[] bound = Geospatial.boundingCoordinates(loc, dist,
				unit.radius);
		StringBuffer sb = new StringBuffer(latitudeField);
		NumberFormat nf = DegreesRadiansFilter.getRadiansFormat();
		sb.append(":[");
		sb.append(nf.format(bound[0].latitude));
		sb.append(" TO ");
		sb.append(nf.format(bound[1].latitude));
		sb.append("] AND ");
		sb.append(longitudeField);
		sb.append(":[");
		sb.append(nf.format(bound[0].longitude));
		sb.append(" TO ");
		sb.append(nf.format(bound[1].longitude));
		sb.append("]");
		return sb.toString();
	}

	private Query getQuery(SchemaField defaultField, Analyzer analyzer)
			throws ParseException {
		if (query != null)
			return query;
		QueryParser queryParser = new QueryParser(Version.LUCENE_36,
				defaultField.getName(), analyzer);
		queryParser.setLowercaseExpandedTerms(false);
		try {
			query = queryParser.parse(getQueryString());
		} catch (org.apache.lucene.queryParser.ParseException e) {
			throw new ParseException(e);
		}
		return query;
	}

	@Override
	public FilterHits getFilterHits(ReaderLocal reader,
			SchemaField defaultField, Analyzer analyzer, Timer timer)
			throws ParseException, IOException {
		Query query = getQuery(defaultField, analyzer);
		FilterHits filterHits = new FilterHits(query, isNegative(), reader,
				timer);
		return filterHits;
	}

	@Override
	public GeoFilter duplicate() {
		return new GeoFilter(getSource(), isNegative(), getParamPosition(),
				unit, type, distance, latitudeField, longitudeField, latitude,
				longitude, coordUnit);
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
		this.query = null;
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

	@Override
	public void copyTo(FilterAbstract<?> selectedItem) {
		if (!(selectedItem instanceof GeoFilter))
			throw new RuntimeException("Wrong filter type "
					+ selectedItem.getClass().getName());
		super.copyTo(selectedItem);
		GeoFilter copyTo = (GeoFilter) selectedItem;
		copyTo.unit = unit;
		copyTo.type = type;
		copyTo.distance = distance;
		copyTo.latitudeField = latitudeField;
		copyTo.longitudeField = longitudeField;
		copyTo.latitude = latitude;
		copyTo.longitude = longitude;
		copyTo.coordUnit = coordUnit;
		copyTo.query = null;
	}

	/**
	 * @return the latitude
	 */
	public double getLatitude() {
		return latitude;
	}

	/**
	 * @param latitude
	 *            the latitude to set
	 */
	public void setLatitude(double latitude) {
		this.latitude = latitude;
		this.query = null;
	}

	/**
	 * @return the longitude
	 */
	public double getLongitude() {
		return longitude;
	}

	/**
	 * @param longitude
	 *            the longitude to set
	 */
	public void setLongitude(double longitude) {
		this.longitude = longitude;
		this.query = null;
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
		this.query = null;
	}

	@Override
	public void setFromServlet(ServletTransaction transaction) {
		String pp = getParamPosition();
		String q = transaction.getParameterString(pp + ".lon");
		if (q != null)
			setLongitude(Double.parseDouble(q));
		q = transaction.getParameterString(pp + ".lat");
		if (q != null)
			setLatitude(Double.parseDouble(q));
		q = transaction.getParameterString(pp + ".dist");
		if (q != null)
			setDistance(Double.parseDouble(q));
	}

	@Override
	public void setParam(String params) throws SearchLibException {
		String[] values = StringUtils.split(params, '|');
		if (values == null || values.length < 2 || values.length > 3)
			throw new SearchLibException(
					"GeoFilter: Wrong number of parameters (" + values.length
							+ ")");
		setLongitude(Double.parseDouble(values[0]));
		setLatitude(Double.parseDouble(values[1]));
		if (values.length == 3)
			setDistance(Double.parseDouble(values[2]));
	}
}
