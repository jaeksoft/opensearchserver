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
import com.qwazr.store.StoreScript;
import com.qwazr.store.StoreServiceInterface;
import com.qwazr.store.StoreWalkResult;
import com.qwazr.utils.StringUtils;

import javax.ws.rs.core.GenericType;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public class WebCrawlsService extends StoreService<WebCrawlRecord> {

	private final static String WEB_CRAWLS_DIRECTORY = "web_crawls";

	public WebCrawlsService(final StoreServiceInterface storeService) {
		super(storeService, WEB_CRAWLS_DIRECTORY, WebCrawlRecord.class,
				new GenericType<StoreWalkResult<WebCrawlRecord>>() {
				});
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

	final static String VAR_START = "start";
	final static String VAR_ROWS = "rows";
	final static String VAR_FILTER = "filter";

	public int collect(final UUID accountId, final int start, final int rows, final String filter,
			final Consumer<WebCrawlRecord> recordCollector) {
		final Map<String, Object> variables = new HashMap<>();
		variables.put(VAR_START, start);
		variables.put(VAR_ROWS, rows);
		if (!StringUtils.isBlank(filter))
			variables.put(VAR_FILTER, filter.toLowerCase());
		return super.collect(accountId.toString(), null, Filter.class, variables, r -> {
			if (!r.equals(WebCrawlRecord.EMPTY))
				recordCollector.accept(r);
		});
	}

	public void remove(final UUID accountId, UUID webCrawlUuid) {
		super.remove(accountId.toString(), null, webCrawlUuid.toString());
	}

	public static class Filter extends StoreScript<WebCrawlRecord> {

		@Override
		protected WebCrawlRecord run(final File file, final String path, final Integer dirCount,
				final Integer fileCount, final Integer resultSize, final Map<String, ?> variables) throws Exception {
			if (!file.isFile())
				return null;
			final Integer start = (Integer) variables.get(VAR_START);
			final Integer rows = (Integer) variables.get(VAR_ROWS);
			final String filter = (String) variables.get(VAR_FILTER);
			final WebCrawlRecord record = readRecord(file, WebCrawlRecord.class);
			if (filter != null) {
				final boolean found = (record.name != null && record.name.toLowerCase().contains(filter)) ||
						(record.crawlDefinition != null && record.crawlDefinition.entryUrl != null &&
								record.crawlDefinition.entryUrl.contains(filter));
				if (!found)
					return null;
			}
			return resultSize < start || resultSize >= start + rows ? WebCrawlRecord.EMPTY : record;
		}
	}
}
