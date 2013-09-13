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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import javax.ws.rs.core.MediaType;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.template.TemplateList;
import com.jaeksoft.searchlib.webservice.CommonResult;
import com.jaeksoft.searchlib.webservice.index.ResultIndexList;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class RestIndexCreateExistsListTest extends CommonRestAPI {

	@Test
	public void testACreateIndex() throws IllegalStateException, IOException,
			XPathExpressionException, SAXException,
			ParserConfigurationException {
		Target target = getIndexTarget("/services/rest/index/{index_name}/template/{template_name}");
		target.pathParam("index_name", AllRestAPITests.INDEX_NAME);
		target.pathParam("template_name", TemplateList.EMPTY_INDEX.name());
		CommonResult commonResult = target.request(MediaType.APPLICATION_JSON)
				.post(null, CommonResult.class);
		assertNotNull(commonResult.successful);
		assertTrue(commonResult.successful);
	}

	@Test
	public void testBExistsIndex() throws IllegalStateException, IOException,
			XPathExpressionException, SAXException,
			ParserConfigurationException {
		Target target = getIndexTarget("/services/rest/index/{index_name}");
		target.pathParam("index_name", AllRestAPITests.INDEX_NAME);
		CommonResult commonResult = target.request(MediaType.APPLICATION_JSON)
				.get(CommonResult.class);
		assertNotNull(commonResult.successful);
		assertTrue(commonResult.successful);
	}

	@Test
	public void testCListIndex() throws IllegalStateException, IOException,
			XPathExpressionException, SAXException,
			ParserConfigurationException {
		Target target = getTarget("/services/rest/index");
		ResultIndexList resultIndexList = target.request(
				MediaType.APPLICATION_JSON).get(ResultIndexList.class);
		assertNotNull(resultIndexList.successful);
		assertTrue(resultIndexList.successful);
		assertNotNull(resultIndexList.indexList);
		assertTrue(resultIndexList.indexList
				.contains(AllRestAPITests.INDEX_NAME));
	}

}
