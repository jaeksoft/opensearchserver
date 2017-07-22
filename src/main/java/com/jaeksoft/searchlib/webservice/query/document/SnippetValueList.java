/**
 * License Agreement for OpenSearchServer
 * <p>
 * Copyright (C) 2011-2013 Emmanuel Keller / Jaeksoft
 * <p>
 * http://www.open-search-server.com
 * <p>
 * This file is part of OpenSearchServer.
 * <p>
 * OpenSearchServer is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * OpenSearchServer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with OpenSearchServer.
 * If not, see <http://www.gnu.org/licenses/>.
 **/

package com.jaeksoft.searchlib.webservice.query.document;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.jaeksoft.searchlib.snippet.SnippetFieldValue;
import org.json.JSONException;
import org.json.JSONObject;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import java.util.List;

@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
public class SnippetValueList extends FieldValueList {

	@XmlElement(name = "highlighted")
	@JsonProperty("highlighted")
	final public boolean highlighted;

	public SnippetValueList() {
		super();
		highlighted = false;
	}

	public SnippetValueList(SnippetFieldValue snippetFiedValue) {
		super(snippetFiedValue);
		this.highlighted = snippetFiedValue.isHighlighted();
	}

	protected SnippetValueList(String fieldName) {
		super(fieldName);
		highlighted = false;
	}

	public static final void addSnippetValue(JSONObject json, List<SnippetValueList> list) throws JSONException {
		if (!json.has("value"))
			return;
		String fieldName = json.getString("name");
		String value = json.getString("value");
		for (SnippetValueList snippetValueList : list) {
			if (snippetValueList.fieldName.equals(fieldName)) {
				snippetValueList.values.add(value);
				return;
			}
		}
		SnippetValueList snippetValueList = new SnippetValueList(fieldName);
		snippetValueList.values.add(value);
		list.add(snippetValueList);
	}

	@XmlTransient
	public boolean getHighlighted() {
		return highlighted;
	}

}
