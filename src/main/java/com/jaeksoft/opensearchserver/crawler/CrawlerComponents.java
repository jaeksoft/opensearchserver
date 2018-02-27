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

package com.jaeksoft.opensearchserver.crawler;

import com.jaeksoft.opensearchserver.Components;
import com.jaeksoft.opensearchserver.crawler.web.WebAfterCrawl;
import com.jaeksoft.opensearchserver.crawler.web.WebAfterSession;
import com.jaeksoft.opensearchserver.crawler.web.WebBeforeCrawl;
import com.jaeksoft.opensearchserver.crawler.web.WebBeforeSession;
import com.jaeksoft.opensearchserver.services.IndexService;
import com.jaeksoft.opensearchserver.services.WebCrawlsService;
import com.qwazr.crawler.common.EventEnum;
import com.qwazr.crawler.common.ScriptDefinition;
import com.qwazr.crawler.web.WebCrawlDefinition;
import com.qwazr.extractor.ExtractorManager;
import com.qwazr.extractor.ExtractorServiceInterface;
import com.qwazr.search.index.IndexSingleClient;
import com.qwazr.server.RemoteService;
import com.qwazr.store.StoreSingleClient;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class CrawlerComponents implements CrawlerContext {

	private static volatile Map<URL, Map<String, IndexService>> localIndexServices;
	private static volatile Map<URL, Map<String, WebCrawlsService>> localWebCrawlsServices;

	static volatile Components localComponents;

	public static synchronized void setLocalComponents(final Components localComponents) {
		CrawlerComponents.localComponents = localComponents;
	}

	public static synchronized IndexService getIndexService(final URL indexServiceUrl, final String accountId,
			final String indexName) throws IOException, URISyntaxException {
		if (indexServiceUrl == null)
			return Objects.requireNonNull(localComponents, "No local components available")
					.getIndexesService()
					.getIndex(accountId, indexName);
		if (localIndexServices == null)
			localIndexServices = new HashMap<>();
		final Map<String, IndexService> indexServices =
				localIndexServices.computeIfAbsent(indexServiceUrl, r -> new HashMap<>());
		return indexServices.computeIfAbsent(accountId, s -> {
			try {
				final RemoteService remote = RemoteService.of(indexServiceUrl.toURI()).build();
				final IndexSingleClient client = new IndexSingleClient(remote);
				return new IndexService(client, accountId, indexName);
			} catch (URISyntaxException e) {
				throw new RuntimeException(
						"Error while creating the IndexService for " + indexServiceUrl + " / " + accountId + " / " +
								indexName, e);
			}
		});
	}

	public static synchronized WebCrawlsService getWebCrawlsService(final URL storeServiceUrl, final String accountId)
			throws IOException, URISyntaxException {
		if (storeServiceUrl == null)
			return Objects.requireNonNull(localComponents, "No local components available").getWebCrawlsService();
		if (localWebCrawlsServices == null)
			localWebCrawlsServices = new HashMap<>();
		final Map<String, WebCrawlsService> webCrawlsServices =
				localWebCrawlsServices.computeIfAbsent(storeServiceUrl, r -> new HashMap<>());
		return webCrawlsServices.computeIfAbsent(accountId, s -> {
			try {
				final RemoteService remote = RemoteService.of(storeServiceUrl.toURI()).build();
				final StoreSingleClient client = new StoreSingleClient(remote);
				return new WebCrawlsService(client);
			} catch (URISyntaxException e) {
				throw new RuntimeException("Error while creating the WebCrawlsService for " + storeServiceUrl, e);
			}
		});
	}

	static volatile ExtractorManager extractorManager;
	static volatile ExtractorServiceInterface extractorService;

	public static ExtractorServiceInterface getExtractorService() throws IOException, ClassNotFoundException {
		if (extractorService != null)
			return extractorService;
		synchronized (CrawlerComponents.class) {
			if (extractorManager == null) {
				extractorManager = new ExtractorManager();
				extractorManager.registerServices();
			}
			extractorService = extractorManager.getService();
			return extractorService;
		}
	}

	public static WebCrawlDefinition buildCrawl(final String accountId, final String indexName, final UUID crawlUuid,
			final Long taskCreationTime, final URL indexServiceUrl, final URL storeServiceUrl,
			final WebCrawlDefinition.Builder crawlBuilder) {

		// Set the event
		crawlBuilder.script(EventEnum.before_session, ScriptDefinition.of(WebBeforeSession.class).build()).
				script(EventEnum.after_session, ScriptDefinition.of(WebAfterSession.class).build()).
				script(EventEnum.before_crawl, ScriptDefinition.of(WebBeforeCrawl.class).build()).
				script(EventEnum.after_crawl, ScriptDefinition.of(WebAfterCrawl.class).build());

		crawlBuilder.variable(ACCOUNT_ID, accountId)
				.variable(INDEX_NAME, indexName)
				.variable(CRAWL_UUID, crawlUuid.toString())
				.variable(TASK_CREATION_TIME, taskCreationTime.toString());

		if (indexServiceUrl != null)
			crawlBuilder.variable(INDEX_SERVICE_URL, indexServiceUrl.toString());

		if (storeServiceUrl != null)
			crawlBuilder.variable(STORE_SERVICE_URL, storeServiceUrl.toString());

		return crawlBuilder.build();
	}
}
