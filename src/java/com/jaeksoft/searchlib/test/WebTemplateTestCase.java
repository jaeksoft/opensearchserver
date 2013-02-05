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
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import junit.framework.TestCase;

import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpPost;
import org.junit.Test;
import org.xml.sax.SAXException;

/**
 * @author Ayyathurai N Naveen
 * 
 */
public class WebTemplateTestCase extends TestCase {
	private CommomTestCase commomTestCase = null;

	public WebTemplateTestCase(String name) {
		super(name);
		commomTestCase = new CommomTestCase();
	}

	@Test
	public void testCreateIndex() throws IllegalStateException, IOException,
			XPathExpressionException, SAXException,
			ParserConfigurationException {
		List<NameValuePair> namedValuePairs = new ArrayList<NameValuePair>();
		namedValuePairs.add(commomTestCase.getNameValuePair("cmd",
				"createindex"));
		namedValuePairs.add(commomTestCase.getNameValuePair("index.name",
				CommomTestCase.INDEX_NAME));
		namedValuePairs.add(commomTestCase.getNameValuePair("index.template",
				"WEB_CRAWLER"));
		HttpPost httpPost = commomTestCase.queryInstance(namedValuePairs,
				CommomTestCase.SCHEMA_API, false);
		String response = commomTestCase.getHttpResponse(httpPost,
				"response/entry[@key='Info']");
		assertEquals("Index created: oss", response);

	}

	@Test
	public void testCreateSchemaField() throws IllegalStateException,
			IOException, SAXException, ParserConfigurationException,
			XPathExpressionException {
		List<NameValuePair> namedValuePairs = new ArrayList<NameValuePair>();
		namedValuePairs.add(commomTestCase.getNameValuePair("cmd", "setField"));
		namedValuePairs.add(commomTestCase.getNameValuePair("field.name",
				"titleNew"));
		namedValuePairs.add(commomTestCase.getNameValuePair("field.analyzer",
				"StandardAnalyzer"));
		namedValuePairs.add(commomTestCase.getNameValuePair("field.stored",
				"yes"));
		namedValuePairs.add(commomTestCase.getNameValuePair("field.indexed",
				"yes"));
		namedValuePairs.add(commomTestCase.getNameValuePair("term.termvector",
				"no"));
		HttpPost httpPost = commomTestCase.queryInstance(namedValuePairs,
				CommomTestCase.SCHEMA_API, true);
		String response = commomTestCase.getHttpResponse(httpPost,
				"response/entry[@key='Info']");
		assertEquals("field 'titleNew' added/updated", response);
	}

	@Test
	public void testDeleteSchemaField() throws IllegalStateException,
			IOException, SAXException, ParserConfigurationException,
			XPathExpressionException {
		List<NameValuePair> namedValuePairs = new ArrayList<NameValuePair>();
		namedValuePairs.add(commomTestCase.getNameValuePair("cmd",
				"deletefield"));
		namedValuePairs.add(commomTestCase.getNameValuePair("field.name",
				"titleNew"));
		HttpPost httpPost = commomTestCase.queryInstance(namedValuePairs,
				CommomTestCase.SCHEMA_API, true);
		String response = commomTestCase.getHttpResponse(httpPost,
				"response/entry[@key='Info']");
		assertEquals("field 'titleNew' removed", response);
	}

	@Test
	public void testGetSchema() throws IllegalStateException, IOException,
			SAXException, ParserConfigurationException,
			XPathExpressionException {
		List<NameValuePair> namedValuePairs = new ArrayList<NameValuePair>();
		namedValuePairs
				.add(commomTestCase.getNameValuePair("cmd", "getschema"));
		HttpPost httpPost = commomTestCase.queryInstance(namedValuePairs,
				CommomTestCase.SCHEMA_API, true);
		String response = commomTestCase.getHttpResponse(httpPost,
				"response/schema/fields/field[3]/@name");
		String responseContent = commomTestCase.getHttpResponse(httpPost,
				"response/schema/fields/field[5]/@name");
		assertEquals("content", responseContent);
		assertEquals("titleExact", response);
	}

	@Test
	public void testGetIndexLists() throws IllegalStateException, IOException,
			SAXException, ParserConfigurationException,
			XPathExpressionException {
		List<NameValuePair> namedValuePairs = new ArrayList<NameValuePair>();
		namedValuePairs
				.add(commomTestCase.getNameValuePair("cmd", "indexlist"));
		HttpPost httpPost = commomTestCase.queryInstance(namedValuePairs,
				CommomTestCase.SCHEMA_API, true);
		String response = commomTestCase.getHttpResponse(httpPost,
				"response/index/@name");
		assertEquals(CommomTestCase.INDEX_NAME, response);
	}

}
