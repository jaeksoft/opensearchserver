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
import com.jaeksoft.opensearchserver.crawler.web.WebBeforeSession;
import com.jaeksoft.opensearchserver.model.AccountRecord;
import com.jaeksoft.opensearchserver.services.ConfigService;
import com.jaeksoft.opensearchserver.services.IndexService;
import com.qwazr.crawler.common.EventEnum;
import com.qwazr.crawler.common.ScriptDefinition;
import com.qwazr.crawler.web.WebCrawlDefinition;
import com.qwazr.extractor.ExtractorManager;
import com.qwazr.extractor.ExtractorServiceInterface;
import com.qwazr.search.index.IndexSingleClient;
import com.qwazr.server.RemoteService;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class CrawlerComponents implements CrawlerContext {

    private static volatile Map<URI, Map<String, IndexService>> localIndexServices;

    private static volatile Components localComponents;

    public static synchronized void setLocalComponents(final Components localComponents) {
        CrawlerComponents.localComponents = localComponents;
    }

    static synchronized IndexService getIndexService(final URI indexServiceUri, final String accountId,
        final String indexName) {
        if (indexServiceUri == null)
            return Objects.requireNonNull(localComponents, "No local components available")
                .getIndexesService()
                .getIndex(accountId, indexName);
        if (localIndexServices == null)
            localIndexServices = new HashMap<>();
        final Map<String, IndexService> indexServices =
            localIndexServices.computeIfAbsent(indexServiceUri, r -> new HashMap<>());
        return indexServices.computeIfAbsent(accountId, s -> {
            try {
                final RemoteService remote = RemoteService.of(indexServiceUri).build();
                final IndexSingleClient client = new IndexSingleClient(remote);
                return new IndexService(client, accountId, indexName);
            } catch (URISyntaxException e) {
                throw new RuntimeException(
                    "Error while creating the IndexService for " + indexServiceUri + " / " + accountId + " / " +
                        indexName, e);
            }
        });
    }

    private static volatile ExtractorManager extractorManager;
    private static volatile ExtractorServiceInterface extractorService;

    private static ExtractorServiceInterface getExtractorService() {
        if (extractorService != null)
            return extractorService;
        synchronized (CrawlerComponents.class) {
            if (extractorService == null) {
                if (extractorManager == null) {
                    extractorManager = new ExtractorManager();
                    extractorManager.registerServices();
                }
                extractorService = extractorManager.getService();
            }
            return extractorService;
        }
    }

    private static volatile ExtractorIndexer extractorIndexer;

    public static ExtractorIndexer getExtractorIndexer() {
        if (extractorIndexer != null)
            return extractorIndexer;
        synchronized (CrawlerComponents.class) {
            if (extractorIndexer == null) {
                try {
                    extractorIndexer = new ExtractorIndexer(getExtractorService());
                } catch (URISyntaxException e) {
                    throw new RuntimeException("Error while creating the ExtractorIndexer", e);
                }
            }
            return extractorIndexer;
        }
    }

    public static WebCrawlDefinition buildCrawl(final AccountRecord accountRecord, final String indexName,
        final UUID crawlUuid, final Long sessionTimeId, final ConfigService configService,
        final WebCrawlDefinition.Builder crawlBuilder) {

        // Set the event
        crawlBuilder.script(EventEnum.before_session, ScriptDefinition.of(WebBeforeSession.class).build()).
            script(EventEnum.after_session, ScriptDefinition.of(WebAfterSession.class).build()).
            script(EventEnum.after_crawl, ScriptDefinition.of(WebAfterCrawl.class).build());

        crawlBuilder.variable(ACCOUNT_ID, accountRecord.id)
            .variable(MAX_RECORDS_NUMBER, Long.toString(accountRecord.getRecordNumberLimit()))
            .variable(INDEX_NAME, indexName)
            .variable(CRAWL_UUID, crawlUuid.toString())
            .variable(SESSION_TIME_ID, sessionTimeId.toString());

        if (configService != null) {
            if (configService.getIndexServiceUri() != null)
                crawlBuilder.variable(INDEX_SERVICE_URL, configService.getIndexServiceUri().toString());

            if (configService.getStoreServiceUri() != null)
                crawlBuilder.variable(STORE_SERVICE_URL, configService.getStoreServiceUri().toString());
        }

        return crawlBuilder.build();
    }
}
