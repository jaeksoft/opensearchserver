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

import com.jaeksoft.opensearchserver.model.IndexStatus;
import com.jaeksoft.opensearchserver.model.Language;
import com.jaeksoft.opensearchserver.model.SearchResults;
import com.qwazr.search.function.IntFieldSource;
import com.qwazr.search.function.MultiValuedLongFieldSource;
import com.qwazr.search.index.HighlighterDefinition;
import com.qwazr.search.index.QueryBuilder;
import com.qwazr.search.index.QueryDefinition;
import com.qwazr.search.query.AbstractQueryParser;
import com.qwazr.search.query.BooleanQuery;
import com.qwazr.search.query.CustomScoreQuery;
import com.qwazr.search.query.FunctionQuery;
import com.qwazr.search.query.IntDocValuesExactQuery;
import com.qwazr.search.query.QueryParserOperator;
import com.qwazr.search.query.SimpleQueryParser;
import com.qwazr.search.query.SortedDocValuesExactQuery;
import com.qwazr.utils.StringUtils;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.queries.CustomScoreProvider;
import org.apache.lucene.search.SortedNumericSelector;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class SearchService {

	// Optimization: They are global singleton
	protected final HighlighterDefinition titleHighlighter = HighlighterDefinition.of().setMaxPassages(1).build();

	protected final HighlighterDefinition contentHighlighter = HighlighterDefinition.of().setMaxPassages(5).build();

	protected final FunctionQuery depthFunctionQuery = new FunctionQuery(new IntFieldSource("depth"));

	protected final FunctionQuery datePublishedFunctionQuery =
			new FunctionQuery(new MultiValuedLongFieldSource("datePublished", SortedNumericSelector.Type.MAX));

	protected final BooleanQuery.BooleanClause indexedStatus = new BooleanQuery.BooleanClause(BooleanQuery.Occur.filter,
			new IntDocValuesExactQuery("indexStatus", IndexStatus.INDEXED.code));

	// TODO EK: Check that semantic feature normalizes using lowercase
	protected final BooleanQuery.BooleanClause newsFilter = new BooleanQuery.BooleanClause(BooleanQuery.Occur.filter,
			BooleanQuery.of(false, null)
					.should(new SortedDocValuesExactQuery("schemaOrgType", "article"))
					.should(new SortedDocValuesExactQuery("schemaOrgType", "Article"))
					.should(new SortedDocValuesExactQuery("schemaOrgType", "NewsArticle"))
					.should(new SortedDocValuesExactQuery("schemaOrgType", "newsarticle"))
					.should(new SortedDocValuesExactQuery("schemaOrgType", "Blog"))
					.should(new SortedDocValuesExactQuery("schemaOrgType", "blog"))
					.should(new SortedDocValuesExactQuery("schemaOrgType", "Report"))
					.should(new SortedDocValuesExactQuery("schemaOrgType", "report"))
					.should(new SortedDocValuesExactQuery("schemaOrgType", "TechArticle"))
					.should(new SortedDocValuesExactQuery("schemaOrgType", "techarticle"))
					.build());

	// Optimization: They are per thread singleton
	private final ThreadLocal<Map<Language, AbstractQueryParser.AbstractBuilder>> queryParserSupplier;
	private final ThreadLocal<QueryBuilder> queryBuilderSupplier;
	private final ThreadLocal<BooleanQuery.Builder> booleanQueryBuilderSupplier;

	public SearchService() {
		queryParserSupplier = ThreadLocal.withInitial(HashMap::new);
		queryBuilderSupplier = ThreadLocal.withInitial(
				() -> new QueryBuilder().returnedField("urlStore", "imageUri", "schemaOrgType", "organizationName",
						"datePublished")
						.highlighter("title", titleHighlighter)
						.highlighter("description", contentHighlighter)
						.highlighter("content", contentHighlighter));
		booleanQueryBuilderSupplier = ThreadLocal.withInitial(BooleanQuery::of);
	}

	protected AbstractQueryParser.AbstractBuilder getQueryParser(final Language language) {
		return queryParserSupplier.get()
				.computeIfAbsent(language, lang -> SimpleQueryParser.of()
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

	protected QueryBuilder getQueryBuilder() {
		return queryBuilderSupplier.get();
	}

	protected BooleanQuery.Builder getBooleanQueryBuilder() {
		return booleanQueryBuilderSupplier.get();
	}

	public SearchResults search(final IndexService indexService, final Language lang, final String keywords,
			final int start, final int rows,
			final Function<AbstractQueryParser, CustomScoreQuery> customScoreQueryFunction,
			final BooleanQuery.BooleanClause... filters) {

		if (StringUtils.isBlank(keywords))
			return null;

		final AbstractQueryParser fullTextQuery = getQueryParser(lang).setQueryString(keywords).build();

		final BooleanQuery booleanQuery = getBooleanQueryBuilder().setClauses(
				new BooleanQuery.BooleanClause(BooleanQuery.Occur.must, customScoreQueryFunction.apply(fullTextQuery)))
				.addClauses(filters)
				.build();

		final QueryBuilder queryBuilder = getQueryBuilder().query(booleanQuery).start(start).rows(rows);
		lang.highlights(queryBuilder);

		return new SearchResults(start, rows, indexService.search(queryBuilder.build()), lang);
	}

	public SearchResults search(final IndexService indexService, final Language lang, final String keywords,
			final int start, final int rows, final String sortField, final QueryDefinition.SortEnum sortEnum,
			final BooleanQuery.BooleanClause... filters) {

		if (StringUtils.isBlank(keywords))
			return null;

		final AbstractQueryParser fullTextQuery = getQueryParser(lang).setQueryString(keywords).build();

		final BooleanQuery booleanQuery = getBooleanQueryBuilder().setClause(
				new BooleanQuery.BooleanClause(BooleanQuery.Occur.must, fullTextQuery)).addClauses(filters).build();

		final QueryBuilder queryBuilder =
				getQueryBuilder().query(booleanQuery).sort(sortField, sortEnum).start(start).rows(rows);
		lang.highlights(queryBuilder);

		return new SearchResults(start, rows, indexService.search(queryBuilder.build()), lang);
	}

	public SearchResults webSearch(final IndexService indexService, final Language lang, final String keywords,
			final int start, final int rows) {
		return search(indexService, lang, keywords, start, rows,
				query -> new CustomScoreQuery(query, DepthScore.class.getName(), depthFunctionQuery), indexedStatus);
	}

	public SearchResults newsSearch(final IndexService indexService, final Language lang, final String keywords,
			final int start, final int rows) {
		return search(indexService, lang, keywords, start, rows, "datePublished",
				QueryDefinition.SortEnum.descending_missing_last, indexedStatus, newsFilter);
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

	public static class RecentScore extends CustomScoreProvider {

		private static final float rangeTime = 1000 * 60 * 60 * 24 * 7;

		private final long currentTime = System.currentTimeMillis();
		private final float pivotTime = currentTime - rangeTime;

		private final static float MAX_BOOST = 3.0f;
		private final static float RANGE_BOOST = 2.0f;

		public RecentScore(LeafReaderContext context) {
			super(context);
		}

		public float customScore(final int doc, final float subQueryScore, final float valSrcScore) {
			if (valSrcScore == 0 || valSrcScore <= pivotTime)
				return subQueryScore;
			final float distanceTime = valSrcScore - pivotTime;
			if (distanceTime <= 0)
				return MAX_BOOST * subQueryScore;
			final float distanceCoef = 1 + RANGE_BOOST * (1 / (float) StrictMath.pow(rangeTime / distanceTime, 2));
			return distanceCoef * subQueryScore;
		}
	}

}
