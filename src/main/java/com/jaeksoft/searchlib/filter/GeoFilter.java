/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2012-2014 Emmanuel Keller / Jaeksoft
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

import javax.xml.bind.annotation.XmlType;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermRangeQuery;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.analysis.filter.DegreesRadiansFilter;
import com.jaeksoft.searchlib.geo.GeoParameters;
import com.jaeksoft.searchlib.query.ParseException;
import com.jaeksoft.searchlib.request.AbstractLocalSearchRequest;
import com.jaeksoft.searchlib.result.ResultSearchSingle;
import com.jaeksoft.searchlib.result.collector.DistanceInterface;
import com.jaeksoft.searchlib.result.collector.DocIdInterface;
import com.jaeksoft.searchlib.schema.SchemaField;
import com.jaeksoft.searchlib.util.Geospatial;
import com.jaeksoft.searchlib.util.StringUtils;
import com.jaeksoft.searchlib.util.Timer;
import com.jaeksoft.searchlib.util.XPathParser;
import com.jaeksoft.searchlib.util.XmlWriter;
import com.jaeksoft.searchlib.web.ServletTransaction;
import com.jaeksoft.searchlib.webservice.query.search.SearchQueryAbstract.OperatorEnum;

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

	@XmlType(name = "GeoType")
	public static enum Type {

		SQUARED("Squared"),

		RADIUS("Radius");

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

	private transient Query query;

	private Unit unit;

	private Type type;

	private double distance;

	public GeoFilter() {
		this(Source.REQUEST, false, null, Unit.KILOMETERS, Type.SQUARED, 0);
	}

	public GeoFilter(Source source, boolean negative, String paramPosition,
			Unit unit, Type type, double distance) {
		super(FilterType.GEO_FILTER, source, negative, paramPosition);
		query = null;
		this.unit = unit;
		this.type = type;
		this.distance = distance;
	}

	public GeoFilter(XPathParser xpp, Node node) {
		super(FilterType.GEO_FILTER, Source.CONFIGXML, "yes".equals(XPathParser
				.getAttributeString(node, "negative")), null);
		this.unit = Unit.find(XPathParser.getAttributeString(node, "unit"));
		this.type = Type.find(XPathParser.getAttributeString(node, "type"));
		this.distance = XPathParser.getAttributeDouble(node, "distance");
	}

	@Override
	public String getDescription() {
		StringBuilder sb = new StringBuilder("Geo filter: ");
		sb.append(type);
		sb.append(" - ");
		sb.append(distance);
		sb.append(' ');
		sb.append(unit);
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
	public double getMaxDistance() {
		return distance;
	}

	/**
	 * @param distance
	 *            the distance to set
	 */
	public void setMaxDistance(double distance) {
		this.distance = distance;
		this.query = null;
	}

	@Override
	public void writeXmlConfig(XmlWriter xmlWriter) throws SAXException {
		xmlWriter.startElement("geofilter", "type", type.name(), "unit",
				unit.name(), "distance", Double.toString(distance));
		xmlWriter.endElement();
	}

	@Override
	public String getCacheKey(SchemaField defaultField, Analyzer analyzer,
			AbstractLocalSearchRequest request) throws ParseException {
		StringBuilder sb = new StringBuilder("GeoFilter - ");
		sb.append(getQuery(request.getGeoParameters()).toString());
		sb.append(" - ");
		sb.append(type.name());
		return sb.toString();
	}

	private Query getQuery(GeoParameters geoParams) throws ParseException {
		if (query != null)
			return query;
		double lat = geoParams.getLatitudeRadian();
		double lon = geoParams.getLongitudeRadian();
		Geospatial.Location loc = new Geospatial.Location(lat, lon);
		double dist = distance / unit.div;
		NumberFormat nf = DegreesRadiansFilter.getRadiansFormat();
		Geospatial.Location[] bound = Geospatial.boundingCoordinates(loc, dist,
				unit.radius);
		BooleanQuery booleanQuery = new BooleanQuery(true);
		booleanQuery.add(
				getBooleanQuery(nf, geoParams.getLatitudeField(),
						bound[0].latitude, bound[1].latitude), Occur.MUST);
		booleanQuery.add(
				getBooleanQuery(nf, geoParams.getLongitudeField(),
						bound[0].longitude, bound[1].longitude), Occur.MUST);
		query = booleanQuery;
		return query;
	}

	private final static BooleanQuery getBooleanQuery(final NumberFormat nf,
			final String field, final double min, final double max) {
		String fMin = nf.format(min);
		String fMax = nf.format(max);
		BooleanQuery booleanQuery = new BooleanQuery(true);
		if (min < 0) {
			if (max < 0)
				booleanQuery.add(new TermRangeQuery(field, fMax, fMin, true,
						true), Occur.MUST);
			else {
				booleanQuery.add(new TermRangeQuery(field,
						DegreesRadiansFilter.NEGATIVE_RADIAN_ZERO, fMin, true,
						true),

				Occur.SHOULD);
				booleanQuery.add(new TermRangeQuery(field,
						DegreesRadiansFilter.POSITIVE_RADIAN_ZERO, fMax, true,
						true),

				Occur.SHOULD);
			}
		} else {
			booleanQuery.add(new TermRangeQuery(field, fMin, fMax, true, true),
					Occur.MUST);
		}
		return booleanQuery;
	}

	public static void main(String[] args) {
		NumberFormat nf = DegreesRadiansFilter.getRadiansFormat();
		System.out.println(getBooleanQuery(nf, "neg_neg", -1.25, -0.50));
		System.out.println(getBooleanQuery(nf, "neg_zero", -0.50, 0));
		System.out.println(getBooleanQuery(nf, "neg_pos", -0.50, +0.50));
		System.out.println(getBooleanQuery(nf, "zero_zero", 0, 0));
		System.out.println(getBooleanQuery(nf, "zero_pos", 0, +0.50));
		System.out.println(getBooleanQuery(nf, "pos_pos", 0.50, 1.25));
	}

	@Override
	public FilterHits getFilterHits(SchemaField defaultField,
			Analyzer analyzer, AbstractLocalSearchRequest searchRequest,
			Timer timer) throws ParseException, IOException, SearchLibException {
		GeoParameters geoParams = searchRequest.getGeoParameters();
		Query query = getQuery(geoParams);
		ResultSearchSingle result = getResult(searchRequest.getConfig(), query,
				geoParams, timer);
		FilterHits filterHits = new FilterHits(result.getDocSetHits()
				.getFilterHitsCollector(), isNegative(), timer);
		if (type == Type.SQUARED)
			return filterHits;
		if (type == Type.RADIUS) {
			DocIdInterface docIdInterface = result.getDocs();
			DistanceInterface distanceInterface = docIdInterface
					.getCollector(DistanceInterface.class);
			int[] docIds = docIdInterface.getIds();
			int pos = 0;
			for (float dist : distanceInterface.getDistances()) {
				if (dist > distance)
					filterHits.fastRemove(docIds[pos]);
				pos++;
			}
			return filterHits;
		}
		return null;
	}

	@Override
	public GeoFilter duplicate() {
		return new GeoFilter(getSource(), isNegative(), getParamPosition(),
				unit, type, distance);
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
		copyTo.query = null;
	}

	@Override
	final public void setFromServlet(final ServletTransaction transaction,
			final String prefix) {
		String pp = StringUtils.fastConcat(prefix, getParamPosition(), ".dist");
		String q = transaction.getParameterString(pp);
		if (q != null)
			setMaxDistance(Double.parseDouble(q));
	}

	@Override
	public void setParam(String params) throws SearchLibException {
		setMaxDistance(Double.parseDouble(params));
	}

	@Override
	public boolean isDistance() {
		return type == GeoFilter.Type.RADIUS;
	}

	@Override
	public void reset() {
		query = null;
	}

	@Override
	public OperatorEnum getOperator(OperatorEnum defaultOperator) {
		return defaultOperator;
	}
}
