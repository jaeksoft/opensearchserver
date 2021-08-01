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

import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import java.io.IOException;
import java.util.Map;

public class GraphQLService {

    private volatile GraphQL graphQL;

    public GraphQLService(final GraphQLSchema schema) throws IOException {
        newSchema(schema);
    }

    public synchronized void newSchema(final GraphQLSchema schema) {
        graphQL = GraphQL.newGraphQL(schema).build();
    }

    public ExecutionResult query(String operationName, String query, Map<String, Object> variables) {
        final ExecutionInput.Builder builder = ExecutionInput.newExecutionInput(query);
        if (operationName != null)
            builder.operationName(operationName);
        if (variables != null)
            builder.variables(variables);
        return graphQL.execute(builder.build());
    }

}
