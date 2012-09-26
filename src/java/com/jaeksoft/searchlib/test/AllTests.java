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

import junit.framework.TestSuite;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ WebTemplateTestCase.class, PatternTestCase.class,
		WebCrawlerTestCase.class, IndexTestCase.class, OptimizeTestCase.class,
		SearchTestCase.class })
public class AllTests {

	public static TestSuite openSearchServerTestSuits()
			throws InterruptedException {
		TestSuite allTests = new TestSuite();
		allTests.addTest(WebTemplateTestCase.suite());
		allTests.addTest(PatternTestCase.suite());
		allTests.addTest(WebCrawlerTestCase.suite());
		allTests.addTest(IndexTestCase.suite());
		allTests.addTest(OptimizeTestCase.suite());
		allTests.addTest(SearchTestCase.suite());
		return allTests;
	}

}
