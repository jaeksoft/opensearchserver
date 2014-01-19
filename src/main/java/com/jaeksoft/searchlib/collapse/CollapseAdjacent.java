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

import org.apache.lucene.util.OpenBitSet;

import com.jaeksoft.searchlib.index.FieldCacheIndex;
import com.jaeksoft.searchlib.request.AbstractSearchRequest;
import com.jaeksoft.searchlib.result.collector.DocIdInterface;
import com.jaeksoft.searchlib.result.collector.collapsing.CollapseCollectorInterface;
import com.jaeksoft.searchlib.util.Timer;

public class CollapseAdjacent extends CollapseAbstract {

	protected CollapseAdjacent(AbstractSearchRequest searchRequest) {
		super(searchRequest);
	}

	@Override
	protected CollapseCollectorInterface collapse(DocIdInterface collector,
			int fetchLength, FieldCacheIndex collapseStringIndex, Timer timer) {

		Timer t = new Timer(timer, "adjacent collapse");

		OpenBitSet collapsedSet = new OpenBitSet(fetchLength);

		int[] ids = collector.getIds();
		String lastTerm = null;
		int adjacent = 0;
		for (int i = 0; i < fetchLength; i++) {
			String term = collapseStringIndex.lookup[collapseStringIndex.order[ids[i]]];
			if (term != null && term.equals(lastTerm)) {
				if (++adjacent >= getCollapseMax())
					collapsedSet.set(i);
			} else {
				lastTerm = term;
				adjacent = 0;
			}
		}

		int collapsedDocCount = (int) collapsedSet.cardinality();
		CollapseCollectorInterface collapseCollector = getNewCollapseInterfaceInstance(
				collector, fetchLength - collapsedDocCount,
				getCollectDocArray());
		int collapsePos = -1;
		for (int i = 0; i < fetchLength; i++) {
			if (!collapsedSet.get(i))
				collapsePos = collapseCollector.collectDoc(i);
			else
				collapseCollector.collectCollapsedDoc(i, collapsePos);
		}
		collapseCollector.endCollection();

		t.getDuration();

		return collapseCollector;

	}

}
