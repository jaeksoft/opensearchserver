/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2010-2012 Emmanuel Keller / Jaeksoft
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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.lucene.search.FieldCache.StringIndex;

import com.jaeksoft.searchlib.request.SearchRequest;
import com.jaeksoft.searchlib.result.ResultScoreDoc;
import com.jaeksoft.searchlib.result.ResultScoreDocCollapse;
import com.jaeksoft.searchlib.util.Timer;

public class CollapseCluster extends CollapseAbstract {

	protected CollapseCluster(SearchRequest searchRequest) {
		super(searchRequest);
	}

	@Override
	protected int collapse(ResultScoreDoc[] fetchedDocs, int fetchLength,
			StringIndex collapseStringIndex, Timer timer) {

		Timer t = new Timer(timer, "Build collapse map");
		Map<String, ResultScoreDocCollapse> collapsedDocMap = new LinkedHashMap<String, ResultScoreDocCollapse>();
		ResultScoreDocCollapse collapseDoc;
		for (int i = 0; i < fetchLength; i++) {
			ResultScoreDoc fetchedDoc = fetchedDocs[i];
			String term = collapseStringIndex.lookup[collapseStringIndex.order[fetchedDoc.doc]];
			if (term != null
					&& ((collapseDoc = collapsedDocMap.get(term)) != null)) {
				collapseDoc.collapsedDocs = ArrayUtils.<ResultScoreDoc> add(
						collapseDoc.collapsedDocs, fetchedDoc);
			} else {
				collapsedDocMap.put(term, fetchedDoc.newCollapseInstance());
			}
		}
		t.duration();

		int collapsedDocCount = 0;
		t = new Timer(timer, "Build collapse array");
		int max = getCollapseMax();
		if (max <= 1) {
			ResultScoreDoc[] collapsedDocs = new ResultScoreDoc[collapsedDocMap
					.size()];
			collapsedDocMap.values().toArray(collapsedDocs);
			collapsedDocCount = fetchLength - collapsedDocs.length;
			setCollapsedDoc(collapsedDocs);
		} else {
			List<ResultScoreDoc> collapsedList = new ArrayList<ResultScoreDoc>(
					0);
			for (ResultScoreDocCollapse rsdc : collapsedDocMap.values())
				rsdc.populateList(collapsedList, max);
			collapsedDocCount = fetchLength - collapsedList.size();
			setCollapsedDoc(collapsedList);
		}
		t.duration();
		return collapsedDocCount;
	}

}
