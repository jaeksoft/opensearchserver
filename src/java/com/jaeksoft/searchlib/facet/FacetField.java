/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2008 Emmanuel Keller / Jaeksoft
 * 
 * http://www.open-search-server.com
 * 
 * This file is part of Jaeksoft OpenSearchServer.
 *
 * Jaeksoft OpenSearchServer is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * Jaeksoft OpenSearchServer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Jaeksoft OpenSearchServer. 
 *  If not, see <http://www.gnu.org/licenses/>.
 **/

package com.jaeksoft.searchlib.facet;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.result.ResultSingle;
import com.jaeksoft.searchlib.schema.Field;
import com.jaeksoft.searchlib.schema.FieldList;
import com.jaeksoft.searchlib.schema.SchemaField;
import com.jaeksoft.searchlib.util.XPathParser;
import com.jaeksoft.searchlib.util.XmlWriter;

public class FacetField extends Field implements Externalizable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -941940505054128025L;

	private int minCount;

	private boolean multivalued;

	public FacetField() {
	}

	protected FacetField(FacetField field) {
		super(field);
		this.minCount = field.minCount;
		this.multivalued = field.multivalued;
	}

	public FacetField(String name, int minCount, boolean multivalued) {
		super(name);
		this.minCount = minCount;
		this.multivalued = multivalued;
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

	public Facet getFacet(ResultSingle result) throws IOException {
		if (multivalued)
			return Facet.facetMultivalued(result, this);
		else
			return Facet.facetSingleValue(result, this);
	}

	public static void copyFacetFields(Node node,
			FieldList<SchemaField> source, FieldList<FacetField> target) {
		String fieldName = XPathParser.getAttributeString(node, "name");
		int minCount = XPathParser.getAttributeValue(node, "minCount");
		boolean multivalued = "yes".equals(XPathParser.getAttributeString(node,
				"multivalued"));
		FacetField facetField = new FacetField(source.get(fieldName).getName(),
				minCount, multivalued);
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
	public static FacetField buildFacetField(String value, boolean multivalued)
			throws SyntaxError {
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

		return new FacetField(fieldName, minCount, multivalued);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {
		super.readExternal(in);
		minCount = in.readInt();
		multivalued = in.readBoolean();
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		super.writeExternal(out);
		out.writeInt(minCount);
		out.writeBoolean(multivalued);
	}

	@Override
	public void writeXmlConfig(XmlWriter xmlWriter) throws SAXException {
		xmlWriter.startElement("field", "name", name, "minCount", Integer
				.toString(minCount), "multivalued", multivalued ? "yes" : "no");
		xmlWriter.endElement();
	}
}
