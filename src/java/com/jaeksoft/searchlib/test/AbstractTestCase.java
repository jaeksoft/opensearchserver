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

import java.io.IOException;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.util.XPathParser;

/**
 * @author Ayyathurai N Naveen
 * 
 */
public class AbstractTestCase {
	protected static String INDEX_NAME = "oss_1.3";
	protected static String SERVER_URL = "http://localhost:8080/oss_1.3";
	protected static String USER_NAME = "";
	protected static String API_KEY = "";

	protected static String SCHEMA_API = "schema";

	protected HttpPost queryInstance(List<NameValuePair> namedValuePairs,
			String apiPath, boolean use) throws IllegalStateException,
			IOException {
		HttpPost httpPost = null;

		httpPost = new HttpPost(SERVER_URL + "/" + apiPath);
		if (use)
			namedValuePairs.add(new BasicNameValuePair("use", INDEX_NAME));
		namedValuePairs.add(new BasicNameValuePair("use", INDEX_NAME));
		namedValuePairs.add(new BasicNameValuePair("login", USER_NAME));
		namedValuePairs.add(new BasicNameValuePair("key", API_KEY));
		httpPost.setEntity(new UrlEncodedFormEntity(namedValuePairs, "UTF-8"));

		return httpPost;
	}

	protected String getHttpResponse(HttpPost httpPost, String xpath)
			throws IllegalStateException, SAXException, IOException,
			ParserConfigurationException, XPathExpressionException {
		DefaultHttpClient httpClient = new DefaultHttpClient();
		HttpResponse HttpResponse = httpClient.execute(httpPost);
		XPathParser parser = new XPathParser(HttpResponse.getEntity()
				.getContent());
		return parser.getNodeString(xpath);

	}

	protected BasicNameValuePair getNameValuePair(String key, String value) {
		return new BasicNameValuePair(key, value);
	}
}
