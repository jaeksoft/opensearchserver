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

import com.jaeksoft.opensearchserver.BaseTest;
import com.jaeksoft.opensearchserver.model.AccountRecord;
import com.jaeksoft.opensearchserver.model.Language;
import com.jaeksoft.opensearchserver.model.SearchResults;
import com.jaeksoft.opensearchserver.services.IndexService;
import com.jaeksoft.opensearchserver.services.IndexesService;
import com.jaeksoft.opensearchserver.services.SearchService;
import com.qwazr.crawler.web.WebCrawlDefinition;
import com.qwazr.crawler.web.WebCrawlStatus;
import com.qwazr.crawler.web.WebCrawlerServiceInterface;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.UUID;

public class CrawlTest extends BaseTest {

    @Before
    public void setup() throws IOException {
        CrawlerComponents.setLocalComponents(getComponents());
    }

    @Test
    public void test() throws IOException, InterruptedException {

        final long sessionTimeId = System.currentTimeMillis();
        final UUID crawlUuid = UUID.randomUUID();
        final String url = "http://www.opensearchserver.com/documentation/tutorials/functionalities.md";
        final String indexName = "crawl";
        final AccountRecord accountRecord = getAccount();
        final String crawlSessionName = "test";

        final IndexesService indexesService = getIndexesService();
        indexesService.createIndex(accountRecord, indexName);
        final IndexService indexService = indexesService.getIndex(accountRecord.getId().toString(), indexName);
        final SearchService searchService = new SearchService();

        final WebCrawlDefinition.Builder webCrawl = WebCrawlDefinition.of().setEntryUrl(url).addUrl(url, 1);

        CrawlerComponents.buildCrawl(accountRecord, indexName, crawlUuid, sessionTimeId, null, webCrawl);

        final WebCrawlerServiceInterface webCrawlerService = getWebCrawlService();
        Assert.assertNotNull(webCrawlerService.runSession(crawlSessionName, webCrawl.build()));

        for (; ; ) {
            final WebCrawlStatus status = webCrawlerService.getSession(crawlSessionName);
            Assert.assertNotNull(status);
            if (status.endTime != null)
                break;
            Thread.sleep(2000);
        }

        Assert.assertTrue(indexService.isAlreadyCrawled(url, crawlUuid, sessionTimeId));
        final SearchResults results = searchService.webSearch(indexService, Language.en, "frame", 0, 1);
        Assert.assertNotNull(results);
        Assert.assertNotNull(results.getResults());
        Assert.assertEquals(1, results.getResults().size());

        final String title = results.getResults().get(0).getTitle();
        Assert.assertNotNull(title);
    }
}


