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
import com.qwazr.crawler.web.WebCurrentCrawl;
import com.qwazr.extractor.ExtractorServiceInterface;
import com.qwazr.extractor.ParserResult;
import com.qwazr.extractor.UriInfoImpl;
import com.qwazr.server.ServerException;
import com.qwazr.utils.LinkUtils;
import com.qwazr.utils.LoggerUtils;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ExtractorIndexer {

	private final static Logger LOGGER = LoggerUtils.getLogger(ExtractorIndexer.class);

	private final ExtractorServiceInterface extractorService;
	private final UriInfo htmlParserParameters;

	ExtractorIndexer(ExtractorServiceInterface extractorService) throws URISyntaxException {
		this.extractorService = extractorService;
		htmlParserParameters = new UriInfoImpl(new URI("http://localhost:9090"), UriBuilder.fromPath("")
				.queryParam("xpath_name", "script_ldjson")
				.queryParam("xpath", LinkUtils.urlEncode("//script[@type='application/ld+json']"))
				.queryParam("title", "1")
				.queryParam("content", "1")
				.build());
	}

	public void extract(final WebCurrentCrawl currentCrawl, final UrlRecord.Builder urlBuilder) throws IOException {

		final String contentType = currentCrawl.getContentType();
		UriInfo uriInfoParameters = null;
		try {
			final MediaType mediaType = MediaType.valueOf(contentType);
			if (mediaType.isCompatible(MediaType.TEXT_HTML_TYPE))
				uriInfoParameters = htmlParserParameters;
		} catch (IllegalArgumentException e) {
			LOGGER.log(Level.WARNING, e, () -> "Bad content type: " + contentType);
		}

		// Extract indexable data
		try (final InputStream inputStream = currentCrawl.getBody().getContent().getInput()) {

			final ParserResult parserResult =
					extractorService.putMagic(uriInfoParameters, null, null, contentType, inputStream);
			if (parserResult != null && parserResult.documents != null) {
				final Object language = parserResult.getDocumentFieldValue(0, "lang_detection", 0);
				final Language foundLanguage = Language.find(language, Language.en);
				if (foundLanguage != null)
					urlBuilder.lang(foundLanguage);
				urlBuilder.title(parserResult.getDocumentFieldValue(0, "title", 0), foundLanguage);
				parserResult.documents.forEach(fields -> {
					final Object contentLang = fields.get("lang_detection");
					urlBuilder.contentObject(fields.get("content"), Language.find(contentLang, Language.en));
				});
				SchemaOrgExtractor.extract(parserResult, urlBuilder);
			}

		} catch (WebApplicationException | ServerException e) {
			LOGGER.log(Level.WARNING, "Parsing failed with " + contentType + " on " + currentCrawl.getUri(), e);
		}

	}
}
