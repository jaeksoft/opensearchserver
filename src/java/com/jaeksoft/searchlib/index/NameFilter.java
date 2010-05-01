/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2008 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.index;

public abstract class NameFilter {

	private String indexName;

	protected NameFilter(String name) {
		indexName = name;
	}

	public String getName() {
		return indexName;
	}

	protected boolean acceptNameOrEmpty(String indexName) {
		if (indexName == null)
			return true;
		if (indexName.length() == 0)
			return true;
		return indexName.equals(this.indexName);
	}

	protected boolean acceptOnlyRightName(String indexName) {
		if (indexName == null)
			return false;
		if (indexName.length() == 0)
			return false;
		return indexName.equals(this.indexName);
	}

}
