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

import com.qwazr.server.client.ErrorWrapper;
import com.qwazr.store.StoreFileResult;
import com.qwazr.store.StoreServiceInterface;
import com.qwazr.utils.ObjectMappers;
import org.apache.tools.ant.util.StringUtils;

import javax.ws.rs.WebApplicationException;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

abstract class StoreService<T> {

	private final static String JSON_GZ_SUFFIX = ".json.gz";

	private final StoreServiceInterface storeService;
	private final String storeSchema;
	private final String directory;
	private final Class<T> recordClass;
	private final RecordsResult empty;

	StoreService(final StoreServiceInterface storeService, final String storeSchema, final String directory,
			final Class<T> recordClass) {
		this.storeService = storeService;
		this.storeSchema = storeSchema;
		this.directory = directory;
		this.recordClass = recordClass;
		this.empty = new RecordsResult(0, Collections.emptyList());
	}

	private String getRecordPath(final String subDirectory, final UUID uuid) {
		if (subDirectory == null)
			return directory + '/' + uuid + JSON_GZ_SUFFIX;
		else
			return directory + '/' + subDirectory + '/' + uuid + JSON_GZ_SUFFIX;
	}

	protected abstract UUID getUuid(T record);

	/**
	 * Save the given record
	 *
	 * @param record the record to save
	 * @throws IOException if any I/O error occured
	 */
	public void save(final String subDirectory, final T record) throws IOException {
		final UUID recordUuid = getUuid(record);
		final Path tmpJsonCrawlFile = Files.createTempFile(recordUuid.toString(), JSON_GZ_SUFFIX);
		try {
			try (final OutputStream fileOutput = Files.newOutputStream(tmpJsonCrawlFile, StandardOpenOption.CREATE)) {
				try (final BufferedOutputStream bufOutput = new BufferedOutputStream(fileOutput)) {
					try (final GZIPOutputStream compressedOutput = new GZIPOutputStream(bufOutput)) {
						ObjectMappers.JSON.writeValue(compressedOutput, record);
					}
				}
			}
			storeService.createSchema(storeSchema);
			storeService.putFile(storeSchema, getRecordPath(subDirectory, recordUuid), tmpJsonCrawlFile,
					System.currentTimeMillis());
		} finally {
			Files.deleteIfExists(tmpJsonCrawlFile);
		}
	}

	/**
	 * Retrieve a record
	 *
	 * @param recordUuid the UUID of the record
	 * @return the record or null if there is any
	 * @throws IOException if any I/O error occured
	 */
	public T read(final String subDirectory, final UUID recordUuid) throws IOException {
		return ErrorWrapper.bypass(() -> {
			try (final InputStream fileInput = storeService.getFile(storeSchema,
					getRecordPath(subDirectory, recordUuid))) {
				try (final BufferedInputStream bufInput = new BufferedInputStream(fileInput)) {
					try (final GZIPInputStream compressedInput = new GZIPInputStream(bufInput)) {
						return ObjectMappers.JSON.readValue(compressedInput, recordClass);
					}
				}
			}
		}, 404);
	}

	/**
	 * Read the web crawl list with paging parameters
	 *
	 * @param start
	 * @param rows
	 * @return
	 */
	public RecordsResult get(final String subDirectory, int start, int rows) throws IOException {
		try {
			final String directoryPath = subDirectory == null ? directory : directory + '/' + subDirectory;
			final Map<String, StoreFileResult> files = storeService.getDirectory(storeSchema, directoryPath).files;
			if (files == null)
				return empty;
			final Iterator<String> iterator = files.keySet().iterator();
			while (start-- > 0 && iterator.hasNext())
				iterator.next();
			final List<T> records = new ArrayList<>();
			while (rows-- > 0 && iterator.hasNext())
				records.add(
						read(subDirectory, UUID.fromString(StringUtils.removeSuffix(iterator.next(), JSON_GZ_SUFFIX))));
			return new RecordsResult(files.size(), records);
		} catch (WebApplicationException e) {
			if (e.getResponse().getStatus() == 404)
				return empty;
			throw e;
		}
	}

	public class RecordsResult {

		public final int totalCount;
		public final List<T> records;

		private RecordsResult(final int totalCount, final List<T> crawlRecords) {
			this.totalCount = totalCount;
			this.records = crawlRecords == null ? Collections.emptyList() : Collections.unmodifiableList(crawlRecords);
		}

		public int getTotalCount() {
			return totalCount;
		}

		public List<T> getRecords() {
			return records;
		}
	}

	/**
	 * Remove the records
	 *
	 * @param recordUuid the UUID of the record
	 */
	public void remove(final String subDirectory, final UUID recordUuid) {
		storeService.deleteFile(storeSchema, getRecordPath(subDirectory, recordUuid));
	}
}
