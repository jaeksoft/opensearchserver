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

import com.qwazr.utils.ConfigService.FileConfigService;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Objects;
import java.util.Properties;

public class ConfigService extends FileConfigService<ConfigService.Config> {

    public ConfigService(final Path configPath) throws IOException {
        super(configPath, Config::new);
    }

    public boolean isProduction() {
        return getCurrent().isProduction;
    }

    public URI getIndexServiceUri() {
        return getCurrent().indexServiceUri;
    }

    public URI getFileCrawlerServiceUri() {
        return getCurrent().fileCrawlerServiceUri;
    }

    public URI getWebCrawlerServiceUri() {
        return getCurrent().webCrawlerServiceUri;
    }

    public Path getParsersDirectoryPath() {
        return getCurrent().parsersDirectoryPath;
    }

    public int getCrawlerThreadPoolSize() {
        return getCurrent().crawlerThreadPoolSize;
    }

    public static class Config extends PropertiesConfig {

        private static final String SERVER_NAME = "serverName";
        private static final String IS_PRODUCTION = "isProduction";
        private static final String INDEX_SERVICE_URI = "indexServiceUri";
        private static final String FILE_CRAWLER_SERVICE_URI = "fileCrawlerServiceUri";
        private static final String WEB_CRAWLER_SERVICE_URI = "webCrawlerServiceUri";
        private static final String PARSERS_DIRECTORY_PATH = "parsersDirectoryPath";
        private static final String CRAWLER_THREADPOOL_SIZE = "crawlerThreadPoolSize";

        private final String servername;
        private final boolean isProduction;
        private final URI indexServiceUri;
        private final URI fileCrawlerServiceUri;
        private final URI webCrawlerServiceUri;
        private final Path parsersDirectoryPath;
        private final int crawlerThreadPoolSize;

        public Config(Properties properties, Instant creationTime) {
            super(properties, creationTime);
            servername = getStringProperty(SERVER_NAME, () -> "localhost:9090");
            isProduction = getBooleanProperty(IS_PRODUCTION, () -> Boolean.TRUE);
            indexServiceUri = getUriProperty(INDEX_SERVICE_URI, () -> null);
            fileCrawlerServiceUri = getUriProperty(FILE_CRAWLER_SERVICE_URI, () -> null);
            webCrawlerServiceUri = getUriProperty(WEB_CRAWLER_SERVICE_URI, () -> null);
            parsersDirectoryPath = getPathProperty(PARSERS_DIRECTORY_PATH, () -> null);
            crawlerThreadPoolSize = getIntegerProperty(CRAWLER_THREADPOOL_SIZE, () -> Runtime.getRuntime().availableProcessors() * 2 + 1);
        }

        @Override
        public boolean equals(Object other) {
            if (!(other instanceof Config))
                return false;
            if (other == this)
                return true;
            final Config o = (Config) other;
            return Objects.equals(servername, o.servername)
                && isProduction == o.isProduction
                && Objects.equals(indexServiceUri, o.indexServiceUri)
                && Objects.equals(fileCrawlerServiceUri, o.fileCrawlerServiceUri)
                && Objects.equals(webCrawlerServiceUri, o.webCrawlerServiceUri)
                && Objects.equals(parsersDirectoryPath, o.parsersDirectoryPath)
                && crawlerThreadPoolSize == o.crawlerThreadPoolSize;
        }

        @Override
        public int hashCode() {
            return Objects.hash(servername, isProduction,
                indexServiceUri, fileCrawlerServiceUri, webCrawlerServiceUri,
                parsersDirectoryPath, crawlerThreadPoolSize);
        }
    }
}
