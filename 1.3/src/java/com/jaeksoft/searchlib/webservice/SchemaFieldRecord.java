/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2011-2012 Emmanuel Keller / Jaeksoft
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
package com.jaeksoft.searchlib.webservice;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

import com.jaeksoft.searchlib.schema.Indexed;
import com.jaeksoft.searchlib.schema.Stored;
import com.jaeksoft.searchlib.schema.TermVector;

@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
public class SchemaFieldRecord {

	@XmlElement(name = "name")
	public String name;

	@XmlElement(name = "indexAnalyzer")
	public String indexAnalyzer;

	@XmlElement(name = "indexed")
	public Indexed indexed;

	@XmlElement(name = "stored")
	public Stored stored;

	@XmlElement(name = "termVector")
	public TermVector termVector;

	public SchemaFieldRecord() {
		name = null;
		indexAnalyzer = null;
		indexed = null;
		stored = null;
		termVector = null;

	}

	public SchemaFieldRecord(String name, String analyzer, Indexed indexed,
			Stored stored, TermVector termVector) {
		this.name = name;
		this.indexAnalyzer = analyzer;
		this.indexed = indexed;
		this.stored = stored;
		this.termVector = termVector;
	}
}
