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

package com.jaeksoft.searchlib.test;

import java.io.File;
import java.io.IOException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.jaeksoft.searchlib.ClientCatalog;
import com.jaeksoft.searchlib.test.library.LibraryIndexCreateExistsListTest;
import com.jaeksoft.searchlib.test.library.LibraryIndexDataTest;
import com.jaeksoft.searchlib.test.library.LibraryIndexFileTest;
import com.jaeksoft.searchlib.test.library.LibrarySchemaTest;
import com.jaeksoft.searchlib.util.FilesUtils;

@RunWith(Suite.class)
@SuiteClasses({ LibraryIndexCreateExistsListTest.class,
		LibraryIndexFileTest.class, LibrarySchemaTest.class,
		LibraryIndexDataTest.class })
public class LibraryTest {

	public static final String EMPTY_INDEX_NAME = "oss_testing_empty";
	public static final String FILE_INDEX_NAME = "oss_testing_file";
	public static File DATA_DIRECTORY = null;
	public static File PDF_TEST_FILE = new File("src/test/resources/"
			+ LibraryTest.class.getPackage().getName().replace('.', '/')
			+ "/library", "Open Source Search Engine OpenSearchServer.pdf");

	@BeforeClass
	public static void before() throws IOException {
		DATA_DIRECTORY = FilesUtils.createTempDirectory("oss_data", "dir");
		ClientCatalog.init(DATA_DIRECTORY);
	}

	@AfterClass
	public static void after() {
		ClientCatalog.close();
	}
}