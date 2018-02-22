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

import com.jaeksoft.opensearchserver.model.UrlRecord;
import com.qwazr.crawler.web.WebCrawlDefinition;
import com.qwazr.search.annotations.AnnotatedIndexService;
import com.qwazr.search.index.IndexServiceInterface;
import com.qwazr.search.index.QueryBuilder;
import com.qwazr.search.index.QueryDefinition;
import com.qwazr.search.index.ResultDefinition;
import com.qwazr.search.query.TermQuery;
import com.qwazr.utils.HashUtils;

import java.net.URISyntaxException;
import java.util.UUID;

public class IndexService extends AnnotatedIndexService<UrlRecord> {

	public IndexService(final IndexServiceInterface indexService, final String schemaName, final String indexName)
			throws URISyntaxException {
		super(indexService, UrlRecord.class, schemaName, indexName, null);
		createUpdateFields();
	}

	public int fillUnknownUrls(int rows, final UUID crawlUuid, final WebCrawlDefinition.Builder crawlBuilder) {
		final QueryDefinition queryDef = new QueryBuilder().returnedField("*")
				.query(new TermQuery("crawlUuid", HashUtils.toBase64(crawlUuid)))
				.sort("lastModificationTime", QueryDefinition.SortEnum.ascending)
				.rows(rows)
				.build();
		final ResultDefinition.WithObject<UrlRecord> result = searchQuery(queryDef);
		if (result == null || result.documents == null || result.documents.isEmpty())
			return 0;
		result.documents.forEach(doc -> crawlBuilder.addUrl(doc.record.url, doc.record.depth));
		return result.documents.size();
	}
}
