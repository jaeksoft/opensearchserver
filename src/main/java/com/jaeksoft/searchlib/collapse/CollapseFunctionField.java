/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2013 Emmanuel Keller / Jaeksoft
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
package com.jaeksoft.searchlib.collapse;

import java.io.IOException;
import java.text.ParseException;
import java.util.Set;
import java.util.TreeSet;

import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.collapse.CollapseFunction.FunctionExecutor;
import com.jaeksoft.searchlib.collapse.CollapseParameters.Function;
import com.jaeksoft.searchlib.index.FieldCacheIndex;
import com.jaeksoft.searchlib.index.ReaderAbstract;
import com.jaeksoft.searchlib.request.AbstractSearchRequest;
import com.jaeksoft.searchlib.util.DomUtils;
import com.jaeksoft.searchlib.util.Geospatial;
import com.jaeksoft.searchlib.util.Geospatial.Location;
import com.jaeksoft.searchlib.util.StringUtils;
import com.jaeksoft.searchlib.util.XmlWriter;

public class CollapseFunctionField implements Comparable<CollapseFunctionField> {

	public final static String DIST_KM = "dist_km()";
	public final static String DIST_MILES = "dist_miles()";

	public final static String[] DIST_FUNCTIONS = { DIST_KM, DIST_MILES };

	private Function function;
	private String field;
	private transient FieldCacheIndex stringIndex;
	private transient FieldCacheIndex stringIndexLatitude;
	private transient FieldCacheIndex stringIndexLongitude;
	private transient double radius;
	private transient Location location;
	private transient FunctionExecutor executor;

	public CollapseFunctionField(Function function, String field) {
		this.function = function;
		this.field = field;
		this.stringIndex = null;
	}

	public CollapseFunctionField(CollapseFunctionField functionField) {
		this.function = functionField.function;
		this.field = functionField.field;
		this.stringIndex = null;
	}

	/**
	 * @param function
	 *            the function to set
	 */
	public void setFunction(Function function) {
		this.function = function;
	}

	/**
	 * @param field
	 *            the field to set
	 */
	public void setField(String field) {
		this.field = field;
	}

	/**
	 * @return the function
	 */
	public Function getFunction() {
		return function;
	}

	/**
	 * @return the field
	 */
	public String getField() {
		return field;
	}

	@Override
	public int compareTo(CollapseFunctionField functionField) {
		int c;
		if ((c = function.compareTo(functionField.function)) != 0)
			return c;
		return StringUtils.compareNullString(field, functionField.field);
	}

	public void prepareExecute(AbstractSearchRequest searchRequest,
			ReaderAbstract reader) throws IOException, InstantiationException,
			IllegalAccessException {
		this.stringIndex = reader.getStringIndex(field);
		radius = 0;
		if (DIST_KM.equals(field))
			radius = Geospatial.EARTH_RADIUS_KM;
		else if (DIST_MILES.equals(field))
			radius = Geospatial.EARTH_RADIUS_MILES;
		if (radius != 0) {
			stringIndexLatitude = reader.getStringIndex(searchRequest
					.getGeoParameters().getLatitudeField());
			stringIndexLongitude = reader.getStringIndex(searchRequest
					.getGeoParameters().getLongitudeField());
			location = new Location(searchRequest.getGeoParameters()
					.getLatitudeRadian(), searchRequest.getGeoParameters()
					.getLongitudeRadian());
		}
		executor = function.newExecutor();
	}

	public String execute(int doc, int[] collapsedDocs) throws ParseException {
		if (radius != 0)
			return executor.execute(location, radius, stringIndexLatitude,
					stringIndexLongitude, doc, collapsedDocs);
		else
			return executor.execute(stringIndex, doc, collapsedDocs);
	}

	public static Set<CollapseFunctionField> duplicate(
			Set<CollapseFunctionField> functionFields) {
		if (functionFields == null)
			return null;
		return new TreeSet<CollapseFunctionField>(functionFields);
	}

	public static CollapseFunctionField fromXmlConfig(Node node) {
		return new CollapseFunctionField(Function.valueOf(DomUtils
				.getAttributeText(node, "function")),
				DomUtils.getAttributeText(node, "field"));
	}

	public void writeXmlConfig(XmlWriter xmlWriter, String nodeName)
			throws SAXException {
		xmlWriter.startElement(nodeName, "function", function.name(), "field",
				field);
		xmlWriter.endElement();
	}

}