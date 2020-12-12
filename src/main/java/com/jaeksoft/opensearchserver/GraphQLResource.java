/*
 *  Copyright 2015-2020 Emmanuel Keller / Jaeksoft
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

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.qwazr.utils.ObjectMappers;
import graphql.ExecutionResult;
import graphql.GraphQLError;
import java.util.List;
import java.util.Map;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/")
public class GraphQLResource {

    private final GraphQLService graphQLService;

    public GraphQLResource(final GraphQLService graphQLService) {
        this.graphQLService = graphQLService;
    }

    @POST
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public ExecutionResult query(final String query) throws JsonProcessingException {
        final InputQuery inputQuery = ObjectMappers.JSON.readValue(query, InputQuery.class);
        return new ExecutionResultWrapper(graphQLService.query(inputQuery.operationName, inputQuery.query, inputQuery.variables));
    }

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JsonAutoDetect(
        setterVisibility = JsonAutoDetect.Visibility.NONE,
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        isGetterVisibility = JsonAutoDetect.Visibility.NONE,
        creatorVisibility = JsonAutoDetect.Visibility.NONE,
        fieldVisibility = JsonAutoDetect.Visibility.NONE)
    public static class ExecutionResultWrapper implements ExecutionResult {

        private final ExecutionResult result;

        private ExecutionResultWrapper(final ExecutionResult result) {
            this.result = result;
        }

        @Override
        @JsonProperty
        public List<GraphQLError> getErrors() {
            return result.getErrors();
        }

        @Override
        @JsonProperty
        public <T> T getData() {
            return result.getData();
        }

        @Override
        @JsonIgnore
        public boolean isDataPresent() {
            return result.isDataPresent();
        }

        @Override
        @JsonProperty
        public Map<Object, Object> getExtensions() {
            return result.getExtensions();
        }

        @Override
        @JsonIgnore
        public Map<String, Object> toSpecification() {
            return result.toSpecification();
        }
    }

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JsonAutoDetect(
        setterVisibility = JsonAutoDetect.Visibility.NONE,
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        isGetterVisibility = JsonAutoDetect.Visibility.NONE,
        creatorVisibility = JsonAutoDetect.Visibility.NONE,
        fieldVisibility = JsonAutoDetect.Visibility.NONE)
    public static class InputQuery {

        private final String query;

        private final String operationName;

        private final Map<String, Object> variables;

        @JsonCreator
        private InputQuery(@JsonProperty("query") String query,
                           @JsonProperty("operationName") String operationName,
                           @JsonProperty("variables") Map<String, Object> variables) {
            this.query = query;
            this.operationName = operationName;
            this.variables = variables;
        }
    }
}
