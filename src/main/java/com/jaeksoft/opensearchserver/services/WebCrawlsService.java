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
import com.qwazr.server.ServerException;
import com.qwazr.store.StoreServiceInterface;
import com.qwazr.utils.ObjectMappers;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class WebCrawlsService {

	private final static String WEB_CRAWLS_PREFIX = "web_crawls";
	private final static String WEB_CRAWLS_SUFFIX = ".json.gz";

	private final StoreServiceInterface storeService;
	private final String storeSchema;

	public WebCrawlsService(final StoreServiceInterface storeService, final String storeSchema) {
		this.storeService = storeService;
		this.storeSchema = storeSchema;
	}

	private String getWebCrawlPath(String indexName) {
		return WEB_CRAWLS_PREFIX + '/' + indexName + WEB_CRAWLS_SUFFIX;
	}

	/**
	 * Set the web crawls for the given index
	 *
	 * @param indexName the name of the index
	 * @param record    the web crawls or null
	 * @throws IOException if any I/O error occured
	 */
	public void set(final String indexName, WebCrawlRecord record) throws IOException {
		if (record == null || record.crawls == null || record.crawls.isEmpty()) {
			remove(indexName);
			return;
		}
		final Path tmpJsonCrawlFile = Files.createTempFile(indexName, WEB_CRAWLS_SUFFIX);
		try {
			try (final OutputStream fileOutput = Files.newOutputStream(tmpJsonCrawlFile, StandardOpenOption.CREATE)) {
				try (final BufferedOutputStream bufOutput = new BufferedOutputStream(fileOutput)) {
					try (final GZIPOutputStream compressedOutput = new GZIPOutputStream(bufOutput)) {
						ObjectMappers.JSON.writeValue(compressedOutput, record);
					}
				}
			}
			storeService.putFile(storeSchema, getWebCrawlPath(indexName), tmpJsonCrawlFile, System.currentTimeMillis());
		} finally {
			Files.deleteIfExists(tmpJsonCrawlFile);
		}
	}

	/**
	 * Retrieve the web crawls for a given index
	 *
	 * @param indexName the name of the index
	 * @return the WebCrawlRecord or null if there is any
	 * @throws IOException if any I/O error occured
	 */
	public WebCrawlRecord get(final String indexName) throws IOException {
		try (final InputStream fileInput = storeService.getFile(storeSchema, getWebCrawlPath(indexName))) {
			try (final BufferedInputStream bufInput = new BufferedInputStream(fileInput)) {
				try (final GZIPInputStream compressedInput = new GZIPInputStream(bufInput)) {
					return ObjectMappers.JSON.readValue(compressedInput, WebCrawlRecord.TYPE_REFERENCE);
				}
			}
		} catch (ServerException e) {
			if (e.getStatusCode() == 404)
				return null;
			throw e;
		}

	}

	/**
	 * Remove the web crawl entries for the given index
	 *
	 * @param indexName the name of the index
	 */
	public void remove(final String indexName) {
		storeService.deleteFile(storeSchema, getWebCrawlPath(indexName));
	}
}
