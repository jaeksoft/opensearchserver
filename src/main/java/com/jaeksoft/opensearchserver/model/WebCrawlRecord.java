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
import com.fasterxml.jackson.core.type.TypeReference;
import com.qwazr.crawler.web.WebCrawlDefinition;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.PUBLIC_ONLY,
		getterVisibility = JsonAutoDetect.Visibility.NONE,
		setterVisibility = JsonAutoDetect.Visibility.NONE)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class WebCrawlRecord {

	public final UUID uuid;

	public final String name;

	public final WebCrawlDefinition crawlDefinition;

	@JsonCreator
	WebCrawlRecord(@JsonProperty("uuid") final UUID uuid, @JsonProperty("name") final String name,
			@JsonProperty("crawlDefinition") final WebCrawlDefinition crawlDefinition) {
		this.uuid = uuid;
		this.name = name;
		this.crawlDefinition = crawlDefinition;
	}

	WebCrawlRecord(final Builder builder) {
		uuid = builder.uuid;
		name = builder.name;
		crawlDefinition = builder.crawlDefinition;
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

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof WebCrawlRecord))
			return false;
		if (o == this)
			return true;
		final WebCrawlRecord w = (WebCrawlRecord) o;
		return Objects.equals(uuid, w.uuid) && Objects.equals(name, w.name) &&
				Objects.equals(crawlDefinition, w.crawlDefinition);
	}

	public static Builder of(final UUID uuid) {
		return new Builder(uuid);
	}

	public static Builder of(final WebCrawlRecord record) {
		return new Builder(record.uuid).name(record.name).crawlDefinition(record.crawlDefinition);
	}

	public static final TypeReference<List<WebCrawlRecord>> TYPE_WEBCRAWLS = new TypeReference<List<WebCrawlRecord>>() {
	};

	public static class Builder {

		private final UUID uuid;
		private String name;
		private WebCrawlDefinition crawlDefinition;

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

		public WebCrawlRecord build() {
			return new WebCrawlRecord(this);
		}

	}
}
