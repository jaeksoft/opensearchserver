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

package com.jaeksoft.opensearchserver.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.qwazr.crawler.web.WebCrawlDefinition;

import java.util.Objects;
import java.util.UUID;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.PUBLIC_ONLY,
    getterVisibility = JsonAutoDetect.Visibility.NONE,
    setterVisibility = JsonAutoDetect.Visibility.NONE,
    creatorVisibility = JsonAutoDetect.Visibility.NONE)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeName(WebCrawlTaskDefinition.TYPE)
public class WebCrawlTaskDefinition extends CrawlTaskDefinition<WebCrawlDefinition> {

    public final static String TYPE = "web";

    public final Boolean deleteOlderSession;

    @JsonCreator
    WebCrawlTaskDefinition(@JsonProperty("id") UUID id, @JsonProperty("indexUuid") UUID indexUuid,
        @JsonProperty("crawlDefinition") WebCrawlDefinition crawlDefinition,
        @JsonProperty("deleteOlderSession") Boolean deleteOlderSession) {
        super(id, TYPE, indexUuid, crawlDefinition);
        this.deleteOlderSession = deleteOlderSession;
    }

    public WebCrawlTaskDefinition(final WebCrawlRecord webCrawlRecord, final UUID indexUuid) {
        this(webCrawlRecord.getUuid(), indexUuid, webCrawlRecord.getCrawlDefinition(),
            webCrawlRecord.isDeleteOlderSession());
    }

    public boolean getDeleteOlderSession() {
        return deleteOlderSession == null || deleteOlderSession;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, type, indexUuid, deleteOlderSession);
    }

    @Override
    public boolean equals(final Object o) {
        return super.equals(o) &&
            (o == this || Objects.equals(deleteOlderSession, ((WebCrawlTaskDefinition) o).deleteOlderSession));
    }

}
