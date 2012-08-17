/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2012 Emmanuel Keller / Jaeksoft
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

import java.util.Set;

import org.apache.lucene.document.FieldSelector;
import org.apache.lucene.document.FieldSelectorResult;

public class SetFieldSelector implements FieldSelector {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4722731991166993080L;

	private final Set<String> fieldSet;

	public SetFieldSelector(Set<String> fieldSet) {
		this.fieldSet = fieldSet;
	}

	@Override
	public FieldSelectorResult accept(String fieldName) {
		return fieldSet.contains(fieldName) ? FieldSelectorResult.LOAD
				: FieldSelectorResult.NO_LOAD;
	}

}
