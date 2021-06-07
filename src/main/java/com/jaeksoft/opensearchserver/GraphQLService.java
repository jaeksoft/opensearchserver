/*
 *  Copyright 2015-2020 Emmanuel Keller / QWAZR
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.jaeksoft.opensearchserver;

import com.qwazr.utils.IOUtils;
import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.scalars.ExtendedScalars;
import graphql.schema.DataFetcher;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import graphql.schema.idl.TypeRuntimeWiring;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class GraphQLService {

    private final GraphQL graphQL;

    public GraphQLService(final DataFetcherProvider... dataFetcherProviders) throws IOException {

        // Read the schema from resources
        final String schema = IOUtils.resourceToString(
            "/com/jaeksoft/opensearchserver/schema.graphqls",
            StandardCharsets.UTF_8);

        final SchemaParser schemaParser = new SchemaParser();
        final TypeDefinitionRegistry typeDefinitionRegistry = schemaParser.parse(schema);

        // Group together the fetchers by types
        final Map<String, Map<String, DataFetcher<?>>> dataFetcherMap = new HashMap<>();
        for (DataFetcherProvider dataFetcherProvider : dataFetcherProviders) {
            dataFetcherProvider.getDataFetchers().forEach(
                (type, dataFetchers) -> dataFetcherMap.computeIfAbsent(
                    type, t -> new HashMap<>()).putAll(dataFetchers));
        }

        // Build the graphql wiring
        final RuntimeWiring.Builder runtimeWiring = RuntimeWiring.newRuntimeWiring().scalar(ExtendedScalars.GraphQLLong);
        dataFetcherMap.forEach((type, dataFetchers) -> {
            final TypeRuntimeWiring.Builder typeWiring = TypeRuntimeWiring.newTypeWiring(type);
            dataFetchers.forEach(typeWiring::dataFetcher);
            runtimeWiring.type(typeWiring);
        });

        final SchemaGenerator schemaGenerator = new SchemaGenerator();
        final GraphQLSchema graphQLSchema = schemaGenerator.makeExecutableSchema(
            typeDefinitionRegistry, runtimeWiring.build());
        graphQL = GraphQL.newGraphQL(graphQLSchema).build();
    }


    public ExecutionResult query(String operationName, String query, Map<String, Object> variables) {
        final ExecutionInput.Builder builder = ExecutionInput.newExecutionInput(query);
        if (operationName != null)
            builder.operationName(operationName);
        if (variables != null)
            builder.variables(variables);
        return graphQL.execute(builder.build());
    }

    public interface DataFetcherProvider {
        Map<String, Map<String, DataFetcher<?>>> getDataFetchers();
    }

}
