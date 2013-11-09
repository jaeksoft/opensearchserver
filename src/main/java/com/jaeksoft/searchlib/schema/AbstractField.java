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

package com.jaeksoft.searchlib.schema;

import org.apache.lucene.document.FieldSelector;
import org.apache.lucene.document.FieldSelectorResult;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.util.XmlWriter;

public abstract class AbstractField<T extends AbstractField<?>> implements
		FieldSelector, Comparable<T> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7991705496490656001L;

	protected String name;

	public AbstractField() {
		name = null;
	}

	public AbstractField(String name) {
		this.name = name;
	}

	public AbstractField(T field) {
		this.name = field.name;
	}

	public void copyFrom(T sourceField) {
		this.name = sourceField.name;
	}

	public abstract T duplicate();

	@Override
	public FieldSelectorResult accept(String fieldName) {
		if (this.name.equals(fieldName))
			return FieldSelectorResult.LOAD;
		return FieldSelectorResult.NO_LOAD;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void toString(StringBuilder sb) {
		sb.append(name);
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof AbstractField))
			return false;
		return name.equals(((AbstractField<?>) o).name);
	}

	public boolean equals(T field) {
		return field.name.equals(this.name);
	}

	public void writeXmlConfig(XmlWriter xmlWriter) throws SAXException {
		xmlWriter.startElement("field", "name", name);
		xmlWriter.endElement();
	}

	@Override
	public int compareTo(T o) {
		return name.compareTo(o.name);
	}

}
