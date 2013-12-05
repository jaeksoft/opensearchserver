/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008-2012 Emmanuel Keller / Jaeksoft
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.index.FieldCacheIndex;
import com.jaeksoft.searchlib.index.ReaderLocal;
import com.jaeksoft.searchlib.result.collector.CollapseDocInterface;
import com.jaeksoft.searchlib.result.collector.DocIdInterface;
import com.jaeksoft.searchlib.schema.AbstractField;
import com.jaeksoft.searchlib.schema.SchemaField;
import com.jaeksoft.searchlib.schema.SchemaFieldList;
import com.jaeksoft.searchlib.util.DomUtils;
import com.jaeksoft.searchlib.util.Timer;
import com.jaeksoft.searchlib.util.XPathParser;
import com.jaeksoft.searchlib.util.XmlWriter;

public class FacetField extends AbstractField<FacetField> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2765602823379594542L;

	private int minCount;

	private boolean multivalued;

	private boolean postCollapsing;

	private List<Range> ranges;

	public FacetField() {
	}

	protected FacetField(FacetField field) {
		super(field);
		this.minCount = field.minCount;
		this.multivalued = field.multivalued;
		this.postCollapsing = field.postCollapsing;
		this.ranges = Range.duplicate(field.ranges);
	}

	public FacetField(String name, int minCount, boolean multivalued,
			boolean postCollapsing, List<Range> ranges) {
		super(name);
		this.minCount = minCount;
		this.multivalued = multivalued;
		this.postCollapsing = postCollapsing;
		this.ranges = ranges;
	}

	@Override
	public FacetField duplicate() {
		return new FacetField(this);
	}

	public int getMinCount() {
		return minCount;
	}

	public void setMinCount(int value) {
		minCount = value;
	}

	public boolean isCheckMultivalued() {
		return multivalued;
	}

	public String getMultivalued() {
		return multivalued ? "yes" : "no";
	}

	public void setMultivalued(boolean b) {
		multivalued = b;
	}

	public void setMultivalued(String value) {
		multivalued = "yes".equalsIgnoreCase(value)
				|| "true".equalsIgnoreCase(value)
				|| "1".equalsIgnoreCase(value);
	}

	public boolean isCheckPostCollapsing() {
		return postCollapsing;
	}

	public String getPostCollapsing() {
		return postCollapsing ? "yes" : "no";
	}

	public void setPostCollapsing(boolean b) {
		postCollapsing = b;
	}

	public void setPostCollapsing(String value) {
		postCollapsing = "yes".equalsIgnoreCase(value)
				|| "true".equalsIgnoreCase(value)
				|| "1".equalsIgnoreCase(value);
	}

	final public Facet getFacet(ReaderLocal reader,
			DocIdInterface notCollapsedDocs,
			CollapseDocInterface collapsedDocs, Timer timer) throws IOException {
		// Two conditions for use postCollapsing
		boolean useCollapsing = postCollapsing && collapsedDocs != null;
		if (multivalued) {
			if (useCollapsing)
				return Facet.facetMultivalued(reader, collapsedDocs, this,
						timer);
			else {
				return Facet.facetMultivalued(reader, notCollapsedDocs, this,
						timer);
			}
		} else {
			if (useCollapsing)
				return Facet.facetSingleValue(reader, collapsedDocs, this,
						timer);
			else {
				return Facet.facetSingleValue(reader, notCollapsedDocs, this,
						timer);

			}
		}
	}

	public static void copyFacetFields(Node node, SchemaFieldList source,
			FacetFieldList target) {
		String fieldName = XPathParser.getAttributeString(node, "name");
		int minCount = XPathParser.getAttributeValue(node, "minCount");
		boolean multivalued = "yes".equals(XPathParser.getAttributeString(node,
				"multivalued"));
		boolean postCollapsing = "yes".equals(XPathParser.getAttributeString(
				node, "postCollapsing"));
		if (source == null)
			return;
		SchemaField field = source.get(fieldName);
		if (field == null)
			return;
		List<Range> ranges = null;
		Node rangesNode = DomUtils.getFirstNode(node, "ranges");
		if (rangesNode != null)
			ranges = Range.loadList(rangesNode);
		FacetField facetField = new FacetField(field.getName(), minCount,
				multivalued, postCollapsing, ranges);
		target.put(facetField);
	}

	/**
	 * Return a new FacetField instance for field(mincount) syntax
	 * 
	 * @param value
	 * @param multivalued
	 * @return
	 * @throws SyntaxError
	 */
	public static FacetField buildFacetField(String value, boolean multivalued,
			boolean postCollapsing) throws SyntaxError {
		int minCount = 1;
		String fieldName = null;

		int i1 = value.indexOf('(');
		if (i1 != -1) {
			fieldName = value.substring(0, i1);
			int i2 = value.indexOf(')', i1);
			if (i2 == -1)
				throw new SyntaxError("closed braket missing");
			minCount = Integer.parseInt(value.substring(i1 + 1, i2));
		} else
			fieldName = value;

		return new FacetField(fieldName, minCount, multivalued, postCollapsing,
				null);
	}

	@Override
	public void writeXmlConfig(XmlWriter writer) throws SAXException {
		writer.startElement("facetField", "name", name, "minCount", Integer
				.toString(minCount), "multivalued", multivalued ? "yes" : "no",
				"postCollapsing", postCollapsing ? "yes" : "no");
		Range.writeXml(ranges, "ranges", writer);
		writer.endElement();
	}

	@Override
	public int compareTo(FacetField f) {
		int c = super.compareTo(f);
		if (c != 0)
			return c;
		if ((c = minCount - f.minCount) != 0)
			return c;
		if (multivalued != f.multivalued)
			return multivalued == f.multivalued ? 0 : -1;
		return postCollapsing == f.postCollapsing ? 0 : -1;
	}

	public FieldCacheIndex getStringIndex(ReaderLocal reader)
			throws IOException {
		if (name.equals("score"))
			return null;
		else
			return reader.getStringIndex(name);
	}

	public static FieldCacheIndex[] newStringIndexArrayForCollapsing(
			FacetFieldList facetFieldList, ReaderLocal reader, Timer timer)
			throws IOException {
		if (facetFieldList.size() == 0)
			return null;
		List<FieldCacheIndex> facetFieldArray = new ArrayList<FieldCacheIndex>(
				0);
		for (FacetField facetField : facetFieldList)
			if (facetField.isCheckPostCollapsing())
				facetFieldArray.add(facetField.getStringIndex(reader));

		FieldCacheIndex[] stringIndexArray = new FieldCacheIndex[facetFieldArray
				.size()];
		return facetFieldArray.toArray(stringIndexArray);
	}

}
