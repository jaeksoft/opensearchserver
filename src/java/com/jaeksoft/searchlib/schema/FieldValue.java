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
import java.util.List;
import java.util.StringTokenizer;

public class FieldValue extends Field {

	private static final long serialVersionUID = -6131981428734961071L;

	private String[] valueArray;
	private transient List<String> valueList;

	protected FieldValue(String name) {
		super(name);
		valueArray = null;
		valueList = null;
	}

	public FieldValue(FieldValue field) {
		this(field.name);
		this.valueArray = field.valueArray;
	}

	public FieldValue(Field field, String[] values) {
		super(field.name);
		setValues(values);
	}

	public FieldValue(Field field, List<String> values) {
		super(field.name);
		setValues(values);
	}

	public int getValuesCount() {
		if (valueArray == null)
			return 0;
		return valueArray.length;
	}

	public String[] getValueArray() {
		return valueArray;
	}

	public List<String> getValueList() {
		if (valueList != null)
			return valueList;
		if (valueArray == null)
			return null;
		valueList = new ArrayList<String>();
		for (String value : valueArray)
			valueList.add(value);
		return valueList;
	}

	public void setValues(String[] values) {
		valueArray = values;
		valueList = null;
	}

	public void setValues(List<String> values) {
		if (values == null) {
			valueArray = null;
			valueList = null;
			return;
		}
		valueArray = new String[values.size()];
		values.toArray(valueArray);
		valueList = null;
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
