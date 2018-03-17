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
import com.qwazr.search.function.IntFieldSource;
import com.qwazr.search.index.HighlighterDefinition;
import com.qwazr.search.index.QueryBuilder;
import com.qwazr.search.query.BooleanQuery;
import com.qwazr.search.query.CustomScoreQuery;
import com.qwazr.search.query.FunctionQuery;
import com.qwazr.search.query.IntDocValuesExactQuery;
import com.qwazr.search.query.MultiFieldQueryParser;
import com.qwazr.search.query.QueryParserOperator;
import com.qwazr.utils.StringUtils;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.queries.CustomScoreProvider;

import java.util.HashMap;
import java.util.Map;

public class SearchService {

	// Optimization: They are global singleton
	private final HighlighterDefinition titleHighlighter = HighlighterDefinition.of().setMaxPassages(1).build();
	private final HighlighterDefinition contentHighlighter = HighlighterDefinition.of().setMaxPassages(5).build();
	private final FunctionQuery depthFunctionQuery = new FunctionQuery(new IntFieldSource("depth"));
	private final IntDocValuesExactQuery crawledQuery =
			new IntDocValuesExactQuery("crawlStatus", CrawlStatus.CRAWLED.code);

	// Optimization: They are per thread singleton
	private final ThreadLocal<Map<Language, MultiFieldQueryParser.Builder>> queryParserSupplier;
	private final ThreadLocal<QueryBuilder> queryBuilderSupplier;

	public SearchService() {
		queryParserSupplier = ThreadLocal.withInitial(HashMap::new);
		queryBuilderSupplier = ThreadLocal.withInitial(
				() -> new QueryBuilder().returnedField("urlStore", "imageUri", "schemaOrgType", "organizationName",
						"datePublished")
						.highlighter("title", titleHighlighter)
						.highlighter("description", contentHighlighter)
						.highlighter("content", contentHighlighter));
	}

	private MultiFieldQueryParser.Builder getQueryParser(final Language language) {
		return queryParserSupplier.get()
				.computeIfAbsent(language, lang -> MultiFieldQueryParser.of()
						.setDefaultOperator(QueryParserOperator.and)
						.addField("urlLike", "title", lang.title, "description", lang.description, "content",
								lang.content, "full", lang.full)
						.addBoost("urlLike", 13f)
						.addBoost("title", 8f)
						.addBoost(lang.title, 8f)
						.addBoost("description", 5f)
						.addBoost(lang.description, 5f)
						.addBoost("content", 3f)
						.addBoost(lang.content, 3f)
						.addBoost("full", 1f)
						.addBoost(lang.full, 1f));
	}

	public SearchResults webSearch(final IndexService indexService, final Language lang, final String keywords,
			final int start, final int rows) {

		if (StringUtils.isBlank(keywords))
			return null;

		final MultiFieldQueryParser fullTextQuery = getQueryParser(lang).setQueryString(keywords).build();

		final CustomScoreQuery customScoreQuery =
				new CustomScoreQuery(fullTextQuery, DepthScore.class.getName(), depthFunctionQuery);

		final BooleanQuery booleanQuery = BooleanQuery.of()
				.addClause(BooleanQuery.Occur.must, customScoreQuery)
				.addClause(BooleanQuery.Occur.filter, crawledQuery)
				.build();

		final QueryBuilder queryBuilder = queryBuilderSupplier.get().query(booleanQuery).start(start).rows(rows);
		lang.highlights(queryBuilder);

		return new SearchResults(start, rows, indexService.search(queryBuilder.build()), lang);
	}

	public static class DepthScore extends CustomScoreProvider {

		public DepthScore(LeafReaderContext context) {
			super(context);
		}

		public float customScore(int doc, float subQueryScore, float valSrcScore) {
			switch ((int) valSrcScore) {
			case 0:
				return subQueryScore * 2.3f;
			case 1:
				return subQueryScore * 1.8f;
			case 2:
				return subQueryScore * 1.3f;
			case 3:
				return subQueryScore * 1.2f;
			default:
				return subQueryScore;
			}
		}
	}

}
