/*
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2013-2017 Emmanuel Keller / Jaeksoft
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
 */

package com.jaeksoft.searchlib.test.rest;

import com.jaeksoft.searchlib.webservice.query.search.SearchPatternQuery;
import com.jaeksoft.searchlib.webservice.query.search.SearchResult;
import org.apache.http.client.ClientProtocolException;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class RestSearchTest extends CommonRestAPI {

	@Test
	public void testA_WildcardSearchPattern() throws ClientProtocolException, IOException {
		SearchPatternQuery query = getResource("wildcard_search_pattern.json", SearchPatternQuery.class);
		SearchResult searchResult = searchPattern(query);
		assertEquals(10, searchResult.numFound);
	}
}
