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
import static graphql.Scalars.GraphQLBoolean;
import static graphql.Scalars.GraphQLFloat;
import static graphql.Scalars.GraphQLInt;
import static graphql.Scalars.GraphQLString;
import graphql.schema.DataFetchingEnvironment;
import static graphql.schema.FieldCoordinates.coordinates;
import graphql.schema.GraphQLArgument;
import static graphql.schema.GraphQLArgument.newArgument;
import graphql.schema.GraphQLCodeRegistry;
import static graphql.schema.GraphQLCodeRegistry.newCodeRegistry;
import graphql.schema.GraphQLEnumType;
import static graphql.schema.GraphQLEnumType.newEnum;
import graphql.schema.GraphQLFieldDefinition;
import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import graphql.schema.GraphQLInputObjectField;
import static graphql.schema.GraphQLInputObjectField.newInputObjectField;
import graphql.schema.GraphQLInputObjectType;
import static graphql.schema.GraphQLInputObjectType.newInputObject;
import graphql.schema.GraphQLInterfaceType;
import static graphql.schema.GraphQLInterfaceType.newInterface;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLNonNull;
import graphql.schema.GraphQLObjectType;
import static graphql.schema.GraphQLObjectType.newObject;
import graphql.schema.GraphQLSchema;
import graphql.schema.TypeResolver;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

class GraphQLFunctions {

    private final IndexServiceInterface indexService;
    private final AtomicInteger renewSchema;

    GraphQLFunctions(final IndexServiceInterface indexService, final AtomicInteger renewSchema) {
        this.indexService = indexService;
        this.renewSchema = renewSchema;
    }

    private static <T extends Enum<T>> GraphQLEnumType createEnum(String name, String description, T... values) {
        final GraphQLEnumType.Builder builder = newEnum()
            .name(name)
            .description(description);
        for (final T value : values) {
            builder.value(value.name());
        }
        return builder.build();
    }

    private final static String indexDocumentPrefix = "Index_";
    private final static String indexDocumentSuffix = "_doc";

    public GraphQLSchema getGraphQLSchema() {

        final List<GraphQLFieldDefinition> rootQueries = new ArrayList();
        final List<GraphQLFieldDefinition> rootMutations = new ArrayList();
        final GraphQLCodeRegistry.Builder codeRegistry = newCodeRegistry();

        final String indexNameDescription = "The name of the index";

        final GraphQLArgument indexNameArgument = newArgument()
            .name("indexName")
            .description("The name of the index")
            .type(GraphQLNonNull.nonNull(GraphQLString))
            .build();

        // INDEX
        {
            final GraphQLObjectType indexType = newObject()
                .name("Index")
                .description("An index")
                .field(newFieldDefinition()
                    .name("name")
                    .description(indexNameDescription)
                    .type(GraphQLNonNull.nonNull(GraphQLString)))
                .field(newFieldDefinition()
                    .name("id")
                    .description("The unique id of the index")
                    .type(GraphQLNonNull.nonNull(GraphQLString)))
                .build();

            final GraphQLFieldDefinition createIndexFunc = newFieldDefinition()
                .name("createIndex")
                .argument(indexNameArgument)
                .type(GraphQLBoolean)
                .build();

            codeRegistry.dataFetcher(
                coordinates("Mutation", "createIndex"),
                this::createIndex);

            rootMutations.add(createIndexFunc);

            final GraphQLFieldDefinition deleteIndexFunc = newFieldDefinition()
                .name("deleteIndex")
                .argument(indexNameArgument)
                .type(GraphQLBoolean)
                .build();

            codeRegistry.dataFetcher(
                coordinates("Mutation", "deleteIndex"),
                this::deleteIndex);

            rootMutations.add(deleteIndexFunc);

            final GraphQLFieldDefinition indexListFunc = newFieldDefinition()
                .name("indexList")
                .argument(
                    newArgument()
                        .name("keywords")
                        .description("Filter the index list ")
                        .type(GraphQLString)
                        .build())
                .type(GraphQLList.list(indexType))
                .build();

            codeRegistry.dataFetcher(
                coordinates("Query", "indexList"),
                this::indexList);

            rootQueries.add(indexListFunc);

        }

        // FIELDS

        // Field type

        final GraphQLEnumType fieldTypeEnum = createEnum("fieldTypeEnum", "The type of the field",
            SmartFieldDefinition.Type.TEXT, SmartFieldDefinition.Type.INTEGER, SmartFieldDefinition.Type.FLOAT);

        final GraphQLFieldDefinition fieldTypeField = newFieldDefinition()
            .name("type")
            .description("The type of the field")
            .type(fieldTypeEnum)
            .build();

        // Field name

        final String fieldNameDescription = "The name of the field";

        final GraphQLFieldDefinition fieldNameField = newFieldDefinition()
            .name("name")
            .description(fieldNameDescription)
            .type(GraphQLNonNull.nonNull(GraphQLString)).build();

        final GraphQLArgument fieldNameArgument = newArgument()
            .name("fieldName")
            .description(fieldNameDescription)
            .type(GraphQLNonNull.nonNull(GraphQLString))
            .build();

        // Field Sortable

        final String sortableDescription = "Define if the field can be use to sort the result";

        final GraphQLFieldDefinition sortableFieldField = newFieldDefinition()
            .name("sortable")
            .description(sortableDescription)
            .type(GraphQLBoolean).build();

        final GraphQLArgument sortableArgument = newArgument()
            .name("sortable")
            .description(sortableDescription)
            .type(GraphQLBoolean)
            .build();

        // Field Stored

        final String storedDescription = "Define if the field content can be returned";

        final GraphQLFieldDefinition storedFieldField = newFieldDefinition()
            .name("stored")
            .description(storedDescription)
            .type(GraphQLBoolean).build();

        final GraphQLArgument storedArgument = newArgument()
            .name("stored")
            .description(storedDescription)
            .type(GraphQLBoolean)
            .build();

        // Field facet

        final String facetDescription = "Define if the field can be used in a facet";

        final GraphQLFieldDefinition facetFieldField = newFieldDefinition()
            .name("facet")
            .description(facetDescription)
            .type(GraphQLBoolean).build();

        final GraphQLArgument facetArgument = newArgument()
            .name("facet")
            .description(facetDescription)
            .type(GraphQLBoolean)
            .build();

        // Abstract field

        final GraphQLInterfaceType fieldInterface = newInterface()
            .name("Field")
            .description("An abstract field")
            .field(fieldNameField)
            .field(fieldTypeField)
            .build();

        // Text Field
        final String textAnalyzerDescription = "The text analyzer applied to the content";

        final GraphQLEnumType textAnalyzerEnum = createEnum("TextAnalyzer", "Text analyzer", SmartAnalyzerSet.values());

        final GraphQLFieldDefinition textAnalyzerField = newFieldDefinition()
            .name("textAnalyzer")
            .description(textAnalyzerDescription)
            .type(textAnalyzerEnum)
            .build();

        final GraphQLObjectType textField = newObject()
            .name("TextField")
            .description("A text field")
            .withInterface(fieldInterface)
            .fields(List.of(fieldNameField, fieldTypeField, textAnalyzerField, sortableFieldField, storedFieldField, facetFieldField))
            .build();

        final GraphQLArgument textAnalyzerArgument = newArgument()
            .name("textAnalyzer")
            .description(textAnalyzerDescription)
            .type(textAnalyzerEnum)
            .build();

        final GraphQLFieldDefinition upsertTextFieldFunc = newFieldDefinition()
            .name("upsertTextField")
            .description("Create or update a text field")
            .arguments(List.of(indexNameArgument, fieldNameArgument, textAnalyzerArgument, sortableArgument, storedArgument, facetArgument))
            .type(GraphQLBoolean)
            .build();

        codeRegistry.dataFetcher(
            coordinates("Mutation", "upsertTextField"),
            this::upsertTextField);

        rootMutations.add(upsertTextFieldFunc);

        // Integer Field

        final String integerDescription = "A 32 bits integer field. Whole numbers from -2,147,483,648 to 2,147,483,647";

        final GraphQLObjectType integerField = newObject()
            .name("IntegerField")
            .description(integerDescription)
            .withInterface(fieldInterface)
            .fields(List.of(fieldNameField, fieldTypeField, sortableFieldField))
            .build();

        final GraphQLFieldDefinition upsertIntegerFieldFunc = newFieldDefinition()
            .name("upsertIntegerField")
            .description("Create or update a 32 bits integer field")
            .arguments(List.of(indexNameArgument, fieldNameArgument, sortableArgument))
            .type(GraphQLBoolean)
            .build();

        rootMutations.add(upsertIntegerFieldFunc);

        // Float Field

        final String floatDescription = "A 32 bits decimal field for fractional numbers. Sufficient for storing 6 to 7 decimal digits.";

        final GraphQLObjectType floatField = newObject()
            .name("floatField")
            .description(floatDescription)
            .withInterface(fieldInterface)
            .fields(List.of(fieldNameField, fieldTypeField, sortableFieldField))
            .build();

        final GraphQLFieldDefinition upsertFloatFieldFunc = newFieldDefinition()
            .name("upsertFloatField")
            .description("Create or update a 32 bits decimal field")
            .arguments(List.of(indexNameArgument, fieldNameArgument, sortableArgument))
            .type(GraphQLBoolean)
            .build();

        rootMutations.add(upsertFloatFieldFunc);

        // Field list function

        final GraphQLFieldDefinition fieldListFunc = newFieldDefinition()
            .name("fieldList")
            .argument(indexNameArgument)
            .type(GraphQLList.list(fieldInterface))
            .build();

        rootQueries.add(fieldListFunc);

        // Field deletion

        final GraphQLFieldDefinition deleteFieldFunc = newFieldDefinition()
            .name("deleteField")
            .description("Delete a field")
            .argument(fieldNameArgument)
            .type(GraphQLBoolean)
            .build();

        rootMutations.add(deleteFieldFunc);

        // Abstract field resolver

        final TypeResolver fieldTypeResolver = env -> {
            final SmartFieldDefinition o = env.getObject();
            switch (o.getType()) {
                case TEXT:
                    return textField;
                case INTEGER:
                    return integerField;
                case FLOAT:
                    return floatField;
                default:
                    return null;

            }
        };

        codeRegistry.typeResolver(fieldInterface, fieldTypeResolver);


        // Per index

        {
            for (final String indexName : indexService.getIndexes().keySet()) {

                final GraphQLInputObjectType.Builder indexDocumentBuilder = newInputObject()
                    .name(indexDocumentPrefix + indexName + indexDocumentSuffix)
                    .description("A document following the schema of the index \"" + indexName + "\"");

                indexService.getFields(indexName).forEach((fieldName, fieldDefinition) -> {
                    if (!(fieldDefinition instanceof SmartFieldDefinition))
                        return;
                    final SmartFieldDefinition smartField = (SmartFieldDefinition) fieldDefinition;
                    final GraphQLInputObjectField.Builder fieldBuilder = newInputObjectField().name(fieldName);
                    switch (smartField.getType()) {
                        case TEXT:
                            fieldBuilder.type(GraphQLString);
                            break;
                        case INTEGER:
                            fieldBuilder.type(GraphQLInt);
                            break;
                        case FLOAT:
                            fieldBuilder.type(GraphQLFloat);
                            break;
                        default:
                            return;
                    }
                    indexDocumentBuilder.field(fieldBuilder.build());
                });

                final GraphQLInputObjectType indexDocument = indexDocumentBuilder.build();
                if (indexDocument.getFields().isEmpty())
                    continue;
                final GraphQLFieldDefinition ingestDocuments = newFieldDefinition()
                    .name("ingest_" + indexName)
                    .description("Ingest a document into \"" + indexName + "\"")
                    .argument(newArgument().name("docs").type(GraphQLList.list(indexDocument)))
                    .type(GraphQLBoolean)
                    .build();

                rootMutations.add(ingestDocuments);
            }
        }

        // Build the schema

        final GraphQLSchema.Builder builder = GraphQLSchema.newSchema();

        builder.query(newObject()
            .name("Query")
            .description("root queries")
            .fields(rootQueries)
            .build());

        builder.mutation(newObject()
            .name("Mutation")
            .description("root mutations")
            .fields(rootMutations)
            .build());

        return builder.codeRegistry(codeRegistry.build()).build();
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
        final String name = environment.getArgument("indexName");
        if (name == null || name.isEmpty() || name.isBlank())
            return false;
        indexService.createUpdateIndex(name.trim());
        renewSchema.incrementAndGet();
        return true;
    }

    private Boolean deleteIndex(final DataFetchingEnvironment environment) {
        final String name = environment.getArgument("indexName");
        if (name == null || name.isEmpty() || name.isBlank())
            return false;
        if (!indexService.deleteIndex(name.trim()))
            return false;
        renewSchema.incrementAndGet();
        return true;
    }

    private static String getIndexName(final DataFetchingEnvironment environment) {
        final String gqlFieldName = environment.getField().getName();
        return gqlFieldName.substring(indexDocumentPrefix.length(), gqlFieldName.length() - indexDocumentSuffix.length());
    }

    private Boolean upsertTextField(final DataFetchingEnvironment environment) {
        final String indexName = environment.getArgument("indexName");
        if (indexName == null || indexName.isEmpty() || indexName.isBlank())
            return false;
        final String fieldName = environment.getArgument("fieldName");
        if (fieldName == null || fieldName.isEmpty() || fieldName.isBlank())
            return false;
        final Boolean sortable = environment.getArgument("sortable");
        final Boolean facet = environment.getArgument("facet");
        final Boolean stored = environment.getArgument("stored");
        final SmartFieldDefinition.SmartBuilder builder = SmartFieldDefinition.of()
            .type(SmartFieldDefinition.Type.TEXT)
            .sort(sortable)
            .facet(facet)
            .stored(stored);
        final String textAnalyzer = environment.getArgument("textAnalyzer");
        if (textAnalyzer != null) {
            builder.index(true).analyzer(textAnalyzer);
        }
        indexService.setField(indexName, fieldName, builder.build());
        renewSchema.incrementAndGet();
        return true;
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
