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
import junit.framework.TestSuite;

import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpPost;
import org.xml.sax.SAXException;

/**
 * @author Ayyathurai N Naveen
 * 
 */
public class SearchTestCase extends TestCase {
	private CommomTestCase commomTestCase = null;

	public SearchTestCase(String name) {
		super(name);
		commomTestCase = new CommomTestCase();
	}

	public void getDocumentsFound() throws IllegalStateException, IOException,
			XPathExpressionException, SAXException,
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

	public void checkFacetField() throws IllegalStateException, IOException,
			XPathExpressionException, SAXException,
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

	public void getFilterSearch() throws IllegalStateException, IOException,
			XPathExpressionException, SAXException,
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

	public void getCollapsedDocumentCount() throws IllegalStateException,
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

	public void checkSpellCheck() throws IllegalStateException, IOException,
			XPathExpressionException, SAXException,
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

	public static TestSuite suite() throws InterruptedException {
		TestSuite searchTestCase = new TestSuite();
		searchTestCase.addTest(new SearchTestCase("getDocumentsFound"));
		searchTestCase.addTest(new SearchTestCase("getCollapsedDocumentCount"));
		searchTestCase.addTest(new SearchTestCase("getFilterSearch"));
		searchTestCase.addTest(new SearchTestCase("checkFacetField"));
		searchTestCase.addTest(new SearchTestCase("checkSpellCheck"));
		return searchTestCase;
	}
}
