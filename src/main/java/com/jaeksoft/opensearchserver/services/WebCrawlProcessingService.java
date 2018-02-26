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

import com.jaeksoft.opensearchserver.Components;
import com.jaeksoft.opensearchserver.crawler.CrawlerComponents;
import com.jaeksoft.opensearchserver.model.WebCrawlTaskRecord;
import com.qwazr.crawler.web.WebCrawlDefinition;
import com.qwazr.crawler.web.WebCrawlStatus;
import com.qwazr.crawler.web.WebCrawlerServiceInterface;

import java.net.MalformedURLException;
import java.net.URL;

public class WebCrawlProcessingService
		extends CrawlProcessingService<WebCrawlTaskRecord, WebCrawlDefinition, WebCrawlStatus> {

	public WebCrawlProcessingService(final WebCrawlerServiceInterface webCrawlerService,
			final IndexesService indexesService) {
		super(webCrawlerService, indexesService);
	}

	public Class<WebCrawlTaskRecord> getTaskRecordClass() {
		return WebCrawlTaskRecord.class;
	}

	@Override
	protected WebCrawlDefinition getNewCrawlDefinition(final String schema, final WebCrawlTaskRecord taskRecord)
			throws MalformedURLException {

		final WebCrawlDefinition.Builder crawlBuilder = WebCrawlDefinition.of(taskRecord.crawlDefinition);

		final String indexName = indexesService.getIndexNameResolver(schema).get(taskRecord.indexUuid);
		final IndexService indexService = indexesService.getIndex(schema, indexName);

		final int count =
				indexService.fillUnknownUrls(100, taskRecord.crawlUuid, taskRecord.creationTime, crawlBuilder);
		if (count == 0)
			crawlBuilder.addUrl(taskRecord.crawlDefinition.entryUrl, 0);

		final URL baseUrl = new URL(taskRecord.crawlDefinition.entryUrl);
		crawlBuilder.addInclusionPattern(baseUrl.toString());
		crawlBuilder.addInclusionPattern(baseUrl.getProtocol() + "://" + baseUrl.getHost() + "/*");

		if (taskRecord.crawlDefinition.crawlWaitMs == null)
			crawlBuilder.setCrawlWaitMs(1000);
		else if (taskRecord.crawlDefinition.crawlWaitMs < 1000)
			crawlBuilder.setCrawlWaitMs(1000);
		else if (taskRecord.crawlDefinition.crawlWaitMs > 60000)
			crawlBuilder.setCrawlWaitMs(60000);

		return CrawlerComponents.buildCrawl(schema, indexName, taskRecord.crawlUuid, taskRecord.creationTime, null,
				null, crawlBuilder);
	}
}
