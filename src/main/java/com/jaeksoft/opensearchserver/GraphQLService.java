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

import com.qwazr.search.index.IndexServiceInterface;
import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import java.util.Map;
import java.util.function.Supplier;

public class GraphQLService {

    private final IndexServiceInterface indexService;
    private final GraphQLFunctions functions;
    private final Object schemaLock;
    private volatile GraphQL graphQL;

    public GraphQLService(final IndexServiceInterface indexService) {
        this.indexService = indexService;
        this.functions = new GraphQLFunctions(indexService, this);
        this.schemaLock = new Object();
        refreshSchema(null);
    }

    public boolean refreshSchema(final Supplier<Boolean> supplier) {
        synchronized (schemaLock) {
            final Boolean result;
            if (supplier != null) {
                result = supplier.get();
                if (result == null || !result)
                    return false;
            } else {
                result = true;
            }
            graphQL = new GraphQLSchemaBuilder(functions).build();
            return result;
        }
    }


    public ExecutionResult query(String operationName, String query, Map<String, Object> variables) {
        final ExecutionInput.Builder builder = ExecutionInput.newExecutionInput(query);
        if (operationName != null)
            builder.operationName(operationName);
        if (variables != null)
            builder.variables(variables);
        final ExecutionInput executionInput = builder.build();
        final ExecutionResult executionResult = graphQL.execute(executionInput);
        return executionResult;
    }

}
