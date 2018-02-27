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
import org.apache.commons.lang3.tuple.Pair;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class IndexesService {

	private final IndexServiceInterface indexService;
	private final ConcurrentHashMap<Pair<String, String>, IndexService> indexes;

	public IndexesService(final IndexServiceInterface indexService) {
		this.indexService = indexService;
		indexes = new ConcurrentHashMap<>();
	}

	public Set<String> getIndexes(final String accountId) {
		indexService.createUpdateSchema(accountId);
		final Map<String, UUID> indexMap = indexService.getIndexes(accountId);
		return indexMap == null ? null : indexMap.keySet();
	}

	public void createIndex(final String accountId, final String indexName) {
		indexService.createUpdateSchema(accountId);
		indexService.createUpdateIndex(accountId, indexName);
	}

	public void deleteIndex(final String accountId, final String indexName) {
		indexService.deleteIndex(accountId, indexName);
		indexes.remove(Pair.of(accountId, indexName));
	}

	public IndexService getIndex(final String accountId, final String indexName) {
		return indexes.computeIfAbsent(Pair.of(accountId, indexName), in -> {
			try {
				return new IndexService(indexService, accountId, indexName);
			} catch (URISyntaxException e) {
				throw new RuntimeException(e);
			}
		});
	}

	public Map<UUID, String> getIndexNameResolver(final String accountId) {
		final Map<UUID, String> indexMap = new HashMap<>();
		indexService.getIndexes(accountId).forEach((name, uuid) -> indexMap.put(uuid, name));
		return indexMap;
	}

	/**
	 * Remove expired service (not used since 5 minutes)
	 */
	public void removeExpired() {
		final List<Pair<String, String>> expiredServices = new ArrayList<>();
		final long refTime = TimeUnit.MINUTES.toMillis(5);
		indexes.forEach((k, v) -> {
			if (v.hasExpired(refTime))
				expiredServices.add(k);
		});
		expiredServices.forEach(indexes::remove);
	}

}
