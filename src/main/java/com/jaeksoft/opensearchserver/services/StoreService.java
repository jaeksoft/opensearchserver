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
import com.qwazr.store.StoreScript;
import com.qwazr.store.StoreServiceInterface;
import com.qwazr.store.StoreWalkResult;
import com.qwazr.utils.ObjectMappers;
import com.qwazr.utils.concurrent.ThreadUtils;

import javax.ws.rs.InternalServerErrorException;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public abstract class StoreService<T> {

	private final static String JSON_GZ_SUFFIX = ".json.gz";

	private final StoreServiceInterface storeService;
	private final String directory;
	private final Class<T> recordClass;

	StoreService(final StoreServiceInterface storeService, final String directory, final Class<T> recordClass) {
		this.storeService = storeService;
		this.directory = directory;
		this.recordClass = recordClass;
	}

	private String getRecordPath(final String subDirectory, final String storeName) {
		final String fileName = storeName + JSON_GZ_SUFFIX;
		if (subDirectory == null)
			return directory + '/' + fileName;
		else
			return directory + '/' + subDirectory + '/' + fileName;
	}

	protected abstract String getStoreName(T record);

	/**
	 * Save the given record
	 *
	 * @param record the record to save
	 * @throws IOException if any I/O error occured
	 */
	protected void save(final String storeSchema, final String subDirectory, final T record) {
		final String storeName = getStoreName(record);
		try {
			final Path tmpJsonCrawlFile = Files.createTempFile(storeName, JSON_GZ_SUFFIX);
			try (final OutputStream fileOutput = Files.newOutputStream(tmpJsonCrawlFile, StandardOpenOption.CREATE)) {
				try (final BufferedOutputStream bufOutput = new BufferedOutputStream(fileOutput)) {
					try (final GZIPOutputStream compressedOutput = new GZIPOutputStream(bufOutput)) {
						ObjectMappers.JSON.writeValue(compressedOutput, record);
					}
				}
			}
			storeService.createSchema(storeSchema);
			storeService.putFile(storeSchema, getRecordPath(subDirectory, getStoreName(record)), tmpJsonCrawlFile,
					System.currentTimeMillis());
		} catch (IOException e) {
			throw new InternalServerErrorException(e);
		}
	}

	/**
	 * Retrieve a record
	 *
	 * @param storeName the base name of the record
	 * @return the record or null if there is any
	 * @throws IOException if any I/O error occured
	 */
	protected T read(final String storeSchema, final String subDirectory, final String storeName) {
		return ErrorWrapper.bypass(() -> {
			try (final InputStream fileInput = storeService.getFile(storeSchema,
					getRecordPath(subDirectory, storeName))) {
				return readRecord(fileInput, recordClass);
			} catch (IOException e) {
				throw new InternalServerErrorException(e);
			}
		}, 404);
	}

/*
	protected int collect(final String storeSchema, final String subDirectory, Integer start, Integer rows,
			final Consumer<T> collector) {
		try {

			final String directoryPath = subDirectory == null ? directory : directory + '/' + subDirectory;
			final Map<String, StoreFileResult> files = storeService.getDirectory(storeSchema, directoryPath).files;
			if (files == null)
				return 0;
			final Iterator<String> iterator = files.keySet().iterator();
			int count = 0;
			if (start != null) {
				while (start-- > 0 && iterator.hasNext()) {
					final String baseName = StringUtils.removeEnd(iterator.next(), JSON_GZ_SUFFIX);
					if (fileNameFilter != null && !fileNameFilter.apply(baseName))
						continue;
					count++;
				}
			}
			while ((rows == null || rows-- > 0) && iterator.hasNext()) {
				final String baseName = StringUtils.removeEnd(iterator.next(), JSON_GZ_SUFFIX);
				if (fileNameFilter != null && !fileNameFilter.apply(baseName))
					continue;
				count++;
				if (collector != null)
					collector.accept(read(storeSchema, subDirectory, baseName));
			}
			while (iterator.hasNext()) {
				final String baseName = StringUtils.removeEnd(iterator.next(), JSON_GZ_SUFFIX);
				if (fileNameFilter == null || fileNameFilter.apply(baseName))
					count++;
			}
			return count;
		} catch (WebApplicationException e) {
			if (e.getResponse().getStatus() == 404)
				return 0;
			throw e;
		}
	}
	*/

	/**
	 * Read the record list with paging parameters
	 *
	 * @param collector the record collector
	 * @return the total number of records found, and the paginated records as a list
	 */
	protected int collect(final String storeSchema, final String subDirectory,
			final Class<? extends StoreScript> script, final Map<String, Object> variables,
			final Consumer<T> collector) {
		final String directoryPath = subDirectory == null ? directory : directory + '/' + subDirectory;
		final UUID processId = ErrorWrapper.bypass(
				() -> storeService.createProcess(storeSchema, directoryPath, 1, script.getName(), variables), 404);
		if (processId == null)
			return 0;
		StoreWalkResult result;
		for (; ; ) {
			result = storeService.getProcess(storeSchema, processId);
			if (result == null)
				return 0;
			if (result.finalTime != null)
				break;
			ThreadUtils.sleep(100, TimeUnit.MICROSECONDS);
		}
		if (result.resultObjects != null)
			for (final Object o : result.resultObjects)
				collector.accept(recordClass.cast(o));
		return result.resultCount == null ? 0 : result.resultCount.intValue();
	}

	/**
	 * Remove the records
	 *
	 * @param storeName the ID of the record
	 */
	protected void remove(final String storeSchema, final String subDirectory, final String storeName) {
		storeService.deleteFile(storeSchema, getRecordPath(subDirectory, storeName));
	}

	protected static <T> T readRecord(final InputStream inputStream, final Class<T> recordClass) throws IOException {
		try (final BufferedInputStream bufInput = new BufferedInputStream(inputStream)) {
			try (final GZIPInputStream compressedInput = new GZIPInputStream(bufInput)) {
				return ObjectMappers.JSON.readValue(compressedInput, recordClass);
			}
		}
	}

	protected static <T> T readRecord(final File file, final Class<T> recordClass) throws IOException {
		try (final FileInputStream fileInput = new FileInputStream(file)) {
			return readRecord(fileInput, recordClass);
		}
	}
}
