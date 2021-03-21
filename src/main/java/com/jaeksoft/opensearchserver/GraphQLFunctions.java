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

package com.jaeksoft.opensearchserver;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.qwazr.search.index.IndexServiceInterface;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

class GraphQLFunctions implements GraphQLService.DataFetcherProvider {

    private final IndexServiceInterface indexService;

    GraphQLFunctions(final IndexServiceInterface indexService) {
        this.indexService = indexService;
    }

    @Override
    public Map<String, Map<String, DataFetcher<?>>> getDataFetchers() {
        return Map.of("Query",
            Map.of("indexList", this::indexList),
            "Mutation",
            Map.of("createIndex", this::createIndex,
                "deleteIndex", this::deleteIndex));
    }

    private List<Index> indexList(final DataFetchingEnvironment environment) {
        final String keywords = environment.getArgument("keywords");
        final Integer start_arg = environment.getArgument("start");
        final Integer rows_arg = environment.getArgument("rows");
        final List<Index> result = new ArrayList<>();
        int start = start_arg == null ? 0 : start_arg;
        int rows = rows_arg == null ? 20 : rows_arg;
        final Map<String, UUID> indices = indexService.getIndexes();
        final Iterator<String> iterator = indices.keySet().iterator();
        while (start > 0 && iterator.hasNext()) {
            iterator.next();
            start--;
        }
        while (rows > 0 && iterator.hasNext()) {
            final String name = iterator.next();
            if (keywords == null || name.contains(keywords)) {
                result.add(new Index(name, indices.get(name)));
                rows--;
            }
        }
        return result;
    }

    private Boolean createIndex(final DataFetchingEnvironment environment) {
        final String name = environment.getArgument("name");
        if (name == null || name.isEmpty() || name.isBlank())
            return false;
        indexService.createUpdateIndex(name.trim());
        return true;
    }

    private Boolean deleteIndex(final DataFetchingEnvironment environment) {
        final String name = environment.getArgument("name");
        if (name == null || name.isEmpty() || name.isBlank())
            return false;
        return indexService.deleteIndex(name.trim());
    }

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JsonAutoDetect(
        setterVisibility = JsonAutoDetect.Visibility.NONE,
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        isGetterVisibility = JsonAutoDetect.Visibility.NONE,
        creatorVisibility = JsonAutoDetect.Visibility.NONE,
        fieldVisibility = JsonAutoDetect.Visibility.PUBLIC_ONLY)
    public static class Index {

        @JsonProperty
        public final String name;
        @JsonProperty
        public final String id;

        Index(final String name, final UUID uuid) {
            this.name = name;
            this.id = uuid.toString();
        }

    }
}
