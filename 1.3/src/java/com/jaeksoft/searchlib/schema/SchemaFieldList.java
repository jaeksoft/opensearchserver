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

import java.util.List;

import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.util.XmlWriter;

public class SchemaFieldList extends AbstractFieldList<SchemaField> {

	private SchemaField defaultField;
	private SchemaField uniqueField;

	public SchemaFieldList() {
		defaultField = null;
		uniqueField = null;
	}

	public void setDefaultField(String fieldName) {
		if (fieldName != null)
			this.defaultField = this.get(fieldName);
		else
			this.defaultField = null;
	}

	public void setUniqueField(String fieldName) {
		if (fieldName != null)
			this.uniqueField = this.get(fieldName);
		else
			this.uniqueField = null;
	}

	public SchemaField getDefaultField() {
		return this.defaultField;
	}

	public SchemaField getUniqueField() {
		return this.uniqueField;
	}

	public void getIndexedFields(List<String> fieldList) {
		for (SchemaField field : getList())
			if (field.isIndexed())
				fieldList.add(field.getName());
	}

	@Override
	public void writeXmlConfig(XmlWriter writer) throws SAXException {
		if (size() == 0)
			return;
		writer.startElement("fields", "default",
				defaultField != null ? defaultField.name : null, "unique",
				uniqueField != null ? uniqueField.name : null);
		for (SchemaField field : this)
			field.writeXmlConfig(writer);
		writer.endElement();

	}

}
