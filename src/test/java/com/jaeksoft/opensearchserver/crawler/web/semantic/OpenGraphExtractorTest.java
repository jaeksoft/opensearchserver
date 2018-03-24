/*
 * Copyright 2017-2018 Emmanuel Keller / Jaeksoft
 *  <p>
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *  <p>
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  <p>
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.jaeksoft.opensearchserver.crawler.web.semantic;

import com.jaeksoft.opensearchserver.model.UrlRecord;
import com.qwazr.extractor.ExtractorManager;
import com.qwazr.extractor.ParserResult;
import org.junit.Assert;
import org.junit.Test;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

public class OpenGraphExtractorTest {

	@Test
	public void test() throws IOException {
		final ExtractorManager extractorManager = new ExtractorManager();
		extractorManager.registerServices();
		final MultivaluedMap<String, String> params = new MultivaluedHashMap<>();
		params.addAll("xpath", OpenGraphExtractor.SELECTOR_XPATH_PROPERTY, OpenGraphExtractor.SELECTOR_XPATH_CONTENT);
		params.addAll("xpath_name", OpenGraphExtractor.SELECTOR_NAME_PROPERTY,
				OpenGraphExtractor.SELECTOR_NAME_CONTENT);
		try (final InputStream input = getClass().getResourceAsStream("open-graph-test.html")) {
			final ParserResult parserResult = extractorManager.getService().extract("html", params, null, input);
			final UrlRecord.Builder urlBuilder = UrlRecord.of(URI.create("file://open-graph-test.html"));
			Assert.assertTrue(OpenGraphExtractor.extract(parserResult, urlBuilder));
			final UrlRecord urlRecord = urlBuilder.build();
			Assert.assertEquals("Binance denies 'criminal warning' from Japanese regulator » Brave New Coin",
					urlRecord.title);
			Assert.assertEquals(
					"The sharp pullback in the price of Bitcoin late on Thursday was attributed to the news that Japan's regulator had issued a warning to Binance, one of the world's biggest cryptocurrency exchanges, that it was operating in the country illegally and criminal charges would be filed if it did not cease.",
					urlRecord.description);
			Assert.assertEquals(Long.valueOf(1521772005), urlRecord.datePublished);
			Assert.assertEquals("https://bravenewcoin.com/assets/Uploads/bnc-tokyo-banner.jpg", urlRecord.imageUri);
			Assert.assertEquals("article", urlRecord.schemaOrgType);
			Assert.assertEquals("Bravenewcoin", urlRecord.organizationName);
		}
	}
}
