/*
 * Copyright 2017-2021 Emmanuel Keller / Jaeksoft
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

import com.qwazr.search.analysis.SmartAnalyzerSet;
import com.qwazr.utils.StringUtils;
import graphql.GraphQL;
import static graphql.Scalars.GraphQLBoolean;
import static graphql.Scalars.GraphQLString;
import graphql.schema.DataFetcher;
import static graphql.schema.FieldCoordinates.coordinates;
import graphql.schema.GraphQLArgument;
import static graphql.schema.GraphQLArgument.newArgument;
import graphql.schema.GraphQLCodeRegistry;
import static graphql.schema.GraphQLCodeRegistry.newCodeRegistry;
import graphql.schema.GraphQLEnumType;
import static graphql.schema.GraphQLEnumType.newEnum;
import graphql.schema.GraphQLFieldDefinition;
import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static graphql.schema.GraphQLInputObjectField.newInputObjectField;
import graphql.schema.GraphQLInputObjectType;
import static graphql.schema.GraphQLInputObjectType.newInputObject;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLNonNull;
import graphql.schema.GraphQLObjectType;
import static graphql.schema.GraphQLObjectType.newObject;
import graphql.schema.GraphQLSchema;
import graphql.schema.GraphQLUnionType;
import static graphql.schema.GraphQLUnionType.newUnionType;
import graphql.schema.TypeResolver;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GraphQLSchemaBuilder {

    private final static String indexDocumentPrefix = "Index";
    private final static String indexDocumentSuffix = "Document";

    private final GraphQLFunctions functions;

    private final List<GraphQLFieldDefinition> rootQueries;
    private final List<GraphQLFieldDefinition> rootMutations;
    private final GraphQLCodeRegistry.Builder codeRegistry;

    private final IndexContext indexContext;
    private final FieldContext fieldContext;

    private static <T extends Enum<T>> GraphQLEnumType createEnum(String name, String description, T... values) {
        final GraphQLEnumType.Builder builder = newEnum()
            .name(name)
            .description(description);
        for (final T value : values) {
            builder.value(value.name());
        }
        return builder.build();
    }

    GraphQLSchemaBuilder(final GraphQLFunctions functions) {

        this.functions = functions;

        rootQueries = new ArrayList();
        rootMutations = new ArrayList();
        codeRegistry = newCodeRegistry();

        indexContext = new IndexContext();
        fieldContext = new FieldContext();
    }

    public GraphQL build() {

        indexContext.build();
        fieldContext.build();

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

        final GraphQLSchema schema = builder.codeRegistry(codeRegistry.build()).build();
        return GraphQL.newGraphQL(schema).build();
    }

    private class IndexContext {

        private final static String INDEX_NAME_DESCRIPTION = "The name of the index";

        private final GraphQLArgument indexNameArgument;

        private IndexContext() {
            indexNameArgument = newArgument()
                .name("indexName")
                .description(INDEX_NAME_DESCRIPTION)
                .type(GraphQLNonNull.nonNull(GraphQLString))
                .build();
        }

        private void createIndexFunctions() {
            final GraphQLObjectType indexType = newObject()
                .name("Index")
                .description("An index")
                .field(newFieldDefinition()
                    .name("name")
                    .description(INDEX_NAME_DESCRIPTION)
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
                functions::createIndex);

            rootMutations.add(createIndexFunc);

            final GraphQLFieldDefinition deleteIndexFunc = newFieldDefinition()
                .name("deleteIndex")
                .argument(indexNameArgument)
                .type(GraphQLBoolean)
                .build();

            codeRegistry.dataFetcher(
                coordinates("Mutation", "deleteIndex"),
                functions::deleteIndex);

            rootMutations.add(deleteIndexFunc);

            final GraphQLFieldDefinition indexListFunc = newFieldDefinition()
                .name("getIndexes")
                .argument(
                    newArgument()
                        .name("keywords")
                        .description("Filter the index list ")
                        .type(GraphQLString)
                        .build())
                .type(GraphQLList.list(indexType))
                .build();

            codeRegistry.dataFetcher(
                coordinates("Query", "getIndexes"),
                functions::getIndexes);

            rootQueries.add(indexListFunc);

        }

        private void createDocumentTypes() {

            for (final GraphQLFunctions.Index index : functions.getIndexList(null, 0, Integer.MAX_VALUE)) {

                final String sourceIndexName = index.name.trim();
                final String capitalizedIndexName = StringUtils.capitalize(sourceIndexName);

                final GraphQLInputObjectType.Builder indexDocumentBuilder = newInputObject()
                    .name(indexDocumentPrefix + capitalizedIndexName + indexDocumentSuffix)
                    .description("A document following the schema of the index \"" + sourceIndexName + "\"");

                for (final GraphQLFunctions.Field field : functions.getFieldList(sourceIndexName)) {
                    indexDocumentBuilder.field(newInputObjectField()
                        .name(field.name)
                        .type(field.getGraphScalarType())
                        .build());
                }

                final GraphQLInputObjectType indexDocument = indexDocumentBuilder.build();
                if (indexDocument.getFields().isEmpty())
                    continue;
                final String ingestFunctionName = "ingest" + capitalizedIndexName;
                final GraphQLFieldDefinition ingestDocuments = newFieldDefinition()
                    .name(ingestFunctionName)
                    .description("Ingest a document into \"" + sourceIndexName + "\"")
                    .argument(newArgument().name("docs").type(GraphQLList.list(indexDocument)))
                    .type(GraphQLBoolean)
                    .build();

                rootMutations.add(ingestDocuments);

                codeRegistry.dataFetcher(
                    coordinates("Mutation", ingestFunctionName),
                    (DataFetcher<?>) env -> functions.ingestDocuments(sourceIndexName, env));
            }
        }

        private void build() {
            createIndexFunctions();
            createDocumentTypes();
        }
    }

    private class FieldContext {

        private final GraphQLFieldDefinition fieldNameField;
        private final GraphQLArgument fieldNameArgument;
        private final GraphQLFieldDefinition indexedFieldField;
        private final GraphQLArgument indexedArgument;
        private final GraphQLFieldDefinition sortableFieldField;
        private final GraphQLArgument sortableArgument;
        private final GraphQLFieldDefinition storedFieldField;
        private final GraphQLFieldDefinition facetFieldField;
        private final GraphQLArgument storedArgument;
        private final GraphQLArgument facetArgument;

        private FieldContext() {

            // Field name

            final String fieldNameDescription = "The name of the field";

            fieldNameField = newFieldDefinition()
                .name("name")
                .description(fieldNameDescription)
                .type(GraphQLNonNull.nonNull(GraphQLString)).build();

            fieldNameArgument = newArgument()
                .name("fieldName")
                .description(fieldNameDescription)
                .type(GraphQLNonNull.nonNull(GraphQLString))
                .build();

            // Field Indexed

            final String indexedDescription = "Define if the field can be used im queries";

            indexedFieldField = newFieldDefinition()
                .name("indexed")
                .description(indexedDescription)
                .type(GraphQLBoolean).build();

            indexedArgument = newArgument()
                .name("indexed")
                .description(indexedDescription)
                .type(GraphQLBoolean)
                .build();

            // Field Sortable

            final String sortableDescription = "Define if the field can be used to sort the result";

            sortableFieldField = newFieldDefinition()
                .name("sortable")
                .description(sortableDescription)
                .type(GraphQLBoolean).build();

            sortableArgument = newArgument()
                .name("sortable")
                .description(sortableDescription)
                .type(GraphQLBoolean)
                .build();

            // Field Stored

            final String storedDescription = "Define if the field content can be returned";

            storedFieldField = newFieldDefinition()
                .name("stored")
                .description(storedDescription)
                .type(GraphQLBoolean).build();

            storedArgument = newArgument()
                .name("stored")
                .description(storedDescription)
                .type(GraphQLBoolean)
                .build();

            // Field facet

            final String facetDescription = "Define if the field can be used in a facet";

            facetFieldField = newFieldDefinition()
                .name("facet")
                .description(facetDescription)
                .type(GraphQLBoolean).build();

            facetArgument = newArgument()
                .name("facet")
                .description(facetDescription)
                .type(GraphQLBoolean)
                .build();

        }

        private GraphQLObjectType createTextField() {

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
                .fields(List.of(fieldNameField, textAnalyzerField, sortableFieldField, storedFieldField, facetFieldField))
                .build();

            final GraphQLArgument textAnalyzerArgument = newArgument()
                .name("textAnalyzer")
                .description(textAnalyzerDescription)
                .type(textAnalyzerEnum)
                .build();

            final GraphQLFieldDefinition setTextFieldFunc = newFieldDefinition()
                .name("setTextField")
                .description("Create or update a text field")
                .arguments(List.of(indexContext.indexNameArgument, fieldNameArgument, textAnalyzerArgument, sortableArgument, storedArgument, facetArgument))
                .type(GraphQLBoolean)
                .build();

            codeRegistry.dataFetcher(
                coordinates("Mutation", "setTextField"),
                functions::setTextField);

            rootMutations.add(setTextFieldFunc);
            return textField;
        }

        private GraphQLObjectType createIntegerField() {
            final String integerDescription = "A 32 bits integer field. Whole numbers from -2,147,483,648 to 2,147,483,647.";

            final GraphQLObjectType integerField = newObject()
                .name("IntegerField")
                .description(integerDescription)
                .fields(List.of(fieldNameField, indexedFieldField, sortableFieldField, storedFieldField))
                .build();

            final GraphQLFieldDefinition setIntegerFieldFunc = newFieldDefinition()
                .name("setIntegerField")
                .description("Create or update a 32 bits integer field. Whole numbers from -2,147,483,648 to 2,147,483,647.")
                .arguments(List.of(indexContext.indexNameArgument, fieldNameArgument, indexedArgument, sortableArgument, storedArgument))
                .type(GraphQLBoolean)
                .build();

            rootMutations.add(setIntegerFieldFunc);

            codeRegistry.dataFetcher(
                coordinates("Mutation", "setIntegerField"),
                functions::setIntegerField);

            return integerField;
        }

        private GraphQLObjectType createFloatField() {
            final String floatDescription = "A 32 bits decimal field for fractional numbers. Sufficient for storing 6 to 7 decimal digits.";

            final GraphQLObjectType floatField = newObject()
                .name("FloatField")
                .description(floatDescription)
                .fields(List.of(fieldNameField, indexedFieldField, sortableFieldField, storedFieldField))
                .build();

            final GraphQLFieldDefinition setFloatFieldFunc = newFieldDefinition()
                .name("setFloatField")
                .description("Create or update a 32 bits decimal field. Sufficient for storing 6 to 7 decimal digits.")
                .arguments(List.of(indexContext.indexNameArgument, fieldNameArgument, indexedArgument, sortableArgument, storedArgument))
                .type(GraphQLBoolean)
                .build();

            rootMutations.add(setFloatFieldFunc);

            codeRegistry.dataFetcher(
                coordinates("Mutation", "setFloatField"),
                functions::setFloatField);

            return floatField;
        }

        private void createFieldList(GraphQLObjectType textField,
                                     GraphQLObjectType integerField,
                                     GraphQLObjectType floatField) {
            final GraphQLUnionType unionFieldType = newUnionType()
                .name("Field")
                .possibleTypes(textField, integerField, floatField)
                .build();

            final GraphQLFieldDefinition fieldListFunc = newFieldDefinition()
                .name("getFields")
                .argument(indexContext.indexNameArgument)
                .type(GraphQLList.list(unionFieldType))
                .build();

            rootQueries.add(fieldListFunc);

            final Map<Class<? extends GraphQLFunctions.Field>, GraphQLObjectType> fieldTypeClassMap = Map.of(
                GraphQLFunctions.TextField.class, textField,
                GraphQLFunctions.IntegerField.class, integerField,
                GraphQLFunctions.FloatField.class, floatField
            );

            final TypeResolver fieldTypeResolver = env -> fieldTypeClassMap.get(env.getObject().getClass());

            codeRegistry
                .typeResolver(unionFieldType, fieldTypeResolver)
                .dataFetcher(
                    coordinates("Query", "getFields"),
                    functions::getFields);

            // Field deletion

            final GraphQLFieldDefinition deleteFieldFunc = newFieldDefinition()
                .name("deleteField")
                .description("Delete a field")
                .arguments(List.of(indexContext.indexNameArgument, fieldNameArgument))
                .type(GraphQLBoolean)
                .build();

            rootMutations.add(deleteFieldFunc);

            codeRegistry.dataFetcher(
                coordinates("Mutation", "deleteField"),
                functions::deleteField);
        }

        private void build() {
            final GraphQLObjectType textField = createTextField();
            final GraphQLObjectType integerField = createIntegerField();
            final GraphQLObjectType floatField = createFloatField();
            createFieldList(textField, integerField, floatField);
        }
    }

}
