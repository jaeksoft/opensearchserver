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

public abstract class CrawlTaskRecord<T extends CrawlDefinition> extends TaskRecord {

	public final UUID crawlUuid;

	public final UUID indexUuid;

	public final T crawlDefinition;

	protected CrawlTaskRecord(final UUID uuid, final UUID crawlUuid, final UUID indexUuid, final T crawlDefinition) {
		super(uuid);
		this.crawlUuid = crawlUuid;
		this.indexUuid = indexUuid;
		this.crawlDefinition = crawlDefinition;
	}

	CrawlTaskRecord(final BaseBuilder<T, ?> builder) {
		super(builder.uuid);
		crawlUuid = builder.crawlUuid;
		indexUuid = builder.indexUuid;
		crawlDefinition = builder.crawlDefinition;
	}

	@Override
	public boolean equals(final Object o) {
		if (o == null || !(o instanceof CrawlTaskRecord))
			return false;
		if (o == this)
			return true;
		final CrawlTaskRecord r = (CrawlTaskRecord) o;
		return Objects.equals(uuid, r.uuid) && Objects.equals(crawlUuid, r.crawlUuid) &&
				Objects.equals(indexUuid, r.indexUuid) && Objects.equals(crawlDefinition, r.crawlDefinition);
	}

	public abstract BaseBuilder<T, ?> from();

	public static abstract class BaseBuilder<T extends CrawlDefinition, R extends CrawlTaskRecord> {

		private final UUID uuid;
		private final UUID crawlUuid;
		private final UUID indexUuid;
		private final T crawlDefinition;

		protected BaseBuilder(UUID uuid, UUID crawlUuid, UUID indexUuid, T crawlDefinition) {
			this.uuid = uuid;
			this.crawlUuid = crawlUuid;
			this.indexUuid = indexUuid;
			this.crawlDefinition = crawlDefinition;
		}

		public abstract R build();
	}
}
