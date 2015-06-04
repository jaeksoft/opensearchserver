/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2015 Emmanuel Keller / Jaeksoft
 * 
 * http://www.open-search-server.com
 * 
 * This file is part of OpenSearchServer.
 *
 * OpenSearchServer is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * OpenSearchServer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with OpenSearchServer. 
 *  If not, see <http://www.gnu.org/licenses/>.
 **/

package com.jaeksoft.searchlib.request;

import java.io.IOException;

import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.util.Version;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.analysis.LanguageEnum;
import com.jaeksoft.searchlib.analysis.PerFieldAnalyzer;
import com.jaeksoft.searchlib.authentication.AuthManager;
import com.jaeksoft.searchlib.config.Config;
import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.index.ReaderAbstract;
import com.jaeksoft.searchlib.index.ReaderInterface;
import com.jaeksoft.searchlib.query.ParseException;
import com.jaeksoft.searchlib.result.AbstractResult;
import com.jaeksoft.searchlib.result.ResultSearchSingle;
import com.jaeksoft.searchlib.schema.Schema;
import com.jaeksoft.searchlib.schema.SchemaField;

public abstract class AbstractLocalSearchRequest extends AbstractSearchRequest
		implements RequestInterfaces.ReturnedFieldInterface,
		RequestInterfaces.FilterListInterface {

	private transient Query boostedComplexQuery;
	private transient Query notBoostedComplexQuery;
	private transient Query snippetSimpleQuery;

	protected transient PerFieldAnalyzer analyzer;

	protected transient QueryParser queryParser;
	private String queryParsed;

	protected AbstractLocalSearchRequest(Config config, RequestTypeEnum type) {
		super(config, type);
	}

	@Override
	protected void setDefaultValues() {
		super.setDefaultValues();
		this.queryParser = null;
		this.queryParsed = null;
		this.snippetSimpleQuery = null;
		this.boostedComplexQuery = null;
		this.notBoostedComplexQuery = null;
		this.analyzer = null;
		this.queryParsed = null;
	}

	@Override
	public void copyFrom(AbstractRequest request) {
		super.copyFrom(request);
		this.snippetSimpleQuery = null;
		this.boostedComplexQuery = null;
		this.notBoostedComplexQuery = null;
		this.analyzer = null;
		this.queryParsed = null;
	}

	@Override
	protected void resetNoLock() {
		super.resetNoLock();
		this.queryParser = null;
		this.queryParsed = null;
		this.snippetSimpleQuery = null;
		this.boostedComplexQuery = null;
		this.notBoostedComplexQuery = null;
		this.analyzer = null;
	}

	private PerFieldAnalyzer checkAnalyzer() throws SearchLibException {
		if (analyzer == null)
			analyzer = config.getSchema().getQueryPerFieldAnalyzer(lang);
		return analyzer;
	}

	public PerFieldAnalyzer getAnalyzer() throws SearchLibException {
		rwl.r.lock();
		try {
			if (analyzer != null)
				return analyzer;
		} finally {
			rwl.r.unlock();
		}
		rwl.w.lock();
		try {
			checkAnalyzer();
			return analyzer;
		} finally {
			rwl.w.unlock();
		}
	}

	protected abstract Query newSnippetQuery(String queryString)
			throws IOException, ParseException, SyntaxError, SearchLibException;

	public Query getSnippetQuery() throws IOException, ParseException,
			SyntaxError, SearchLibException {
		rwl.r.lock();
		try {
			if (snippetSimpleQuery != null)
				return snippetSimpleQuery;
		} finally {
			rwl.r.unlock();
		}
		rwl.w.lock();
		try {
			if (snippetSimpleQuery != null)
				return snippetSimpleQuery;
			getQueryParser();
			checkAnalyzer();
			snippetSimpleQuery = newSnippetQuery(queryString);
			return snippetSimpleQuery;
		} finally {
			rwl.w.unlock();
		}
	}

	protected abstract Query newComplexQuery(String queryString)
			throws ParseException, SyntaxError, SearchLibException, IOException;

	private Query newComplexQuery() throws ParseException, SearchLibException,
			SyntaxError, IOException {
		getQueryParser();
		checkAnalyzer();
		Query query = newComplexQuery(queryString);
		if (query == null)
			query = new BooleanQuery();
		return query;
	}

	public Query getNotBoostedQuery() throws ParseException,
			SearchLibException, SyntaxError, IOException {
		rwl.r.lock();
		try {
			if (notBoostedComplexQuery != null)
				return notBoostedComplexQuery;
		} finally {
			rwl.r.unlock();
		}
		rwl.w.lock();
		try {
			if (notBoostedComplexQuery != null)
				return notBoostedComplexQuery;
			notBoostedComplexQuery = newComplexQuery();
			return notBoostedComplexQuery;
		} finally {
			rwl.w.unlock();
		}
	}

	@Override
	public Query getQuery() throws ParseException, SyntaxError,
			SearchLibException, IOException {
		rwl.r.lock();
		try {
			if (boostedComplexQuery != null)
				return boostedComplexQuery;
		} finally {
			rwl.r.unlock();
		}
		rwl.w.lock();
		try {
			if (boostedComplexQuery != null)
				return boostedComplexQuery;
			boostedComplexQuery = newComplexQuery();
			for (BoostQuery boostQuery : boostingQueries)
				boostedComplexQuery = boostQuery.getNewQuery(
						boostedComplexQuery, queryParser);
			queryParsed = boostedComplexQuery.toString();
			return boostedComplexQuery;
		} finally {
			rwl.w.unlock();
		}
	}

	private QueryParser getQueryParser() throws ParseException,
			SearchLibException {
		if (queryParser != null)
			return queryParser;
		Schema schema = getConfig().getSchema();
		SchemaField field = schema.getFieldList().getDefaultField();
		if (field == null)
			throw new SearchLibException(
					"Please select a default field in the schema");
		queryParser = new QueryParser(Version.LUCENE_36, field.getName(),
				checkAnalyzer());
		queryParser.setAllowLeadingWildcard(allowLeadingWildcard);
		queryParser.setPhraseSlop(phraseSlop);
		queryParser.setDefaultOperator(defaultOperator.lucop);
		queryParser.setLowercaseExpandedTerms(false);
		return queryParser;
	}

	public void setBoostedComplexQuery(Query query) {
		rwl.w.lock();
		try {
			boostedComplexQuery = query;
		} finally {
			rwl.w.unlock();
		}
	}

	final public String getQueryParsed() throws ParseException, SyntaxError,
			SearchLibException, IOException {
		getQuery();
		rwl.r.lock();
		try {
			return queryParsed;
		} finally {
			rwl.r.unlock();
		}
	}

	@Override
	protected void setQueryStringNoLock(String q) {
		super.setQueryStringNoLock(q);
		boostedComplexQuery = null;
		notBoostedComplexQuery = null;
		snippetSimpleQuery = null;
	}

	@Override
	protected void setLangNoLock(LanguageEnum lang) {
		super.setLangNoLock(lang);
		analyzer = null;
	}

	@Override
	public AbstractResult<?> execute(ReaderInterface reader)
			throws SearchLibException {
		try {
			AuthManager authManager = config.getAuthManager();
			if (authManager.isEnabled()
					&& !(this instanceof SearchFilterRequest)) {
				authManager.apply(this);
			}
			return new ResultSearchSingle((ReaderAbstract) reader, this);
		} catch (IOException e) {
			throw new SearchLibException(e);
		} catch (ParseException e) {
			throw new SearchLibException(e);
		} catch (SyntaxError e) {
			throw new SearchLibException(e);
		} catch (SearchLibException e) {
			throw new SearchLibException(e);
		} catch (InstantiationException e) {
			throw new SearchLibException(e);
		} catch (IllegalAccessException e) {
			throw new SearchLibException(e);
		} catch (ClassNotFoundException e) {
			throw new SearchLibException(e);
		}
	}

}
