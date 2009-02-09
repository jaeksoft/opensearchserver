/**   
 * License Agreement for Jaeksoft SearchLib Community
 *
 * Copyright (C) 2008 Emmanuel Keller / Jaeksoft
 * 
 * http://www.jaeksoft.com
 * 
 * This file is part of Jaeksoft SearchLib Community.
 *
 * Jaeksoft SearchLib Community is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * Jaeksoft SearchLib Community is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Jaeksoft SearchLib Community. 
 *  If not, see <http://www.gnu.org/licenses/>.
 **/

package com.jaeksoft.searchlib.facet;

import java.io.IOException;

import org.w3c.dom.Node;

import com.jaeksoft.searchlib.result.ResultSingle;
import com.jaeksoft.searchlib.schema.Field;
import com.jaeksoft.searchlib.schema.FieldList;
import com.jaeksoft.searchlib.schema.SchemaField;
import com.jaeksoft.searchlib.util.XPathParser;

public class FacetField extends Field {

	/**
	 * 
	 */
	private static final long serialVersionUID = -941940505054128025L;

	private int minCount;

	private boolean multivalued;

	protected FacetField(String name, int minCount, boolean multivalued) {
		super(name);
		this.minCount = minCount;
		this.multivalued = multivalued;
	}

	@Override
	public Object clone() {
		return new FacetField(name, minCount, multivalued);
	}

	public int getMinCount() {
		return minCount;
	}

	public Facet getFacetInstance(ResultSingle result) throws IOException {
		if (multivalued)
			return new FacetMultivalued(result, this);
		else
			return new FacetSingle(result, this);
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
}
