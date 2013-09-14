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

import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;

import org.apache.commons.io.FileUtils;
import org.apache.http.client.ClientProtocolException;
import org.junit.Test;

/**
 * @author Ayyathurai N Naveen
 * 
 */
public class PatternTest extends TestCase {
	private CommonTestCase commomTestCase = null;

	public PatternTest(String name) {
		super(name);
		commomTestCase = new CommonTestCase();
	}

	@Test
	public void testInsertPattern() throws ClientProtocolException, IOException {
		File patterns = FileUtils.toFile(this.getClass().getResource(
				"patterns.txt"));
		int status = commomTestCase.postFile(patterns, "text/plain",
				CommonTestCase.PATTERN_API);
		assertEquals(200, status);
	}
}
