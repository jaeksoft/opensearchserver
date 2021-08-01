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
import static graphql.Scalars.GraphQLBoolean;
import static graphql.Scalars.GraphQLString;
import graphql.schema.DataFetchingEnvironment;
import static graphql.schema.FieldCoordinates.coordinates;
import static graphql.schema.GraphQLArgument.newArgument;
import graphql.schema.GraphQLCodeRegistry;
import static graphql.schema.GraphQLCodeRegistry.newCodeRegistry;
import graphql.schema.GraphQLFieldDefinition;
import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLNonNull;
import graphql.schema.GraphQLObjectType;
import static graphql.schema.GraphQLObjectType.newObject;
import graphql.schema.GraphQLSchema;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

class GraphQLFunctions {

    private final IndexServiceInterface indexService;

    GraphQLFunctions(final IndexServiceInterface indexService) {
        this.indexService = indexService;
    }

    public GraphQLSchema getGraphQLSchema() {

        final GraphQLObjectType indexType = newObject()
            .name("Index")
            .description("An index")
            .field(newFieldDefinition()
                .name("name")
                .description("The name of the index")
                .type(GraphQLNonNull.nonNull(GraphQLString)))
            .field(newFieldDefinition()
                .name("id")
                .description("The unique id of the index")
                .type(GraphQLNonNull.nonNull(GraphQLString)))
            .build();

        final List<GraphQLFieldDefinition> queries = List.of(
            newFieldDefinition()
                .name("indexList")
                .argument(
                    newArgument()
                        .name("keywords")
                        .description("Filter the index list ")
                        .type(GraphQLString)
                        .build())
                .type(GraphQLList.list(indexType))
                .build()
        );


        final List<GraphQLFieldDefinition> mutations = List.of(
            newFieldDefinition()
                .name("createIndex")
                .argument(
                    newArgument()
                        .name("name")
                        .description("The name of the index to create")
                        .type(GraphQLNonNull.nonNull(GraphQLString))
                        .build())
                .type(GraphQLBoolean)
                .build(),
            newFieldDefinition()
                .name("deleteIndex")
                .argument(
                    newArgument()
                        .name("name")
                        .description("The name of the index to delete")
                        .type(GraphQLNonNull.nonNull(GraphQLString))
                        .build())
                .type(GraphQLBoolean)
                .build());

        final GraphQLSchema.Builder builder = GraphQLSchema.newSchema();

        builder.query(newObject()
            .name("Query")
            .description("root queries")
            .fields(queries)
            .build());

        builder.mutation(newObject()
            .name("Mutation")
            .description("root mutations")
            .fields(mutations)
            .build());

        final GraphQLCodeRegistry codeRegistry = newCodeRegistry()
            .dataFetcher(
                coordinates("Query", "indexList"),
                this::indexList)
            .dataFetcher(
                coordinates("Mutation", "createIndex"),
                this::createIndex)
            .dataFetcher(
                coordinates("Mutation", "deleteIndex"),
                this::deleteIndex)
            .build();

        return builder.codeRegistry(codeRegistry).build();
    }

    private List<Index> indexList(final DataFetchingEnvironment environment) {
        final String keywords = environment.getArgument("keywords");
        final Integer startArg = environment.getArgument("start");
        final Integer rowsArg = environment.getArgument("rows");
        final List<Index> result = new ArrayList<>();
        int start = startArg == null ? 0 : startArg;
        int rows = rowsArg == null ? 20 : rowsArg;
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
