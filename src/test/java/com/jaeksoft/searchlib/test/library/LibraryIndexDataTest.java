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

package com.jaeksoft.searchlib.test.library;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.ClientCatalog;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.analysis.LanguageEnum;
import com.jaeksoft.searchlib.facet.Facet;
import com.jaeksoft.searchlib.facet.FacetCounter;
import com.jaeksoft.searchlib.index.IndexDocument;
import com.jaeksoft.searchlib.query.ParseException;
import com.jaeksoft.searchlib.request.SearchFieldRequest;
import com.jaeksoft.searchlib.result.AbstractResultSearch;
import com.jaeksoft.searchlib.result.ResultDocument;
import com.jaeksoft.searchlib.test.LibraryTest;
import com.jaeksoft.searchlib.webservice.query.search.SearchFieldQuery.SearchField.Mode;
import com.jaeksoft.searchlib.webservice.query.search.SearchQueryAbstract.FragmenterEnum;
import com.jaeksoft.searchlib.webservice.query.search.SearchQueryAbstract.OperatorEnum;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class LibraryIndexDataTest {

	@Test
	public void testA_InsertData() throws IOException, SearchLibException {

		// Get the client instance
		Client client = ClientCatalog.getClient(LibraryTest.EMPTY_INDEX_NAME);

		// Building a collection of document
		List<IndexDocument> documents = new ArrayList<IndexDocument>();

		// Create a first document
		IndexDocument firstDocument = new IndexDocument(LanguageEnum.ENGLISH);
		firstDocument.addString("id", "1");
		firstDocument.addString("title", "Open source search engine");
		firstDocument.addString("content",
				"OpenSearchServer is an open source search engine software.");
		firstDocument.addString("category", "Article");
		// Add it to the list
		documents.add(firstDocument);

		// Create a second document
		IndexDocument secondDocument = new IndexDocument(LanguageEnum.FRENCH);
		secondDocument.addString("id", "2");
		secondDocument.addString("title", "Moteur de recherche open source");
		secondDocument.addString("content",
				"OpenSearchServer est un moteur de recherche open source.");
		secondDocument.addString("category", "Article");
		// Add it to the list
		documents.add(secondDocument);

		// Put documents in the index
		assertEquals(client.updateDocuments(documents), 2);
	}

	@Test
	public void testB_SearchData() throws SearchLibException, ParseException {

		// Get the client instance
		Client client = ClientCatalog.getClient(LibraryTest.EMPTY_INDEX_NAME);

		// Create a new search request
		SearchFieldRequest request = new SearchFieldRequest(client);

		// In the field TITLE, we look for term and phrase, with a respective
		// boost factor of 5 and 10.
		request.addSearchField("title", Mode.TERM_AND_PHRASE, 5.0F, 10.0F, 2,
				null);
		// In the field "titleExact", we look for terms, with a boost factor of
		// 5.
		request.addSearchField("titleExact", 5.0F);
		// Same for "content" and "contentExact"
		request.addSearchField("content", Mode.TERM_AND_PHRASE, 1.0F, 2.0F, 2,
				null);
		request.addSearchField("contentExact", 1.0F);
		// The "full" field contains both title and content terms
		request.addSearchField("full", 1.0F);

		// For each returned document, we want the "category" field
		request.addReturnField("category");

		// We want a snippet (extracted text with highlighting) on "title" and
		// "content"
		request.addSnippetField("title", FragmenterEnum.NO, 70, "b", 1);
		request.addSnippetField("content", FragmenterEnum.SENTENCE, 200, "b", 1);

		// We search the expression "open source"
		request.setQueryString("open source");
		request.setDefaultOperator(OperatorEnum.AND);

		// We want the first 10 documents found
		request.setStart(0);
		request.setRows(10);

		// We only want "Article" document
		request.addFilter("category:Article", false);

		// We want facet count on category
		request.addFacet("category", 1, false, false, null, null, null);

		// Let's execute the search request
		AbstractResultSearch<?> results = (AbstractResultSearch<?>) client
				.request(request);

		// Check the number of returned document
		assertEquals(2, results.getNumFound());

		// Iterate over the documents found
		for (ResultDocument document : results) {
			// Get and check the category value
			String category = document.getValueContent("category", 0);
			assertEquals(category, "Article");

			// Get and check snippet of the title
			String title = document.getSnippetContent("title", 0);
			assertNotNull(title);
			assertTrue(title.trim().length() > 0);

			// Get and check snippet of the content
			String content = document.getSnippetContent("content", 0);
			assertNotNull(content);
			assertTrue(content.trim().length() > 0);
		}

		// Get the facet calculated over the category field
		Facet facet = results.getFacetList().getByField("category");
		assertNotNull(facet);
		// Iterate over the facet items
		for (Map.Entry<String, FacetCounter> facetItem : facet) {
			String term = facetItem.getKey();
			assertEquals(term, "Article");
			long count = facetItem.getValue().count;
			assertEquals(count, 2);
		}
	}
}
