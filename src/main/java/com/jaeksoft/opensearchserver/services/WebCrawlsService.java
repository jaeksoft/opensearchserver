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
import com.qwazr.store.StoreServiceInterface;

import java.io.IOException;
import java.util.Collection;
import java.util.UUID;

public class WebCrawlsService extends StoreService<WebCrawlRecord> {

	private final static String WEB_CRAWLS_DIRECTORY = "web_crawls";

	public WebCrawlsService(final StoreServiceInterface storeService) {
		super(storeService, WEB_CRAWLS_DIRECTORY, WebCrawlRecord.class);
	}

	@Override
	protected String getStoreName(final WebCrawlRecord record) {
		return record.getUuid().toString();
	}

	public void save(final UUID accountId, final WebCrawlRecord crawlRecord) {
		super.save(accountId.toString(), null, crawlRecord);
	}

	public WebCrawlRecord read(final UUID accountId, final UUID webCrawlUuid) {
		return super.read(accountId.toString(), null, webCrawlUuid.toString());
	}

	public int collect(final UUID accountId, final int start, final int rows,
			final Collection<WebCrawlRecord> recordCollector) {
		return super.collect(accountId.toString(), null, start, rows, null, recordCollector);
	}

	public void remove(final UUID accountId, UUID webCrawlUuid) {
		super.remove(accountId.toString(), null, webCrawlUuid.toString());
	}

}
