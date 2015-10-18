/**   
 * License Agreement for OpenSearchServer Pojodbc
 *
 * Copyright 2008-2013 Emmanuel Keller / Jaeksoft
 * Copyright 2014-2015 OpenSearchServer Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jaeksoft.pojodbc;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Represents a row from a ResultSet. A convienient way to retrieve data from
 * ResultSet if you don't want to use POJO.
 * 
 * @author ekeller
 * 
 */
public class Row {

	private Object[] columns;

	protected Row(int columnCount) {
		columns = new Object[columnCount];
	}

	protected Row(int columnCount, ResultSet rs) throws SQLException {
		this(columnCount);
		for (int columnIndex = 0; columnIndex < columnCount; columnIndex++)
			columns[columnIndex] = rs.getObject(columnIndex + 1);
	}

	public void set(int column, Object value) {
		columns[column] = value;
	}

	public Object get(int column) {
		Object col = columns[column];
		if (col == null)
			return null;
		return col;
	}
}
