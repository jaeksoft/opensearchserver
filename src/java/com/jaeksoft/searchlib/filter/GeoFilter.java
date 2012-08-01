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

import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.util.XmlWriter;

public class GeoFilter {

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

	public void writeXmlConfig(XmlWriter xmlWriter) throws SAXException {
		xmlWriter.startElement("geofilter", "type", type.name(), "unit",
				unit.name(), "value", Double.toString(value));
		xmlWriter.endElement();
	}
}
