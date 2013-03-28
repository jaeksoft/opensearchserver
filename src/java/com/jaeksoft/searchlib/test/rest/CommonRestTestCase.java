/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2012 Emmanuel Keller / Jaeksoft
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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;

import javax.ws.rs.core.Response;

import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.FileEntity;
import org.apache.http.impl.client.DefaultHttpClient;

public class CommonRestTestCase {
	public static String INDEX_NAME = "oss";
	public static String SERVER_URL = "http://localhost:8080";
	public static String REST_PATH = "/jenkins-oss-1.5-testing/services/rest";

	public WebClient getNewWebClient(String path) {
		String restPath = REST_PATH + path;
		WebClient webClient = WebClient.create(SERVER_URL).path(restPath);
		return webClient;
	}

	public Response doPostRequest(WebClient webClient) {
		return webClient.post(null);
	}

	public Response doDeleteRequest(WebClient webClient) {
		return webClient.delete();
	}

	public URIBuilder getURIBuilder(String apiPath) {
		String restPath = REST_PATH + apiPath;
		URIBuilder builder = new URIBuilder();
		builder.setScheme("http").setHost("localhost:8080").setPath(restPath);
		return builder;
	}

	public InputStream httpGet(URIBuilder builder)
			throws IllegalStateException, IOException, URISyntaxException {
		HttpGet httpGet = new HttpGet(builder.build());
		DefaultHttpClient httpClient = new DefaultHttpClient();
		HttpResponse httpResponse = httpClient.execute(httpGet);
		return httpResponse.getEntity().getContent();
	}

	public Response doGetRequest(WebClient webClient) {
		return webClient.get();
	}

	public int restAPIPostFile(File file, String contentType, String api)
			throws IllegalStateException, IOException {
		String url = SERVER_URL + REST_PATH + api + "/" + INDEX_NAME + "/xml";
		HttpPost httpPost = new HttpPost(url);
		FileEntity entity = new FileEntity(file);
		httpPost.setEntity(entity);
		DefaultHttpClient httpClient = new DefaultHttpClient();
		HttpResponse httpResponse = httpClient.execute(httpPost);
		return httpResponse.getStatusLine().getStatusCode();
	}
}
