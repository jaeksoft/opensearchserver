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

import com.qwazr.crawler.common.CrawlDefinition;

import java.util.Objects;
import java.util.UUID;

public abstract class CrawlTaskDefinition<D extends CrawlDefinition> extends TaskDefinition {

    public final UUID indexUuid;

    public final D crawlDefinition;

    protected CrawlTaskDefinition(final UUID id, final String type, final UUID indexUuid, final D crawlDefinition) {
        super(id, type);
        this.indexUuid = indexUuid;
        this.crawlDefinition = crawlDefinition;
    }

    private volatile String taskId;

    @Override
    public String getTaskId() {
        if (taskId == null && id != null && indexUuid != null)
            taskId = id.toString() + "_" + indexUuid.toString();
        return taskId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, type, indexUuid);
    }

    @Override
    public boolean equals(final Object o) {
        if (!super.equals(o))
            return false;
        if (!(o instanceof CrawlTaskDefinition))
            return false;
        if (o == this)
            return true;
        final CrawlTaskDefinition r = (CrawlTaskDefinition) o;
        return Objects.equals(indexUuid, r.indexUuid) && Objects.equals(crawlDefinition, r.crawlDefinition);
    }

}
