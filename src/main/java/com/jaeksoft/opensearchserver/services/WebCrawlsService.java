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

import com.jaeksoft.opensearchserver.model.WebCrawlRecord;
import com.qwazr.crawler.web.WebCrawlStatus;
import com.qwazr.crawler.web.WebCrawlerServiceInterface;
import com.qwazr.server.client.ErrorWrapper;
import com.qwazr.store.StoreServiceInterface;

import java.io.IOException;
import java.util.UUID;

public class WebCrawlsService extends StoreService<WebCrawlRecord> {

	private final static String WEB_CRAWLS_DIRECTORY = "web_crawls";

	private final WebCrawlerServiceInterface webCrawlerService;

	public WebCrawlsService(final StoreServiceInterface storeService, final String storeSchema,
			final WebCrawlerServiceInterface webCrawlerService) {
		super(storeService, storeSchema, WEB_CRAWLS_DIRECTORY, WebCrawlRecord.class);
		this.webCrawlerService = webCrawlerService;
	}

	@Override
	protected UUID getUuid(final WebCrawlRecord record) {
		return record.getUuid();
	}

	public void save(final WebCrawlRecord crawlRecord) throws IOException {
		super.save(null, crawlRecord);
	}

	public WebCrawlRecord read(final UUID webCrawlUuid) throws IOException {
		return super.read(null, webCrawlUuid);
	}

	public RecordsResult get(final int start, final int rows) throws IOException {
		return super.get(null, start, rows);
	}

	public void remove(UUID webCrawlUuid) {
		super.remove(null, webCrawlUuid);
	}

	public WebCrawlStatus getCrawlStatus(final UUID webCrawlUuid) {
		return ErrorWrapper.bypass(() -> webCrawlerService.getSession(webCrawlUuid.toString()), 404);
	}

}
