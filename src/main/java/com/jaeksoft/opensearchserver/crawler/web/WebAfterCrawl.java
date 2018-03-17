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

package com.jaeksoft.opensearchserver.crawler.web;

import com.jaeksoft.opensearchserver.crawler.CrawlerComponents;
import com.jaeksoft.opensearchserver.model.CrawlStatus;
import com.jaeksoft.opensearchserver.model.IndexStatus;
import com.jaeksoft.opensearchserver.model.UrlRecord;

import java.net.URI;

public class WebAfterCrawl extends WebAbstractEvent {

	@Override
	protected boolean run(final EventContext context) throws Exception {

		if (context.currentCrawl.isIgnored())
			return true;

		final URI currentUri = context.currentCrawl.getUri();

		final UrlRecord.Builder urlBuilder = UrlRecord.of(currentUri)
				.crawlUuid(context.sessionStore.crawlUuid)
				.hostAndUrlStore(currentUri.getHost())
				.taskCreationTime(context.sessionStore.taskCreationTime)
				.depth(context.currentCrawl.getDepth())
				.lastModificationTime(System.currentTimeMillis())
				.httpContentType(context.currentCrawl.getContentType())
				.httpStatus(context.currentCrawl.getStatusCode());

		if (context.currentCrawl.getRedirect() != null) {
			context.sessionStore.saveCrawl(currentUri,
					urlBuilder.crawlStatus(CrawlStatus.REDIRECTION).indexStatus(IndexStatus.NOT_INDEXABLE).build());
			return true;
		}

		if (context.currentCrawl.getError() != null) {
			context.sessionStore.saveCrawl(currentUri,
					urlBuilder.crawlStatus(CrawlStatus.ERROR).indexStatus(IndexStatus.ERROR).build());
			return true;
		}

		if (!context.currentCrawl.isCrawled()) {
			context.sessionStore.saveCrawl(currentUri,
					urlBuilder.crawlStatus(CrawlStatus.NOT_CRAWLABLE).indexStatus(IndexStatus.NOT_INDEXABLE).build());
			return true;
		}

		urlBuilder.crawlStatus(CrawlStatus.CRAWLED).indexStatus(IndexStatus.INDEXED);

		// We put links in the database
		final int nextDepth = context.currentCrawl.getDepth() + 1;
		context.sessionStore.saveNewLinks(context.currentCrawl.getFilteredLinks(), nextDepth);

		// Call indexer
		CrawlerComponents.getExtractorIndexer().extract(context.currentCrawl, urlBuilder);

		context.sessionStore.saveCrawl(currentUri, urlBuilder.build());
		return true;
	}

}
