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

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.ClientCatalog;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.analysis.LanguageEnum;
import com.jaeksoft.searchlib.index.IndexDocument;
import com.jaeksoft.searchlib.parser.Parser;
import com.jaeksoft.searchlib.parser.ParserResultItem;
import com.jaeksoft.searchlib.parser.ParserSelector;
import com.jaeksoft.searchlib.query.ParseException;
import com.jaeksoft.searchlib.request.AbstractSearchRequest;
import com.jaeksoft.searchlib.result.AbstractResultSearch;
import com.jaeksoft.searchlib.result.ResultDocument;
import com.jaeksoft.searchlib.test.LibraryTest;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class LibraryIndexFileTest {

	@Test
	public void testACreateIndex() throws IllegalStateException, IOException,
			XPathExpressionException, SAXException,
			ParserConfigurationException, SearchLibException {
		// Create an index using the FILE_CRAWLER template
		ClientCatalog.createIndex(LibraryTest.FILE_INDEX_NAME, "FILE_CRAWLER",
				null, null);
	}

	@Test
	public void testBIndexPdfDocument() throws SearchLibException, IOException,
			ClassNotFoundException {
		// Get the client instance
		Client client = ClientCatalog.getClient(LibraryTest.FILE_INDEX_NAME);

		// Check that the PDF test file exixts
		assertTrue(
				"File not found: "
						+ LibraryTest.PDF_TEST_FILE.getAbsolutePath(),
				LibraryTest.PDF_TEST_FILE.exists());

		// Get the parser selector instance
		ParserSelector parserSelector = client.getParserSelector();

		// Extract full-text information
		Parser parser = parserSelector.parseFile(LibraryTest.PDF_TEST_FILE,
				LanguageEnum.ENGLISH);

		int count = 0;

		// The parser may returns several documents
		for (ParserResultItem parserResultItem : parser.getParserResults()) {

			// Create a document
			IndexDocument document = new IndexDocument(LanguageEnum.ENGLISH);

			// Populate the document with the full-text fields
			parserResultItem.populate(document);

			// Add the URL field to the document (building a unique URL)
			count++;
			document.addString("url", LibraryTest.PDF_TEST_FILE.toURI()
					.toString() + "#" + count);

			// Put in in the index
			client.updateDocument(document);
		}

	}

	@Test
	public void testC_SearchData() throws SearchLibException, ParseException {

		// Get the client instance
		Client client = ClientCatalog.getClient(LibraryTest.FILE_INDEX_NAME);

		// Get the default search template
		AbstractSearchRequest request = (AbstractSearchRequest) client
				.getNewRequest("search");

		// We search the expression "open"
		request.setQueryString("open source");

		// We want the first 10 documents found
		request.setStart(0);
		request.setRows(10);

		// Let's execute the search request
		AbstractResultSearch results = (AbstractResultSearch) client
				.request(request);

		// Check the number of returned document
		assertEquals(1, results.getNumFound());

		// Iterate over the documents found
		for (ResultDocument document : results) {

			// Get and check snippet of the content
			String content = document.getSnippetContent("content", 0);
			assertNotNull(content);
			assertTrue(content.trim().length() > 0);
		}

	}
}
