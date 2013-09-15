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

import java.io.IOException;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.http.client.ClientProtocolException;
import org.junit.Test;

import com.jaeksoft.searchlib.test.IntegrationTest;
import com.jaeksoft.searchlib.webservice.query.search.SearchResult;

public class RestSearchTest extends CommonRestAPI {

	@Test
	public void testA_WildcardSearchPattern() throws ClientProtocolException,
			IOException {
		String json = getResource("wildcard_search_pattern.json");
		Response response = client()
				.path("/services/rest/index/{index_name}/search/pattern",
						IntegrationTest.INDEX_NAME)
				.accept(MediaType.APPLICATION_JSON)
				.type(MediaType.APPLICATION_JSON).post(json);
		SearchResult searchResult = checkCommonResult(response,
				SearchResult.class, 200);
		assertEquals(3, searchResult.numFound);
	}
}
