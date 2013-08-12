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
package com.jaeksoft.searchlib.webservice.fields;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.Response.Status;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.jaeksoft.searchlib.schema.Indexed;
import com.jaeksoft.searchlib.schema.SchemaField;
import com.jaeksoft.searchlib.schema.Stored;
import com.jaeksoft.searchlib.schema.TermVector;
import com.jaeksoft.searchlib.webservice.CommonServices;

@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
@JsonInclude(Include.NON_NULL)
public class SchemaFieldRecord {

	public String name;

	public String analyzer;

	public Indexed indexed;

	public Stored stored;

	public TermVector termVector;

	public List<String> copyOf;

	public SchemaFieldRecord() {
		name = null;
		analyzer = null;
		indexed = null;
		stored = null;
		termVector = null;
		copyOf = null;
	}

	public SchemaFieldRecord(SchemaField schemaField) {
		this.name = schemaField.getName();
		this.analyzer = schemaField.getIndexAnalyzer();
		this.indexed = schemaField.getIndexed();
		this.stored = schemaField.getStored();
		this.termVector = schemaField.getTermVector();
		this.copyOf = schemaField.getCopyOf() == null ? null
				: new ArrayList<String>(schemaField.getCopyOf());
	}

	public void toShemaField(SchemaField schemaField) {
		if (analyzer != null)
			schemaField.setIndexAnalyzer(analyzer);
		if (indexed != null)
			schemaField.setIndexed(indexed);
		if (name == null)
			throw new CommonServices.CommonServiceException(Status.BAD_REQUEST,
					"The name is missing");
		schemaField.setName(name);
		if (stored != null)
			schemaField.setStored(stored);
		if (termVector != null)
			schemaField.setTermVector(termVector);
		if (copyOf != null)
			schemaField.setCopyOf(copyOf);
	}
}
