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
import com.qwazr.search.analysis.SmartAnalyzerSet;
import com.qwazr.search.field.SmartFieldDefinition;
import com.qwazr.search.index.IndexServiceInterface;
import com.qwazr.search.index.PostDefinition;
import static graphql.Scalars.GraphQLFloat;
import static graphql.Scalars.GraphQLInt;
import static graphql.Scalars.GraphQLString;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLScalarType;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

class GraphQLFunctions {

    private final IndexServiceInterface indexService;
    private final GraphQLService graphQlService;

    GraphQLFunctions(final IndexServiceInterface indexService, final GraphQLService graphQlService) {
        this.indexService = indexService;
        this.graphQlService = graphQlService;
    }

    private String getStringArgument(final DataFetchingEnvironment environment, final String argumentName) {
        final String argumentValue = environment.getArgument(argumentName);
        if (argumentValue == null || argumentValue.isEmpty() || argumentValue.isBlank())
            return null;
        return argumentValue;
    }


    List<Index> getIndexList(final String keywords, final Integer startArg, final Integer rowsArg) {
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

    List<Index> getIndexes(final DataFetchingEnvironment environment) {
        final String keywords = environment.getArgument("keywords");
        final Integer startArg = environment.getArgument("start");
        final Integer rowsArg = environment.getArgument("rows");
        return getIndexList(keywords, startArg, rowsArg);
    }

    Boolean createIndex(final DataFetchingEnvironment environment) {
        final String name = getStringArgument(environment, "indexName");
        if (name == null)
            return false;
        graphQlService.refreshSchema(() -> {
            indexService.createUpdateIndex(name.trim());
            return true;
        });
        return true;
    }

    Boolean deleteIndex(final DataFetchingEnvironment environment) {
        final String name = getStringArgument(environment, "indexName");
        if (name == null)
            return false;
        return graphQlService.refreshSchema(() -> indexService.deleteIndex(name.trim()));
    }

    Boolean buildField(final DataFetchingEnvironment environment, final SmartFieldDefinition.SmartBuilder builder) {
        final String indexName = getStringArgument(environment, "indexName");
        if (indexName == null)
            return false;
        final String fieldName = getStringArgument(environment, "fieldName");
        if (fieldName == null)
            return false;
        final Boolean sortable = environment.getArgument("sortable");
        final Boolean stored = environment.getArgument("stored");
        builder.sort(sortable)
            .stored(stored);
        return graphQlService.refreshSchema(() -> {
            indexService.setField(indexName, fieldName, builder.build());
            return true;
        });
    }

    Boolean setTextField(final DataFetchingEnvironment environment) {
        final Boolean facet = environment.getArgument("facet");
        final SmartFieldDefinition.SmartBuilder builder = SmartFieldDefinition.of()
            .type(SmartFieldDefinition.Type.TEXT)
            .facet(facet);
        final String textAnalyzer = environment.getArgument("textAnalyzer");
        if (textAnalyzer != null) {
            builder.index(true).analyzer(textAnalyzer);
        }
        return buildField(environment, builder);
    }

    Boolean setIntegerField(final DataFetchingEnvironment environment) {
        final Boolean indexed = environment.getArgument("indexed");
        return buildField(environment, SmartFieldDefinition.of()
            .index(indexed)
            .type(SmartFieldDefinition.Type.INTEGER));
    }

    Boolean setFloatField(final DataFetchingEnvironment environment) {
        final Boolean indexed = environment.getArgument("indexed");
        return buildField(environment, SmartFieldDefinition.of()
            .index(indexed)
            .type(SmartFieldDefinition.Type.FLOAT));
    }

    Boolean deleteField(final DataFetchingEnvironment environment) {
        final String indexName = getStringArgument(environment, "indexName");
        if (indexName == null)
            return false;
        final String fieldName = getStringArgument(environment, "fieldName");
        if (fieldName == null)
            return false;
        return graphQlService.refreshSchema(() -> indexService.deleteField(indexName, fieldName));
    }

    List<Field> getFieldList(String indexName) {
        if (indexName == null)
            return null;
        final List<Field> fields = new ArrayList<>();
        indexService.getFields(indexName).forEach((fieldName, fieldDefinition) -> {
            final SmartFieldDefinition smartField = (SmartFieldDefinition) fieldDefinition;
            switch (smartField.getType()) {
                case TEXT:
                    fields.add(new TextField(fieldName, smartField));
                    break;
                case INTEGER:
                    fields.add(new IntegerField(fieldName, smartField));
                    break;
                case FLOAT:
                    fields.add(new FloatField(fieldName, smartField));
                    break;
                default:
                    break;
            }
        });
        return fields;
    }

    List<Field> getFields(final DataFetchingEnvironment environment) {
        return getFieldList(getStringArgument(environment, "indexName"));
    }

    Integer ingestDocuments(final String indexName, final DataFetchingEnvironment environment) {
        final List<Map<String, Object>> list = environment.getArgument("docs");
        if (list.isEmpty())
            return 0;
        if (list.size() == 1)
            return indexService.postMappedDocument(indexName, PostDefinition.Document.of(list.get(0), null));
        else
            return indexService.postMappedDocuments(indexName, PostDefinition.Documents.of(list, null));
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

    public static abstract class Field {

        @JsonProperty
        public final String name;
        @JsonProperty
        public final Boolean sortable;
        @JsonProperty
        public final Boolean stored;

        Field(final String name, final SmartFieldDefinition field) {
            this.name = name;
            this.sortable = field.sort;
            this.stored = field.stored;
        }

        abstract GraphQLScalarType getGraphScalarType();
    }

    public static abstract class IndexedField extends Field {
        @JsonProperty
        public final Boolean indexed;

        IndexedField(String name, SmartFieldDefinition field) {
            super(name, field);
            this.indexed = field.index;
        }
    }

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JsonAutoDetect(
        setterVisibility = JsonAutoDetect.Visibility.NONE,
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        isGetterVisibility = JsonAutoDetect.Visibility.NONE,
        creatorVisibility = JsonAutoDetect.Visibility.NONE,
        fieldVisibility = JsonAutoDetect.Visibility.PUBLIC_ONLY)
    public static class TextField extends Field {

        @JsonProperty
        public final Boolean facet;
        @JsonProperty
        public final SmartAnalyzerSet textAnalyzer;

        TextField(String name, SmartFieldDefinition field) {
            super(name, field);
            this.facet = field.facet;
            this.textAnalyzer = SmartAnalyzerSet.of(field.getAnalyzer());
        }

        GraphQLScalarType getGraphScalarType() {
            return GraphQLString;
        }

    }

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JsonAutoDetect(
        setterVisibility = JsonAutoDetect.Visibility.NONE,
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        isGetterVisibility = JsonAutoDetect.Visibility.NONE,
        creatorVisibility = JsonAutoDetect.Visibility.NONE,
        fieldVisibility = JsonAutoDetect.Visibility.PUBLIC_ONLY)
    public static class IntegerField extends IndexedField {

        IntegerField(String name, SmartFieldDefinition field) {
            super(name, field);
        }

        GraphQLScalarType getGraphScalarType() {
            return GraphQLInt;
        }
    }

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JsonAutoDetect(
        setterVisibility = JsonAutoDetect.Visibility.NONE,
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        isGetterVisibility = JsonAutoDetect.Visibility.NONE,
        creatorVisibility = JsonAutoDetect.Visibility.NONE,
        fieldVisibility = JsonAutoDetect.Visibility.PUBLIC_ONLY)
    public static class FloatField extends IndexedField {

        FloatField(String name, SmartFieldDefinition field) {
            super(name, field);
        }

        GraphQLScalarType getGraphScalarType() {
            return GraphQLFloat;
        }
    }
}
