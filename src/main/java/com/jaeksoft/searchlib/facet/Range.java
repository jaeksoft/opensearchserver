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

package com.jaeksoft.searchlib.facet;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.util.DomUtils;
import com.jaeksoft.searchlib.util.XmlWriter;

public class Range {

	private boolean fromInclusive;

	private String fromValue;

	private boolean toInclusive;

	private String toValue;

	public Range() {
		fromInclusive = false;
		fromValue = null;
		toInclusive = true;
		toValue = null;
	}

	public Range(Node rangeNode) {
		fromInclusive = DomUtils.getAttributeBoolean(rangeNode,
				"fromInclusive", false);
		fromValue = DomUtils.getAttributeText(rangeNode, "fromValue");
		toInclusive = DomUtils.getAttributeBoolean(rangeNode, "toInclusive",
				false);
		toValue = DomUtils.getAttributeText(rangeNode, "toValue");
	}

	public void writeXml(XmlWriter writer) throws SAXException {
		writer.startElement("range", "fromInclusive",
				Boolean.toString(fromInclusive), "fromValue", fromValue,
				"toInclusive", Boolean.toString(toInclusive), "toValue",
				toValue);
		writer.endElement();
	}

	public Range duplicate() {
		Range range = new Range();
		range.fromInclusive = fromInclusive;
		range.fromValue = fromValue;
		range.toInclusive = toInclusive;
		range.toValue = toValue;
		return range;
	}

	/**
	 * @return the fromInclusive
	 */
	public boolean isFromInclusive() {
		return fromInclusive;
	}

	/**
	 * @param fromInclusive
	 *            the fromInclusive to set
	 */
	public void setFromInclusive(boolean fromInclusive) {
		this.fromInclusive = fromInclusive;
	}

	/**
	 * @return the fromValue
	 */
	public String getFromValue() {
		return fromValue;
	}

	/**
	 * @param fromValue
	 *            the fromValue to set
	 */
	public void setFromValue(String fromValue) {
		this.fromValue = fromValue;
	}

	/**
	 * @return the toInclusive
	 */
	public boolean isToInclusive() {
		return toInclusive;
	}

	/**
	 * @param toInclusive
	 *            the toInclusive to set
	 */
	public void setToInclusive(boolean toInclusive) {
		this.toInclusive = toInclusive;
	}

	/**
	 * @return the toValue
	 */
	public String getToValue() {
		return toValue;
	}

	/**
	 * @param toValue
	 *            the toValue to set
	 */
	public void setToValue(String toValue) {
		this.toValue = toValue;
	}

	final public static List<Range> duplicate(List<Range> ranges) {
		if (ranges == null)
			return null;
		List<Range> newRanges = new ArrayList<Range>(ranges.size());
		for (Range range : ranges)
			newRanges.add(range.duplicate());
		return newRanges;
	}

	/**
	 * Load a list of range by parsing the DOM nodes <range>
	 * 
	 * @param rangesNode
	 * @return
	 */
	final public static List<Range> loadList(Node rangesNode) {
		if (rangesNode == null)
			return null;
		List<Node> rangeNodes = DomUtils.getNodes(rangesNode, "range");
		if (CollectionUtils.isEmpty(rangeNodes))
			return null;
		List<Range> ranges = new ArrayList<Range>(rangeNodes.size());
		for (Node rangeNode : rangeNodes)
			ranges.add(new Range(rangeNode));
		return ranges;
	}

	/**
	 * Write a list of range as XML <ranges><range ...>
	 * 
	 * @param ranges
	 *            The array with the list of Range
	 * @param parentNodeName
	 *            The name of the node name. If it is null, no parent node is
	 *            created
	 * @param writer
	 *            The XmlWriter instance
	 * @throws SAXException
	 */
	final public static void writeXml(List<Range> ranges,
			String parentNodeName, XmlWriter writer) throws SAXException {
		if (CollectionUtils.isEmpty(ranges))
			return;
		if (parentNodeName != null)
			writer.startElement(parentNodeName);
		for (Range range : ranges)
			range.writeXml(writer);
		if (parentNodeName != null)
			writer.endElement();
	}

}
