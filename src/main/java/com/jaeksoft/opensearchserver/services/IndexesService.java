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

import com.jaeksoft.opensearchserver.model.AccountRecord;
import com.qwazr.search.index.IndexServiceInterface;
import com.qwazr.server.client.ErrorWrapper;
import org.apache.commons.lang3.tuple.Pair;

import javax.ws.rs.NotAcceptableException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
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
        final Map<String, UUID> indexMap = ErrorWrapper.bypass(() -> indexService.getIndexes(accountId), 404);
        return indexMap == null ? null : indexMap.keySet();
    }

    public void createIndex(final AccountRecord account, final String indexName) {
        indexService.createUpdateSchema(account.id);
        final int indexNumberLimit = account.getIndexNumberLimit();
        if (indexNumberLimit > 0 && indexService.getIndexes(account.id).size() >= indexNumberLimit)
            throw new NotAcceptableException("You maximum number of index is reached: " + indexNumberLimit);
        indexService.createUpdateIndex(account.id, indexName);
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
        final Map<String, UUID> indexMap = ErrorWrapper.bypass(() -> indexService.getIndexes(accountId), 404);
        if (indexMap == null)
            return Collections.emptyMap();
        final Map<UUID, String> indexReverseMap = new HashMap<>();
        indexMap.forEach((name, uuid) -> indexReverseMap.put(uuid, name));
        return indexReverseMap;
    }

    /**
     * Remove expired service (not used since 5 minutes)
     *
     * @return the number of evicted services
     */
    public synchronized int removeExpired() {
        final List<Pair<String, String>> expiredServices = new ArrayList<>();
        final long refTime = TimeUnit.MINUTES.toMillis(5);
        indexes.forEach((k, v) -> {
            if (v.hasExpired(refTime))
                expiredServices.add(k);
        });
        expiredServices.forEach(indexes::remove);
        return expiredServices.size();
    }

}
