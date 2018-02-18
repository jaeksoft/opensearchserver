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

import com.qwazr.search.index.IndexServiceInterface;
import com.qwazr.search.index.SchemaSettingsDefinition;

import java.net.URISyntaxException;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class IndexesService {

	private final IndexServiceInterface indexService;
	private final ConcurrentHashMap<String, IndexService> indexes;
	private final String schemaName;

	public IndexesService(final IndexServiceInterface indexService, final String schemaName,
			final SchemaSettingsDefinition schemaSettings) {
		this.schemaName = schemaName;
		this.indexService = indexService;
		indexes = new ConcurrentHashMap<>();
		indexService.createUpdateSchema(schemaName, schemaSettings);
	}

	public Set<String> getIndexes() {
		final Map<String, UUID> indexMap = indexService.getIndexes(schemaName);
		return indexMap == null ? null : indexMap.keySet();
	}

	public void createIndex(final String indexName) {
		indexService.createUpdateIndex(schemaName, indexName);
	}

	public void deleteIndex(final String indexName) {
		indexService.deleteIndex(schemaName, indexName);
		indexes.remove(indexName);
	}

	public IndexService getIndex(final String indexName) {
		return indexes.computeIfAbsent(indexName, in -> {
			try {
				return new IndexService(indexService, schemaName, indexName);
			} catch (URISyntaxException e) {
				throw new RuntimeException(e);
			}
		});
	}

	public String getIndexName(final UUID indexUuid) {
		final Map<String, UUID> indexMap = indexService.getIndexes(schemaName);
		if (indexMap == null)
			return null;
		for (Map.Entry<String, UUID> entry : indexMap.entrySet())
			if (indexUuid.equals(entry.getValue()))
				return entry.getKey();
		return null;
	}

}
