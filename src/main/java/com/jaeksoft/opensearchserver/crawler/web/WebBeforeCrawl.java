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

import com.jaeksoft.opensearchserver.model.CrawlStatus;
import com.jaeksoft.opensearchserver.model.UrlRecord;

import java.io.IOException;
import java.net.URI;

public class WebBeforeCrawl extends WebAbstractEvent {

	@Override
	public boolean run(final EventContext context) throws Exception {

		final URI uri = context.currentCrawl.getUri();

		final UrlRecord.Builder linkBuilder = UrlRecord.of(uri)
				.crawlStatus(CrawlStatus.BEFORE_CRAWL)
				.crawlUuid(context.crawlUuid)
				.taskCreationTime(context.taskCreationTime);

		//Do we already have a status for this URL ?
		final UrlRecord urlRecord = context.indexService.getDocument(uri.toString());
		// Already known, we do not crawl
		if (urlRecord != null && !CrawlStatus.isUnknown(urlRecord.crawlStatus) &&
				context.crawlUuid.equals(urlRecord.getCrawlUuid()))
			return false;

		// If there is exclusions and if any exclusion matched we do not crawl
		if (context.currentCrawl.isInExclusion() != null && context.currentCrawl.isInExclusion())
			return noCrawl(context, uri, linkBuilder.crawlStatus(CrawlStatus.EXCLUSION_MATCH));

		// If there is inclusions and if no inclusion matches we do not crawl
		if (context.currentCrawl.isInInclusion() != null && !context.currentCrawl.isInInclusion())
			return noCrawl(context, uri, linkBuilder.crawlStatus(CrawlStatus.INCLUSION_MISS));

		if (context.currentCrawl.isRobotsTxtDisallow() != null && context.currentCrawl.isRobotsTxtDisallow())
			return noCrawl(context, uri, linkBuilder.crawlStatus(CrawlStatus.ROBOTS_TXT_DENY));

		if (context.currentCrawl.getError() != null)
			return noCrawl(context, uri, linkBuilder.crawlStatus(CrawlStatus.ERROR));

		// Update the link with the status BEFORE_CRAWL
		post(context, uri, linkBuilder);
		return true;
	}

	private boolean noCrawl(final EventContext context, final URI uri, final UrlRecord.Builder linkBuilder)
			throws IOException, InterruptedException {
		post(context, uri, linkBuilder);
		return false;
	}

	private void post(final EventContext context, final URI uri, final UrlRecord.Builder linkBuilder)
			throws IOException, InterruptedException {
		if (context.indexService.exists(uri.toString()))
			context.indexQueue.update(uri, linkBuilder.build());
		else
			context.indexQueue.post(uri,
					linkBuilder.hostAndUrlStore(null).lastModificationTime(System.currentTimeMillis()).build());
	}

}
