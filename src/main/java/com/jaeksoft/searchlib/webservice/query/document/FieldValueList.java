/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2011-2013 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.webservice.query.document;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

import org.json.JSONException;
import org.json.JSONObject;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.jaeksoft.searchlib.schema.FieldValue;
import com.jaeksoft.searchlib.schema.FieldValueItem;

@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
@JsonInclude(Include.NON_NULL)
public class FieldValueList {

	@XmlElement(name = "name")
	public String fieldName;

	@XmlElement(name = "value")
	public List<String> values;

	public FieldValueList() {
		fieldName = null;
		values = null;
	}

	public FieldValueList(FieldValue fieldValue) {
		this.fieldName = fieldValue.getName();
		values = new ArrayList<String>();
		for (FieldValueItem item : fieldValue.getValueArray())
			values.add(item.getValue());
	}

	protected FieldValueList(String fieldName) {
		this.fieldName = fieldName;
		values = new ArrayList<String>(1);
	}

	public static final void addFieldValue(JSONObject json,
			List<FieldValueList> list) throws JSONException {
		if (!json.has("value"))
			return;
		String fieldName = json.getString("name");
		String value = json.getString("value");
		for (FieldValueList fieldValueList : list)
			if (fieldValueList.fieldName.equals(fieldName)) {
				fieldValueList.values.add(value);
				return;
			}
		FieldValueList fieldValueList = new FieldValueList(fieldName);
		fieldValueList.values.add(value);
		list.add(fieldValueList);
	}

	public static final FieldValueList getField(
			List<? extends FieldValueList> fields, String name) {
		for (FieldValueList field : fields)
			if (name.equals(field.fieldName))
				return field;
		return null;
	}

}
