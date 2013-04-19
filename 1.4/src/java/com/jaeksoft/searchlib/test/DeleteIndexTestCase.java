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
import org.junit.Test;
import org.xml.sax.SAXException;

public class DeleteIndexTestCase extends TestCase {
	private CommonTestCase commomTestCase = null;

	public DeleteIndexTestCase(String name) {
		super(name);
		commomTestCase = new CommonTestCase();
	}

	@Test
	public void testDeleteIndex() throws IllegalStateException, IOException,
			SAXException, ParserConfigurationException,
			XPathExpressionException {
		List<NameValuePair> namedValuePairs = new ArrayList<NameValuePair>();
		namedValuePairs.add(commomTestCase.getNameValuePair("cmd",
				"deleteindex"));
		namedValuePairs.add(commomTestCase.getNameValuePair("index.name",
				CommonTestCase.INDEX_NAME));
		namedValuePairs.add(commomTestCase.getNameValuePair(
				"index.delete.name", CommonTestCase.INDEX_NAME));
		HttpPost httpPost = commomTestCase.queryInstance(namedValuePairs,
				CommonTestCase.SCHEMA_API, false);
		String response = commomTestCase.getHttpResponse(httpPost,
				"response/entry[@key='Info']");
		assertEquals("Index deleted: oss", response);
	}

}
