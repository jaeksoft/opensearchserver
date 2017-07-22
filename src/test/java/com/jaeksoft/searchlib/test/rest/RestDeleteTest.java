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

import com.jaeksoft.searchlib.test.IntegrationTest;
import com.jaeksoft.searchlib.util.IOUtils;
import com.jaeksoft.searchlib.webservice.CommonResult;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertTrue;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class RestDeleteTest extends CommonRestAPI {

	@Test
	public void testA_RestAPIUpdateDocument() throws ClientProtocolException, IOException {
		String json = getResource("documents.json");
		updateDocuments(json);
	}

	@Test
	public void testB_RestAPIDeleteByField() throws IOException, InterruptedException, ExecutionException {
		Response response = client().path("/services/rest/index/" + IntegrationTest.INDEX_NAME + "/document/id/1/2")
				.request(MediaType.APPLICATION_JSON).delete();
		CommonResult result = checkCommonResult(response, CommonResult.class, 200);
		assertTrue("Wrong info: " + result.info, result.info.startsWith("2 document"));
		checkCommonResultDetail(result, "deletedCount", 2);
	}

	class HttpDeleteBody extends HttpPost {

		public HttpDeleteBody(String url) {
			super(url);
		}

		@Override
		public String getMethod() {
			return "DELETE";
		}
	}

	private CloseableHttpResponse doDeleteJson(CloseableHttpClient hc, String path, String json) throws IOException {
		HttpDeleteBody httpDeleteBody = new HttpDeleteBody(IntegrationTest.SERVER_URL + path);
		httpDeleteBody.setEntity(new StringEntity(json, ContentType.APPLICATION_JSON));
		return hc.execute(httpDeleteBody);
	}

	@Test
	public void testC_RestAPIDeleteByJson() throws IOException {
		CloseableHttpClient hc = HttpClientBuilder.create().build();
		try {
			CloseableHttpResponse response = doDeleteJson(hc,
					"/services/rest/index/" + IntegrationTest.INDEX_NAME + "/document/id", "[\"2\",\"3\"]");
			CommonResult result = checkCommonResult(response, CommonResult.class, 200);
			assertTrue("Wrong info: " + result.info, result.info.startsWith("1 document"));
			checkCommonResultDetail(result, "deletedCount", 1);
		} finally {
			IOUtils.closeQuietly(hc);
		}
	}

	@Test
	public void testD_RestAPIDeleteByJsonQueryUnique() throws ClientProtocolException, IOException {
		CloseableHttpClient hc = HttpClientBuilder.create().build();
		try {
			CloseableHttpResponse response = doDeleteJson(hc,
					"/services/rest/index/" + IntegrationTest.INDEX_NAME + "/documents", "{ \"values\": [4, 5] }");
			CommonResult result = checkCommonResult(response, CommonResult.class, 200);
			assertTrue("Wrong info: " + result.info, result.info.startsWith("2 document"));
			checkCommonResultDetail(result, "deletedCount", 2);
		} finally {
			IOUtils.closeQuietly(hc);
		}
	}

	@Test
	public void testE_RestAPIDeleteByJsonQueryField() throws ClientProtocolException, IOException {
		CloseableHttpClient hc = HttpClientBuilder.create().build();
		try {
			CloseableHttpResponse response = doDeleteJson(hc,
					"/services/rest/index/" + IntegrationTest.INDEX_NAME + "/documents",
					"{ \"field\": \"content\", \"values\": [\"hallo\"] }");
			CommonResult result = checkCommonResult(response, CommonResult.class, 200);
			assertTrue("Wrong info: " + result.info, result.info.startsWith("1 document"));
			checkCommonResultDetail(result, "deletedCount", 1);
		} finally {
			IOUtils.closeQuietly(hc);
		}
	}

	@Test
	public void testF_RestAPIDeleteByJsonUniqueReverse() throws ClientProtocolException, IOException {
		CloseableHttpClient hc = HttpClientBuilder.create().build();
		try {
			CloseableHttpResponse response = doDeleteJson(hc,
					"/services/rest/index/" + IntegrationTest.INDEX_NAME + "/documents",
					"{ \"reverse\": true, \"values\": [10] }");
			CommonResult result = checkCommonResult(response, CommonResult.class, 200);
			assertTrue("Wrong info: " + result.info, result.info.startsWith("3 document"));
			checkCommonResultDetail(result, "deletedCount", 3);
		} finally {
			IOUtils.closeQuietly(hc);
		}
	}
}
