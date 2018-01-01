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
import com.qwazr.utils.ObjectMappers;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class WebCrawlsService {

	private final static String TEMP_EXTENSION = ".temp";
	private final static String OLD_EXTENSION = ".old.gz";
	private final static String JSON_GZ_EXTENSION = ".json.gz";

	private final Path webCrawlerDirectory;

	public WebCrawlsService(final Path webCrawlerDirectory) {
		this.webCrawlerDirectory = webCrawlerDirectory;
	}

	public SortedSet<String> getList() throws IOException {
		return Files.list(webCrawlerDirectory)
				.filter(p -> Files.isRegularFile(p))
				.filter(p -> p.endsWith(JSON_GZ_EXTENSION))
				.map(p -> p.getFileName().toString())
				.collect(Collectors.toCollection(TreeSet::new));
	}

	public void set(final String indexName, WebCrawlRecord record) throws IOException {
		final Path tmpJsonCrawlFile = webCrawlerDirectory.resolve(indexName + TEMP_EXTENSION);
		try (final OutputStream fileOutput = Files.newOutputStream(tmpJsonCrawlFile, StandardOpenOption.CREATE)) {
			try (final BufferedOutputStream bufOutput = new BufferedOutputStream(fileOutput)) {
				try (final GZIPOutputStream compressedOutput = new GZIPOutputStream(bufOutput)) {
					ObjectMappers.JSON.writeValue(compressedOutput, record);
				}
			}
		}
		final Path gzJsonCrawlFile = webCrawlerDirectory.resolve(indexName + JSON_GZ_EXTENSION);
		final Path oldGzJsonCrawlFile = webCrawlerDirectory.resolve(indexName + OLD_EXTENSION);
		if (Files.exists(gzJsonCrawlFile))
			Files.move(gzJsonCrawlFile, oldGzJsonCrawlFile, StandardCopyOption.ATOMIC_MOVE,
					StandardCopyOption.REPLACE_EXISTING);
		Files.move(tmpJsonCrawlFile, gzJsonCrawlFile, StandardCopyOption.ATOMIC_MOVE);
	}

	public WebCrawlRecord get(final String indexName) throws IOException {
		final Path gzJsonCrawlFile = webCrawlerDirectory.resolve(indexName + JSON_GZ_EXTENSION);
		if (!Files.exists(gzJsonCrawlFile))
			return null;
		try (final InputStream fileInput = Files.newInputStream(gzJsonCrawlFile)) {
			try (final BufferedInputStream bufInput = new BufferedInputStream(fileInput)) {
				try (final GZIPInputStream compressedInput = new GZIPInputStream(bufInput)) {
					return ObjectMappers.JSON.readValue(compressedInput, WebCrawlRecord.TYPE_REFERENCE);
				}
			}
		}
	}

	public void remove(final String indexName) throws IOException {
		Files.deleteIfExists(webCrawlerDirectory.resolve(indexName + JSON_GZ_EXTENSION));
	}
}
