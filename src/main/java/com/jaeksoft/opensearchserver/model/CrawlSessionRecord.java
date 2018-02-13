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
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.qwazr.crawler.common.CrawlDefinition;
import com.qwazr.utils.HashUtils;

import java.util.UUID;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.PUBLIC_ONLY,
		getterVisibility = JsonAutoDetect.Visibility.NONE,
		setterVisibility = JsonAutoDetect.Visibility.NONE)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CrawlSessionRecord {
	
	public final UUID uuid;

	public final UUID crawlRecordUuid;

	public final CrawlDefinition crawlDefinition;

	CrawlSessionRecord() {
		uuid = null;
		crawlRecordUuid = null;
		crawlDefinition = null;
	}

	CrawlSessionRecord(Builder builder) {
		uuid = builder.uuid;
		crawlRecordUuid = builder.crawlRecordUuid;
		crawlDefinition = builder.crawlDefinition;
	}

	public Builder from() {
		return new Builder(uuid, crawlRecordUuid, crawlDefinition);
	}

	public static Builder of(WebCrawlRecord record) {
		return new Builder(HashUtils.newTimeBasedUUID(), record.getUuid(), record.crawlDefinition);
	}

	public static class Builder {

		private final UUID uuid;
		private final UUID crawlRecordUuid;
		private final CrawlDefinition crawlDefinition;

		private Builder(UUID uuid, UUID crawlRecordUuid, CrawlDefinition crawlDefinition) {
			this.uuid = uuid;
			this.crawlRecordUuid = crawlRecordUuid;
			this.crawlDefinition = crawlDefinition;
		}

		public CrawlSessionRecord build() {
			return new CrawlSessionRecord(this);
		}
	}
}
