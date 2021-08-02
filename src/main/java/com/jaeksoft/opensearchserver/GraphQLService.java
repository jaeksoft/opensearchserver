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
import java.util.concurrent.atomic.AtomicInteger;

public class GraphQLService {

    private final IndexServiceInterface indexService;

    private final AtomicInteger renewSchema;
    private volatile GraphQL graphQL;

    public GraphQLService(final IndexServiceInterface indexService) {
        this.indexService = indexService;
        this.renewSchema = new AtomicInteger(0);
        graphQL = newSchema(indexService, renewSchema);
    }

    private static GraphQL newSchema(final IndexServiceInterface indexService, final AtomicInteger renewSchema) {
        return GraphQL.newGraphQL(new GraphQLFunctions(indexService, renewSchema).getGraphQLSchema()).build();
    }

    private synchronized void checklRenewSchema() {
        if (renewSchema.get() == 0)
            return;
        graphQL = newSchema(indexService, renewSchema);
        renewSchema.decrementAndGet();
    }

    public ExecutionResult query(String operationName, String query, Map<String, Object> variables) {
        final ExecutionInput.Builder builder = ExecutionInput.newExecutionInput(query);
        if (operationName != null)
            builder.operationName(operationName);
        if (variables != null)
            builder.variables(variables);
        final ExecutionInput executionInput = builder.build();
        final ExecutionResult executionResult = graphQL.execute(executionInput);
        checklRenewSchema();
        return executionResult;
    }

}
