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

package com.jaeksoft.opensearchserver.crawler.web.semantic;

import com.jaeksoft.opensearchserver.model.UrlRecord;
import com.qwazr.extractor.ParserResult;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class OpenGraphExtractor {

	public static final String SELECTOR_NAME_PROPERTY = "og-property";
	public static final String SELECTOR_XPATH_PROPERTY = "//meta[@property and @content]/@property";

	public static final String SELECTOR_NAME_CONTENT = "og-content";
	public static final String SELECTOR_XPATH_CONTENT = "//meta[@property and @content]/@content";

	public static boolean extract(final ParserResult parserResult, final UrlRecord.Builder urlBuilder) {
		if (parserResult.documents == null)
			return false;
		boolean found = false;
		for (final Map<String, Object> document : parserResult.documents) {
			final Map<String, Object> selectors = (Map<String, Object>) document.get("selectors");
			if (selectors == null)
				continue;
			final List<String> properties = (List<String>) selectors.get(SELECTOR_NAME_PROPERTY);
			final List<String> contents = (List<String>) selectors.get(SELECTOR_NAME_CONTENT);
			if (properties == null || contents == null)
				continue;
			if (properties.size() != contents.size())
				continue;
			final Map<String, String> openGraphMap = new HashMap<>();
			final Iterator<String> contentIterator = contents.iterator();
			for (final String property : properties) {
				final String content = contentIterator.next();
				if (property != null && content != null)
					openGraphMap.putIfAbsent(property.toLowerCase(), content);
			}
			if (extractByType(openGraphMap, urlBuilder))
				found = true;
		}
		return found;
	}

	private static boolean extractByType(final Map<String, String> openGraphMap, final UrlRecord.Builder urlBuilder) {
		final String type = openGraphMap.get("og:type");
		if (type == null)
			return false;
		switch (type.toLowerCase()) {
		case "article":
			extractArticle(openGraphMap, urlBuilder);
			return true;
		}
		return false;
	}

	private static void extractArticle(final Map<String, String> openGraphMap, final UrlRecord.Builder urlBuilder) {
		urlBuilder.schemaOrgType("article");
		urlBuilder.title(openGraphMap.get("og:title"));
		urlBuilder.description(openGraphMap.get("og:description"));
		urlBuilder.organizationName(openGraphMap.get("og:site_name"));
		SemanticExtractor.extractImage(openGraphMap.get("og:image"), urlBuilder);
		urlBuilder.datePublished(SemanticExtractor.asDateLong(openGraphMap.get("article:published_time")));
	}

}
