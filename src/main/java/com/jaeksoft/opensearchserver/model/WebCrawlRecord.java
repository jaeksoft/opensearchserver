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
import com.qwazr.crawler.web.WebCrawlDefinition;
import com.qwazr.utils.HashUtils;

import java.util.Objects;
import java.util.UUID;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.PUBLIC_ONLY,
		getterVisibility = JsonAutoDetect.Visibility.NONE,
		setterVisibility = JsonAutoDetect.Visibility.NONE,
		creatorVisibility = JsonAutoDetect.Visibility.NONE)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class WebCrawlRecord {

	public final UUID uuid;

	public final String name;

	public final WebCrawlDefinition crawlDefinition;

	public final Boolean deleteOlderSession;

	@JsonCreator
	WebCrawlRecord(@JsonProperty("uuid") final UUID uuid, @JsonProperty("name") final String name,
			@JsonProperty("crawlDefinition") final WebCrawlDefinition crawlDefinition,
			@JsonProperty("deleteOlderSession") final Boolean deleteOlderSession) {
		this.uuid = uuid;
		this.name = name;
		this.crawlDefinition = crawlDefinition;
		this.deleteOlderSession = deleteOlderSession == null || deleteOlderSession;
	}

	WebCrawlRecord(final Builder builder) {
		uuid = builder.uuid;
		name = builder.name;
		crawlDefinition = builder.crawlDefinition;
		deleteOlderSession = builder.deleteOlderSession == null || builder.deleteOlderSession;
	}

	public UUID getUuid() {
		return uuid;
	}

	public String getName() {
		return name;
	}

	public WebCrawlDefinition getCrawlDefinition() {
		return crawlDefinition;
	}

	public boolean isDeleteOlderSession() {
		return deleteOlderSession;
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof WebCrawlRecord))
			return false;
		if (o == this)
			return true;
		final WebCrawlRecord w = (WebCrawlRecord) o;
		return Objects.equals(uuid, w.uuid) && Objects.equals(name, w.name) &&
				Objects.equals(crawlDefinition, w.crawlDefinition) &&
				Objects.equals(deleteOlderSession, w.deleteOlderSession);
	}

	/**
	 * Create a new builder with a new UUID
	 *
	 * @return a new WebCrawlRecord builder
	 */
	public static Builder of() {
		return new Builder(HashUtils.newTimeBasedUUID());
	}

	/**
	 * Create a builder that can be used to update the given WebCrawlRecord.
	 * The builder is initially filled with all the data from the given WebCrawlRecord.
	 *
	 * @return a new WebCrawlRecord builder
	 */
	public Builder from() {
		return new Builder(uuid).name(name).crawlDefinition(crawlDefinition).deleteOlderSession(deleteOlderSession);
	}

	public static class Builder {

		private final UUID uuid;
		private String name;
		private WebCrawlDefinition crawlDefinition;
		private Boolean deleteOlderSession;

		private Builder(final UUID uuid) {
			this.uuid = uuid;
		}

		public Builder name(final String name) {
			this.name = name;
			return this;
		}

		public Builder crawlDefinition(final WebCrawlDefinition crawlDefinition) {
			this.crawlDefinition = crawlDefinition;
			return this;
		}

		public Builder deleteOlderSession(final Boolean deleteOlderSession) {
			this.deleteOlderSession = deleteOlderSession;
			return this;
		}

		public WebCrawlRecord build() {
			return new WebCrawlRecord(this);
		}

	}
}
