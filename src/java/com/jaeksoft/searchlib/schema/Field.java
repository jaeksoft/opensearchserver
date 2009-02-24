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

package com.jaeksoft.searchlib.schema;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.PrintWriter;
import java.util.StringTokenizer;

import org.apache.lucene.document.FieldSelector;
import org.apache.lucene.document.FieldSelectorResult;

import com.jaeksoft.searchlib.util.XmlInfo;

public class Field implements FieldSelector, Externalizable, XmlInfo {

	private static final long serialVersionUID = -7666123998960959190L;

	protected String name;

	public Field() {
	}

	protected Field(String name) {
		this.name = name;
	}

	public Field(Field field) {
		this.name = field.name;
	}

	public Field newInstance(Field field) {
		return new Field(field);
	}

	public FieldSelectorResult accept(String fieldName) {
		if (this.name.equals(fieldName))
			return FieldSelectorResult.LOAD;
		return FieldSelectorResult.NO_LOAD;
	}

	public void xmlInfo(PrintWriter writer) {
		writer.print("<field name=\"" + name + "\"/>");
	}

	public String getName() {
		return name;
	}

	public void toString(StringBuffer sb) {
		sb.append(name);
	}

	public boolean equals(Field field) {
		return field.name == this.name;
	}

	/**
	 * Alimente la liste "target" avec les champs nommé dans le champ fl.
	 * 
	 * @param fl
	 *            "Champ1,Champ2,Champ3"
	 * @param target
	 *            Liste de champs destinataire des champs trouvés
	 */
	public static <T extends Field> void filterCopy(FieldList<T> source,
			String filter, FieldList<Field> target) {
		if (filter == null)
			return;
		StringTokenizer st = new StringTokenizer(filter, ", \t\r\n");
		while (st.hasMoreTokens()) {
			String fieldName = st.nextToken().trim();
			target.add(new Field(source.get(fieldName)));
		}
	}

	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {
		name = in.readUTF();
	}

	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeUTF(name);
	}

}
