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
package com.jaeksoft.searchlib.test.legacy;

import junit.framework.TestCase;
import org.apache.commons.io.IOUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpPost;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class AutocompletionTest extends TestCase {
	private CommonTestCase commomTestCase = null;

	public AutocompletionTest(String name) {
		super(name);
		commomTestCase = new CommonTestCase();
	}

	public void setAutocompletionField() throws IllegalStateException,
			IOException, XPathExpressionException, SAXException,
			ParserConfigurationException {
		List<NameValuePair> namedValuePairs = new ArrayList<NameValuePair>();
		namedValuePairs.add(commomTestCase.getNameValuePair("cmd", "set"));
		namedValuePairs.add(commomTestCase.getNameValuePair("field",
				"titleExact"));
		HttpPost httpPost = commomTestCase.queryInstance(namedValuePairs,
				CommonTestCase.AUTOCOMPLETE_API, true);
		String response = commomTestCase.getHttpResponse(httpPost,
				"response/entry[@key='Field']");
		assertEquals("titleExact", response);
	}

	public void startAutoCompletion() throws IllegalStateException,
			IOException, XPathExpressionException, SAXException,
			ParserConfigurationException {
		List<NameValuePair> namedValuePairs = new ArrayList<NameValuePair>();
		namedValuePairs.add(commomTestCase.getNameValuePair("cmd", "build"));
		HttpPost httpPost = commomTestCase.queryInstance(namedValuePairs,
				CommonTestCase.AUTOCOMPLETE_API, true);
		String response = commomTestCase.getHttpResponse(httpPost,
				"response/entry[@key='Count']");
		assertEquals("293", response);
	}

	public void getAutocompletion() throws IllegalStateException, IOException,
			XPathExpressionException, SAXException,
			ParserConfigurationException {
		List<NameValuePair> namedValuePairs = new ArrayList<NameValuePair>();
		namedValuePairs.add(commomTestCase.getNameValuePair("query", "a"));
		HttpPost httpPost = commomTestCase.queryInstance(namedValuePairs,
				CommonTestCase.AUTOCOMPLETE_API, true);
		InputStream response = commomTestCase.getResponse(httpPost);
		String suggestions = IOUtils.toString(response, "UTF-8");
		String suggests[] = suggestions.split("\n");
		assertEquals(10, suggests.length);
	}

}
