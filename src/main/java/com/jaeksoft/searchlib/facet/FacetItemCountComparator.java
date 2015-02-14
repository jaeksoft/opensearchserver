/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2011-2015 Emmanuel Keller / Jaeksoft
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
package com.jaeksoft.searchlib.facet;

import java.util.Comparator;
import java.util.Map;

public class FacetItemCountComparator implements
		Comparator<Map.Entry<String, Long>> {

	@Override
	final public int compare(Map.Entry<String, Long> item1,
			Map.Entry<String, Long> item2) {
		int c = item1.getValue().compareTo(item2.getValue());
		if (c != 0)
			return c;
		return item1.getKey().compareTo(item2.getKey());
	}
}
