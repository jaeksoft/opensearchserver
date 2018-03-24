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
import com.qwazr.utils.LoggerUtils;
import com.qwazr.utils.StringUtils;

import javax.xml.bind.DatatypeConverter;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

abstract class SemanticExtractor {

	final static Logger LOGGER = LoggerUtils.getLogger(SemanticExtractor.class);

	private final static DateTimeFormatter DateFormats1 = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssX");
	private final static DateTimeFormatter DateFormats2 = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX");
	private final static DateTimeFormatter DateFormats3 = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
	private final static DateTimeFormatter DateFormats4 = DateTimeFormatter.ofPattern("yyyy-MM-dd");

	final static Collection<Function<String, Long>> DateParsers = new ArrayList<>();

	static {
		DateParsers.add(s -> DatatypeConverter.parseDateTime(s).toInstant().getEpochSecond());
		DateParsers.add(s -> LocalDateTime.parse(s, DateFormats3).toEpochSecond(ZoneOffset.UTC));
		DateParsers.add(s -> OffsetDateTime.parse(s, DateFormats1).toEpochSecond());
		DateParsers.add(s -> OffsetDateTime.parse(s, DateFormats2).toEpochSecond());
		DateParsers.add(s -> LocalDate.parse(s, DateFormats4).atTime(0, 0).toEpochSecond(ZoneOffset.UTC));
	}

	static void extractImage(final String imageUri, final UrlRecord.Builder urlBuilder) {
		if (imageUri == null || imageUri.isEmpty())
			return;
		try {
			final URI uri = new URI(imageUri);
			urlBuilder.imageUri(uri);
		} catch (URISyntaxException e) {
			LOGGER.log(Level.FINE, "URI error on " + imageUri, e);
		}
	}

	static Long asDateLong(final String dateString) {
		if (StringUtils.isBlank(dateString))
			return null;
		Exception dateTimeParseException = null;
		for (final Function<String, Long> dateParser : DateParsers) {
			try {
				return dateParser.apply(dateString);
			} catch (DateTimeParseException | IllegalArgumentException e) {
				dateTimeParseException = e;
			}
		}
		if (dateTimeParseException != null)
			LOGGER.log(Level.FINE, dateTimeParseException.getMessage(), dateTimeParseException);
		return null;
	}

}
