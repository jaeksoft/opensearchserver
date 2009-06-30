/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2009 Emmanuel Keller / Jaeksoft
 * 
 * http://www.open-search-server.com
 * 
 * This file is part of Jaeksoft OpenSearchServer.
 *
 * Jaeksoft OpenSearchServer is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * Jaeksoft OpenSearchServer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Jaeksoft OpenSearchServer. 
 *  If not, see <http://www.gnu.org/licenses/>.
 **/

package com.jaeksoft.searchlib.schema;

public enum TermVector {

	POSITIONS_OFFSETS(
			"The positions and offset of the tokens (words) are stored. This is mandatory to extract snippets from fields.",
			org.apache.lucene.document.Field.TermVector.WITH_POSITIONS_OFFSETS),

	NO("No term vector is stored (only mandatory for snippets).",
			org.apache.lucene.document.Field.TermVector.NO);

	private String description;
	private org.apache.lucene.document.Field.TermVector luceneTermVector;

	private TermVector(String description,
			org.apache.lucene.document.Field.TermVector luceneTermVector) {
		this.description = description;
		this.luceneTermVector = luceneTermVector;
	}

	public String getDescription() {
		return description;
	}

	public org.apache.lucene.document.Field.TermVector getLuceneTermVector() {
		return luceneTermVector;
	}

	public String getValue() {
		return name().toLowerCase();
	}

	public static TermVector fromValue(String value) {
		for (TermVector fs : values())
			if (fs.name().equalsIgnoreCase(value))
				return fs;
		return TermVector.NO;
	}

}
