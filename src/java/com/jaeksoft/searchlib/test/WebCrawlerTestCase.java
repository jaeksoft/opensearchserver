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

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.junit.Test;

import com.jaeksoft.searchlib.util.XPathParser;

/**
 * @author Ayyathurai N Naveen
 * 
 */
public class WebCrawlerTestCase extends AbstractTestCase {

	@Test
	public void createIndex() {
		try {
			List<NameValuePair> namedValuePairs = new ArrayList<NameValuePair>();
			namedValuePairs.add(new BasicNameValuePair("cmd", "createindex"));
			namedValuePairs
					.add(new BasicNameValuePair("index.name", INDEX_NAME));
			namedValuePairs.add(new BasicNameValuePair("index.template",
					"WEB_CRAWLER"));
			HttpPost httpPost = queryInstance(namedValuePairs, "schema", false);
			DefaultHttpClient httpClient = new DefaultHttpClient();
			HttpResponse HttpResponse = httpClient.execute(httpPost);
			XPathParser parser = new XPathParser(HttpResponse.getEntity()
					.getContent());
			String response = parser.getNodeString("response/entry");
			assertEquals("Index created: oss_1.3", response);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
