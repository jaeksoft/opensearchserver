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

package com.jaeksoft.searchlib.index;

import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;

import com.jaeksoft.searchlib.util.XmlInfo;

public class FieldContent implements Serializable, XmlInfo {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4734981884898319100L;
	private String field;
	private List<String> values;

	public FieldContent(String field) {
		this.field = field;
		values = new ArrayList<String>();
	}

	public String getField() {
		return field;
	}

	public void add(String value) {
		values.add(value);
	}

	public void clear() {
		values.clear();
	}

	public String getValue(int pos) {
		if (values == null)
			return null;
		return values.get(pos);
	}

	public List<String> getValues() {
		return values;
	}

	public void xmlInfo(PrintWriter writer, HashSet<String> classDetail) {
		writer.println("<field name=\"" + field + "\">");
		for (String value : values)
			writer.println("<value>" + StringEscapeUtils.escapeXml(value)
					+ "</value>");
		writer.println("</field>");
	}

}
