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

package com.jaeksoft.opensearchserver.services;

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

    public String getServerName() {
        return getCurrent().servername;
    }

    public boolean isProduction() {
        return getCurrent().isProduction;
    }

    public String getApplicationSalt() {
        return getCurrent().applicationSalt;
    }

    public URI getStoreServiceUri() {
        return getCurrent().storeServiceUri;
    }

    public URI getIndexServiceUri() {
        return getCurrent().indexServiceUri;
    }

    public URI getTableServiceUrl() {
        return getCurrent().tableServiceUri;
    }

    public URI getCrawlerServiceUri() {
        return getCurrent().crawlerServiceUri;
    }

    public String getJwtKey() {
        return getCurrent().jwtKey;
    }

    public URI getJwtUri() {
        return getCurrent().jwtUri;
    }

    public boolean hasJwtSignin() {
        return getJwtKey() != null && getJwtUri() != null;
    }

    public static class Config extends PropertiesConfig {

        private static final String SERVER_NAME = "serverName";
        private static final String IS_PRODUCTION = "isProduction";
        private static final String APPLICATION_SALT = "applicationSalt";
        private static final String STORE_SERVICE_URI = "storeServiceUri";
        private static final String INDEX_SERVICE_URI = "indexServiceUri";
        private static final String TABLE_SERVICE_URI = "tableServiceUri";
        private static final String CRAWLER_SERVICE_URI = "crawlerServiceUri";
        private static final String JWT_KEY = "jwtKey";
        private static final String JWT_URI = "jwtUri";

        private final String servername;
        private final boolean isProduction;
        private final String applicationSalt;
        private final URI storeServiceUri;
        private final URI indexServiceUri;
        private final URI tableServiceUri;
        private final URI crawlerServiceUri;
        private final String jwtKey;
        private final URI jwtUri;

        public Config(Properties properties, Instant creationTime) {
            super(properties, creationTime);
            servername = getStringProperty(SERVER_NAME, () -> "localhost:9090");
            isProduction = getBooleanProperty(IS_PRODUCTION, () -> Boolean.TRUE);
            applicationSalt = getStringProperty(APPLICATION_SALT, () -> "oss-salt");
            storeServiceUri = getUriProperty(STORE_SERVICE_URI, () -> null);
            indexServiceUri = getUriProperty(INDEX_SERVICE_URI, () -> null);
            tableServiceUri = getUriProperty(TABLE_SERVICE_URI, () -> null);
            crawlerServiceUri = getUriProperty(CRAWLER_SERVICE_URI, () -> null);
            jwtKey = getStringProperty(JWT_KEY, () -> null);
            jwtUri = getUriProperty(JWT_URI, () -> null);
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
                && Objects.equals(applicationSalt, o.applicationSalt)
                && Objects.equals(storeServiceUri, o.storeServiceUri)
                && Objects.equals(indexServiceUri, o.indexServiceUri)
                && Objects.equals(tableServiceUri, o.tableServiceUri)
                && Objects.equals(crawlerServiceUri, o.crawlerServiceUri)
                && Objects.equals(jwtKey, o.jwtKey)
                && Objects.equals(jwtUri, o.jwtUri);
        }

        @Override
        public int hashCode() {
            return Objects.hash(servername, isProduction, applicationSalt, storeServiceUri,
                indexServiceUri, tableServiceUri, crawlerServiceUri, jwtKey, jwtUri);
        }
    }
}
