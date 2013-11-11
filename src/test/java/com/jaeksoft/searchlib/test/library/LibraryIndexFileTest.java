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
import com.jaeksoft.searchlib.parser.ParserSelector;
import com.jaeksoft.searchlib.test.LibraryTest;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class LibraryIndexFileTest {

	@Test
	public void testACreateIndex() throws IllegalStateException, IOException,
			XPathExpressionException, SAXException,
			ParserConfigurationException, SearchLibException {
		// Create an index using the FILE_CRAWLER template
		ClientCatalog.createIndex(LibraryTest.FILE_INDEX_NAME, "FILE_CRAWLER");
	}

	@Test
	public void testBIndexPdfDocument() throws SearchLibException, IOException {
		// Get the client instance
		Client client = ClientCatalog.getClient(LibraryTest.FILE_INDEX_NAME);

		// Create a document
		IndexDocument document = new IndexDocument(LanguageEnum.ENGLISH);
		assertTrue(
				"File not found: "
						+ LibraryTest.PDF_TEST_FILE.getAbsolutePath(),
				LibraryTest.PDF_TEST_FILE.exists());

		// Get the parser selector instance
		ParserSelector parserSelector = client.getParserSelector();

		// Extract full-text information and populate the document
		parserSelector.parseFile(document, LibraryTest.PDF_TEST_FILE.getName(),
				null, null, LibraryTest.PDF_TEST_FILE, LanguageEnum.ENGLISH);

		// Add the URI field to the document
		document.addString("uri", LibraryTest.PDF_TEST_FILE.toURI().toString());

		// Put in in the index
		client.updateDocument(document);
	}
}
