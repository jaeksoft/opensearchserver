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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpPost;
import org.junit.Test;
import org.xml.sax.SAXException;

/**
 * @author Ayyathurai N Naveen
 * 
 */
public class WebCrawlerTestCase extends AbstractTestCase {

	@Test
	public void createIndex() throws IllegalStateException, IOException,
			SAXException, ParserConfigurationException,
			XPathExpressionException {
		List<NameValuePair> namedValuePairs = new ArrayList<NameValuePair>();
		namedValuePairs.add(getNameValuePair("cmd", "createindex"));
		namedValuePairs.add(getNameValuePair("index.name", INDEX_NAME));
		namedValuePairs.add(getNameValuePair("index.template", "WEB_CRAWLER"));
		HttpPost httpPost = queryInstance(namedValuePairs, SCHEMA_API, false);
		String response = getHttpResponse(httpPost,
				"response/entry[@key='Info']");
		assertEquals("Index created: oss_1.3", response);

	}

	@Test
	public void createSchemaField() throws IllegalStateException, IOException,
			SAXException, ParserConfigurationException,
			XPathExpressionException {
		List<NameValuePair> namedValuePairs = new ArrayList<NameValuePair>();
		namedValuePairs.add(getNameValuePair("cmd", "setField"));
		namedValuePairs.add(getNameValuePair("field.name", "titleNew"));
		namedValuePairs.add(getNameValuePair("field.analyzer",
				"StandardAnalyzer"));
		namedValuePairs.add(getNameValuePair("field.stored", "yes"));
		namedValuePairs.add(getNameValuePair("field.indexed", "yes"));
		namedValuePairs.add(getNameValuePair("term.termvector", "no"));
		HttpPost httpPost = queryInstance(namedValuePairs, SCHEMA_API, true);
		String response = getHttpResponse(httpPost,
				"response/entry[@key='Info']");
		assertEquals("field 'titleNew' added/updated", response);
	}

	@Test
	public void deleteSchemaField() throws IllegalStateException, IOException,
			SAXException, ParserConfigurationException,
			XPathExpressionException {
		List<NameValuePair> namedValuePairs = new ArrayList<NameValuePair>();
		namedValuePairs.add(getNameValuePair("cmd", "deletefield"));
		namedValuePairs.add(getNameValuePair("field.name", "titleNew"));
		HttpPost httpPost = queryInstance(namedValuePairs, SCHEMA_API, true);
		String response = getHttpResponse(httpPost,
				"response/entry[@key='Info']");
		assertEquals("field 'titleNew' removed", response);
	}

	@Test
	public void getSchema() throws IllegalStateException, IOException,
			SAXException, ParserConfigurationException,
			XPathExpressionException {
		List<NameValuePair> namedValuePairs = new ArrayList<NameValuePair>();
		namedValuePairs.add(getNameValuePair("cmd", "getschema"));
		HttpPost httpPost = queryInstance(namedValuePairs, SCHEMA_API, true);
		String response = getHttpResponse(httpPost,
				"response/schema/fields/field[3]/@name");
		String responseContent = getHttpResponse(httpPost,
				"response/schema/fields/field[5]/@name");
		assertEquals("content", responseContent);
		assertEquals("titleExact", response);
	}

	@Test
	public void getIndexLists() throws IllegalStateException, IOException,
			SAXException, ParserConfigurationException,
			XPathExpressionException {
		List<NameValuePair> namedValuePairs = new ArrayList<NameValuePair>();
		namedValuePairs.add(getNameValuePair("cmd", "indexlist"));
		HttpPost httpPost = queryInstance(namedValuePairs, SCHEMA_API, true);
		String response = getHttpResponse(httpPost, "response/index/@name");
		assertEquals(INDEX_NAME, response);
	}

	@Test
	public void deleteIndex() throws IllegalStateException, IOException,
			SAXException, ParserConfigurationException,
			XPathExpressionException {
		List<NameValuePair> namedValuePairs = new ArrayList<NameValuePair>();
		namedValuePairs.add(getNameValuePair("cmd", "deleteindex"));
		namedValuePairs.add(getNameValuePair("index.name", INDEX_NAME));
		namedValuePairs.add(getNameValuePair("index.delete.name", INDEX_NAME));
		HttpPost httpPost = queryInstance(namedValuePairs, SCHEMA_API, false);
		String response = getHttpResponse(httpPost,
				"response/entry[@key='Info']");
		assertEquals("Index deleted: oss_1.3", response);

	}
}
