/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008-2009 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.sort;

import com.jaeksoft.searchlib.result.collector.DocIdInterface;

public class AscScoreSorter extends AbstractScoreSorter {

	public AscScoreSorter(DocIdInterface collector) {
		super(collector);
	}

	@Override
	final public int compare(int pos1, int pos2) {
		float s1 = scores[pos1];
		float s2 = scores[pos2];
		if (s1 > s2)
			return 1;
		else if (s1 < s2)
			return -1;
		else
			return 0;
	}

}
