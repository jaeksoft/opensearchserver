/*
 * Copyright 2017-2020 Emmanuel Keller / Jaeksoft
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
package com.jaeksoft.opensearchserver;

import com.qwazr.crawler.common.CrawlCollector;
import com.qwazr.crawler.file.FileCrawlCollectorFactory;
import com.qwazr.crawler.file.FileCrawlDefinition;
import com.qwazr.crawler.file.FileCrawlItem;
import com.qwazr.crawler.ftp.FtpCrawlCollectorFactory;
import com.qwazr.crawler.ftp.FtpCrawlDefinition;
import com.qwazr.crawler.ftp.FtpCrawlItem;
import com.qwazr.crawler.web.WebCrawlCollectorFactory;
import com.qwazr.crawler.web.WebCrawlDefinition;
import com.qwazr.crawler.web.WebCrawlItem;
import javax.validation.constraints.NotNull;

public class CrawlerCollector {

    private CrawlerCollector(final WebCrawlDefinition crawlDefinition) {
        System.out.println(crawlDefinition);
    }

    private CrawlerCollector(final FileCrawlDefinition crawlDefinition) {
        System.out.println(crawlDefinition);
    }

    private CrawlerCollector(final FtpCrawlDefinition crawlDefinition) {
        System.out.println(crawlDefinition);
    }

    public void collect(final WebCrawlItem crawlItem) {
        System.out.println(crawlItem);
    }

    public void collect(final FileCrawlItem crawlItem) {
        System.out.println(crawlItem);

    }

    public void collect(final FtpCrawlItem crawlItem) {
        System.out.println(crawlItem);
    }


    public static class Web implements WebCrawlCollectorFactory {

        @Override
        public @NotNull CrawlCollector<WebCrawlItem> createCrawlCollector(final WebCrawlDefinition crawlDefinition) {
            return new CrawlerCollector(crawlDefinition)::collect;
        }
    }

    static class File implements FileCrawlCollectorFactory {

        @Override
        public @NotNull CrawlCollector<FileCrawlItem> createCrawlCollector(final FileCrawlDefinition crawlDefinition) {
            return new CrawlerCollector(crawlDefinition)::collect;
        }
    }

    static class Ftp implements FtpCrawlCollectorFactory {

        @Override
        public @NotNull CrawlCollector<FtpCrawlItem> createCrawlCollector(final FtpCrawlDefinition crawlDefinition) {
            return new CrawlerCollector(crawlDefinition)::collect;
        }
    }
}
