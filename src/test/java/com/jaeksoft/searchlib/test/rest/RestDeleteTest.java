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

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.http.client.ClientProtocolException;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import com.jaeksoft.searchlib.test.IntegrationTest;
import com.jaeksoft.searchlib.webservice.CommonResult;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class RestDeleteTest extends CommonRestAPI {

	@Test
	public void testA_RestAPIUpdateDocument() throws ClientProtocolException,
			IOException {
		String json = getResource("documents.json");
		updateDocuments(json);
	}

	@Test
	public void testB_RestAPIDeleteByField() throws ClientProtocolException,
			IOException, InterruptedException, ExecutionException {
		Response response = client()
				.path("/services/rest/index/{index_name}/document/id/1/2",
						IntegrationTest.INDEX_NAME)
				.accept(MediaType.APPLICATION_JSON).async().delete().get();
		CommonResult result = checkCommonResult(response, CommonResult.class,
				200);
		assertTrue("Wrong info: " + result.info,
				result.info.startsWith("2 document"));
		checkCommonResultDetail(result, "deletedCount", "2");
	}

	@Test
	public void testC_RestAPIDeleteByJson() {
		Response response = client()
				.path("/services/rest/index/{index_name}/document/id",
						IntegrationTest.INDEX_NAME)
				.accept(MediaType.APPLICATION_JSON)
				.type(MediaType.APPLICATION_JSON)
				.invoke("DELETE", "[\"2\",\"3\"]");
		CommonResult result = checkCommonResult(response, CommonResult.class,
				200);
		assertTrue("Wrong info: " + result.info,
				result.info.startsWith("1 document"));
		checkCommonResultDetail(result, "deletedCount", "1");
	}

	@Test
	public void testD_RestAPIDeleteByJsonQueryUnique() {
		Response response = client()
				.path("/services/rest/index/{index_name}/documents",
						IntegrationTest.INDEX_NAME)
				.accept(MediaType.APPLICATION_JSON)
				.type(MediaType.APPLICATION_JSON)
				.invoke("DELETE", "{ \"values\": [4, 5] }");
		CommonResult result = checkCommonResult(response, CommonResult.class,
				200);
		assertTrue("Wrong info: " + result.info,
				result.info.startsWith("2 document"));
		checkCommonResultDetail(result, "deletedCount", "2");
	}

	@Test
	public void testE_RestAPIDeleteByJsonQueryField() {
		Response response = client()
				.path("/services/rest/index/{index_name}/documents",
						IntegrationTest.INDEX_NAME)
				.accept(MediaType.APPLICATION_JSON)
				.type(MediaType.APPLICATION_JSON)
				.invoke("DELETE",
						"{ \"field\": \"content\", \"values\": [\"hallo\"] }");
		CommonResult result = checkCommonResult(response, CommonResult.class,
				200);
		assertTrue("Wrong info: " + result.info,
				result.info.startsWith("1 document"));
		checkCommonResultDetail(result, "deletedCount", "1");
	}

	@Test
	public void testF_RestAPIDeleteByJsonUniqueReverse() {
		Response response = client()
				.path("/services/rest/index/{index_name}/documents",
						IntegrationTest.INDEX_NAME)
				.accept(MediaType.APPLICATION_JSON)
				.type(MediaType.APPLICATION_JSON)
				.invoke("DELETE", "{ \"reverse\": true, \"values\": [10] }");
		CommonResult result = checkCommonResult(response, CommonResult.class,
				200);
		assertTrue("Wrong info: " + result.info,
				result.info.startsWith("3 document"));
		checkCommonResultDetail(result, "deletedCount", "3");
	}
}
