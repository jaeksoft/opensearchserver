/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2008-2009 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.schema;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.util.External;
import com.jaeksoft.searchlib.util.XmlWriter;

public class SchemaFieldList extends FieldList<SchemaField> {

	private Field defaultField;
	private Field uniqueField;

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

	public Field getDefaultField() {
		return this.defaultField;
	}

	public Field getUniqueField() {
		return this.uniqueField;
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {
		super.readExternal(in);
		defaultField = External.<Field> readObject(in);
		uniqueField = External.<Field> readObject(in);
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		super.writeExternal(out);
		External.writeObject(defaultField, out);
		External.writeObject(uniqueField, out);
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
