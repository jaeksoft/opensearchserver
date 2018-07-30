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

import com.jaeksoft.opensearchserver.BaseTest;
import com.jaeksoft.opensearchserver.model.WebCrawlRecord;
import com.qwazr.crawler.web.WebCrawlDefinition;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class WebCrawlsServiceTest extends BaseTest {

    private WebCrawlsService webCrawlsService;

    @Before
    public void setup() throws IOException {
        webCrawlsService = getWebCrawlsService();
    }

    private void checkWebCrawlRecordsResult(int totalCount, List<WebCrawlRecord> records, int expectedTotalCount,
        WebCrawlRecord... expectedRecords) {
        Assert.assertEquals(expectedTotalCount, totalCount);
        Assert.assertEquals(expectedRecords.length, records.size());
        for (WebCrawlRecord record : expectedRecords)
            Assert.assertTrue(records.contains(record));
    }

    @Test
    public void getEmptyList() {
        List<WebCrawlRecord> crawlRecords = new ArrayList<>();
        checkWebCrawlRecordsResult(webCrawlsService.collect(getAccount().getId(), 0, 10, null, crawlRecords::add),
            crawlRecords, 0);
    }

    public static WebCrawlRecord createNewCrawlRecord(String name) {
        final WebCrawlDefinition crawlDefinition =
            WebCrawlDefinition.of().setEntryUrl("http://www.opensearchserver.com").build();
        return WebCrawlRecord.of().name(name).crawlDefinition(crawlDefinition).build();
    }

    @Test
    public void saveOneRecord() {
        final WebCrawlRecord crawlRecord1 = createNewCrawlRecord("test1");
        webCrawlsService.save(getAccount().getId(), crawlRecord1);
        final List<WebCrawlRecord> crawlRecords = new ArrayList<>();
        checkWebCrawlRecordsResult(webCrawlsService.collect(getAccount().getId(), 0, 10, null, crawlRecords::add),
            crawlRecords, 1, crawlRecord1);
    }

    List<WebCrawlRecord> createAndSaveTwoRecords() {
        final WebCrawlRecord crawlRecord1 = createNewCrawlRecord("test1");
        webCrawlsService.save(getAccount().getId(), crawlRecord1);
        final WebCrawlRecord crawlRecord2 = createNewCrawlRecord("test2");
        webCrawlsService.save(getAccount().getId(), crawlRecord2);
        final List<WebCrawlRecord> records = new ArrayList<>();
        checkWebCrawlRecordsResult(webCrawlsService.collect(getAccount().getId(), 0, 10, null, records::add), records,
            2, crawlRecord1, crawlRecord2);
        return records;
    }

    @Test
    public void saveTwoRecords() {
        createAndSaveTwoRecords();
    }

    @Test
    public void updateOneRecord() {
        final List<WebCrawlRecord> records1 = createAndSaveTwoRecords();

        webCrawlsService.save(getAccount().getId(), records1.get(0));
        final List<WebCrawlRecord> records2 = new ArrayList<>();
        checkWebCrawlRecordsResult(webCrawlsService.collect(getAccount().getId(), 0, 10, null, records2::add), records2,
            2, records1.get(0), records1.get(1));

        Assert.assertArrayEquals(records1.toArray(), records2.toArray());
    }

    @Test
    public void removeOneRecord() {
        List<WebCrawlRecord> records1 = createAndSaveTwoRecords();
        final List<WebCrawlRecord> records2 = new ArrayList<>();
        webCrawlsService.remove(getAccount().getId(), records1.get(0).getUuid());
        checkWebCrawlRecordsResult(webCrawlsService.collect(getAccount().getId(), 0, 10, null, records2::add), records2,
            1, records1.get(1));
    }

    @Test
    public void pagingStart1Rows10() {
        final List<WebCrawlRecord> records1 = createAndSaveTwoRecords();
        final List<WebCrawlRecord> records2 = new ArrayList<>();
        checkWebCrawlRecordsResult(webCrawlsService.collect(getAccount().getId(), 1, 10, null, records2::add), records2,
            2, records1.get(1));
    }

    @Test
    public void pagingStart0Rows1() {
        final List<WebCrawlRecord> records1 = createAndSaveTwoRecords();
        final List<WebCrawlRecord> records2 = new ArrayList<>();
        checkWebCrawlRecordsResult(webCrawlsService.collect(getAccount().getId(), 0, 1, null, records2::add), records2,
            2, records1.get(0));
    }

    @Test
    public void pagingStartFilters() {

        final WebCrawlRecord record1 = createNewCrawlRecord("test1");
        webCrawlsService.save(getAccount().getId(), record1);
        final WebCrawlRecord record2 = createNewCrawlRecord("test2");
        webCrawlsService.save(getAccount().getId(), record2);

        final List<WebCrawlRecord> records = new ArrayList<>();
        checkWebCrawlRecordsResult(webCrawlsService.collect(getAccount().getId(), 0, 10, "test1", records::add),
            records, 1, record1);

        records.clear();
        checkWebCrawlRecordsResult(webCrawlsService.collect(getAccount().getId(), 0, 10, "test2", records::add),
            records, 1, record2);

        records.clear();
        checkWebCrawlRecordsResult(webCrawlsService.collect(getAccount().getId(), 0, 10, "open", records::add), records,
            2, record1, record2);

        records.clear();
        checkWebCrawlRecordsResult(
            webCrawlsService.collect(getAccount().getId(), 0, 10, "fsmlfsmdflksmdfl", records::add), records, 0);

    }

}
