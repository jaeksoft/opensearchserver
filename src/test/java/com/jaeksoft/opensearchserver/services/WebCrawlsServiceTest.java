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

package com.jaeksoft.opensearchserver.services;

import com.jaeksoft.opensearchserver.BaseTest;
import com.jaeksoft.opensearchserver.model.WebCrawlRecord;
import com.qwazr.crawler.web.WebCrawlDefinition;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

public class WebCrawlsServiceTest extends BaseTest {

	private WebCrawlsService webCrawlsService;

	@Before
	public void setup() throws IOException {
		webCrawlsService = getWebCrawlsService();
	}

	private void checkWebCrawlRecordsResult(WebCrawlsService.RecordsResult result, int expectedTotalCount,
			WebCrawlRecord... expectedRecords) throws IOException {
		Assert.assertNotNull(result);
		Assert.assertEquals(expectedTotalCount, result.getTotalCount());
		int i = 0;
		for (WebCrawlRecord record : expectedRecords) {
			Assert.assertEquals(record, result.getRecords().get(i++));
			Assert.assertEquals(record, webCrawlsService.read(null, record.getUuid()));
		}
	}

	@Test
	public void getEmptyList() throws IOException {
		checkWebCrawlRecordsResult(webCrawlsService.get(null, 0, 10), 0);
	}

	public static WebCrawlRecord createNewCrawlRecord() {
		final WebCrawlDefinition crawlDefinition =
				WebCrawlDefinition.of().setEntryUrl("http://www.opensearchserver.com").build();
		return WebCrawlRecord.of().name("test").crawlDefinition(crawlDefinition).build();
	}

	@Test
	public void globalTest() throws IOException {
		// Save one record
		final WebCrawlRecord crawlRecord1 = createNewCrawlRecord();
		webCrawlsService.save(null, crawlRecord1);
		checkWebCrawlRecordsResult(webCrawlsService.get(null, 0, 10), 1, crawlRecord1);

		// Save a second new record
		final WebCrawlRecord crawlRecord2 = createNewCrawlRecord();
		webCrawlsService.save(null, crawlRecord2);
		checkWebCrawlRecordsResult(webCrawlsService.get(null, 0, 10), 2, crawlRecord1, crawlRecord2);

		// Update the first one
		webCrawlsService.save(null, crawlRecord1);
		checkWebCrawlRecordsResult(webCrawlsService.get(null, 0, 10), 2, crawlRecord1, crawlRecord2);

		// Remove the first one
		webCrawlsService.remove(null, crawlRecord1.getUuid());
		checkWebCrawlRecordsResult(webCrawlsService.get(null, 0, 10), 1, crawlRecord2);
	}

}
