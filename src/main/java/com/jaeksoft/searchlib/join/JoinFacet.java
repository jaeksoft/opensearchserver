/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2012-2014 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.join;

import java.io.IOException;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.facet.Facet;
import com.jaeksoft.searchlib.facet.FacetField;
import com.jaeksoft.searchlib.facet.FacetFieldList;
import com.jaeksoft.searchlib.index.ReaderAbstract;
import com.jaeksoft.searchlib.result.ResultSearchSingle;
import com.jaeksoft.searchlib.result.collector.DocIdInterface;
import com.jaeksoft.searchlib.result.collector.join.JoinDocCollector;
import com.jaeksoft.searchlib.util.Timer;

public class JoinFacet {

	private JoinResult joinResult;

	private FacetFieldList facetFieldList;

	private ResultSearchSingle resultSearch;

	public JoinFacet(JoinResult joinResult, FacetFieldList facetFieldList,
			ResultSearchSingle resultSearch) {
		this.joinResult = joinResult;
		this.facetFieldList = new FacetFieldList(facetFieldList);
		this.resultSearch = resultSearch;
	}

	public void apply(DocIdInterface collector, Timer timer)
			throws SearchLibException {
		try {
			ReaderAbstract readerAbstract = resultSearch.getReader();
			int maxDoc = (int) readerAbstract.maxDoc();
			for (FacetField facetField : facetFieldList) {
				DocIdInterface facetCollector;
				Facet facet = resultSearch.getFacetList().getByField(
						facetField.getName());
				if (collector instanceof JoinDocCollector) {
					facetCollector = JoinDocCollector.getDocIdInterface(maxDoc,
							joinResult.joinPosition,
							(JoinDocCollector) collector);
					facet = facetField.getFacet(readerAbstract, facetCollector,
							null, timer);
				}
				if (facet != null)
					joinResult.add(facet);
			}
		} catch (IOException e) {
			throw new SearchLibException(e);
		}
	}
}
