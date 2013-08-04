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

package com.jaeksoft.searchlib.test.rest;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import junit.framework.TestCase;

import org.apache.cxf.jaxrs.client.WebClient;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.template.TemplateList;
import com.jaeksoft.searchlib.webservice.CommonResult;

/**
 * @author Ayyathurai N Naveen
 * 
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class RestIndexTestCase extends TestCase {
	private CommonRestTestCase commonRestTestCase = null;

	public RestIndexTestCase(String name) {
		super(name);
		commonRestTestCase = new CommonRestTestCase();
	}

	@Test
	public void testACreateIndexXML() throws IllegalStateException,
			IOException, XPathExpressionException, SAXException,
			ParserConfigurationException {
		WebClient webClient = commonRestTestCase
				.getNewWebClient("/index/create/xml");
		webClient.query("name", CommonRestTestCase.INDEX_NAME);
		webClient.query("template", TemplateList.WEB_CRAWLER.name());
		CommonResult commonResult = webClient.post(null, CommonResult.class);
		assertEquals("Created Index oss", commonResult.info);

	}

	@Test
	public void testBDeleteIndexXML() throws IllegalStateException,
			IOException, XPathExpressionException, SAXException,
			ParserConfigurationException {
		WebClient webClient = commonRestTestCase
				.getNewWebClient("/index/delete/xml");
		webClient.query("name", CommonRestTestCase.INDEX_NAME);
		CommonResult commonResult = webClient.invoke("DELETE", null,
				CommonResult.class);
		assertEquals("Index deleted: oss", commonResult.info);

	}

	@Test
	public void testCCreateIndexJSON() throws IllegalStateException,
			IOException, XPathExpressionException, SAXException,
			ParserConfigurationException {
		WebClient webClient = commonRestTestCase
				.getNewWebClient("/index/create/json");
		webClient.query("name", CommonRestTestCase.INDEX_NAME);
		webClient.query("template", TemplateList.WEB_CRAWLER.name());
		CommonResult commonResult = webClient.post(null, CommonResult.class);
		assertEquals("Created Index oss", commonResult.info);

	}

	@Test
	public void testDListIndexXML() throws IllegalStateException, IOException,
			XPathExpressionException, SAXException,
			ParserConfigurationException {
		WebClient webClient = commonRestTestCase
				.getNewWebClient("/index/list/xml");
		CommonResult commonResult = webClient.get(CommonResult.class);
		assertEquals("1 index(es)", commonResult.info);

	}

	@Test
	public void testDListIndexJSON() throws IllegalStateException, IOException,
			XPathExpressionException, SAXException,
			ParserConfigurationException {
		WebClient webClient = commonRestTestCase
				.getNewWebClient("/index/list/json");
		CommonResult commonResult = webClient.get(CommonResult.class);
		assertEquals("1 index(es)", commonResult.info);

	}
}
