/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2010-2013 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.collapse;

import java.util.Map;
import java.util.TreeMap;

import com.jaeksoft.searchlib.index.FieldCacheIndex;
import com.jaeksoft.searchlib.request.AbstractSearchRequest;
import com.jaeksoft.searchlib.result.collector.CollapseDocInterface;
import com.jaeksoft.searchlib.result.collector.DocIdInterface;
import com.jaeksoft.searchlib.util.Timer;

public class CollapseCluster extends CollapseAbstract {

	protected CollapseCluster(AbstractSearchRequest searchRequest) {
		super(searchRequest);
	}

	@Override
	protected CollapseDocInterface collapse(DocIdInterface collector,
			int fetchLength, FieldCacheIndex collapseStringIndex, Timer timer) {

		Timer t = new Timer(timer, "Build collapse map");
		Map<String, Integer> collapsedDocMap = new TreeMap<String, Integer>();
		int[] ids = collector.getIds();

		CollapseDocInterface collapseInterface = getNewCollapseInterfaceInstance(
				collector, fetchLength, getCollectDocArray());
		Integer collapsePos;

		for (int i = 0; i < fetchLength; i++) {
			String term = collapseStringIndex.lookup[collapseStringIndex.order[ids[i]]];
			if (term != null
					&& ((collapsePos = collapsedDocMap.get(term)) != null)) {
				collapseInterface.collectCollapsedDoc(i, collapsePos);
			} else {
				collapsePos = collapseInterface.collectDoc(i);
				if (term != null)
					collapsedDocMap.put(term, collapsePos);
			}
		}
		t.getDuration();

		return collapseInterface;
	}
}
