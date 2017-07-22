/*
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
 */

package com.jaeksoft.searchlib.test.rest;

import com.jaeksoft.searchlib.test.IntegrationTest;
import com.jaeksoft.searchlib.webservice.CommonListResult;
import com.jaeksoft.searchlib.webservice.CommonResult;
import com.qwazr.utils.StringUtils;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.xml.sax.SAXException;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class RestWebCrawlerTest extends CommonRestAPI {

	public final static String path = "/services/rest/index/" + IntegrationTest.INDEX_NAME + "/crawler/web/sitemap";
	public final static String siteMapUrl = "http://www.google.com/sitemap.xml";

	@Test
	public void testA_AddSiteMap() throws IllegalStateException, IOException, XPathExpressionException, SAXException,
			ParserConfigurationException {
		Response response = client().path(path).queryParam("site_map_url", siteMapUrl).request(
				MediaType.APPLICATION_JSON).put(Entity.json(StringUtils.EMPTY));
		checkCommonResult(response, CommonResult.class, 200);
	}

	@Test
	public void testB_testGetSiteMap()
			throws IllegalStateException, IOException, XPathExpressionException, SAXException,
			ParserConfigurationException {
		Response response = client().path(path).request(MediaType.APPLICATION_JSON).get();
		CommonListResult<String> cmd = checkCommonResult(response, CommonListResult.class, 200);
		assertNotNull(cmd.items.toArray());
		assertEquals(cmd.items.toArray()[0], siteMapUrl);
	}

	@Test
	public void testC_DeleteSiteMap() throws IllegalStateException, IOException, XPathExpressionException, SAXException,
			ParserConfigurationException {
		Response response = client().path(path).queryParam("site_map_url", siteMapUrl).request(
				MediaType.APPLICATION_JSON).delete();
		checkCommonResult(response, CommonResult.class, 200);
	}

	@Test
	public void testD_DeleteNoneSiteMap()
			throws IllegalStateException, IOException, XPathExpressionException, SAXException,
			ParserConfigurationException {
		Response response = client().path(path).queryParam("site_map_url", siteMapUrl).request(
				MediaType.APPLICATION_JSON).delete();
		checkCommonResult(response, CommonResult.class, 200);
	}

	@Test
	public void testE_GetSiteMap() throws IllegalStateException, IOException, XPathExpressionException, SAXException,
			ParserConfigurationException {
		Response response = client().path(path).request(MediaType.APPLICATION_JSON).get();
		checkCommonResult(response, CommonListResult.class, 200);
	}
}
