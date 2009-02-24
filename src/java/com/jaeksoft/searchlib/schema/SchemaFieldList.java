/**   
 * License Agreement for Jaeksoft SearchLib Community
 *
 * Copyright (C) 2008-2009 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.schema;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.PrintWriter;

import com.jaeksoft.searchlib.util.External;

public class SchemaFieldList extends FieldList<SchemaField> {

	private Field defaultField;
	private Field uniqueField;

	protected void setDefaultField(String fieldName) {
		this.defaultField = this.get(fieldName);
	}

	protected void setUniqueField(String fieldName) {
		this.uniqueField = this.get(fieldName);
	}

	public Field getDefaultField() {
		return this.defaultField;
	}

	public Field getUniqueField() {
		return this.uniqueField;
	}

	public void xmlInfo(PrintWriter writer) {
		writer.print("<fields");
		if (defaultField != null)
			writer.print(" default=\"" + defaultField.getName() + "\"");
		if (uniqueField != null)
			writer.print(" unique=\"" + uniqueField.getName() + "\"");
		writer.println(">");
		for (Field field : this)
			field.xmlInfo(writer);
		writer.println("</fields>");
	}

	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {
		super.readExternal(in);
		defaultField = External.<Field> readObject(in);
		uniqueField = External.<Field> readObject(in);
	}

	public void writeExternal(ObjectOutput out) throws IOException {
		super.writeExternal(out);
		External.writeObject(defaultField, out);
		External.writeObject(uniqueField, out);
	}

}
