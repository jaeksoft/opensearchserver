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

import javax.naming.NamingException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.ClientCatalog;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.test.LibraryTest;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class LibraryIndexCreateExistsListTest {

	@Test
	public void testAdeleteRemainingIndex() throws SearchLibException,
			NamingException, IOException {
		if (ClientCatalog.exists(LibraryTest.INDEX_NAME))
			ClientCatalog.eraseIndex(LibraryTest.INDEX_NAME);
	}

	@Test
	public void testBCreateIndex() throws IllegalStateException, IOException,
			XPathExpressionException, SAXException,
			ParserConfigurationException, SearchLibException {
		ClientCatalog.createIndex(LibraryTest.INDEX_NAME, "FILE_CRAWLER");
	}

	@Test
	public void testCExistsIndex() throws IllegalStateException, IOException,
			XPathExpressionException, SAXException,
			ParserConfigurationException, SearchLibException {
		assertTrue(ClientCatalog.exists(LibraryTest.INDEX_NAME));
	}

}
