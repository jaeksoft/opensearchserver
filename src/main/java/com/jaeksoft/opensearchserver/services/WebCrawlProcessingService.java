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

import com.jaeksoft.opensearchserver.model.WebCrawlTaskRecord;
import com.qwazr.crawler.web.WebCrawlStatus;
import com.qwazr.crawler.web.WebCrawlerServiceInterface;
import com.qwazr.server.client.ErrorWrapper;

import java.util.UUID;

public class WebCrawlProcessingService implements TasksProcessingService<WebCrawlTaskRecord> {

	private WebCrawlerServiceInterface webCrawlerService;

	public WebCrawlProcessingService(final WebCrawlerServiceInterface webCrawlerService) {
		this.webCrawlerService = webCrawlerService;
	}

	public Class<WebCrawlTaskRecord> getTaskRecordClass() {
		return WebCrawlTaskRecord.class;
	}

	@Override
	public boolean isRunning(final UUID taskUuid) {
		final WebCrawlStatus webCrawlStatus =
				ErrorWrapper.bypass(() -> webCrawlerService.getSession(taskUuid.toString()), 404);
		return webCrawlStatus != null && webCrawlStatus.endTime != null;
	}

	public void checkIsRunning(final WebCrawlTaskRecord taskRecord) {
		if (isRunning(taskRecord.getUuid()))
			return;
		// TODO Start the crawl process
		System.out.println("TODO Start the crawl process");
	}
}
