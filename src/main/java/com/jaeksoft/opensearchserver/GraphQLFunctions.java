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
import com.qwazr.crawler.file.FileCrawlDefinition;
import com.qwazr.crawler.file.FileCrawlSessionStatus;
import com.qwazr.crawler.file.FileCrawlerServiceInterface;
import com.qwazr.crawler.web.WebCrawlDefinition;
import com.qwazr.crawler.web.WebCrawlSessionStatus;
import com.qwazr.crawler.web.WebCrawlerServiceInterface;
import com.qwazr.search.index.IndexServiceInterface;
import com.qwazr.utils.ObjectMappers;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

class GraphQLFunctions implements GraphQLService.DataFetcherProvider {

    private final IndexServiceInterface indexService;
    private final WebCrawlerServiceInterface webCrawlService;
    private final FileCrawlerServiceInterface fileCrawlService;

    GraphQLFunctions(final IndexServiceInterface indexService,
                     final WebCrawlerServiceInterface webCrawlService,
                     final FileCrawlerServiceInterface fileCrawlService) {
        this.indexService = indexService;
        this.webCrawlService = webCrawlService;
        this.fileCrawlService = fileCrawlService;
    }

    @Override
    public Map<String, Map<String, DataFetcher<?>>> getDataFetchers() {
        return Map.of(
            "Query", Map.of(
                "indexList", this::indexList,
                "webCrawlList", this::webCrawlList,
                "fileCrawlList", this::fileCrawlList
            ),
            "Mutation", Map.of(
                "createIndex", this::createIndex,
                "deleteIndex", this::deleteIndex,
                "runWebCrawl", this::runWebCrawl,
                "stopWebCrawl", this::stopWebCrawl,
                "deleteWebCrawl", this::deleteWebCrawl,
                "runFileCrawl", this::runFileCrawl,
                "stopFileCrawl", this::stopFileCrawl,
                "deleteFileCrawl", this::deleteFileCrawl
            )
        );
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

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JsonAutoDetect(
        setterVisibility = JsonAutoDetect.Visibility.NONE,
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        isGetterVisibility = JsonAutoDetect.Visibility.NONE,
        creatorVisibility = JsonAutoDetect.Visibility.NONE,
        fieldVisibility = JsonAutoDetect.Visibility.PUBLIC_ONLY)
    public static class FileCrawlStatus {

        @JsonProperty
        public final String name;
        @JsonProperty
        public final FileCrawlSessionStatus status;

        FileCrawlStatus(final String name, final FileCrawlSessionStatus status) {
            this.name = name;
            this.status = status;
        }
    }

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JsonAutoDetect(
        setterVisibility = JsonAutoDetect.Visibility.NONE,
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        isGetterVisibility = JsonAutoDetect.Visibility.NONE,
        creatorVisibility = JsonAutoDetect.Visibility.NONE,
        fieldVisibility = JsonAutoDetect.Visibility.PUBLIC_ONLY)
    public static class WebCrawlStatus {

        @JsonProperty
        public final String name;
        @JsonProperty
        public final WebCrawlSessionStatus status;

        WebCrawlStatus(final String name, final WebCrawlSessionStatus status) {
            this.name = name;
            this.status = status;
        }
    }

    private List<WebCrawlStatus> webCrawlList(final DataFetchingEnvironment environment) {
        final String keywords = environment.getArgument("keywords");
        final Integer startArg = environment.getArgument("start");
        final Integer rowsArg = environment.getArgument("rows");
        final List<WebCrawlStatus> result = new ArrayList<>();
        webCrawlService.getSessions(keywords, startArg, rowsArg, i -> {
        }).forEach((name, status) -> result.add(new WebCrawlStatus(name, status)));
        return result;
    }

    private WebCrawlStatus runWebCrawl(final DataFetchingEnvironment environment) throws IOException {
        final String name = environment.getArgument("name");
        final Map<String, Object> def = environment.getArgument("settings");
        final byte[] bytes = ObjectMappers.JSON.writeValueAsBytes(def);
        final WebCrawlDefinition webCrawlDefinition = ObjectMappers.JSON.readValue(bytes, WebCrawlDefinition.class);
        return new WebCrawlStatus(name, webCrawlService.runSession(name, webCrawlDefinition));
    }

    private Boolean stopWebCrawl(final DataFetchingEnvironment environment) {
        final String name = environment.getArgument("name");
        final String abortingReason = environment.getArgument("aborting_reason");
        webCrawlService.stopSession(name, abortingReason);
        return true;
    }

    private Boolean deleteWebCrawl(final DataFetchingEnvironment environment) {
        final String name = environment.getArgument("name");
        webCrawlService.removeSession(name);
        return true;
    }

    private List<FileCrawlStatus> fileCrawlList(final DataFetchingEnvironment environment) {
        final String keywords = environment.getArgument("keywords");
        final Integer startArg = environment.getArgument("start");
        final Integer rowsArg = environment.getArgument("rows");
        final List<FileCrawlStatus> result = new ArrayList<>();
        fileCrawlService.getSessions(keywords, startArg, rowsArg, i -> {
        }).forEach((name, status) -> result.add(new FileCrawlStatus(name, status)));
        return result;
    }

    private FileCrawlStatus runFileCrawl(final DataFetchingEnvironment environment) {
        final String name = environment.getArgument("name");
        final FileCrawlDefinition fileCrawlDefinition = environment.getArgument("settings");
        return new FileCrawlStatus(name, fileCrawlService.runSession(name, fileCrawlDefinition));
    }

    private Boolean stopFileCrawl(final DataFetchingEnvironment environment) {
        final String name = environment.getArgument("name");
        final String abortingReason = environment.getArgument("aborting_reason");
        fileCrawlService.stopSession(name, abortingReason);
        return true;
    }

    private Boolean deleteFileCrawl(final DataFetchingEnvironment environment) {
        final String name = environment.getArgument("name");
        fileCrawlService.removeSession(name);
        return true;
    }
}
