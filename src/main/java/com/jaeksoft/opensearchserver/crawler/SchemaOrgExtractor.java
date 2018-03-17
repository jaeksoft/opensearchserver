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

import com.fasterxml.jackson.databind.JsonNode;
import com.jaeksoft.opensearchserver.model.UrlRecord;
import com.qwazr.extractor.ParserResult;
import com.qwazr.utils.LoggerUtils;
import com.qwazr.utils.ObjectMappers;
import com.qwazr.utils.StringUtils;

import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
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
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

class SchemaOrgExtractor {

	private final static Logger LOGGER = LoggerUtils.getLogger(SchemaOrgExtractor.class);

	private final static DateTimeFormatter DateFormats1 = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssX");
	private final static DateTimeFormatter DateFormats2 = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX");
	private final static DateTimeFormatter DateFormats3 = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
	private final static DateTimeFormatter DateFormats4 = DateTimeFormatter.ofPattern("yyyy-MM-dd");

	private final static Collection<Function<String, Long>> DateParsers = new ArrayList<>();

	static {
		DateParsers.add(s -> DatatypeConverter.parseDateTime(s).toInstant().getEpochSecond());
		DateParsers.add(s -> LocalDateTime.parse(s, DateFormats3).toEpochSecond(ZoneOffset.UTC));
		DateParsers.add(s -> OffsetDateTime.parse(s, DateFormats1).toEpochSecond());
		DateParsers.add(s -> OffsetDateTime.parse(s, DateFormats2).toEpochSecond());
		DateParsers.add(s -> LocalDate.parse(s, DateFormats4).atTime(0, 0).toEpochSecond(ZoneOffset.UTC));
	}

	static void extract(final ParserResult parserResult, final UrlRecord.Builder urlBuilder) {
		if (parserResult.documents == null)
			return;
		parserResult.documents.forEach(document -> {
			if (document == null)
				return;
			final Map<String, List<String>> selectors = (Map<String, List<String>>) document.get("selectors");
			if (selectors == null)
				return;
			final List<String> scriptsLdJson = selectors.get("script_ldjson");
			if (scriptsLdJson == null || scriptsLdJson.isEmpty())
				return;
			scriptsLdJson.forEach(scriptLdJson -> extractSchemaOrg(scriptLdJson, urlBuilder));
		});
	}

	private static void extractSchemaOrg(final String scriptLdJson, final UrlRecord.Builder urlBuilder) {
		try {
			if (scriptLdJson == null || scriptLdJson.isEmpty())
				return;
			final JsonNode jsonNode = ObjectMappers.JSON.readTree(scriptLdJson);
			if (jsonNode == null)
				return;
			final String context = asText(jsonNode.get("@context"));
			if (!"http://schema.org".equals(context) && !"http://schema.org/".equals(context))
				return;
			final String type = asText(jsonNode.get("@type"));
			if (type == null)
				return;
			urlBuilder.schemaOrgType(type);
			switch (type) {
			case "Product":
				extractSchemaOrgProduct(jsonNode, urlBuilder);
				break;
			case "Article":
			case "NewsArticle":
			case "Blog":
			case "Report":
			case "TechArticle":
			case "APIReference":
			case "WebPage":
			case "WebSite":
				extractSchemaOrgCreativeWork(jsonNode, urlBuilder);
				break;
			}
		} catch (IOException e) {
			LOGGER.log(Level.FINE, e.getMessage(), e);
		}
	}

	private static void extractImage(final String imageUri, final UrlRecord.Builder urlBuilder) {
		if (imageUri == null || imageUri.isEmpty())
			return;
		try {
			final URI uri = new URI(imageUri);
			urlBuilder.imageUri(uri);
		} catch (URISyntaxException e) {
			LOGGER.log(Level.FINE, "URI error on " + imageUri, e);
		}
	}

	private static void extractSchemaOrgImage(final JsonNode image, final UrlRecord.Builder urlBuilder) {
		if (image == null)
			return;
		if (image.isTextual())
			extractImage(image.asText(), urlBuilder);
		else if (image.isObject())
			extractImage(asText(image.get("url")), urlBuilder);
	}

	private static void extractSchemaOrgPublisher(final JsonNode publisher, final UrlRecord.Builder builder) {
		if (publisher == null)
			return;
		final String type = asText(publisher.get("@type"));
		if (type == null)
			return;
		switch (type) {
		case "Organization":
			builder.organizationName(asText(publisher.get("name")));
			break;
		}
	}

	private static String asText(JsonNode node) {
		return node == null ? null : node.asText();
	}

	private static Double asDouble(JsonNode node) {
		return node == null ? null : node.asDouble();
	}

	private static Long asDateLong(final String dateString) {
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
			LOGGER.log(Level.WARNING, dateTimeParseException.getMessage(), dateTimeParseException);
		return null;
	}

	private static void extractShemaOrgThings(final JsonNode thing, final UrlRecord.Builder urlBuilder) {
		urlBuilder.title(asText(thing.get("name")));
		urlBuilder.description(asText(thing.get("description")));
		extractSchemaOrgImage(thing.get("image"), urlBuilder);
	}

	private static void extractSchemaOrgOffers(final JsonNode offers, final UrlRecord.Builder urlBuilder) {
		if (offers == null || !offers.isArray())
			return;
		for (JsonNode offer : offers)
			urlBuilder.lowestPrice(asText(offer.get("priceCurrency")), asDouble(offer.get("price")));
	}

	private static void extractSchemaOrgProduct(final JsonNode jsonNode, final UrlRecord.Builder urlBuilder) {
		extractShemaOrgThings(jsonNode, urlBuilder);
		urlBuilder.gtin13(asText(jsonNode.get("gtin13")));
		extractSchemaOrgOffers(jsonNode.get("offers"), urlBuilder);
	}

	private static void extractSchemaOrgCreativeWork(final JsonNode jsonNode, final UrlRecord.Builder urlBuilder) {
		extractShemaOrgThings(jsonNode, urlBuilder);
		urlBuilder.title(asText(jsonNode.get("headline")));
		extractSchemaOrgImage(jsonNode.get("thumbnailUrl"), urlBuilder);
		urlBuilder.datePublished(asDateLong(asText(jsonNode.get("datePublished"))));
		extractSchemaOrgPublisher(jsonNode.get("publisher"), urlBuilder);
	}

}
