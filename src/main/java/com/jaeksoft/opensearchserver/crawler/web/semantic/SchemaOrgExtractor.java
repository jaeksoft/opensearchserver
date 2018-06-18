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

import com.fasterxml.jackson.databind.JsonNode;
import com.jaeksoft.opensearchserver.model.UrlRecord;
import com.qwazr.extractor.ParserResult;
import com.qwazr.utils.LoggerUtils;
import com.qwazr.utils.ObjectMappers;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SchemaOrgExtractor {

    public static final String SELECTOR_NAME = "script_ldjson";
    public static final String SELECTOR_XPATH = "//script[@type='application/ld+json']";

    private final static Logger LOGGER = LoggerUtils.getLogger(SchemaOrgExtractor.class);

    public static boolean extract(final ParserResult parserResult, final UrlRecord.Builder urlBuilder) {
        if (parserResult.documents == null)
            return false;
        boolean found = false;
        for (final Map<String, Object> document : parserResult.documents) {
            if (document == null)
                continue;
            final Map<String, List<String>> selectors = (Map<String, List<String>>) document.get("selectors");
            if (selectors == null)
                continue;
            final List<String> scriptsLdJson = selectors.get(SELECTOR_NAME);
            if (scriptsLdJson == null || scriptsLdJson.isEmpty())
                continue;
            for (final String scriptLdJson : scriptsLdJson)
                if (extractSchemaOrg(scriptLdJson, urlBuilder))
                    found = true;
        }
        return found;
    }

    private static boolean extractSchemaOrg(final String scriptLdJson, final UrlRecord.Builder urlBuilder) {
        try {
            if (scriptLdJson == null || scriptLdJson.isEmpty())
                return false;
            final JsonNode jsonNode = ObjectMappers.JSON.readTree(scriptLdJson);
            if (jsonNode == null || jsonNode.isNull())
                return false;
            final String context = asText(jsonNode.get("@context"));
            if (!"http://schema.org".equals(context) && !"http://schema.org/".equals(context))
                return false;
            final String type = asText(jsonNode.get("@type"));
            if (type == null)
                return false;
            urlBuilder.schemaOrgType(type);
            switch (type) {
            case "Product":
                extractSchemaOrgProduct(jsonNode, urlBuilder);
                return true;
            case "Article":
            case "NewsArticle":
            case "Blog":
            case "Report":
            case "TechArticle":
            case "APIReference":
            case "WebPage":
            case "WebSite":
                extractSchemaOrgCreativeWork(jsonNode, urlBuilder);
                return true;
            }
        } catch (IOException e) {
            LOGGER.log(Level.FINE, e.getMessage(), e);
        }
        return false;
    }

    private static void extractSchemaOrgImage(final JsonNode image, final UrlRecord.Builder urlBuilder) {
        if (image == null || image.isNull())
            return;
        if (image.isTextual())
            SemanticExtractor.extractImage(image.asText(), urlBuilder);
        else if (image.isObject())
            SemanticExtractor.extractImage(asText(image.get("url")), urlBuilder);
    }

    private static void extractSchemaOrgPublisher(final JsonNode publisher, final UrlRecord.Builder builder) {
        if (publisher == null || publisher.isNull())
            return;
        final String type = asText(publisher.get("@type"));
        if (type == null)
            return;
        switch (type) {
        case "Organization":
            builder.organizationName(asText(publisher.get("name")));
            break;
        default:
            break;
        }
    }

    private static String asText(JsonNode node) {
        return node == null || node.isNull() ? null : node.asText();
    }

    private static Double asDouble(JsonNode node) {
        return node == null || node.isNull() ? null : node.asDouble();
    }

    private static void extractShemaOrgThings(final JsonNode thing, final UrlRecord.Builder urlBuilder) {
        urlBuilder.title(asText(thing.get("name")));
        urlBuilder.description(asText(thing.get("description")));
        extractSchemaOrgImage(thing.get("image"), urlBuilder);
    }

    private static void extractSchemaOrgOffers(final JsonNode offers, final UrlRecord.Builder urlBuilder) {
        if (offers == null || offers.isNull() || !offers.isArray())
            return;
        for (final JsonNode offer : offers)
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
        urlBuilder.datePublished(SemanticExtractor.asDateLong(asText(jsonNode.get("datePublished"))));
        extractSchemaOrgPublisher(jsonNode.get("publisher"), urlBuilder);
    }

}
