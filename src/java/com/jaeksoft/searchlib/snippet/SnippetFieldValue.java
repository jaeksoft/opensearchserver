/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008-2012 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.snippet;

import java.util.List;

import com.jaeksoft.searchlib.schema.FieldValue;
import com.jaeksoft.searchlib.schema.FieldValueItem;

public class SnippetFieldValue extends FieldValue {

	/**
	 * 
	 */
	private static final long serialVersionUID = 9069619321715897099L;

	private boolean highlighted;

	public SnippetFieldValue() {
	}

	private SnippetFieldValue(SnippetFieldValue field) {
		super(field);
		this.highlighted = field.highlighted;
	}

	public SnippetFieldValue(String fieldName, List<FieldValueItem> values,
			boolean highlighted) {
		super(fieldName, values);
		this.highlighted = highlighted;
	}

	public boolean isHighlighted() {
		return highlighted;
	}

	@Override
	public SnippetFieldValue duplicate() {
		return new SnippetFieldValue(this);
	}

}
