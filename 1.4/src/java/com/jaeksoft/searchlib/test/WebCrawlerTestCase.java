/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2012 - 2013 Emmanuel Keller / Jaeksoft
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
public class WebCrawlerTestCase extends TestCase {
	private CommomTestCase commomTestCase = null;

	public WebCrawlerTestCase(String name) {
		super(name);
		commomTestCase = new CommomTestCase();
	}

	public String startStopCrawler(String action) throws IllegalStateException,
			IOException, XPathExpressionException, SAXException,
			ParserConfigurationException {
		List<NameValuePair> namedValuePairs = new ArrayList<NameValuePair>();
		namedValuePairs.add(commomTestCase.getNameValuePair("action", action));
		HttpPost httpPost = commomTestCase.queryInstance(namedValuePairs,
				CommomTestCase.WEBCRAWLER_API, true);
		return commomTestCase.getHttpResponse(httpPost,
				"response/entry[@key='Info']");

	}

	@Test
	public void testAStartCrawler() throws IllegalStateException,
			XPathExpressionException, IOException, SAXException,
			ParserConfigurationException {
		String response = startStopCrawler("start");
		assertEquals("STARTED", response);
	}

	@Test
	public void testBStopCrawler() throws IllegalStateException,
			XPathExpressionException, IOException, SAXException,
			ParserConfigurationException {
		String response = startStopCrawler("stop");
		assertEquals("STOPPED", response);
	}

}
