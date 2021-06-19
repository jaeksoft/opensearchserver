/*
 * Copyright 2017-2020 Emmanuel Keller / Jaeksoft
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

import com.fasterxml.jackson.databind.JsonNode;
import com.qwazr.crawler.common.Attributes;
import com.qwazr.crawler.common.CrawlCollector;
import com.qwazr.crawler.common.CrawlDefinition;
import com.qwazr.crawler.common.CrawlItem;
import com.qwazr.crawler.web.WebCrawlCollectorFactory;
import com.qwazr.crawler.web.WebCrawlDefinition;
import com.qwazr.crawler.web.WebCrawlItem;
import com.qwazr.crawler.web.driver.DriverInterface;
import com.qwazr.extractor.ExtractorServiceInterface;
import com.qwazr.extractor.ParserResult;
import com.qwazr.search.index.IndexServiceInterface;
import com.qwazr.utils.ObjectMappers;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.NotNull;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotAcceptableException;
import javax.ws.rs.core.MediaType;

public abstract class CrawlerCollector<ITEM extends CrawlItem<?>> implements CrawlCollector<ITEM> {

    public static class Variables {

        public final String index;
        public final Integer buffer;

        public Variables(List<CrawlDefinition.Variable> variables) {
            String varIndex = null;
            Integer varBuffer = null;
            if (variables != null) {
                for (final CrawlDefinition.Variable variable : variables) {
                    if (variable.key == null || variable.value == null) {
                        continue;
                    }
                    switch (variable.key) {
                        case VARIABLE_INDEX:
                            varIndex = variable.value.toString();
                            break;
                        case VARIABLE_BUFFER:
                            if (!(variable.value instanceof Number))
                                throw new NotAcceptableException("The \"buffer\" value is not a number: " + variable.value);
                            varBuffer = ((Number) variable.value).intValue();
                            break;
                    }
                }
            }
            this.index = varIndex;
            this.buffer = varBuffer;
        }
    }

    public final static String VARIABLE_INDEX = "index";
    public final static String VARIABLE_BUFFER = "buffer";

    private final static int DEFAULT_BUFFER_SIZE = 100;

    protected final ExtractorServiceInterface extractorService;
    private final IndexServiceInterface indexServiceInterface;
    private final String indexName;
    private final int bufferSize;
    private final List<CrawlerResult> buffer;

    private CrawlerCollector(final Attributes attributes, final CrawlDefinition<?> crawlDefinition) {
        extractorService = attributes.getInstance(Components.EXTRACTOR_SERVICE_ATTRIBUTE, ExtractorServiceInterface.class);
        indexServiceInterface = attributes.getInstance(Components.INDEX_SERVICE_ATTRIBUTE, IndexServiceInterface.class);
        final Variables variables = new Variables(crawlDefinition.getVariables());
        if (variables.index == null) {
            throw new NotAcceptableException("The \"index\" variable is missing");
        }
        indexName = variables.index;
        bufferSize = variables.buffer == null ? DEFAULT_BUFFER_SIZE : variables.buffer;
        if (bufferSize < 1 || bufferSize > 10000)
            throw new NotAcceptableException("The \"buffer\" value should be between 1 and 10,0000. Actually: " + bufferSize);
        buffer = new ArrayList<>();
    }

    private void flush() {
        if (buffer.isEmpty())
            return;
        try {
            final byte[] bytes = ObjectMappers.SMILE.writeValueAsBytes(buffer);
            final JsonNode jsonNode = ObjectMappers.SMILE.readTree(bytes);
            indexServiceInterface.postJson(indexName, false, jsonNode);
            buffer.clear();
        } catch (final IOException e) {
            throw new InternalServerErrorException("Error while converting json: " + e.getMessage(), e);
        }
    }

    protected void index(final CrawlerResult crawlerResult) {
        buffer.add(crawlerResult);
        if (buffer.size() >= bufferSize)
            flush();
    }

    @Override
    final public void done() {
        flush();
    }

    public static class WebCollector extends CrawlerCollector<WebCrawlItem> {

        private WebCollector(final Attributes attributes, final WebCrawlDefinition crawlDefinition) {
            super(attributes, crawlDefinition);
        }

        public void collect(final WebCrawlItem crawlItem) {
            ParserResult parserResult = null;
            Exception parsingError = null;
            try {
                final DriverInterface.Body body = crawlItem.getBody();
                if (body != null) {
                    final DriverInterface.Content content = body.getContent();
                    if (content != null) {
                        try (final InputStream inputStream = content.getInput()) {
                            final MediaType mediaType = MediaType.valueOf(content.getContentType());
                            parserResult = extractorService.extract(null, inputStream, mediaType);
                        }
                    }
                }
            } catch (Exception e) {
                parsingError = e;
            }
            index(new CrawlerResult(crawlItem, parserResult, parsingError));
        }

    }

    public static class Web implements WebCrawlCollectorFactory {

        @Override
        public @NotNull CrawlCollector<WebCrawlItem> createCrawlCollector(final Attributes attributes,
                                                                          final WebCrawlDefinition crawlDefinition) {
            return new WebCollector(attributes, crawlDefinition);
        }
    }

}
