/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2013 Emmanuel Keller / Jaeksoft
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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlTransient;

import com.jaeksoft.searchlib.collapse.CollapseFunctionField;
import com.jaeksoft.searchlib.collapse.CollapseParameters.Function;

@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
public class FunctionFieldValue {

	final public Function function;
	final public String field;
	final public String value;

	public FunctionFieldValue() {
		function = null;
		field = null;
		value = null;
	}

	public FunctionFieldValue(CollapseFunctionField functionField, String value) {
		this.function = functionField.getFunction();
		this.field = functionField.getField();
		this.value = value;
	}

	@XmlTransient
	public Function getFunction() {
		return function;
	}

	@XmlTransient
	public String getField() {
		return field;
	}

	@XmlTransient
	public String getValue() {
		return value;
	}

}
