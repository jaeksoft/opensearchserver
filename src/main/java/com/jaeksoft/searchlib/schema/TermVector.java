/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2009-2011 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.schema;

import javax.xml.bind.annotation.XmlTransient;

public enum TermVector {

	POSITIONS_OFFSETS(
			"The position and offsets of the words/token are recorded. This allows for extracting snippets from this field.",
			org.apache.lucene.document.Field.TermVector.WITH_POSITIONS_OFFSETS),

	YES("Store the term vectors of each document",
			org.apache.lucene.document.Field.TermVector.YES),

	NO(
			"Term vectors are not recorded. This prevents using snippets in this field.",
			org.apache.lucene.document.Field.TermVector.NO);

	final public String description;
	@XmlTransient
	final public org.apache.lucene.document.Field.TermVector luceneTermVector;
	final public String value;

	private TermVector(String description,
			org.apache.lucene.document.Field.TermVector luceneTermVector) {
		this.description = description;
		this.luceneTermVector = luceneTermVector;
		this.value = name().toLowerCase();
	}

	final public String getDescription() {
		return description;
	}

	final public org.apache.lucene.document.Field.TermVector getLuceneTermVector() {
		return luceneTermVector;
	}

	final public String getValue() {
		return value;
	}

	final public static TermVector fromValue(String value) {
		for (TermVector fs : values())
			if (fs.name().equalsIgnoreCase(value))
				return fs;
		return TermVector.NO;
	}

}
