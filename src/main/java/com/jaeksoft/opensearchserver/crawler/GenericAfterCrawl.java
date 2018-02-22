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

package com.jaeksoft.opensearchserver.crawler;

import com.jaeksoft.opensearchserver.model.Language;
import com.jaeksoft.opensearchserver.model.UrlRecord;
import com.qwazr.extractor.ParserResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.net.URI;
import java.util.Set;

public class GenericAfterCrawl extends AbstractEvent {

	private static final Logger LOGGER = LoggerFactory.getLogger(GenericAfterCrawl.class);

	@Override
	public boolean run(final EventContext context) throws Exception {

		final UrlRecord.Builder urlBuilder = (UrlRecord.Builder) context.currentSession.getVariable(URLRECORD_BUILDER);
		if (urlBuilder == null)
			throw new Exception("Missing session variable: " + URLRECORD_BUILDER);

		if (context.currentURI.isIgnored())
			return true;

		urlBuilder.lastModificationTime(System.currentTimeMillis())
				.depth(context.currentURI.getDepth())
				.httpContentType(context.currentURI.getContentType())
				.httpStatus(context.currentURI.getStatusCode());

		if (context.currentURI.getRedirect() != null) {
			context.indexQueue.post(urlBuilder.build());
			return true;
		}

		if (!context.currentURI.isCrawled()) {
			context.indexQueue.post(urlBuilder.build());
			return true;
		}

		final String url = context.currentURI.getUri().toString();
		final int nextDepth = context.currentURI.getDepth() + 1;

		// We put links in the database
		final Set<URI> uris = context.currentURI.getFilteredLinks();

		if (uris != null) {
			for (URI uri : uris) {
				final String u = uri.toString();
				if (url.equals(u))
					continue;
				if (context.indexService.getDocument(u) == null)
					context.indexQueue.post(UrlRecord.of(uri)
							.lastModificationTime(System.currentTimeMillis())
							.depth(nextDepth)
							.build());
			}
		}

		final String contentType = context.currentURI.getContentType();

		// Extract indexable data
		try (final InputStream inputStream = context.currentURI.getBody().getContent().getInput()) {
			final ParserResult parserResult =
					CrawlerComponents.getExtractorService().putMagic(null, null, null, contentType, inputStream);
			if (parserResult != null && parserResult.documents != null) {
				final Object language = parserResult.getDocumentFieldValue(0, "lang_detection", 0);
				urlBuilder.title(parserResult.getDocumentFieldValue(0, "title", 0), Language.find(language));
				parserResult.documents.forEach(fields -> {
					final Object contentLang = fields.get("lang_detection");
					urlBuilder.contentObject(fields.get("content"), Language.find(contentLang));
				});
			}
		}

		return true;
	}

}
