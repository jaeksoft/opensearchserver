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
package com.jaeksoft.searchlib.webservice.fields;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.jaeksoft.searchlib.schema.SchemaField;
import com.jaeksoft.searchlib.webservice.CommonResult;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
@XmlRootElement(name = "result")
public class ResultFieldList extends CommonResult {

	@XmlElement(name = "unique")
	@JsonProperty("unique")
	final public String uniqueField;

	@XmlElement(name = "default")
	@JsonProperty("default")
	final public String defaultField;

	final public List<SchemaFieldRecord> fields;

	public ResultFieldList() {
		fields = null;
		uniqueField = null;
		defaultField = null;
	}

	public ResultFieldList(Boolean successful, List<SchemaFieldRecord> fields,
			SchemaField uniqueField, SchemaField defaultField) {
		super(successful, fields.size() + " field(s)");
		this.fields = fields;
		this.uniqueField = uniqueField == null ? null : uniqueField.getName();
		this.defaultField = defaultField == null ? null : defaultField
				.getName();
	}
}
