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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import com.jaeksoft.searchlib.schema.Indexed;
import com.jaeksoft.searchlib.schema.SchemaField;
import com.jaeksoft.searchlib.schema.Stored;
import com.jaeksoft.searchlib.schema.TermVector;

@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
public class SchemaFieldRecord {

	public String name;

	public String indexAnalyzer;

	public Indexed indexed;

	public Stored stored;

	public TermVector termVector;

	public List<String> copyOf;

	public SchemaFieldRecord() {
		name = null;
		indexAnalyzer = null;
		indexed = null;
		stored = null;
		termVector = null;
		copyOf = null;
	}

	public SchemaFieldRecord(SchemaField schemaField) {
		this.name = schemaField.getName();
		this.indexAnalyzer = schemaField.getIndexAnalyzer();
		this.indexed = schemaField.getIndexed();
		this.stored = schemaField.getStored();
		this.termVector = schemaField.getTermVector();
		this.copyOf = schemaField.getCopyOf() == null ? null
				: new ArrayList<String>(schemaField.getCopyOf());
	}
}
