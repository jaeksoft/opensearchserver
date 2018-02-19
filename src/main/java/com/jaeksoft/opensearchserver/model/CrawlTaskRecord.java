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

public abstract class CrawlTaskRecord<D extends CrawlDefinition> extends TaskRecord {

	public final UUID crawlUuid;

	public final UUID indexUuid;

	public final D crawlDefinition;

	protected CrawlTaskRecord(final UUID crawlUuid, final UUID indexUuid, final D crawlDefinition,
			final Long creationTime, final Status status, final Long statusTime) {
		super(taskId(crawlUuid, indexUuid), creationTime, status, statusTime);
		this.crawlUuid = crawlUuid;
		this.indexUuid = indexUuid;
		this.crawlDefinition = crawlDefinition;
	}

	CrawlTaskRecord(final BaseBuilder<D, ?, ?> builder) {
		super(builder);
		this.crawlUuid = builder.crawlUuid;
		this.indexUuid = builder.indexUuid;
		this.crawlDefinition = builder.crawlDefinition;
	}

	static String taskId(final UUID crawlUuid, final UUID indexUuid) {
		return crawlUuid.toString() + '_' + indexUuid.toString();
	}

	@Override
	public boolean equals(final Object o) {
		if (!super.equals(o))
			return false;
		if (!(o instanceof CrawlTaskRecord))
			return false;
		if (o == this)
			return true;
		final CrawlTaskRecord r = (CrawlTaskRecord) o;
		return Objects.equals(crawlUuid, r.crawlUuid) && Objects.equals(indexUuid, r.indexUuid) &&
				Objects.equals(crawlDefinition, r.crawlDefinition);
	}
	
	public static abstract class BaseBuilder<D extends CrawlDefinition, R extends CrawlTaskRecord, B extends BaseBuilder<D, R, B>>
			extends TaskBuilder<R, B> {

		private final UUID crawlUuid;
		private final UUID indexUuid;
		private final D crawlDefinition;

		/**
		 * Constructor used to update a task
		 *
		 * @param builderClass
		 * @param crawlTaskRecord
		 */
		protected BaseBuilder(final Class<B> builderClass, final CrawlTaskRecord<D> crawlTaskRecord) {
			super(builderClass, crawlTaskRecord);
			this.crawlUuid = crawlTaskRecord.crawlUuid;
			this.indexUuid = crawlTaskRecord.indexUuid;
			this.crawlDefinition = crawlTaskRecord.crawlDefinition;
		}

		/**
		 * Constructor to use for a new task
		 *
		 * @param builderClass
		 * @param crawlUuid
		 * @param indexUuid
		 * @param crawlDefinition
		 */
		protected BaseBuilder(final Class<B> builderClass, final UUID crawlUuid, final UUID indexUuid,
				final D crawlDefinition) {
			super(builderClass, taskId(crawlUuid, indexUuid));
			this.crawlUuid = crawlUuid;
			this.indexUuid = indexUuid;
			this.crawlDefinition = crawlDefinition;
		}

		public abstract R build();
	}
}
