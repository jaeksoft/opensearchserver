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

package com.jaeksoft.searchlib.webservice.select;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

import org.json.JSONException;
import org.json.JSONObject;

import com.jaeksoft.searchlib.snippet.SnippetFieldValue;

@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
public class SnippetValueList extends FieldValueList {

	@XmlElement(name = "highlighted")
	public boolean highlighted;

	public SnippetValueList() {
		super();
		highlighted = false;
	}

	public SnippetValueList(boolean highlighted,
			SnippetFieldValue snippetFiedValue) {
		super(snippetFiedValue);
		this.highlighted = highlighted;
	}

	protected SnippetValueList(String fieldName) {
		super(fieldName);
	}

	public static final void add(JSONObject json, List<SnippetValueList> list)
			throws JSONException {
		String fieldName = json.getString("name");
		String value = json.getString("value");
		for (SnippetValueList snippetValueList : list)
			if (snippetValueList.fieldName.equals(fieldName)) {
				snippetValueList.values.add(value);
				return;
			}
		SnippetValueList snippetValueList = new SnippetValueList(fieldName);
		snippetValueList.values.add(value);
		list.add(snippetValueList);
	}

}
