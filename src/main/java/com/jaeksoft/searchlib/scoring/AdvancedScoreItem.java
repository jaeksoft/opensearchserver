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

package com.jaeksoft.searchlib.scoring;

import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.util.DomUtils;
import com.jaeksoft.searchlib.util.XPathParser;
import com.jaeksoft.searchlib.util.XmlWriter;

public class AdvancedScoreItem {

	public static enum Type {
		DISTANCE, FIELD_ORDER;
	}

	public final static String SCORE_ITEM_FIELD_ATTR = "fieldName";

	public final static String SCORE_ITEM_ASCENDING_ATTR = "ascending";

	public final static String SCORE_ITEM_WEIGHT_ATTR = "weight";

	public final static String SCORE_ITEM_TYPE_ATTR = "type";

	private boolean ascending;

	private String fieldName;

	private float weight;

	private Type type;

	public AdvancedScoreItem() {
		fieldName = null;
		ascending = false;
		weight = 1.0F;
		type = Type.FIELD_ORDER;
	}

	public AdvancedScoreItem(AdvancedScoreItem from) {
		copy(from);
	}

	public void copy(AdvancedScoreItem from) {
		this.ascending = from.ascending;
		this.fieldName = from.fieldName;
		this.weight = from.weight;
		this.type = from.type;
	}

	public AdvancedScoreItem(Node node) {
		this.fieldName = XPathParser.getAttributeString(node,
				SCORE_ITEM_FIELD_ATTR);
		this.ascending = "true".equalsIgnoreCase(XPathParser
				.getAttributeString(node, SCORE_ITEM_ASCENDING_ATTR));
		this.weight = XPathParser.getAttributeFloat(node,
				SCORE_ITEM_WEIGHT_ATTR);
		this.type = DomUtils.getAttributeEnum(node, SCORE_ITEM_TYPE_ATTR,
				Type.values(), Type.FIELD_ORDER);
	}

	/**
	 * @return the ascending
	 */
	final public boolean isAscending() {
		return ascending;
	}

	/**
	 * @param ascending
	 *            the ascending to set
	 */
	public void setAscending(boolean ascending) {
		this.ascending = ascending;
	}

	/**
	 * @return the type
	 */
	final public Type getType() {
		return type;
	}

	/**
	 * 
	 * @param type
	 *            the type to set
	 */
	final public void setType(Type type) {
		this.type = type;
	}

	public final static String ASCENDING = "ascending";
	public final static String DESCENDING = "descending";
	public final static String[] DIRECTION = { ASCENDING, DESCENDING };

	public String getDirection() {
		return ascending ? ASCENDING : DESCENDING;
	}

	public void setDirection(String direction) {
		ascending = ASCENDING.equalsIgnoreCase(ASCENDING) ? true : false;
	}

	/**
	 * @return the fieldName
	 */
	public String getFieldName() {
		return fieldName;
	}

	/**
	 * @param fieldName
	 *            the fieldName to set
	 */
	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}

	/**
	 * @return the weight
	 */
	public float getWeight() {
		return weight;
	}

	/**
	 * @param weight
	 *            the weight to set
	 */
	public void setWeight(float weight) {
		this.weight = weight;
	}

	public void writeXmlConfig(XmlWriter xmlWriter) throws SAXException {
		xmlWriter.startElement(AdvancedScore.ADVANCED_SCORE_ITEM_NODE,
				SCORE_ITEM_FIELD_ATTR, fieldName, SCORE_ITEM_ASCENDING_ATTR,
				Boolean.toString(ascending), SCORE_ITEM_WEIGHT_ATTR,
				Float.toString(weight), SCORE_ITEM_TYPE_ATTR, type.name());
		xmlWriter.endElement();
	}

	public String name() {
		StringBuilder sb = new StringBuilder();
		switch (type) {
		case FIELD_ORDER:
			sb.append(ascending ? "ord(" : "rord(");
			sb.append(fieldName);
			break;
		case DISTANCE:
			sb.append(ascending ? "dist(" : "rdist(");
			break;
		}
		sb.append(")*");
		sb.append(weight);
		return sb.toString();
	}
}
