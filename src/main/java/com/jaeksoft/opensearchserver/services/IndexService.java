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
package com.jaeksoft.opensearchserver.services;

import com.jaeksoft.opensearchserver.model.CrawlStatus;
import com.jaeksoft.opensearchserver.model.UrlRecord;
import com.qwazr.crawler.web.WebCrawlDefinition;
import com.qwazr.search.annotations.AnnotatedIndexService;
import com.qwazr.search.field.FieldDefinition;
import com.qwazr.search.index.HighlighterDefinition;
import com.qwazr.search.index.IndexServiceInterface;
import com.qwazr.search.index.IndexStatus;
import com.qwazr.search.index.QueryDefinition;
import com.qwazr.search.index.ResultDefinition;
import com.qwazr.search.query.BooleanQuery;
import com.qwazr.search.query.IntDocValuesExactQuery;
import com.qwazr.search.query.LongDocValuesExactQuery;
import com.qwazr.search.query.MultiFieldQuery;
import com.qwazr.search.query.QueryParserOperator;
import com.qwazr.search.query.TermQuery;
import com.qwazr.server.client.ErrorWrapper;
import com.qwazr.utils.StringUtils;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class IndexService extends UsableService {

	private final AnnotatedIndexService<UrlRecord> service;

	public IndexService(final IndexServiceInterface indexService, final String accountId, final String indexName)
			throws URISyntaxException {
		service = new AnnotatedIndexService<>(indexService, UrlRecord.class, accountId, indexName, null);
		service.createUpdateFields();
	}

	public int fillUnknownUrls(int rows, final UUID crawlUuid, final Long taskCreationTime,
			final WebCrawlDefinition.Builder crawlBuilder) {
		updateLastUse();
		final QueryDefinition queryDef = QueryDefinition.of(BooleanQuery.of(false, null)
				.addClause(BooleanQuery.Occur.filter,
						new LongDocValuesExactQuery("crawlUuidMost", crawlUuid.getMostSignificantBits()))
				.addClause(BooleanQuery.Occur.filter,
						new LongDocValuesExactQuery("crawlUuidLeast", crawlUuid.getLeastSignificantBits()))
				.addClause(BooleanQuery.Occur.filter, new LongDocValuesExactQuery("taskCreationTime", taskCreationTime))
				.addClause(BooleanQuery.Occur.filter,
						new IntDocValuesExactQuery("crawlStatus", CrawlStatus.UNKNOWN.code))
				.build())
				.returnedField("*")
				.sort("lastModificationTime", QueryDefinition.SortEnum.ascending)
				.rows(rows)
				.build();
		final ResultDefinition.WithObject<UrlRecord> result = service.searchQuery(queryDef);
		if (result == null || result.documents == null || result.documents.isEmpty())
			return 0;
		result.documents.forEach(doc -> crawlBuilder.addUrl(doc.record.urlStore, doc.record.depth));
		return result.documents.size();
	}

	public boolean isAlreadyCrawled(final String url, final UUID crawlUuid, final long taskCreationTime)
			throws Exception {
		updateLastUse();
		final UrlRecord urlRecord = getDocument(url);
		return urlRecord != null && !CrawlStatus.isUnknown(urlRecord.crawlStatus) &&
				crawlUuid.equals(urlRecord.getCrawlUuid()) && urlRecord.getTaskCreationTime() == taskCreationTime;
	}

	public boolean exists(final String url) {
		updateLastUse();
		final QueryDefinition queryDef = QueryDefinition.of(new TermQuery(FieldDefinition.ID_FIELD, url)).build();
		final ResultDefinition result = service.searchQueryWithMap(queryDef);
		return result != null && result.total_hits != null && result.total_hits > 0;
	}

	public void postDocuments(final Collection<UrlRecord> values) throws IOException, InterruptedException {
		updateLastUse();
		service.postDocuments(values);
	}

	public void updateDocumentsValues(final Collection<UrlRecord> values) throws IOException, InterruptedException {
		updateLastUse();
		service.updateDocumentsValues(values);
	}

	public UrlRecord getDocument(final String url) throws Exception {
		updateLastUse();
		return ErrorWrapper.bypass(() -> service.getDocument(url), 404);
	}

	public IndexStatus getIndexStatus() {
		updateLastUse();
		return service.getIndexStatus();
	}

	public Map<CrawlStatus, Long> getCrawlStatusCount() {
		updateLastUse();
		final Map<CrawlStatus, Long> crawlStatusMap = new LinkedHashMap<>();
		for (final CrawlStatus crawlStatus : CrawlStatus.values()) {
			final ResultDefinition result = service.searchQueryWithMap(
					QueryDefinition.of(new IntDocValuesExactQuery("crawlStatus", crawlStatus.code)).build());
			if (result != null && result.total_hits != null && result.total_hits > 0)
				crawlStatusMap.put(crawlStatus, result.total_hits);
		}
		return crawlStatusMap;
	}

	public ResultDefinition.WithObject<UrlRecord> search(final String keywords, final int start, final int rows) {
		if (StringUtils.isBlank(keywords))
			return null;

		final MultiFieldQuery fullTextQuery = MultiFieldQuery.of()
				.tieBreakerMultiplier(0.02f)
				.queryString(keywords)
				.defaultOperator(QueryParserOperator.and)
				.fieldAndFilter("full", "fullFr", "fullDe", "fullEn", "fullIt")
				.fieldBoost("urlLike", 13f)
				.fieldBoost("title", 8f)
				.fieldBoost("titleFr", 8f)
				.fieldBoost("titleDe", 8f)
				.fieldBoost("titleEn", 8f)
				.fieldBoost("titleIt", 8f)
				.fieldBoost("description", 5f)
				.fieldBoost("descriptionFr", 5f)
				.fieldBoost("descriptionDe", 5f)
				.fieldBoost("descriptionEn", 5f)
				.fieldBoost("descriptionIt", 5f)
				.fieldBoost("content", 3f)
				.fieldBoost("contentFr", 3f)
				.fieldBoost("contentDe", 3f)
				.fieldBoost("contentEn", 3f)
				.fieldBoost("contentIt", 3F)
				.fieldBoost("full", 1f)
				.fieldBoost("fullFr", 1f)
				.fieldBoost("fullDe", 1f)
				.fieldBoost("fullEn", 1f)
				.fieldBoost("fullIt", 1f)
				.build();

		final BooleanQuery booleanQuery = BooleanQuery.of()
				.addClause(BooleanQuery.Occur.must, fullTextQuery)
				.addClause(BooleanQuery.Occur.filter,
						new IntDocValuesExactQuery("crawlStatus", CrawlStatus.CRAWLED.code))
				.build();

		final QueryDefinition queryDef = QueryDefinition.of(booleanQuery)
				.start(start)
				.rows(rows)
				.returnedField("urlStore")
				.highlighter("title", HighlighterDefinition.of("title")	.setMaxPassages(1).setMaxLength(1000).build())
				.highlighter("description", HighlighterDefinition.of("description")
						.setMaxNoHighlightPassages(5)
						.setMaxPassages(5)
						.setMaxLength(100)
						.build())
				.highlighter("content", HighlighterDefinition.of("content")
						.setMaxNoHighlightPassages(5)
						.setMaxPassages(5)
						.setMaxLength(100)
						.build())
				.build();

		return service.searchQuery(queryDef);

	}
}
