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
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.xml.sax.SAXException;

/**
 * @author Ayyathurai N Naveen
 * 
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SearchTestCase extends TestCase {
	private CommomTestCase commomTestCase = null;

	public SearchTestCase(String name) {
		super(name);
		commomTestCase = new CommomTestCase();
	}

	@Test
	public void testAgetDocumentsFound() throws IllegalStateException,
			IOException, XPathExpressionException, SAXException,
			ParserConfigurationException {
		List<NameValuePair> namedValuePairs = new ArrayList<NameValuePair>();
		namedValuePairs.add(commomTestCase.getNameValuePair("query", "*:*"));
		namedValuePairs.add(commomTestCase.getNameValuePair("qt", "search"));
		HttpPost httpPost = commomTestCase.queryInstance(namedValuePairs,
				CommomTestCase.SEARCH_API, true);
		String response = commomTestCase.getHttpResponse(httpPost,
				"response/result/@numFound");
		assertEquals("174", response);
	}

	@Test
	public void testBCheckFacetField() throws IllegalStateException,
			IOException, XPathExpressionException, SAXException,
			ParserConfigurationException {
		List<NameValuePair> namedValuePairs = new ArrayList<NameValuePair>();
		namedValuePairs.add(commomTestCase.getNameValuePair("query", "*:*"));
		namedValuePairs.add(commomTestCase.getNameValuePair("qt", "search"));
		namedValuePairs.add(commomTestCase.getNameValuePair("facet", "lang"));
		namedValuePairs.add(commomTestCase.getNameValuePair("facet.multi",
				"true"));
		HttpPost httpPost = commomTestCase.queryInstance(namedValuePairs,
				CommomTestCase.SEARCH_API, true);
		String response = commomTestCase.getHttpResponse(httpPost,
				"response/faceting/field[@name='lang']/facet[@name='en']");
		assertEquals("137", response);
	}

	@Test
	public void testCgetFilterSearch() throws IllegalStateException,
			IOException, XPathExpressionException, SAXException,
			ParserConfigurationException {
		List<NameValuePair> namedValuePairs = new ArrayList<NameValuePair>();
		namedValuePairs.add(commomTestCase.getNameValuePair("query", "*:*"));
		namedValuePairs.add(commomTestCase.getNameValuePair("qt", "search"));
		namedValuePairs.add(commomTestCase.getNameValuePair("fq", "lang:en"));
		HttpPost httpPost = commomTestCase.queryInstance(namedValuePairs,
				CommomTestCase.SEARCH_API, true);
		String response = commomTestCase.getHttpResponse(httpPost,
				"response/result/@numFound");
		assertEquals("137", response);
	}

	@Test
	public void testDGetSortedSearch() throws IllegalStateException,
			IOException, XPathExpressionException, SAXException,
			ParserConfigurationException {
		List<NameValuePair> namedValuePairs = new ArrayList<NameValuePair>();
		namedValuePairs.add(commomTestCase.getNameValuePair("query", "*:*"));
		namedValuePairs.add(commomTestCase.getNameValuePair("qt", "search"));
		namedValuePairs.add(commomTestCase.getNameValuePair("sort", "url"));
		HttpPost httpPost = commomTestCase.queryInstance(namedValuePairs,
				CommomTestCase.SEARCH_API, true);
		String response = commomTestCase.getHttpResponse(httpPost,
				"response/result/doc/field[@name='url']");
		assertEquals("http://dev.open-search-server.com/13", response);
	}

	@Test
	public void testEGetReturnField() throws IllegalStateException,
			IOException, XPathExpressionException, SAXException,
			ParserConfigurationException {
		List<NameValuePair> namedValuePairs = new ArrayList<NameValuePair>();
		namedValuePairs.add(commomTestCase.getNameValuePair("query",
				"open search"));
		namedValuePairs.add(commomTestCase.getNameValuePair("qt", "search"));
		namedValuePairs.add(commomTestCase.getNameValuePair("rf", "lang"));
		HttpPost httpPost = commomTestCase.queryInstance(namedValuePairs,
				CommomTestCase.SEARCH_API, true);
		String response = commomTestCase.getHttpResponse(httpPost,
				"response/result/doc/field[@name='lang']");
		assertEquals("fr", response);
	}

	@Test
	public void testFGetRows() throws IllegalStateException, IOException,
			XPathExpressionException, SAXException,
			ParserConfigurationException {
		List<NameValuePair> namedValuePairs = new ArrayList<NameValuePair>();
		namedValuePairs.add(commomTestCase.getNameValuePair("query", "*:*"));
		namedValuePairs.add(commomTestCase.getNameValuePair("qt", "search"));
		namedValuePairs.add(commomTestCase.getNameValuePair("rows", "50"));
		HttpPost httpPost = commomTestCase.queryInstance(namedValuePairs,
				CommomTestCase.SEARCH_API, true);
		String response = commomTestCase.getHttpResponse(httpPost,
				"count(response/result/doc)");
		assertEquals("50", response);
	}

	@Test
	public void testGgetStart() throws IllegalStateException, IOException,
			XPathExpressionException, SAXException,
			ParserConfigurationException {
		List<NameValuePair> namedValuePairs = new ArrayList<NameValuePair>();
		namedValuePairs.add(commomTestCase.getNameValuePair("query", "*:*"));
		namedValuePairs.add(commomTestCase.getNameValuePair("qt", "search"));
		namedValuePairs.add(commomTestCase.getNameValuePair("start", "20"));
		HttpPost httpPost = commomTestCase.queryInstance(namedValuePairs,
				CommomTestCase.SEARCH_API, true);
		String response = commomTestCase.getHttpResponse(httpPost,
				"response/result/doc/@pos");
		assertEquals("20", response);
	}

	@Test
	public void testHGetCollapsedDocumentCount() throws IllegalStateException,
			IOException, XPathExpressionException, SAXException,
			ParserConfigurationException {
		List<NameValuePair> namedValuePairs = new ArrayList<NameValuePair>();
		namedValuePairs.add(commomTestCase.getNameValuePair("query", "*:*"));
		namedValuePairs.add(commomTestCase.getNameValuePair("qt", "search"));
		namedValuePairs.add(commomTestCase.getNameValuePair("collapse.mode",
				"cluster"));
		namedValuePairs.add(commomTestCase.getNameValuePair("collapse.field",
				"host"));
		namedValuePairs.add(commomTestCase
				.getNameValuePair("collapse.max", "1"));
		HttpPost httpPost = commomTestCase.queryInstance(namedValuePairs,
				CommomTestCase.SEARCH_API, true);
		String response = commomTestCase.getHttpResponse(httpPost,
				"response/result/@collapsedDocCount");
		assertEquals("173", response);
	}

	@Test
	public void testICheckSpellCheck() throws IllegalStateException,
			IOException, XPathExpressionException, SAXException,
			ParserConfigurationException {
		List<NameValuePair> namedValuePairs = new ArrayList<NameValuePair>();
		namedValuePairs.add(commomTestCase.getNameValuePair("query",
				"opensearch"));
		namedValuePairs
				.add(commomTestCase.getNameValuePair("qt", "spellcheck"));
		HttpPost httpPost = commomTestCase.queryInstance(namedValuePairs,
				CommomTestCase.SEARCH_API, true);
		String response = commomTestCase.getHttpResponse(httpPost,
				"response/spellcheck/field/word/suggest");
		assertEquals("opensearchserver", response);
	}

}
