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

import org.w3c.dom.Node;

import com.jaeksoft.searchlib.schema.AbstractField;
import com.jaeksoft.searchlib.util.DomUtils;

public class ReturnField extends AbstractField<ReturnField> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6404098756290152049L;

	public ReturnField(String fieldName) {
		super(fieldName);
	}

	@Override
	public ReturnField duplicate() {
		return new ReturnField(this.name);
	}

	final public static ReturnField fromXmlConfig(Node node) {
		String name = DomUtils.getAttributeText(node, "name");
		if (name == null)
			return null;
		return new ReturnField(name);
	}

	@Override
	public String toString() {
		return this.name;
	}

}
