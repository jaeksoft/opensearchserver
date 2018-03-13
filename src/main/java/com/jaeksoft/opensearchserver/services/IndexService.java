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
import com.jaeksoft.opensearchserver.model.Language;
import com.jaeksoft.opensearchserver.model.SearchResults;
import com.jaeksoft.opensearchserver.model.UrlRecord;
import com.qwazr.crawler.web.WebCrawlDefinition;
import com.qwazr.search.annotations.AnnotatedIndexService;
import com.qwazr.search.field.FieldDefinition;
import com.qwazr.search.index.HighlighterDefinition;
import com.qwazr.search.index.IndexServiceInterface;
import com.qwazr.search.index.IndexStatus;
import com.qwazr.search.index.QueryBuilder;
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

	private static BooleanQuery.Builder addCrawlUuidFilter(final BooleanQuery.Builder booleanQueryBuilder,
			final UUID crawlUuid) {
		booleanQueryBuilder.addClause(BooleanQuery.Occur.filter,
				new LongDocValuesExactQuery("crawlUuidMost", crawlUuid.getMostSignificantBits()))
				.addClause(BooleanQuery.Occur.filter,
						new LongDocValuesExactQuery("crawlUuidLeast", crawlUuid.getLeastSignificantBits()));
		return booleanQueryBuilder;
	}

	public boolean isAlreadyCrawled(final String url, final UUID crawlUuid, final Long taskCreationTime) {
		updateLastUse();
		final QueryDefinition queryDef = QueryDefinition.of(
				addCrawlUuidFilter(BooleanQuery.of(true, null), crawlUuid).addClause(BooleanQuery.Occur.filter,
						new TermQuery(FieldDefinition.ID_FIELD, url))
						.addClause(BooleanQuery.Occur.filter,
								new LongDocValuesExactQuery("taskCreationTime", taskCreationTime))
						.build()).start(0).rows(0).build();
		return service.searchQuery(queryDef).total_hits > 0L;
	}

	public Long deleteOldCrawl(final UUID crawlUuid, final Long taskCreationTime) {
		updateLastUse();
		final QueryDefinition queryDef = QueryDefinition.of(
				addCrawlUuidFilter(BooleanQuery.of(true, null), crawlUuid).addClause(BooleanQuery.Occur.must_not,
						new LongDocValuesExactQuery("taskCreationTime", taskCreationTime)).build()).build();
		return service.deleteByQuery(queryDef).getTotalHits();
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

	public SearchResults search(final Language lang, final String keywords, final int start, final int rows) {

		if (StringUtils.isBlank(keywords))
			return null;

		final MultiFieldQuery fullTextQuery = MultiFieldQuery.of()
				.tieBreakerMultiplier(0.02f)
				.queryString(keywords)
				.defaultOperator(QueryParserOperator.and)
				.fieldAndFilter("full", lang.full)
				.fieldBoost("urlLike", 13f)
				.fieldBoost("title", 8f)
				.fieldBoost(lang.title, 8f)
				.fieldBoost("description", 5f)
				.fieldBoost(lang.description, 5f)
				.fieldBoost("content", 3f)
				.fieldBoost(lang.content, 3f)
				.fieldBoost("full", 1f)
				.fieldBoost(lang.full, 1f)
				.fieldAndFilter(lang.full)
				.build();

		final BooleanQuery booleanQuery = BooleanQuery.of()
				.addClause(BooleanQuery.Occur.must, fullTextQuery)
				.addClause(BooleanQuery.Occur.filter,
						new IntDocValuesExactQuery("crawlStatus", CrawlStatus.CRAWLED.code))
				.build();

		final QueryBuilder queryBuilder = QueryDefinition.of(booleanQuery)
				.start(start)
				.rows(rows)
				.returnedField("urlStore")
				.highlighter("title", HighlighterDefinition.of().setMaxPassages(1).build())
				.highlighter("description",
						HighlighterDefinition.of().setMaxNoHighlightPassages(5).setMaxPassages(5).build())
				.highlighter("content",
						HighlighterDefinition.of().setMaxNoHighlightPassages(5).setMaxPassages(5).build());

		lang.highlights(queryBuilder);

		return new SearchResults(service.searchQuery(queryBuilder.build()), lang);

	}

}
