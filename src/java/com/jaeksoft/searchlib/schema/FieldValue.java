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

import java.util.ArrayList;
import java.util.StringTokenizer;

public class FieldValue extends Field {

	private static final long serialVersionUID = -6131981428734961071L;

	private ArrayList<String> values;

	public FieldValue(FieldValue field) {
		this(field.name);
		this.values = field.values;
	}

	protected FieldValue(String name) {
		super(name);
		values = null;
	}

	public void clearValues() {
		if (values == null)
			return;
		values.clear();
		values = null;
	}

	public void addValue(String value) {
		if (values == null)
			values = new ArrayList<String>();
		values.add(value);
	}

	public void addValues(String[] vals) {
		if (vals == null)
			return;
		for (String value : vals)
			addValue(value);
	}

	public void addValues(ArrayList<String> vals) {
		if (vals == null)
			return;
		for (String value : vals)
			addValue(value);
	}

	public int getValuesCount() {
		if (values == null)
			return 0;
		return values.size();
	}

	public ArrayList<String> getValues() {
		return values;
	}

	@Override
	public Object clone() {
		return new FieldValue(this);
	}

	/**
	 * Alimente la liste "target" avec les champs nommé dans le champ fl.
	 * 
	 * @param fl
	 *            "Champ1,Champ2,Champ3"
	 * @param target
	 *            Liste de champs destinataire des champs trouvés
	 */
	public static void filterCopy(FieldList<SchemaField> source, String filter,
			FieldList<FieldValue> target) {
		if (filter == null)
			return;
		StringTokenizer st = new StringTokenizer(filter, ", \t\r\n");
		while (st.hasMoreTokens()) {
			String fieldName = st.nextToken().trim();
			target.add(source.get(fieldName));
		}
	}

}
