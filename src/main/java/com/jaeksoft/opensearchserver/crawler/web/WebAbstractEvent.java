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

import com.jaeksoft.opensearchserver.crawler.CrawlerContext;
import com.jaeksoft.opensearchserver.crawler.IndexQueue;
import com.jaeksoft.opensearchserver.services.IndexService;
import com.jaeksoft.opensearchserver.services.WebCrawlsService;
import com.qwazr.crawler.web.WebCrawlScriptEvent;
import com.qwazr.crawler.web.WebCrawlSession;
import com.qwazr.crawler.web.WebCurrentCrawl;

import java.util.Map;
import java.util.UUID;

public abstract class WebAbstractEvent extends WebCrawlScriptEvent implements CrawlerContext {

	protected abstract boolean run(final EventContext context) throws Exception;

	@Override
	protected final boolean run(final WebCrawlSession session, final WebCurrentCrawl crawl,
			final Map<String, ?> variables) throws Exception {
		return run(new EventContext(session, crawl));
	}

	public class EventContext {

		final WebCrawlSession crawlSession;
		final WebCurrentCrawl currentCrawl;
		final UUID crawlUuid;
		final Long taskCreationTime;
		//final Path sessionTempDirectory;

		public final IndexService indexService;
		public final WebCrawlsService webCrawlsService;
		public final IndexQueue indexQueue;

		private EventContext(final WebCrawlSession crawlSession, final WebCurrentCrawl currentCrawl) {
			this.crawlSession = crawlSession;
			this.currentCrawl = currentCrawl;
			indexService = crawlSession.getAttribute(INDEX_SERVICE, IndexService.class);
			webCrawlsService = crawlSession.getAttribute(WEBCRAWLS_SERVICE, WebCrawlsService.class);
			indexQueue = crawlSession.getAttribute(INDEX_QUEUE, IndexQueue.class);
			crawlUuid = crawlSession.getAttribute(CRAWL_UUID, UUID.class);
			taskCreationTime = crawlSession.getAttribute(TASK_CREATION_TIME, Long.class);
			//sessionTempDirectory = crawlSession.getAttribute(SESSION_TEMP_DIRECTORY, Path.class);
		}

	}

}
