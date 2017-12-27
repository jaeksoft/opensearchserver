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

import com.jaeksoft.opensearchserver.model.UrlRecord;
import com.qwazr.search.annotations.AnnotatedIndexService;
import com.qwazr.search.index.IndexServiceInterface;
import com.qwazr.search.index.SchemaSettingsDefinition;

import java.net.URISyntaxException;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

public class IndexesService {

	private final IndexServiceInterface indexService;
	private final ConcurrentHashMap<String, AnnotatedIndexService<UrlRecord>> indexes;
	private final String schemaName;

	public IndexesService(final IndexServiceInterface indexService, final String schemaName,
			final SchemaSettingsDefinition schemaSettings) {
		this.schemaName = schemaName;
		this.indexService = indexService;
		indexes = new ConcurrentHashMap<>();
		indexService.createUpdateSchema(schemaName, schemaSettings);
	}

	public Set<String> getIndexes() {
		final Set<String> indexes = indexService.getIndexes(schemaName);
		return indexes == null ? indexes : new TreeSet<>(indexes);
	}

	public void createIndex(final String indexName) {
		indexService.createUpdateIndex(schemaName, indexName);
	}

	public void deleteIndex(final String indexName) {
		indexService.deleteIndex(schemaName, indexName);
	}

	public AnnotatedIndexService<UrlRecord> getIndex(String indexName) {
		return indexes.computeIfAbsent(indexName, in -> {
			try {
				AnnotatedIndexService<UrlRecord> index =
						new AnnotatedIndexService<>(indexService, UrlRecord.class, schemaName, indexName, null);
				index.createUpdateFields();
				return index;
			} catch (URISyntaxException e) {
				throw new RuntimeException(e);
			}
		});
	}
}
