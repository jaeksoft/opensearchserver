/**
 * License Agreement for OpenSearchServer
 * <p>
 * Copyright (C) 2016-2017 Emmanuel Keller / Jaeksoft
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
 **/
package com.jaeksoft.searchlib.test;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.analysis.ClassPropertyEnum;
import com.jaeksoft.searchlib.analysis.LanguageEnum;
import com.jaeksoft.searchlib.parser.HtmlParser;
import com.jaeksoft.searchlib.parser.ParserFieldEnum;
import com.jaeksoft.searchlib.parser.ParserFieldTarget;
import com.jaeksoft.searchlib.parser.ParserResultItem;
import com.jaeksoft.searchlib.parser.htmlParser.HtmlParserEnum;
import com.jaeksoft.searchlib.streamlimiter.StreamLimiter;
import com.jaeksoft.searchlib.streamlimiter.StreamLimiterFile;
import com.jaeksoft.searchlib.util.map.SourceField;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class HtmlParserTest {

	private void testGeneratedSourceWithExclusion(final HtmlParserEnum htmlParserEnum) {
		try {
			final Path htmlFile =
					Paths.get("src", "test", "resources", "com", "jaeksoft", "searchlib", "test", "oss.html");
			final StreamLimiter streamLimiter =
					new StreamLimiterFile(0, htmlFile.toFile(), htmlFile.toUri().toString());
			final HtmlParser parser = new HtmlParser();
			parser.initProperties();
			parser.getFieldMap()
					.add(new SourceField("generatedSource"),
							new ParserFieldTarget("generatedSource", null, null, false));
			parser.setUserProperty(ClassPropertyEnum.XPATH_EXCLUSION.getName(), "//h3");
			parser.setUserProperty(ClassPropertyEnum.HTML_PARSER.getName(), htmlParserEnum.getLabel());
			parser.doParserContent(null, null, streamLimiter, LanguageEnum.ENGLISH);
			final List<ParserResultItem> results = parser.getParserResults();
			final String htmlProvider = results.get(0).getFieldValue(ParserFieldEnum.htmlProvider, 0);
			Assert.assertEquals(htmlParserEnum.getLabel(), htmlProvider);
			final String generatedSource = results.get(0).getFieldValue(ParserFieldEnum.generatedSource, 0);
			Assert.assertTrue(generatedSource.contains("<h4>"));
			Assert.assertFalse(generatedSource.contains("<h3>"));
		} catch (IOException | SearchLibException e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test
	public void testGeneratedSourceWithExclusionHtmlCleaner() {
		testGeneratedSourceWithExclusion(HtmlParserEnum.HtmlCleanerParser);
	}

	@Ignore
	public void testGeneratedSourceWithExclusionTagSoup() {
		testGeneratedSourceWithExclusion(HtmlParserEnum.TagSoupParser);
	}

	@Test
	public void testGeneratedSourceWithExclusionNekoHtml() {
		testGeneratedSourceWithExclusion(HtmlParserEnum.NekoHtmlParser);
	}
}
