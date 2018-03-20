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
import com.jaeksoft.opensearchserver.model.IndexStatus;
import com.jaeksoft.opensearchserver.model.UrlRecord;
import com.qwazr.crawler.web.WebCrawlDefinition;
import com.qwazr.search.annotations.AnnotatedIndexService;
import com.qwazr.search.field.FieldDefinition;
import com.qwazr.search.index.IndexServiceInterface;
import com.qwazr.search.index.QueryDefinition;
import com.qwazr.search.index.ResultDefinition;
import com.qwazr.search.query.AbstractQuery;
import com.qwazr.search.query.BooleanQuery;
import com.qwazr.search.query.IntDocValuesExactQuery;
import com.qwazr.search.query.LongDocValuesExactQuery;
import com.qwazr.search.query.TermQuery;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class IndexService extends UsableService {

	private final AnnotatedIndexService<UrlRecord> service;

	public final static BooleanQuery.BooleanClause CRAWLED_FILTER_CLAUSE =
			new BooleanQuery.BooleanClause(BooleanQuery.Occur.filter,
					new IntDocValuesExactQuery("crawlStatus", CrawlStatus.CRAWLED.code));

	public final static BooleanQuery.BooleanClause UNKNOWN_FILTER_CLAUSE =
			new BooleanQuery.BooleanClause(BooleanQuery.Occur.filter,
					new IntDocValuesExactQuery("crawlStatus", CrawlStatus.UNKNOWN.code));

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
				.addClause(UNKNOWN_FILTER_CLAUSE)
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
			final UUID crawlUuid, final Long taskCreationTime) {
		booleanQueryBuilder.filter(new LongDocValuesExactQuery("crawlUuidMost", crawlUuid.getMostSignificantBits()))
				.filter(new LongDocValuesExactQuery("crawlUuidLeast", crawlUuid.getLeastSignificantBits()));
		if (taskCreationTime != null)
			booleanQueryBuilder.filter(new LongDocValuesExactQuery("taskCreationTime", taskCreationTime));
		return booleanQueryBuilder;
	}

	public boolean isAlreadyCrawled(final String url, final UUID crawlUuid, final Long taskCreationTime) {
		updateLastUse();
		final QueryDefinition queryDef = QueryDefinition.of(
				addCrawlUuidFilter(BooleanQuery.of(true, null), crawlUuid, taskCreationTime).filter(
						new TermQuery(FieldDefinition.ID_FIELD, url)).build()).
				start(0).rows(0).build();
		return service.searchQuery(queryDef).total_hits > 0L;
	}

	public Long deleteOldCrawl(final UUID crawlUuid, final Long taskCreationTime) {
		updateLastUse();
		final QueryDefinition queryDef =
				QueryDefinition.of(addCrawlUuidFilter(BooleanQuery.of(true, null), crawlUuid, taskCreationTime).build())
						.build();
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

	public com.qwazr.search.index.IndexStatus getIndexStatus() {
		updateLastUse();
		return service.getIndexStatus();
	}

	ResultDefinition.WithObject<UrlRecord> search(final QueryDefinition queryDefinition) {
		return service.searchQuery(queryDefinition);
	}

	private <T> void count(T status, AbstractQuery query, Map<T, Long> counterMap) {
		final ResultDefinition result = service.searchQueryWithMap(QueryDefinition.of(query).start(0).rows(0).build());
		if (result != null && result.total_hits != null && result.total_hits > 0)
			counterMap.put(status, result.total_hits);
	}

	public Map<CrawlStatus, Long> getCrawlStatusCount(final UUID crawlUuid, final Long taskCreationTime) {
		updateLastUse();
		final Map<CrawlStatus, Long> statusMap = new LinkedHashMap<>();
		for (final CrawlStatus crawlStatus : CrawlStatus.values()) {
			AbstractQuery query = new IntDocValuesExactQuery("crawlStatus", crawlStatus.code);
			if (crawlUuid != null)
				query = addCrawlUuidFilter(BooleanQuery.of(true, null), crawlUuid, taskCreationTime).filter(query)
						.build();
			count(crawlStatus, query, statusMap);
		}
		return statusMap;
	}

	public Map<IndexStatus, Long> getIndexStatusCount(final UUID crawlUuid, final Long taskCreationTime) {
		updateLastUse();
		final Map<IndexStatus, Long> statusMap = new LinkedHashMap<>();
		for (final IndexStatus indexStatus : IndexStatus.values()) {
			AbstractQuery query = new IntDocValuesExactQuery("indexStatus", indexStatus.code);
			if (crawlUuid != null)
				query = addCrawlUuidFilter(BooleanQuery.of(true, null), crawlUuid, taskCreationTime).filter(query)
						.build();
			count(indexStatus, query, statusMap);
		}
		return statusMap;
	}

	public long getCrawledCount(final UUID crawlUuid, final Long taskCreationTime) {
		updateLastUse();
		final QueryDefinition queryDef =
				QueryDefinition.of(addCrawlUuidFilter(BooleanQuery.of(true, null), crawlUuid, taskCreationTime).build())
						.start(0)
						.rows(0)
						.build();
		final ResultDefinition result = service.searchQueryWithMap(queryDef);
		return result != null && result.total_hits != null ? result.total_hits : 0;
	}

}
