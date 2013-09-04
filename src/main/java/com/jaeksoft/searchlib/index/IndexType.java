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

package com.jaeksoft.searchlib.index;

public enum IndexType {

	OSSE("OpenSearchServer"),

	LUCENE("Apache Lucene");

	private final String label;

	private IndexType(String label) {
		this.label = label;
	}

	/**
	 * @return the label
	 */
	public final String getLabel() {
		return label;
	}

	public final static IndexType find(String name) {
		for (IndexType type : values())
			if (type.name().equals(name))
				return type;
		return LUCENE;
	}
}
