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

import org.apache.lucene.search.FieldCache.StringIndex;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.index.ReaderLocal;
import com.jaeksoft.searchlib.result.collector.CollapseDocInterface;
import com.jaeksoft.searchlib.result.collector.DocIdInterface;
import com.jaeksoft.searchlib.schema.Field;
import com.jaeksoft.searchlib.schema.FieldList;
import com.jaeksoft.searchlib.schema.SchemaField;
import com.jaeksoft.searchlib.util.Timer;
import com.jaeksoft.searchlib.util.XPathParser;
import com.jaeksoft.searchlib.util.XmlWriter;

public class FacetField extends Field {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2765602823379594542L;

	private int minCount;

	private boolean multivalued;

	private boolean postCollapsing;

	public FacetField() {
	}

	protected FacetField(FacetField field) {
		super(field);
		this.minCount = field.minCount;
		this.multivalued = field.multivalued;
		this.postCollapsing = field.postCollapsing;
	}

	public FacetField(String name, int minCount, boolean multivalued,
			boolean postCollapsing) {
		super(name);
		this.minCount = minCount;
		this.multivalued = multivalued;
		this.postCollapsing = postCollapsing;
	}

	@Override
	public Field duplicate() {
		return new FacetField(this);
	}

	public int getMinCount() {
		return minCount;
	}

	public void setMinCount(int value) {
		minCount = value;
	}

	public boolean isMultivalued() {
		return multivalued;
	}

	public String getMultivalued() {
		return multivalued ? "yes" : "no";
	}

	public void setMultivalued(String value) {
		multivalued = "yes".equalsIgnoreCase(value)
				|| "true".equalsIgnoreCase(value)
				|| "1".equalsIgnoreCase(value);
	}

	public boolean isPostCollapsing() {
		return postCollapsing;
	}

	public String getPostCollapsing() {
		return postCollapsing ? "yes" : "no";
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

	public static void copyFacetFields(Node node,
			FieldList<SchemaField> source, FieldList<FacetField> target) {
		String fieldName = XPathParser.getAttributeString(node, "name");
		int minCount = XPathParser.getAttributeValue(node, "minCount");
		boolean multivalued = "yes".equals(XPathParser.getAttributeString(node,
				"multivalued"));
		boolean postCollapsing = "yes".equals(XPathParser.getAttributeString(
				node, "postCollapsing"));
		FacetField facetField = new FacetField(source.get(fieldName).getName(),
				minCount, multivalued, postCollapsing);
		target.add(facetField);
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

		return new FacetField(fieldName, minCount, multivalued, postCollapsing);
	}

	@Override
	public void writeXmlConfig(XmlWriter xmlWriter) throws SAXException {
		xmlWriter.startElement("facetField", "name", name, "minCount", Integer
				.toString(minCount), "multivalued", multivalued ? "yes" : "no",
				"postCollapsing", postCollapsing ? "yes" : "no");
		xmlWriter.endElement();
	}

	@Override
	public int compareTo(Field o) {
		int c = super.compareTo(o);
		if (c != 0)
			return c;
		FacetField f = (FacetField) o;
		if ((c = minCount - f.minCount) != 0)
			return c;
		if (multivalued != f.multivalued)
			return multivalued == f.multivalued ? 0 : -1;
		return postCollapsing == f.postCollapsing ? 0 : -1;
	}

	public StringIndex getStringIndex(ReaderLocal reader) throws IOException {
		if (name.equals("score"))
			return null;
		else
			return reader.getStringIndex(name);
	}

	public static StringIndex[] newStringIndexArrayForCollapsing(
			FieldList<FacetField> facetFieldList, ReaderLocal reader,
			Timer timer) throws IOException {
		if (facetFieldList.size() == 0)
			return null;
		List<StringIndex> facetFieldArray = new ArrayList<StringIndex>(0);
		for (FacetField facetField : facetFieldList)
			if (facetField.isPostCollapsing())
				facetFieldArray.add(facetField.getStringIndex(reader));

		StringIndex[] stringIndexArray = new StringIndex[facetFieldArray.size()];
		return facetFieldArray.toArray(stringIndexArray);
	}
}
