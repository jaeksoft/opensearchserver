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

import com.jaeksoft.opensearchserver.crawler.CrawlerComponents;
import com.jaeksoft.opensearchserver.model.Language;
import com.jaeksoft.opensearchserver.model.UrlRecord;
import com.qwazr.extractor.ParserResult;
import com.qwazr.server.ServerException;
import com.qwazr.utils.LoggerUtils;

import java.util.logging.Level;
import java.util.logging.Logger;

import java.io.InputStream;
import java.net.URI;
import java.util.Set;

public class WebAfterCrawl extends WebAbstractEvent {

	final static Logger LOGGER = LoggerUtils.getLogger(WebAfterCrawl.class);

	@Override
	protected boolean run(final EventContext context) throws Exception {

		if (context.currentCrawl.isIgnored())
			return true;

		final URI currentUri = context.currentCrawl.getUri();

		final UrlRecord.Builder urlBuilder = UrlRecord.of(currentUri)
				.crawlUuid(context.crawlUuid)
				.hostAndUrlStore(currentUri.getHost())
				.taskCreationTime(context.taskCreationTime)
				.depth(context.currentCrawl.getDepth())
				.lastModificationTime(System.currentTimeMillis())
				.depth(context.currentCrawl.getDepth())
				.httpContentType(context.currentCrawl.getContentType())
				.httpStatus(context.currentCrawl.getStatusCode())
				.crawlStatus(context.currentCrawl.getStatusCode());

		if (context.currentCrawl.getRedirect() != null) {
			context.indexQueue.post(currentUri, urlBuilder.build());
			return true;
		}

		if (!context.currentCrawl.isCrawled()) {
			context.indexQueue.post(currentUri, urlBuilder.build());
			return true;
		}

		final String currentUrl = currentUri.toString();
		final int nextDepth = context.currentCrawl.getDepth() + 1;

		// We put links in the database
		final Set<URI> uris = context.currentCrawl.getFilteredLinks();

		if (uris != null) {
			for (URI uri : uris) {
				final String url = uri.toString();
				if (currentUrl.equals(url))
					continue;
				final UrlRecord.Builder linkBuilder = UrlRecord.of(uri)
						.crawlStatus(0)
						.crawlUuid(context.crawlUuid)
						.taskCreationTime(context.taskCreationTime)
						.depth(nextDepth);
				if (!context.indexService.exists(url))
					context.indexQueue.post(uri,
							linkBuilder.hostAndUrlStore(null).lastModificationTime(System.currentTimeMillis()).build());
				else
					context.indexQueue.update(uri, linkBuilder.build());
			}
		}

		final String contentType = context.currentCrawl.getContentType();

		// Extract indexable data
		try (final InputStream inputStream = context.currentCrawl.getBody().getContent().getInput()) {
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
		} catch (ServerException e) {
			LOGGER.log(Level.WARNING, "Parsing failed with " + contentType + " on " + currentUrl, e);
		}

		context.indexQueue.post(currentUri, urlBuilder.build());
		return true;
	}

}
