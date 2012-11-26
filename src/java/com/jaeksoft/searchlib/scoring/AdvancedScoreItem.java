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

import com.jaeksoft.searchlib.util.XPathParser;
import com.jaeksoft.searchlib.util.XmlWriter;

public class AdvancedScoreItem {

	public final static String SCORE_ITEM_FIELD_ATTR = "fieldName";

	public final static String SCORE_ITEM_ASCENDING_ATTR = "ascending";

	public final static String SCORE_ITEM_WEIGHT_ATTR = "weight";

	private boolean ascending;

	private String fieldName;

	private float weight;

	public AdvancedScoreItem() {
		fieldName = null;
		ascending = false;
		weight = 1.0F;
	}

	public AdvancedScoreItem(AdvancedScoreItem from) {
		copy(from);
	}

	public void copy(AdvancedScoreItem from) {
		this.ascending = from.ascending;
		this.fieldName = from.fieldName;
		this.weight = from.weight;
	}

	public AdvancedScoreItem(Node node) {
		this.fieldName = XPathParser.getAttributeString(node,
				SCORE_ITEM_FIELD_ATTR);
		this.ascending = "true".equalsIgnoreCase(XPathParser
				.getAttributeString(node, SCORE_ITEM_ASCENDING_ATTR));
		this.weight = XPathParser.getAttributeFloat(node,
				SCORE_ITEM_WEIGHT_ATTR);
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
				Float.toString(weight));
		xmlWriter.endElement();
	}

	public String name() {
		StringBuffer sb = new StringBuffer();
		if (ascending)
			sb.append("ord(");
		else
			sb.append("rord(");
		sb.append(fieldName);
		sb.append(")*");
		sb.append(weight);
		return sb.toString();
	}

}
