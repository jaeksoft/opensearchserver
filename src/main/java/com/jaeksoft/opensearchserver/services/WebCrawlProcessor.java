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

import com.jaeksoft.opensearchserver.crawler.CrawlerComponents;
import com.jaeksoft.opensearchserver.model.TaskRecord;
import com.jaeksoft.opensearchserver.model.WebCrawlTaskDefinition;
import com.qwazr.crawler.web.WebCrawlDefinition;
import com.qwazr.crawler.web.WebCrawlStatus;
import com.qwazr.crawler.web.WebCrawlerServiceInterface;
import com.qwazr.utils.StringUtils;

import java.net.URL;
import java.util.Objects;

public class WebCrawlProcessor extends CrawlProcessor<WebCrawlDefinition, WebCrawlStatus> {

	public WebCrawlProcessor(final ConfigService configService, final WebCrawlerServiceInterface webCrawlerService,
			final IndexesService indexesService) {
		super(configService, webCrawlerService, indexesService);
	}

	@Override
	public String getType() {
		return WebCrawlTaskDefinition.TYPE;
	}

	@Override
	protected WebCrawlDefinition getNextCrawlDefinition(final TaskRecord taskRecord) throws Exception {

		final WebCrawlTaskDefinition webCrawlTask = WebCrawlTaskDefinition.class.cast(taskRecord.getDefinition());

		final WebCrawlDefinition.Builder crawlBuilder = WebCrawlDefinition.of(webCrawlTask.crawlDefinition);

		Objects.requireNonNull(taskRecord.sessionTimeId, "The sessionTimeId is missing");

		final String indexName = indexesService.getIndexNameResolver(taskRecord.accountId).get(webCrawlTask.indexUuid);
		if (indexName == null)
			return null;
		final IndexService indexService = indexesService.getIndex(taskRecord.accountId, indexName);

		final int maxNextCrawl;
		if (webCrawlTask.crawlDefinition.maxUrlNumber != null) {
			final long crawledCount = indexService.getCrawledCount(webCrawlTask.getId(), taskRecord.sessionTimeId);
			if (crawledCount < webCrawlTask.crawlDefinition.maxUrlNumber)
				maxNextCrawl = Math.min((int) (webCrawlTask.crawlDefinition.maxUrlNumber - crawledCount), 100);
			else
				maxNextCrawl = 0;
		} else
			maxNextCrawl = 100;

		final int nextUnknownCount = maxNextCrawl > 0 ?
				indexService.fillUnknownUrls(maxNextCrawl, webCrawlTask.getId(), taskRecord.sessionTimeId,
						crawlBuilder) :
				0;

		if (nextUnknownCount == 0) { // We're done with this session
			if (indexService.isAlreadyCrawled(webCrawlTask.crawlDefinition.entryUrl, webCrawlTask.getId(),
					taskRecord.sessionTimeId)) {
				indexService.deleteOldCrawl(webCrawlTask.getId(), taskRecord.sessionTimeId);
				return null;
			}
			crawlBuilder.addUrl(webCrawlTask.crawlDefinition.entryUrl, 0);
		}

		final URL baseUrl = new URL(webCrawlTask.crawlDefinition.entryUrl);
		crawlBuilder.addInclusionPattern(baseUrl.toString());
		crawlBuilder.addInclusionPattern(baseUrl.getProtocol() + "://" + baseUrl.getHost() + "/*");
		crawlBuilder.setRemoveFragments(true);
		if (StringUtils.isBlank(webCrawlTask.crawlDefinition.userAgent))
			crawlBuilder.userAgent("OpenSearchServer-Bot");

		if (webCrawlTask.crawlDefinition.crawlWaitMs == null)
			crawlBuilder.setCrawlWaitMs(1000);
		else if (webCrawlTask.crawlDefinition.crawlWaitMs < 1000)
			crawlBuilder.setCrawlWaitMs(1000);
		else if (webCrawlTask.crawlDefinition.crawlWaitMs > 60000)
			crawlBuilder.setCrawlWaitMs(60000);

		return CrawlerComponents.buildCrawl(taskRecord.accountId, indexName, webCrawlTask.getId(),
				taskRecord.sessionTimeId, configService, crawlBuilder);
	}
}
