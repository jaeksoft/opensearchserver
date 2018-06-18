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

package com.jaeksoft.opensearchserver.crawler.web;

import com.jaeksoft.opensearchserver.crawler.SessionStore;
import com.qwazr.crawler.web.WebCrawlDefinition;
import com.qwazr.utils.LoggerUtils;

import java.util.UUID;
import java.util.logging.Logger;

public class WebBeforeSession extends WebAbstractEvent {

    private final static Logger LOGGER = LoggerUtils.getLogger(WebBeforeSession.class);

    @Override
    public boolean run(final EventContext context) throws Exception {

        final WebCrawlDefinition crawlDefinition = context.crawlSession.getCrawlDefinition();
        LOGGER.info("Crawl entry: " + crawlDefinition.entryUrl + " - URLs: " +
            (crawlDefinition.urls == null ? 0 : crawlDefinition.urls.size()));

        final String crawlUuid = context.crawlSession.getVariable(CRAWL_UUID, String.class);
        final String taskCreationTime = context.crawlSession.getVariable(SESSION_TIME_ID, String.class);
        context.crawlSession.setAttribute(SESSION_STORE,
            new SessionStore(UUID.fromString(crawlUuid), Long.parseLong(taskCreationTime)), SessionStore.class);

        return true;
    }

}
