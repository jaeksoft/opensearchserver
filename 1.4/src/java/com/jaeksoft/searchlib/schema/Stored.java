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

import org.apache.lucene.document.Field.Store;

public enum Stored {

	YES("The content of this field is stored, and queries can thus return it.",
			Store.YES),

	NO("The content of this field is not stored, and no query can return it.",
			Store.NO),

	COMPRESS(
			"The content of this field is stored, and queries can thus return it. This setting is useful for large bodies of text.",
			Store.YES);

	final private String description;
	final public Store luceneStore;
	final public String value;

	private Stored(String description, Store luceneStore) {
		this.description = description;
		this.luceneStore = luceneStore;
		this.value = name().toLowerCase();
	}

	final public String getDescription() {
		return description;
	}

	final public Store getLuceneStore() {
		return luceneStore;
	}

	final public String getValue() {
		return value;
	}

	final public static Stored fromValue(String value) {
		for (Stored fs : values())
			if (fs.name().equalsIgnoreCase(value))
				return fs;
		return Stored.NO;
	}

}
