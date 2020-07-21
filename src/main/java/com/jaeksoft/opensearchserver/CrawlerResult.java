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

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.qwazr.crawler.web.WebCrawlItem;
import com.qwazr.extractor.ParserResult;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonAutoDetect(setterVisibility = JsonAutoDetect.Visibility.NONE,
    getterVisibility = JsonAutoDetect.Visibility.NONE,
    creatorVisibility = JsonAutoDetect.Visibility.NONE,
    isGetterVisibility = JsonAutoDetect.Visibility.NONE,
    fieldVisibility = JsonAutoDetect.Visibility.PUBLIC_ONLY)
public class CrawlerResult {

    public final String url;
    public final Integer depth;
    public final String contentType;
    public final Integer statusCode;
    public final Map<String, Object> metas;
    public final List<LinkedHashMap<String, Object>> documents;
    public final String crawlingError;
    public final String parsingError;

    CrawlerResult(final WebCrawlItem webCrawlItem,
                  final ParserResult parserResult,
                  final Exception parsingException) {
        url = webCrawlItem.getItem().toString();
        depth = webCrawlItem.getDepth();
        contentType = webCrawlItem.getContentType();
        statusCode = webCrawlItem.getStatusCode();
        crawlingError = webCrawlItem.getError();
        parsingError = parsingException == null ? null : parsingException.getMessage();
        if (parserResult != null) {
            metas = parserResult.metas;
            documents = parserResult.documents;
        } else {
            metas = null;
            documents = null;
        }
    }

}
