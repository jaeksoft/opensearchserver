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
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.qwazr.crawler.web.WebCrawlDefinition;

import java.util.LinkedHashMap;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.PUBLIC_ONLY,
		getterVisibility = JsonAutoDetect.Visibility.NONE,
		setterVisibility = JsonAutoDetect.Visibility.NONE)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class WebCrawlRecord {

	public final LinkedHashMap<String, WebCrawlDefinition> crawls;

	public static final TypeReference<LinkedHashMap<String, WebCrawlDefinition>> TYPE_REFERENCE =
			new TypeReference<LinkedHashMap<String, WebCrawlDefinition>>() {
			};

	@JsonCreator
	WebCrawlRecord(final LinkedHashMap<String, WebCrawlDefinition> crawls) {
		this.crawls = crawls;
	}

	WebCrawlRecord(final Builder builder) {
		if (builder.crawls == null) {
			crawls = null;
		} else {
			crawls = new LinkedHashMap<>();
			builder.crawls.forEach((name, webCrawlBuilder) -> crawls.put(name, webCrawlBuilder.build()));
		}
	}

	public static Builder of() {
		return new Builder();
	}

	public static Builder of(final WebCrawlRecord record) {
		final Builder builder = new Builder();
		if (record != null && record.crawls != null)
			record.crawls.forEach(builder::set);
		return builder;
	}

	public static class Builder {

		private LinkedHashMap<String, WebCrawlDefinition.Builder> crawls;

		public WebCrawlDefinition.Builder set(final String name, final WebCrawlDefinition.Builder builder) {
			if (crawls == null)
				crawls = new LinkedHashMap<>();
			crawls.put(name, builder);
			return builder;
		}

		public WebCrawlDefinition.Builder set(final String name, final WebCrawlDefinition webCrawl) {
			return set(name, WebCrawlDefinition.of(webCrawl));
		}

		public WebCrawlDefinition.Builder set(final String name) {
			if (crawls == null)
				crawls = new LinkedHashMap<>();
			return crawls.computeIfAbsent(name, n -> WebCrawlDefinition.of());
		}
	}
}
