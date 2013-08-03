/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2012 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.request;

import java.util.StringTokenizer;

import com.jaeksoft.searchlib.schema.AbstractFieldList;
import com.jaeksoft.searchlib.schema.SchemaField;
import com.jaeksoft.searchlib.schema.SchemaFieldList;

public class ReturnFieldList extends AbstractFieldList<ReturnField> {

	public ReturnFieldList(ReturnFieldList returnFieldList) {
		super(returnFieldList);
	}

	public ReturnFieldList() {
		super();
	}

	public void filterCopy(SchemaFieldList schemaFieldList, String nodeString) {
		if (nodeString == null)
			return;
		StringTokenizer st = new StringTokenizer(nodeString, ", \t\r\n");
		while (st.hasMoreTokens()) {
			String fieldName = st.nextToken().trim();
			SchemaField f = schemaFieldList.get(fieldName);
			if (f != null)
				put(new ReturnField(f.getName()));
		}

	}

}
