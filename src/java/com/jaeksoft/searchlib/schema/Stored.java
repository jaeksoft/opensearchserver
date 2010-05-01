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

import org.apache.lucene.document.Field.Store;

public enum Stored {

	YES("The content of this field is stored, and queries can thus return it.",
			Store.YES),

	NO("The content of this field is not stored, and no query can return it.",
			Store.NO),

	COMPRESS(
			"The content of this field is stored, and queries can thus return it. This setting is useful for large bodies of text.",
			Store.COMPRESS);

	private String description;
	private Store luceneStore;

	private Stored(String description, Store luceneStore) {
		this.description = description;
		this.luceneStore = luceneStore;
	}

	public String getDescription() {
		return description;
	}

	public Store getLuceneStore() {
		return luceneStore;
	}

	public String getValue() {
		return name().toLowerCase();
	}

	public static Stored fromValue(String value) {
		for (Stored fs : values())
			if (fs.name().equalsIgnoreCase(value))
				return fs;
		return Stored.NO;
	}

}
