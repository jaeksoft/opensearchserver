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

import org.apache.lucene.analysis.Analyzer;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.index.ReaderLocal;
import com.jaeksoft.searchlib.query.ParseException;
import com.jaeksoft.searchlib.schema.Field;
import com.jaeksoft.searchlib.util.Timer;
import com.jaeksoft.searchlib.util.XmlWriter;

public class GeoFilter extends FilterAbstract<GeoFilter> {

	public static enum Unit {

		METERS("Meters"),

		KILOMETERS("Kilometers"),

		MILES("Miles"),

		FEETS("Feets");

		private String label;

		private Unit(String label) {
			this.label = label;
		}

		public String getLabel() {
			return label;
		}
	}

	public static enum Type {

		DISABLED("disabled"),

		SQUARED("Squared"),

		CIRCLE("Circle");

		private String label;

		private Type(String label) {
			this.label = label;
		}

		public String getLabel() {
			return label;
		}

	}

	private Unit unit;

	private Type type;

	private double value;

	private String latitudeField;

	private String longitudeField;

	private double latitude;

	private double longitude;

	public GeoFilter(FilterAbstract.Source source, boolean negative, Unit unit,
			Type type, double value, String latitudeField,
			String longitudeField, double latitude, double longitude) {
		super(source, negative);
		this.unit = unit;
		this.type = type;
		this.value = value;
		this.latitudeField = latitudeField;
		this.longitudeField = longitudeField;
		this.latitude = latitude;
		this.longitude = longitude;
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
	}

	/**
	 * @return the value
	 */
	public double getValue() {
		return value;
	}

	/**
	 * @param value
	 *            the value to set
	 */
	public void setValue(double value) {
		this.value = value;
	}

	@Override
	public void writeXmlConfig(XmlWriter xmlWriter) throws SAXException {
		xmlWriter.startElement("geofilter", "type", type.name(), "unit",
				unit.name(), "value", Double.toString(value), "latitudeField",
				latitudeField, "longitudeField", longitudeField, "latitude",
				Double.toString(latitude), "longitude",
				Double.toString(longitude));
		xmlWriter.endElement();
	}

	@Override
	public String getCacheKey(Field defaultField, Analyzer analyzer)
			throws ParseException {
		return "GeoFilter - " + unit.name() + " " + type.name() + " "
				+ Double.toString(value) + "|" + latitudeField + "|"
				+ longitudeField + " " + Double.toString(latitude) + " "
				+ Double.toString(longitude);
	}

	@Override
	public FilterHits getFilterHits(ReaderLocal reader, Field defaultField,
			Analyzer analyzer, Timer timer) throws ParseException, IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public GeoFilter duplicate() {
		return new GeoFilter(getSource(), isNegative(), unit, type, value,
				latitudeField, longitudeField, latitude, longitude);
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

	@Override
	public void copyTo(FilterAbstract<?> selectedItem) {
		if (!(selectedItem instanceof GeoFilter))
			throw new RuntimeException("Wrong filter type "
					+ selectedItem.getClass().getName());
		super.copyTo(selectedItem);
		GeoFilter copyTo = (GeoFilter) selectedItem;
		copyTo.unit = unit;
		copyTo.type = type;
		copyTo.value = value;
		copyTo.latitudeField = latitudeField;
		copyTo.longitudeField = longitudeField;
		copyTo.latitude = latitude;
		copyTo.longitude = longitude;
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
	}
}
