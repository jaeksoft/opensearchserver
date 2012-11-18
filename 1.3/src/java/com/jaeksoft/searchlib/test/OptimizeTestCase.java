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

public class OptimizeTestCase extends TestCase {
	private CommomTestCase commomTestCase = null;

	public OptimizeTestCase(String name) {
		super(name);
		commomTestCase = new CommomTestCase();
	}

	public void optimizeIndex() throws IllegalStateException, IOException,
			XPathExpressionException, SAXException,
			ParserConfigurationException {
		List<NameValuePair> namedValuePairs = new ArrayList<NameValuePair>();
		HttpPost httpPost = commomTestCase.queryInstance(namedValuePairs,
				CommomTestCase.OPTIMIZE_API, true);
		String response = commomTestCase.getHttpResponse(httpPost,
				"response/entry[@key='Status']");
		assertEquals("OK", response);
	}

	public static TestSuite suite() throws InterruptedException {
		TestSuite optimizeTestSuit = new TestSuite();
		optimizeTestSuit.addTest(new OptimizeTestCase("optimizeIndex"));
		return optimizeTestSuit;
	}

}
