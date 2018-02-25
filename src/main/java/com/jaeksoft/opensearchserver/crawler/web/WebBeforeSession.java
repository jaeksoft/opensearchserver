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
import com.jaeksoft.opensearchserver.crawler.IndexQueue;
import com.jaeksoft.opensearchserver.services.IndexService;
import com.jaeksoft.opensearchserver.services.WebCrawlsService;
import com.qwazr.utils.StringUtils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;

public class WebBeforeSession extends WebAbstractEvent {

	@Override
	public boolean run(final EventContext context) throws Exception {

		final URL indexServiceUrl = toUrl(context.crawlSession.getVariable(INDEX_SERVICE_URL, String.class));
		final URL storeServiceUrl = toUrl(context.crawlSession.getVariable(STORE_SERVICE_URL, String.class));
		final String schemaName = context.crawlSession.getVariable(SCHEMA_NAME, String.class);
		final String indexName = context.crawlSession.getVariable(INDEX_NAME, String.class);
		final String crawlUuid = context.crawlSession.getVariable(CRAWL_UUID, String.class);
		final String taskCreationTime = context.crawlSession.getVariable(TASK_CREATION_TIME, String.class);

		final IndexService indexService = CrawlerComponents.getIndexService(indexServiceUrl, schemaName, indexName);

		context.crawlSession.setAttribute(INDEX_SERVICE, indexService, IndexService.class);
		context.crawlSession.setAttribute(INDEX_QUEUE, new IndexQueue(indexService, 100), IndexQueue.class);
		context.crawlSession.setAttribute(WEBCRAWLS_SERVICE,
				CrawlerComponents.getWebCrawlsService(storeServiceUrl, schemaName), WebCrawlsService.class);
		context.crawlSession.setAttribute(CRAWL_UUID, UUID.fromString(crawlUuid), UUID.class);
		context.crawlSession.setAttribute(TASK_CREATION_TIME, Long.valueOf(taskCreationTime), Long.class);

		return true;
	}

	private static URL toUrl(final String url) throws MalformedURLException {
		return StringUtils.isBlank(url) ? null : new URL(url);
	}
}
