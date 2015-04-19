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

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.ClientCatalog;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.schema.Indexed;
import com.jaeksoft.searchlib.schema.Schema;
import com.jaeksoft.searchlib.schema.Stored;
import com.jaeksoft.searchlib.schema.TermVector;
import com.jaeksoft.searchlib.test.LibraryTest;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class LibrarySchemaTest {

	/**
	 * Create an empty index
	 * 
	 * @throws IOException
	 * @throws SearchLibException
	 */
	@Test
	public void testA_CreateIndex() throws IOException, SearchLibException {
		ClientCatalog.createIndex(LibraryTest.EMPTY_INDEX_NAME, "EMPTY_INDEX",
				null);
	}

	/**
	 * Add fields to the schema
	 * 
	 * @throws SearchLibException
	 * @throws IOException
	 */
	@Test
	public void testB_CreateFields() throws SearchLibException, IOException {
		// Get the client instance of the index
		Client client = ClientCatalog.getClient(LibraryTest.EMPTY_INDEX_NAME);

		// Get the schema instance
		Schema schema = client.getSchema();

		// Create few fields
		// The ID is the primary key of the index
		schema.setField("id", Stored.NO, Indexed.YES, TermVector.NO, null);

		// The "title" field will index the stemmed version of the title.
		// Positions and offsets are activated to allow snippets.
		schema.setField("title", Stored.YES, Indexed.YES,
				TermVector.POSITIONS_OFFSETS, "TextAnalyzer");

		// The "titleExact" field will index another version of the title
		schema.setField("titleExact", Stored.NO, Indexed.YES, TermVector.NO,
				"StandardAnalyzer", "title");

		// "Content" will index the stemmed version of the content (with
		// snippets).
		schema.setField("content", Stored.YES, Indexed.YES,
				TermVector.POSITIONS_OFFSETS, "TextAnalyzer");

		// "ContentExact" index the exact match for the content
		schema.setField("contentExact", Stored.NO, Indexed.YES, TermVector.NO,
				"StandardAnalyzer", "content");

		// "Full" will index the stemmed version of both title and content,
		// let's found document with keywords located on different fields.
		schema.setField("full", Stored.NO, Indexed.YES, TermVector.NO,
				"TextAnalyzer", "title", "content");

		// This field will be used for filtering and faceting.
		schema.setField("category", Stored.NO, Indexed.YES, TermVector.NO, null);

		schema.setField("dummyField", Stored.NO, Indexed.NO, TermVector.NO,
				null);
	}

	/**
	 * Set the defaults and the unique field of the schema
	 * 
	 * @throws SearchLibException
	 */
	@Test
	public void testC_SetDefaultAndUniqueFields() throws SearchLibException {
		// Get the client instance of the index
		Client client = ClientCatalog.getClient(LibraryTest.EMPTY_INDEX_NAME);

		// Get the schema instance
		Schema schema = client.getSchema();

		// Set the default and the unique field
		schema.setDefaultUniqueField("content", "id");

		assertTrue("Unique field is not set",
				"id".equals(schema.getUniqueField()));
		assertTrue("Default field is not set",
				"content".equals(schema.getDefaultField()));
	}

	/**
	 * Remove a field from the schema
	 * 
	 * @throws SearchLibException
	 */
	@Test
	public void testD_DeleteField() throws SearchLibException {
		// Get the client instance of the index
		Client client = ClientCatalog.getClient(LibraryTest.EMPTY_INDEX_NAME);

		// Get the schema instance
		Schema schema = client.getSchema();

		assertTrue("The dummy field does not exist",
				schema.getField("dummyField") != null);

		// Remove the field
		schema.removeField("dummyField");

		assertTrue("The dummy field still exists",
				schema.getField("dummyField") == null);

	}
}
