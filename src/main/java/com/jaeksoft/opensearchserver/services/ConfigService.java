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

import com.google.common.io.Files;
import com.qwazr.utils.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Properties;

public class ConfigService {

	private final static String SERVER_NAME = "serverName";
	private final static String APPLICATION_SALT = "applicationSalt";
	private final static String STORE_SERVICE_URI = "storeServiceUri";
	private final static String INDEX_SERVICE_URI = "indexServiceUri";
	private final static String TABLE_SERVICE_URI = "tableServiceUri";
	private final static String CRAWLER_SERVICE_URI = "crawlerServiceUri";

	final Properties properties;
	final String serverName;
	final String applicationSalt;
	final URI storeServiceUri;
	final URI indexServiceUri;
	final URI tableServiceUri;
	final URI crawlerServiceUri;

	public ConfigService(final Path configPropertiesPath) throws IOException, URISyntaxException {
		properties = new Properties();
		try (final BufferedReader reader = Files.newReader(configPropertiesPath.toFile(), StandardCharsets.UTF_8)) {
			properties.load(reader);
		}
		serverName = getRequiredProperty(SERVER_NAME);
		applicationSalt = getRequiredProperty(APPLICATION_SALT);
		storeServiceUri = getUriProperty(STORE_SERVICE_URI);
		indexServiceUri = getUriProperty(INDEX_SERVICE_URI);
		tableServiceUri = getUriProperty(TABLE_SERVICE_URI);
		crawlerServiceUri = getUriProperty(CRAWLER_SERVICE_URI);
	}

	private String getRequiredProperty(String key) {
		return Objects.requireNonNull(properties.getProperty(key), () -> "Missing config property: " + key);
	}

	private URI getUriProperty(String key) throws URISyntaxException {
		final String value = properties.getProperty(key);
		return StringUtils.isBlank(value) ? null : new URI(value);
	}

	public String getServerName() {
		return serverName;
	}

	public String getApplicationSalt() {
		return applicationSalt;
	}

	public URI getStoreServiceUri() {
		return storeServiceUri;
	}

	public URI getIndexServiceUri() {
		return indexServiceUri;
	}

	public URI getTableServiceUrl() {
		return tableServiceUri;
	}

	public URI getCrawlerServiceUri() {
		return crawlerServiceUri;
	}

}
