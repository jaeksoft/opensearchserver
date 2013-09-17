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
import java.util.Set;
import java.util.TreeSet;

import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.collapse.CollapseParameters.Function;
import com.jaeksoft.searchlib.index.FieldCacheIndex;
import com.jaeksoft.searchlib.index.ReaderLocal;
import com.jaeksoft.searchlib.util.DomUtils;
import com.jaeksoft.searchlib.util.StringUtils;
import com.jaeksoft.searchlib.util.XmlWriter;

public class CollapseFunctionField implements Comparable<CollapseFunctionField> {

	private Function function;
	private String field;
	private transient FieldCacheIndex stringIndex;

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

	public void prepareExecute(ReaderLocal reader) throws IOException {
		this.stringIndex = reader.getStringIndex(field);
	}

	public String execute(int doc, int[] collapsedDocs) {
		return function.executor.execute(stringIndex, doc, collapsedDocs);
	}

	public static Set<CollapseFunctionField> duplicate(Set<CollapseFunctionField> functionFields) {
		if (functionFields == null)
			return null;
		return new TreeSet<CollapseFunctionField>(functionFields);
	}

	public static CollapseFunctionField fromXmlConfig(Node node) {
		return new CollapseFunctionField(Function.valueOf(DomUtils.getAttributeText(
				node, "function")), DomUtils.getAttributeText(node, "field"));
	}

	public void writeXmlConfig(XmlWriter xmlWriter, String nodeName)
			throws SAXException {
		xmlWriter.startElement(nodeName, "function", function.name(), "field",
				field);
		xmlWriter.endElement();
	}

}