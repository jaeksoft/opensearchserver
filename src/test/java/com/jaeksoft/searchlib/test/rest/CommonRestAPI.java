/*
 * License Agreement for OpenSearchServer
 * <p>
 * Copyright (C) 2013-2017 Emmanuel Keller / Jaeksoft
 * <p>
 * http://www.open-search-server.com
 * <p>
 * This file is part of OpenSearchServer.
 * <p>
 * OpenSearchServer is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * OpenSearchServer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with OpenSearchServer.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package com.jaeksoft.searchlib.test.rest;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jaeksoft.searchlib.test.IntegrationTest;
import com.jaeksoft.searchlib.webservice.CommonResult;
import com.jaeksoft.searchlib.webservice.query.search.SearchResult;
import com.jaeksoft.searchlib.webservice.query.search.SearchQueryAbstract;
import com.jaeksoft.searchlib.webservice.query.search.SearchPatternQuery;
import com.jaeksoft.searchlib.webservice.query.search.SearchFieldQuery;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

public abstract class CommonRestAPI {

	public WebTarget client() {
		return ClientBuilder.newClient().target(IntegrationTest.SERVER_URL);
	}

	public <T extends CommonResult> T checkCommonResult(Response response, Class<T> commonResultClass, int httpCode) {
		assertNotNull(response);
		assertEquals((int) httpCode, response.getStatus());
		String json = response.getEntity().toString();
		T commonResult = response.readEntity(commonResultClass);
		assertNotNull(commonResult.successful);
		assertEquals(true, commonResult.successful);
		return commonResult;
	}

	private final static ObjectMapper objectMapper = new ObjectMapper();

	public <T extends CommonResult> T checkCommonResult(CloseableHttpResponse response, Class<T> commonResultClass,
			int httpCode) throws JsonParseException, JsonMappingException, UnsupportedOperationException, IOException {
		assertNotNull(response);
		assertEquals((int) httpCode, response.getStatusLine().getStatusCode());
		T commonResult = objectMapper.readValue(response.getEntity().getContent(), commonResultClass);
		assertNotNull(commonResult.successful);
		assertEquals(true, commonResult.successful);
		return commonResult;
	}

	public void checkCommonResultDetail(CommonResult commonResult, String detail, Object expectedValue) {
		Object value = commonResult.details.get(detail);
		assertEquals(expectedValue, value);
	}

	public String getResource(String name) throws IOException {
		InputStream is = getClass().getResourceAsStream(name);
		assertNotNull("Resource not found: " + name, is);
		String res = IOUtils.toString(is, "UTF-8");
		assertFalse("Resource is empty: " + name, StringUtils.isEmpty(res));
		return res;
	}

	public <T> T getResource(String name, Class<? extends T> objectClass) throws IOException {
		return objectMapper.readValue(getResource(name), objectClass);
	}

	private SearchResult search(SearchQueryAbstract query, String path) throws ClientProtocolException, IOException {
		Response response = client().path(path).request(MediaType.APPLICATION_JSON).post(Entity.json(query));
		return checkCommonResult(response, SearchResult.class, 200);
	}

	public SearchResult searchPattern(SearchPatternQuery query) throws ClientProtocolException, IOException {
		return search(query, "/services/rest/index/" + IntegrationTest.INDEX_NAME + "/search/pattern");
	}

	public SearchResult searchField(SearchFieldQuery query) throws ClientProtocolException, IOException {
		return search(query, "/services/rest/index/" + IntegrationTest.INDEX_NAME + "/search/field");
	}

	public void updateDocuments(String json) throws ClientProtocolException, IOException {
		Response response = client().path("/services/rest/index/" + IntegrationTest.INDEX_NAME + "/document").request(
				MediaType.APPLICATION_JSON).put(Entity.entity(json, MediaType.APPLICATION_JSON));
		checkCommonResult(response, CommonResult.class, 200);
	}

	public void deleteAll() throws ClientProtocolException, IOException {
		Response response = client().path("/services/rest/index/" + IntegrationTest.INDEX_NAME + "/document")
				.queryParam("query", "*:*")
				.request(MediaType.APPLICATION_JSON)
				.delete();
		checkCommonResult(response, CommonResult.class, 200);
	}

}
