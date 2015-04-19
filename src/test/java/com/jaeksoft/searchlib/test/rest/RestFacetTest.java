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

package com.jaeksoft.searchlib.test.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.util.TreeMap;

import org.apache.http.client.ClientProtocolException;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import com.jaeksoft.searchlib.webservice.query.search.FacetFieldItem;
import com.jaeksoft.searchlib.webservice.query.search.FacetResult;
import com.jaeksoft.searchlib.webservice.query.search.SearchResult;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class RestFacetTest extends CommonRestAPI {

	@Test
	public void testA_DeleteAll() throws ClientProtocolException, IOException {
		deleteAll();
	}

	@Test
	public void testB_injectDocuments() throws IOException {
		String json = getResource("facet_single_docs.json");
		updateDocuments(json);
	}

	final static String[] CATEGORIES = { "blue", "green", "red", "yellow" };

	private void facetTest(String searchJson, long[] expectedCounts)
			throws ClientProtocolException, IOException {

		// Build expected structure
		TreeMap<String, Long> facetValues = new TreeMap<String, Long>();
		for (int i = 0; i < expectedCounts.length; i++)
			facetValues.put(CATEGORIES[i], expectedCounts[i]);

		SearchResult searchResult = searchField(searchJson);
		assertNotNull(searchResult.facets);
		FacetResult facetCategory = null;
		for (FacetResult facet : searchResult.facets) {
			if (facet.fieldName.equals("category")) {
				facetCategory = facet;
				break;
			}
		}
		assertNotNull("Category facet not found", facetCategory);
		for (FacetFieldItem facetFieldItem : facetCategory.terms) {
			Long res = facetValues.get(facetFieldItem.term);
			assertNotNull("Unexpected facet: " + facetFieldItem.term, res);
			assertEquals("Facet count is wrong", (long) res,
					facetFieldItem.count);
			facetValues.remove(facetFieldItem.term);
		}
		assertEquals("Non-found facet remains", 0, facetValues.size());
	}

	@Test
	public void testC_FacetSinglePreCollapse() throws ClientProtocolException,
			IOException {
		String json = getResource("facet_single_pre_collapse.json");
		final long[] results = { 2, 3, 4, 5 };
		facetTest(json, results);
	}

	@Test
	public void testC_FacetSinglePostCollapseCluster()
			throws ClientProtocolException, IOException {
		String json = getResource("facet_single_post_collapse_cluster.json");
		final long[] results = { 1, 1, 1, 1 };
		facetTest(json, results);
	}

	@Test
	public void testC_FacetSinglePostCollapseAdjacent()
			throws ClientProtocolException, IOException {
		String json = getResource("facet_single_post_collapse_adjacent.json");
		final long[] results = { 1, 1, 1, 1 };
		facetTest(json, results);
	}
}
