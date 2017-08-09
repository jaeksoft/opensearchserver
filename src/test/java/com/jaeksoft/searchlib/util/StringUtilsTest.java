/*
 * Copyright (C) 2009-2017 Emmanuel Keller / Jaeksoft
 * <p>
 * http://www.open-search-server.com
 * <p>
 * This file is part of OpenSearchServer.
 * <p>
 * OpenSearchServer is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * OpenSearchServer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with OpenSearchServer.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package com.jaeksoft.searchlib.util;

import org.junit.Assert;
import org.junit.Test;

public class StringUtilsTest {

	@Test
	public void testHtmlWrap() {
		final String text =
				"file://&shy;Users/ekeller/Moteur/infotoday_enterprisesearchsourcebook08/Open_on_Windows.exe";
		Assert.assertEquals(
				"file://Users/ekeller&shy;/Moteur/infotoday_en&shy;terprisesearchsource&shy;book08/Open_on_Windo&shy;ws.exe",
				StringUtils.htmlWrap(text, 20));
		Assert.assertEquals("file://Users/ekeller…terprisesearchsourcebook08/Open_on_Windows.exe",
				StringUtils.htmlWrapReduce(text, 20, 80));
	}

	@Test
	public void testUrlHostPathWrapReduce() {
		final String url =
				"file://Users/ekeller/Moteur/infotoday_enterprisesearchsourcebook08/Open_on_Windows.exe?test=2";
		Assert.assertEquals("Users/ekeller/…/infotoday_enterprisesearchsourcebook08/Open_on_Windows.exe",
				StringUtils.urlHostPathWrapReduce(url, 80));
		Assert.assertEquals("www.open-search-server.com",
				StringUtils.urlHostPathWrapReduce("http://www.open-search-server.com/", 80));
	}
}
