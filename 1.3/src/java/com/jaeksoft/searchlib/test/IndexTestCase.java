/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2012 Emmanuel Keller / Jaeksoft
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
package com.jaeksoft.searchlib.test;

import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.commons.io.FileUtils;
import org.apache.http.client.ClientProtocolException;

public class IndexTestCase extends TestCase {
	private CommomTestCase commomTestCase = null;

	public IndexTestCase(String name) {
		super(name);
		commomTestCase = new CommomTestCase();
	}

	public void indexDocument() throws ClientProtocolException, IOException {
		File patterns = FileUtils.toFile(this.getClass().getResource(
				"documents.xml"));
		int status = commomTestCase.postFile(patterns, "text/xml",
				CommomTestCase.INDEX_API);
		assertEquals(200, status);
	}

	public static TestSuite suite() {
		TestSuite indexSuit = new TestSuite();
		indexSuit.addTest(new IndexTestCase("indexDocument"));
		return indexSuit;
	}
}
