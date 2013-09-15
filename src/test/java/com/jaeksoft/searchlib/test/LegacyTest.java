/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2012 - 2013 Emmanuel Keller / Jaeksoft
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

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.jaeksoft.searchlib.test.legacy.DeleteIndexTest;
import com.jaeksoft.searchlib.test.legacy.IndexTest;
import com.jaeksoft.searchlib.test.legacy.OptimizeTest;
import com.jaeksoft.searchlib.test.legacy.PatternTest;
import com.jaeksoft.searchlib.test.legacy.SearchTemplateTest;
import com.jaeksoft.searchlib.test.legacy.SearchTest;
import com.jaeksoft.searchlib.test.legacy.WebTemplateTest;

@RunWith(Suite.class)
@SuiteClasses({ WebTemplateTest.class, PatternTest.class, IndexTest.class,
		OptimizeTest.class, SearchTemplateTest.class, SearchTest.class,
		DeleteIndexTest.class })
public class LegacyTest {

}