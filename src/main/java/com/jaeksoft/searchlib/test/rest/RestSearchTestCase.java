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
import java.io.InputStream;
import java.net.URISyntaxException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import junit.framework.TestCase;

import org.apache.http.client.utils.URIBuilder;
import org.junit.Test;

import com.jaeksoft.searchlib.webservice.CommonResult;
import com.jaeksoft.searchlib.webservice.query.search.SearchResult;

/**
 * @author Ayyathurai N Naveen
 * 
 */
public class RestSearchTestCase extends TestCase {
	private CommonRestTestCase commonRestTestCase = null;

	public RestSearchTestCase(String name) {
		super(name);
		commonRestTestCase = new CommonRestTestCase();

	}

	@Test
	public void testASimpleSearch() throws IllegalStateException, IOException,
			URISyntaxException, JAXBException {
		URIBuilder builder = commonRestTestCase.getURIBuilder("/select/search/"
				+ CommonRestTestCase.INDEX_NAME + "/xml");
		builder.addParameter("query", "*:*");
		builder.addParameter("template", "search");
		InputStream inputStream = commonRestTestCase.httpGet(builder);
		JAXBContext context = JAXBContext.newInstance(CommonResult.class,
				SearchResult.class);
		Unmarshaller unmarshaller = context.createUnmarshaller();
		SearchResult selectResult = (SearchResult) unmarshaller
				.unmarshal(inputStream);
		checkNumResults(selectResult, 233);
		checkRows(selectResult, 10);
		checkCollapseDocCount(selectResult, 0);
		checkFirstDocument(selectResult,
				"http://www.open-search-server.com/features/");
	}

	@Test
	public void testBSimpleSearchWithFilter() throws IllegalStateException,
			IOException, URISyntaxException, JAXBException {
		URIBuilder builder = commonRestTestCase.getURIBuilder("/select/search/"
				+ CommonRestTestCase.INDEX_NAME + "/xml");
		builder.addParameter("query", "*:*");
		builder.addParameter("template", "search");
		builder.addParameter("filter",
				"url:\"http://www.open-search-server.com/\"");
		InputStream inputStream = commonRestTestCase.httpGet(builder);
		JAXBContext context = JAXBContext.newInstance(CommonResult.class,
				SearchResult.class);
		Unmarshaller unmarshaller = context.createUnmarshaller();
		SearchResult selectResult = (SearchResult) unmarshaller
				.unmarshal(inputStream);
		checkNumResults(selectResult, 1);
		checkRows(selectResult, 10);
		checkCollapseDocCount(selectResult, 0);
		checkFirstDocument(selectResult, "http://www.open-search-server.com/");
	}

	@Test
	public void testAfullSearch() throws IllegalStateException, IOException,
			URISyntaxException, JAXBException {
		URIBuilder builder = commonRestTestCase
				.getURIBuilder("/select/fullsearch/"
						+ CommonRestTestCase.INDEX_NAME + "/xml");
		builder.addParameter("query", "*:*");
		builder.addParameter("template", "search");
		builder.addParameter("collapseField", "title");
		builder.addParameter("collapseMax", "1");
		builder.addParameter("collapseMode", "CLUSTER");
		builder.addParameter("collapseType", "FULL");
		builder.addParameter("facet", "titleExact");
		System.out.println(builder.build().toString());
		InputStream inputStream = commonRestTestCase.httpGet(builder);
		JAXBContext context = JAXBContext.newInstance(CommonResult.class,
				SearchResult.class);
		Unmarshaller unmarshaller = context.createUnmarshaller();
		SearchResult selectResult = (SearchResult) unmarshaller
				.unmarshal(inputStream);
		checkNumResults(selectResult, 233);
		checkRows(selectResult, 10);
		checkCollapseDocCount(selectResult, 186);
		checkFirstDocument(selectResult,
				"http://www.open-search-server.com/features/");
		checkFacet(selectResult, 45);
	}

	public void checkFacet(SearchResult selectResult, int facets) {
		assertEquals(facets, selectResult.facets.get(0).terms.size());
	}

	public void checkNumResults(SearchResult selectResult, int numFound) {
		assertEquals(numFound, selectResult.numFound);
	}

	public void checkRows(SearchResult selectResult, int rows) {
		assertEquals(rows, selectResult.rows);
	}

	public void checkCollapseDocCount(SearchResult selectResult,
			int collapseDocCount) {
		assertEquals(collapseDocCount, selectResult.collapsedDocCount);
	}

	public void checkFirstDocument(SearchResult selectResult, String urlExact) {
		String url = selectResult.documents.get(0).fields.get(1).values.get(0);
		assertEquals(urlExact, url);
	}

}
