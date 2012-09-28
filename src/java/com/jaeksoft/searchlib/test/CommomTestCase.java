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

package com.jaeksoft.searchlib.test;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.FileEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.util.XPathParser;

/**
 * @author Ayyathurai N Naveen
 * 
 */
public class CommomTestCase {
	public static String INDEX_NAME = "oss_1.3";
	public static String SERVER_URL = "http://localhost:8080/oss_1.3";
	public static String USER_NAME = "";
	public static String API_KEY = "";

	public static String SCHEMA_API = "schema";
	public static String PATTERN_API = "pattern";
	public static String WEBCRAWLER_API = "webcrawler";
	public static String INDEX_API = "update";
	public static String OPTIMIZE_API = "optimize";
	public static String SEARCH_API = "select";
	public static String DELETE_API = "delete";
	public static String SEARCH_TEMPLATE_API = "searchtemplate";

	public HttpPost queryInstance(List<NameValuePair> namedValuePairs,
			String apiPath, boolean use) throws IllegalStateException,
			IOException {
		HttpPost httpPost = null;
		httpPost = new HttpPost(SERVER_URL + "/" + apiPath);
		if (use)
			namedValuePairs.add(new BasicNameValuePair("use", INDEX_NAME));
		namedValuePairs.add(new BasicNameValuePair("login", USER_NAME));
		namedValuePairs.add(new BasicNameValuePair("key", API_KEY));
		httpPost.setEntity(new UrlEncodedFormEntity(namedValuePairs, "UTF-8"));
		return httpPost;
	}

	@SuppressWarnings("deprecation")
	public int postFile(File file, String contentType, String api)
			throws IllegalStateException, IOException {
		String url = SERVER_URL + "/" + api + "?use=" + INDEX_NAME + "&login="
				+ USER_NAME + "&key=" + API_KEY;
		HttpPut put = new HttpPut(url);
		put.setEntity(new FileEntity(file, contentType));
		DefaultHttpClient httpClient = new DefaultHttpClient();
		return httpClient.execute(put).getStatusLine().getStatusCode();
	}

	public String getHttpResponse(HttpPost httpPost, String xpath)
			throws IllegalStateException, SAXException, IOException,
			ParserConfigurationException, XPathExpressionException {
		DefaultHttpClient httpClient = new DefaultHttpClient();
		HttpResponse httpResponse = httpClient.execute(httpPost);
		XPathParser parser = new XPathParser(httpResponse.getEntity()
				.getContent());
		return parser.getNodeString(xpath);

	}

	protected BasicNameValuePair getNameValuePair(String key, String value) {
		return new BasicNameValuePair(key, value);
	}
}
