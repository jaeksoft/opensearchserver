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
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.MapSerializer;
import com.qwazr.search.analysis.SmartAnalyzerSet;
import com.qwazr.search.field.FieldDefinition;
import com.qwazr.search.field.SmartFieldDefinition;
import com.qwazr.search.index.IndexServiceInterface;
import com.qwazr.search.index.IndexSettingsDefinition;
import com.qwazr.search.index.PostDefinition;
import com.qwazr.search.index.QueryBuilder;
import com.qwazr.search.index.QueryDefinition;
import com.qwazr.search.index.ResultDefinition;
import com.qwazr.search.index.ResultDocumentMap;
import com.qwazr.search.query.AbstractClassicQueryParser;
import com.qwazr.search.query.AbstractQueryParser;
import com.qwazr.search.query.MultiFieldQueryParser;
import com.qwazr.search.query.QueryInterface;
import com.qwazr.search.query.QueryParser;
import com.qwazr.search.query.QueryParserOperator;
import com.qwazr.search.query.SimpleQueryParser;
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
        if (name == null || name.isEmpty() || name.isBlank())
            return false;
        final String indexName = name.trim();
        final IndexSettingsDefinition indexSettings = IndexSettingsDefinition
            .of()
            .primaryKey("")
            .recordField(FieldDefinition.RECORD_FIELD)
            .build();
        graphQlService.refreshSchema(() -> {
            indexService.createUpdateIndex(indexName, indexSettings);
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

    private QueryResult search(final String indexName, final QueryInterface queryInterface, final DataFetchingEnvironment environment) {
        final QueryBuilder queryDefinitionBuilder = QueryDefinition.of(queryInterface)
            .start(environment.getArgument("start"))
            .rows(environment.getArgument("rows"));
        final List<String> returnedFields = environment.getArgument("returnedFields");
        if (returnedFields == null || returnedFields.isEmpty()) {
            queryDefinitionBuilder.returnedFields("*");
        } else {
            queryDefinitionBuilder.returnedFields(returnedFields);
        }
        final ResultDefinition.WithMap result = indexService.searchQuery(indexName, queryDefinitionBuilder.build(), false);
        return new QueryResult(result);
    }

    private void commonQueryParserParameters(final Map<String, Object> params, final AbstractQueryParser.AbstractBuilder builder) {
        builder
            .setEnableGraphQueries((Boolean) params.get("enableGraphQueries"))
            .setEnablePositionIncrements((Boolean) params.get("enablePositionIncrements"))
            .setAutoGenerateMultiTermSynonymsPhraseQuery((Boolean) params.get("autoGenerateMultiTermSynonymsPhraseQuery"))
            .setQueryString((String) params.get("queryString"));
        ;

    }

    private QueryParserOperator getDefaultOperator(final Map<String, Object> params) {
        final String defaultOperator = (String) params.get("defaultOperator");
        return defaultOperator == null ? null : QueryParserOperator.valueOf(defaultOperator);
    }

    private void commonClassicQueryParserParameters(final Map<String, Object> params, final AbstractClassicQueryParser.AbstractParserBuilder builder) {
        commonQueryParserParameters(params, builder);

        builder
            .setAllowLeadingWildcard((Boolean) params.get("allowLeadingWildcard"))
            .setAutoGeneratePhraseQuery((Boolean) params.get("autoGeneratePhraseQuery"))
            .setFuzzyMinSim((Float) params.get("fuzzyMinSim"))
            .setFuzzyPrefixLength((Integer) params.get("fuzzyPrefixLength"))
            .setSplitOnWhitespace((Boolean) params.get("splitOnWhitespace"))
            .setMaxDeterminizedStates((Integer) params.get("maxDeterminizedStates"))
            .setDefaultOperator(getDefaultOperator(params))
            .setPhraseSlop((Integer) params.get("phraseSlop"));
    }

    QueryResult searchWithMultiFieldQueryParser(final String indexName, final DataFetchingEnvironment environment) {
        final MultiFieldQueryParser.Builder builder = MultiFieldQueryParser.of();
        final Map<String, Object> params = environment.getArgument("params");
        commonClassicQueryParserParameters(params, builder);
        final Map<String, Number> fieldBoostMap = environment.getArgument("fieldBoost");
        if (fieldBoostMap != null) {
            fieldBoostMap.forEach((field, boost) -> builder.addBoost(field, boost.floatValue()));
        }
        return search(indexName, builder.build(), environment);
    }

    QueryResult searchWithStandardQueryParser(final String indexName, final DataFetchingEnvironment environment) {
        final QueryParser.Builder builder = QueryParser.of(environment.getArgument("defaultField"));
        final Map<String, Object> params = environment.getArgument("params");
        commonQueryParserParameters(params, builder);
        return search(indexName, builder.build(), environment);
    }

    QueryResult searchWithSimpleQueryParser(final String indexName, final DataFetchingEnvironment environment) {
        final SimpleQueryParser.Builder builder = SimpleQueryParser.of();
        final Map<String, Object> params = environment.getArgument("params");
        commonQueryParserParameters(params, builder);
        final List<Map<String, Object>> fieldBoosts = (List<Map<String, Object>>) params.get("fieldBoosts");
        if (fieldBoosts != null) {
            for (Map<String, Object> fieldBoost : fieldBoosts) {
                final String field = (String) fieldBoost.get("field");
                final Number boost = (Number) fieldBoost.get("boost");
                builder.addBoost(field, boost.floatValue());
            }
        }
        builder.setDefaultOperator(getDefaultOperator(params));
        for (SimpleOperator operator : SimpleOperator.values()) {
            final Boolean enabled = environment.getArgument(operator.name());
            if (enabled != null && enabled) {
                builder.addOperator(operator.operator);
            }
        }
        return search(indexName, builder.build(), environment);
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

    public enum SimpleOperator {

        enableAndOperator(SimpleQueryParser.Operator.and),
        enableEscapeOperator(SimpleQueryParser.Operator.escape),
        enableFuzzyOperator(SimpleQueryParser.Operator.fuzzy),
        enableNearOperator(SimpleQueryParser.Operator.near),
        enableNotOperator(SimpleQueryParser.Operator.not),
        enableOrOperator(SimpleQueryParser.Operator.or),
        enablePhraseOperator(SimpleQueryParser.Operator.phrase),
        enablePrecedenceOperator(SimpleQueryParser.Operator.precedence),
        enablePrefixOperator(SimpleQueryParser.Operator.prefix),
        enableWhitespaceOperator(SimpleQueryParser.Operator.whitespace);

        final SimpleQueryParser.Operator operator;

        SimpleOperator(SimpleQueryParser.Operator operator) {
            this.operator = operator;
        }
    }

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JsonAutoDetect(
        setterVisibility = JsonAutoDetect.Visibility.NONE,
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        isGetterVisibility = JsonAutoDetect.Visibility.NONE,
        creatorVisibility = JsonAutoDetect.Visibility.NONE,
        fieldVisibility = JsonAutoDetect.Visibility.PUBLIC_ONLY)
    public static class QueryResult {

        @JsonProperty
        public final int totalHits;

        @JsonProperty
        @JsonSerialize(keyUsing = MapSerializer.class)
        public final List<Map<String, Object>> documents;

        QueryResult(ResultDefinition.WithMap result) {
            totalHits = result.totalHits > Integer.MAX_VALUE ? null : (int) result.totalHits;
            if (result.documents != null) {
                documents = new ArrayList<>(result.documents.size());
                for (final ResultDocumentMap resultDocumentMap : result.documents) {
                    documents.add(resultDocumentMap.fields);
                }
            } else
                documents = List.of();
        }

    }

}
