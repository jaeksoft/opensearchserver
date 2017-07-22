/**
 * License Agreement for OpenSearchServer
 * <p>
 * Copyright (C) 2013-2017 Emmanuel Keller / Jaeksoft
 * <p>
 * http://www.open-search-server.com
 * <p>
 * This file is part of OpenSearchServer.
 * <p>
 * OpenSearchServer is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * OpenSearchServer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with OpenSearchServer.
 * If not, see <http://www.gnu.org/licenses/>.
 **/

package com.jaeksoft.searchlib.test.rest;

import com.jaeksoft.searchlib.template.TemplateList;
import com.jaeksoft.searchlib.test.IntegrationTest;
import com.jaeksoft.searchlib.webservice.CommonResult;
import com.jaeksoft.searchlib.webservice.index.ResultIndex;
import com.jaeksoft.searchlib.webservice.index.ResultIndexList;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.xml.sax.SAXException;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;

import static org.junit.Assert.assertTrue;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class RestIndexCreateExistsListTest extends CommonRestAPI {

	@Test
	public void testAdeleteRemainingIndex() {
		client().path("/services/rest/index/" + IntegrationTest.INDEX_NAME)
				.request(MediaType.APPLICATION_JSON)
				.delete();
	}

	@Test
	public void testBCreateIndex() throws IllegalStateException, IOException, XPathExpressionException, SAXException,
			ParserConfigurationException {
		Response response = client().path(
				"/services/rest/index/" + IntegrationTest.INDEX_NAME + "/template/" + TemplateList.EMPTY_INDEX.name())
				.request(MediaType.APPLICATION_JSON)
				.post(null);
		checkCommonResult(response, CommonResult.class, 200);
	}

	@Test
	public void testCExistsIndex() throws IllegalStateException, IOException, XPathExpressionException, SAXException,
			ParserConfigurationException {
		Response response = client().path("/services/rest/index/" + IntegrationTest.INDEX_NAME).request(
				MediaType.APPLICATION_JSON).get();
		checkCommonResult(response, ResultIndex.class, 200);
	}

	@Test
	public void testDListIndex() throws IllegalStateException, IOException, XPathExpressionException, SAXException,
			ParserConfigurationException {
		Response response = client().path("/services/rest/index").request(MediaType.APPLICATION_JSON).get();
		ResultIndexList resultIndexList = checkCommonResult(response, ResultIndexList.class, 200);
		assertTrue(resultIndexList.index.contains(IntegrationTest.INDEX_NAME));
	}

}
