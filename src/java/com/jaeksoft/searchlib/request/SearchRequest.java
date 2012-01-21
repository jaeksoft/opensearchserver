/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008-2012 Emmanuel Keller / Jaeksoft
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

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.xpath.XPathExpressionException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.queryParser.QueryParser.Operator;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.similar.MoreLikeThis;
import org.apache.lucene.util.Version;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.analysis.LanguageEnum;
import com.jaeksoft.searchlib.collapse.CollapseMode;
import com.jaeksoft.searchlib.config.Config;
import com.jaeksoft.searchlib.facet.FacetField;
import com.jaeksoft.searchlib.filter.Filter;
import com.jaeksoft.searchlib.filter.Filter.Source;
import com.jaeksoft.searchlib.filter.FilterList;
import com.jaeksoft.searchlib.function.expression.RootExpression;
import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.index.IndexAbstract;
import com.jaeksoft.searchlib.result.Result;
import com.jaeksoft.searchlib.schema.Field;
import com.jaeksoft.searchlib.schema.FieldList;
import com.jaeksoft.searchlib.schema.Schema;
import com.jaeksoft.searchlib.schema.SchemaField;
import com.jaeksoft.searchlib.snippet.SnippetField;
import com.jaeksoft.searchlib.sort.SortField;
import com.jaeksoft.searchlib.sort.SortList;
import com.jaeksoft.searchlib.spellcheck.SpellCheckField;
import com.jaeksoft.searchlib.util.External;
import com.jaeksoft.searchlib.util.ReadWriteLock;
import com.jaeksoft.searchlib.util.Timer;
import com.jaeksoft.searchlib.util.XPathParser;
import com.jaeksoft.searchlib.util.XmlWriter;

public class SearchRequest implements Externalizable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 148522254171520640L;

	private transient QueryParser queryParser;
	private transient Query complexQuery;
	private transient Query primitiveQuery;
	private transient Config config;
	private transient Timer timer;
	private transient long finalTime;
	private transient Analyzer analyzer;

	private String requestName;
	private FilterList filterList;
	private boolean allowLeadingWildcard;
	private int phraseSlop;
	private QueryParser.Operator defaultOperator;
	private FieldList<SnippetField> snippetFieldList;
	private FieldList<Field> returnFieldList;
	private FieldList<Field> documentFieldList;
	private FieldList<FacetField> facetFieldList;
	private FieldList<SpellCheckField> spellCheckFieldList;
	private SortList sortList;
	private String collapseField;
	private int collapseMax;
	private CollapseMode collapseMode;
	private boolean isMoreLikeThis;
	private String moreLikeThisDocQuery;
	private FieldList<Field> moreLikeThisFieldList;
	private int moreLikeThisMinWordLen;
	private int moreLikeThisMaxWordLen;
	private int moreLikeThisMinDocFreq;
	private int moreLikeThisMinTermFreq;
	private String moreLikeThisStopWords;
	private int start;
	private int rows;
	private LanguageEnum lang;
	private String queryString;
	private String patternQuery;
	private String scoreFunction;
	private String queryParsed;
	private boolean withDocuments;
	private boolean withSortValues;
	private boolean withLogReport;
	private List<String> customLogs;

	private final ReadWriteLock rwl = new ReadWriteLock();

	public SearchRequest() {
		queryParser = null;
		config = null;
		timer = null;
		finalTime = 0;
		customLogs = null;
	}

	public SearchRequest(Config config) {
		this.config = config;
		this.requestName = null;
		this.filterList = new FilterList(this.config);
		this.queryParser = null;
		this.allowLeadingWildcard = false;
		this.phraseSlop = 10;
		this.defaultOperator = Operator.OR;
		this.snippetFieldList = new FieldList<SnippetField>();
		this.returnFieldList = new FieldList<Field>();
		this.sortList = new SortList();
		this.documentFieldList = null;
		this.facetFieldList = new FieldList<FacetField>();
		this.spellCheckFieldList = new FieldList<SpellCheckField>();

		this.collapseField = null;
		this.collapseMax = 2;
		this.collapseMode = CollapseMode.COLLAPSE_OFF;

		this.isMoreLikeThis = false;
		this.moreLikeThisFieldList = new FieldList<Field>();
		this.moreLikeThisMinWordLen = 0;
		this.moreLikeThisMaxWordLen = 0;
		this.moreLikeThisMinDocFreq = 0;
		this.moreLikeThisMinTermFreq = 0;
		this.moreLikeThisStopWords = null;

		this.start = 0;
		this.rows = 10;
		this.lang = null;
		this.complexQuery = null;
		this.primitiveQuery = null;
		this.analyzer = null;
		this.queryString = null;
		this.patternQuery = null;
		this.scoreFunction = null;
		this.withDocuments = true;
		this.withSortValues = false;
		this.queryParsed = null;
		this.timer = new Timer("Search request");
		this.finalTime = 0;
		this.withLogReport = false;
		this.customLogs = null;
	}

	public SearchRequest(SearchRequest searchRequest) {
		this(searchRequest.config);
		searchRequest.rwl.r.lock();
		try {
			this.requestName = searchRequest.requestName;
			this.filterList = new FilterList(searchRequest.filterList);
			this.queryParser = null;
			this.allowLeadingWildcard = searchRequest.allowLeadingWildcard;
			this.phraseSlop = searchRequest.phraseSlop;
			this.defaultOperator = searchRequest.defaultOperator;
			this.snippetFieldList = new FieldList<SnippetField>(
					searchRequest.snippetFieldList);
			this.returnFieldList = new FieldList<Field>(
					searchRequest.returnFieldList);
			this.sortList = new SortList(searchRequest.sortList);
			this.documentFieldList = null;
			if (searchRequest.documentFieldList != null)
				this.documentFieldList = new FieldList<Field>(
						searchRequest.documentFieldList);
			this.facetFieldList = new FieldList<FacetField>(
					searchRequest.facetFieldList);
			this.spellCheckFieldList = new FieldList<SpellCheckField>(
					searchRequest.spellCheckFieldList);

			this.collapseField = searchRequest.collapseField;
			this.collapseMax = searchRequest.collapseMax;
			this.collapseMode = searchRequest.collapseMode;

			this.isMoreLikeThis = searchRequest.isMoreLikeThis;
			this.moreLikeThisFieldList = new FieldList<Field>(
					searchRequest.moreLikeThisFieldList);
			this.moreLikeThisMinWordLen = searchRequest.moreLikeThisMinWordLen;
			this.moreLikeThisMaxWordLen = searchRequest.moreLikeThisMaxWordLen;
			this.moreLikeThisMinDocFreq = searchRequest.moreLikeThisMinDocFreq;
			this.moreLikeThisMinTermFreq = searchRequest.moreLikeThisMinTermFreq;
			this.moreLikeThisStopWords = searchRequest.moreLikeThisStopWords;
			this.moreLikeThisDocQuery = searchRequest.moreLikeThisDocQuery;

			this.withDocuments = searchRequest.withDocuments;
			this.withSortValues = searchRequest.withSortValues;
			this.start = searchRequest.start;
			this.rows = searchRequest.rows;
			this.lang = searchRequest.lang;
			this.complexQuery = null;
			this.primitiveQuery = null;
			this.analyzer = null;
			this.queryString = searchRequest.queryString;
			this.patternQuery = searchRequest.patternQuery;
			this.scoreFunction = searchRequest.scoreFunction;
			this.queryParsed = null;
			this.withLogReport = searchRequest.withLogReport;
		} finally {
			searchRequest.rwl.r.unlock();
		}
	}

	public void reset() {
		rwl.w.lock();
		try {
			this.queryParsed = null;
			this.complexQuery = null;
			this.primitiveQuery = null;
			this.queryParser = null;
			this.analyzer = null;
		} finally {
			rwl.w.unlock();
		}
	}

	private SearchRequest(Config config, String requestName,
			boolean allowLeadingWildcard, int phraseSlop,
			QueryParser.Operator defaultOperator, int start, int rows,
			String codeLang, String patternQuery, String queryString,
			String scoreFunction, boolean delete, boolean withDocuments,
			boolean withSortValues) {
		this(config);
		this.requestName = requestName;
		this.allowLeadingWildcard = allowLeadingWildcard;
		this.phraseSlop = phraseSlop;
		this.defaultOperator = defaultOperator;
		this.start = start;
		this.rows = rows;
		this.lang = LanguageEnum.findByCode(codeLang);
		this.queryString = queryString;
		this.patternQuery = patternQuery;
		if (scoreFunction != null)
			if (scoreFunction.trim().length() == 0)
				scoreFunction = null;
		this.scoreFunction = scoreFunction;
		this.withDocuments = withDocuments;
		this.withSortValues = withSortValues;
	}

	public void init(Config config) {
		rwl.w.lock();
		try {
			this.config = config;
			finalTime = 0;
			if (timer != null)
				timer.reset();
			timer = new Timer("Search request");
		} finally {
			rwl.w.unlock();
		}
	}

	public Config getConfig() {
		rwl.r.lock();
		try {
			return this.config;
		} finally {
			rwl.r.unlock();
		}
	}

	private Analyzer checkAnalyzer() throws SearchLibException {
		if (analyzer == null)
			analyzer = config.getSchema().getQueryPerFieldAnalyzer(lang);
		return analyzer;
	}

	public Analyzer getAnalyzer() throws SearchLibException {
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

	public String getRequestName() {
		rwl.r.lock();
		try {
			return this.requestName;
		} finally {
			rwl.r.unlock();
		}
	}

	public void setRequestName(String name) {
		rwl.w.lock();
		try {
			this.requestName = name;
		} finally {
			rwl.w.unlock();
		}
	}

	public int getPhraseSlop() {
		rwl.r.lock();
		try {
			return phraseSlop;
		} finally {
			rwl.r.unlock();
		}
	}

	public void setPhraseSlop(int value) {
		rwl.w.lock();
		try {
			phraseSlop = value;
		} finally {
			rwl.w.unlock();
		}
	}

	private String getFinalQuery() {
		String finalQuery;
		if (patternQuery != null && patternQuery.length() > 0
				&& queryString != null) {
			String escQuery = escapeQuery(queryString);
			finalQuery = patternQuery.replace("$$$", escQuery);
			finalQuery = finalQuery.replace("$$", queryString);
		} else
			finalQuery = queryString;

		return finalQuery;
	}

	public Query getSnippetQuery() throws IOException, ParseException,
			SyntaxError, SearchLibException {
		rwl.r.lock();
		try {
			if (primitiveQuery != null)
				return primitiveQuery;
		} finally {
			rwl.r.unlock();
		}
		rwl.w.lock();
		try {
			if (primitiveQuery != null)
				return primitiveQuery;
			primitiveQuery = config.getIndex().rewrite(getQuery());
			return primitiveQuery;
		} finally {
			rwl.w.unlock();
		}
	}

	private Query getMoreLikeThisQuery() throws SearchLibException, IOException {
		Config config = getConfig();
		IndexAbstract index = config.getIndex();
		MoreLikeThis mlt = index.getMoreLikeThis();
		SearchRequest searchRequest = config.getNewSearchRequest();
		searchRequest.setRows(1);
		searchRequest.setQueryString(moreLikeThisDocQuery);
		Result result = index.search(searchRequest);
		if (result.getNumFound() == 0)
			return mlt.like(new StringReader(""));
		int docId = result.getDocs()[0].doc;
		mlt.setMinWordLen(moreLikeThisMinWordLen);
		mlt.setMaxWordLen(moreLikeThisMaxWordLen);
		mlt.setMinDocFreq(moreLikeThisMinDocFreq);
		mlt.setMinTermFreq(moreLikeThisMinTermFreq);
		mlt.setFieldNames(moreLikeThisFieldList.toArrayName());
		mlt.setAnalyzer(checkAnalyzer());
		// TODO restore stop words
		// if (moreLikeThisStopWords != null)
		// mlt.setStopWords(getConfig().getStopWordsManager().getWords(
		// moreLikeThisStopWords));
		return mlt.like(docId);
	}

	public Query getQuery() throws ParseException, SyntaxError,
			SearchLibException, IOException {
		rwl.r.lock();
		try {
			if (complexQuery != null)
				return complexQuery;
		} finally {
			rwl.r.unlock();
		}
		rwl.w.lock();
		try {
			if (complexQuery != null)
				return complexQuery;
			if (isMoreLikeThis) {
				complexQuery = getMoreLikeThisQuery();
			} else {
				queryParser = getQueryParser();
				String fq = getFinalQuery();
				if (fq == null)
					return null;
				complexQuery = queryParser.parse(fq);
			}
			queryParsed = complexQuery.toString();
			if (scoreFunction != null)
				complexQuery = RootExpression.getQuery(complexQuery,
						scoreFunction);
			return complexQuery;
		} finally {
			rwl.w.unlock();
		}
	}

	private QueryParser getQueryParser() throws ParseException,
			SearchLibException {
		if (queryParser != null)
			return queryParser;
		Schema schema = getConfig().getSchema();
		Field field = schema.getFieldList().getDefaultField();
		if (field == null)
			throw new SearchLibException(
					"Please select a default field in the schema");
		queryParser = new QueryParser(Version.LUCENE_29, field.getName(),
				checkAnalyzer());
		queryParser.setAllowLeadingWildcard(allowLeadingWildcard);
		queryParser.setPhraseSlop(phraseSlop);
		queryParser.setDefaultOperator(defaultOperator);
		queryParser.setLowercaseExpandedTerms(false);
		return queryParser;
	}

	public String getQueryString() {
		rwl.r.lock();
		try {
			return queryString;
		} finally {
			rwl.r.unlock();
		}
	}

	public String getPatternQuery() {
		rwl.r.lock();
		try {
			return patternQuery;
		} finally {
			rwl.r.unlock();
		}
	}

	public void setPatternQuery(String value) {
		rwl.w.lock();
		try {
			patternQuery = value;
			complexQuery = null;
			primitiveQuery = null;
		} finally {
			rwl.w.unlock();
		}
	}

	public String getQueryParsed() throws ParseException, SyntaxError,
			SearchLibException, IOException {
		rwl.r.lock();
		try {
			getQuery();
			return queryParsed;
		} finally {
			rwl.r.unlock();
		}
	}

	public void setQueryString(String q) {
		rwl.w.lock();
		try {
			queryString = q;
			complexQuery = null;
			primitiveQuery = null;
		} finally {
			rwl.w.unlock();
		}
	}

	public String getScoreFunction() {
		rwl.r.lock();
		try {
			return scoreFunction;
		} finally {
			rwl.r.unlock();
		}
	}

	public void setScoreFunction(String v) {
		rwl.w.lock();
		try {
			if (v != null && v.trim().length() == 0)
				v = null;
			scoreFunction = v;
		} finally {
			rwl.w.unlock();
		}
	}

	public FilterList getFilterList() {
		rwl.r.lock();
		try {
			return this.filterList;
		} finally {
			rwl.r.unlock();
		}
	}

	public void addFilter(String req, boolean negative) throws ParseException {
		rwl.w.lock();
		try {
			this.filterList.add(req, negative, Filter.Source.REQUEST);
		} finally {
			rwl.w.unlock();
		}
	}

	public FieldList<SnippetField> getSnippetFieldList() {
		rwl.r.lock();
		try {
			return this.snippetFieldList;
		} finally {
			rwl.r.unlock();
		}
	}

	public FieldList<Field> getReturnFieldList() {
		rwl.r.lock();
		try {
			return this.returnFieldList;
		} finally {
			rwl.r.unlock();
		}
	}

	public void addReturnField(String fieldName) throws SearchLibException {
		rwl.w.lock();
		try {
			returnFieldList.add(new Field(config.getSchema().getFieldList()
					.get(fieldName)));
		} finally {
			rwl.w.unlock();
		}
	}

	public SortList getSortList() {
		rwl.r.lock();
		try {
			return this.sortList;
		} finally {
			rwl.r.unlock();
		}
	}

	public void addSort(String fieldName, boolean desc) {
		rwl.w.lock();
		try {
			sortList.add(fieldName, desc);
		} finally {
			rwl.w.unlock();
		}
	}

	public FieldList<FacetField> getFacetFieldList() {
		rwl.r.lock();
		try {
			return this.facetFieldList;
		} finally {
			rwl.r.unlock();
		}
	}

	public FieldList<SpellCheckField> getSpellCheckFieldList() {
		rwl.r.lock();
		try {
			return this.spellCheckFieldList;
		} finally {
			rwl.r.unlock();
		}
	}

	public void setCollapseField(String collapseField) {
		rwl.w.lock();
		try {
			this.collapseField = collapseField;
		} finally {
			rwl.w.unlock();
		}
	}

	public void setCollapseMax(int collapseMax) {
		rwl.w.lock();
		try {
			this.collapseMax = collapseMax;
		} finally {
			rwl.w.unlock();
		}
	}

	public String getCollapseField() {
		rwl.r.lock();
		try {
			return this.collapseField;
		} finally {
			rwl.r.unlock();
		}
	}

	public int getCollapseMax() {
		rwl.r.lock();
		try {
			return this.collapseMax;
		} finally {
			rwl.r.unlock();
		}
	}

	public int getStart() {
		rwl.r.lock();
		try {
			return this.start;
		} finally {
			rwl.r.unlock();
		}
	}

	public void setStart(int start) {
		rwl.w.lock();
		try {
			this.start = start;
		} finally {
			rwl.w.unlock();
		}
	}

	public boolean isWithDocument() {
		rwl.r.lock();
		try {
			return this.withDocuments;
		} finally {
			rwl.r.unlock();
		}
	}

	public void setWithDocument(boolean withDocuments) {
		rwl.w.lock();
		try {
			this.withDocuments = withDocuments;
		} finally {
			rwl.w.unlock();
		}
	}

	public boolean isWithSortValues() {
		rwl.r.lock();
		try {
			return withSortValues;
		} finally {
			rwl.r.unlock();
		}
	}

	public void setWithSortValues(boolean withSortValues) {
		rwl.w.lock();
		try {
			this.withSortValues = withSortValues;
		} finally {
			rwl.w.unlock();
		}
	}

	public void setLogReport(boolean withLogReport) {
		rwl.w.lock();
		try {
			this.withLogReport = withLogReport;
		} finally {
			rwl.w.unlock();
		}
	}

	public boolean isLogReport() {
		rwl.r.lock();
		try {
			return withLogReport;
		} finally {
			rwl.r.unlock();
		}
	}

	public void addCustomLog(String p) {
		rwl.w.lock();
		try {
			if (customLogs == null)
				customLogs = new ArrayList<String>(0);
			customLogs.add(p);
		} finally {
			rwl.w.unlock();
		}
	}

	public List<String> getCustomLogs() {
		rwl.r.lock();
		try {
			return customLogs;
		} finally {
			rwl.r.unlock();
		}
	}

	public int getRows() {
		rwl.r.lock();
		try {
			return this.rows;
		} finally {
			rwl.r.unlock();
		}
	}

	public LanguageEnum getLang() {
		rwl.r.lock();
		try {
			return this.lang;
		} finally {
			rwl.r.unlock();
		}
	}

	public void setRows(int rows) {
		rwl.w.lock();
		try {
			this.rows = rows;
		} finally {
			rwl.w.unlock();
		}
	}

	public int getEnd() {
		rwl.r.lock();
		try {
			return this.start + this.rows;
		} finally {
			rwl.r.unlock();
		}
	}

	@Override
	public String toString() {
		rwl.r.lock();
		try {
			StringBuffer sb = new StringBuffer();
			sb.append("RequestName: ");
			sb.append(requestName);
			sb.append(" DefaultOperator: ");
			sb.append(defaultOperator);
			sb.append(" Start: ");
			sb.append(start);
			sb.append(" Rows: ");
			sb.append(rows);
			sb.append(" Query: ");
			sb.append(complexQuery);
			sb.append(" Facet: " + getFacetFieldList().toString());
			if (getCollapseMode() != CollapseMode.COLLAPSE_OFF)
				sb.append(" Collapsing: " + getCollapseMode() + " "
						+ getCollapseField() + "(" + getCollapseMax() + ")");
			return sb.toString();
		} finally {
			rwl.r.unlock();
		}
	}

	public void setLang(LanguageEnum lang) {
		rwl.w.lock();
		try {
			if (this.lang == lang)
				return;
			this.lang = lang;
			analyzer = null;
		} finally {
			rwl.w.unlock();
		}
	}

	public FieldList<Field> getDocumentFieldList() {
		rwl.r.lock();
		try {
			if (documentFieldList != null)
				return documentFieldList;
		} finally {
			rwl.r.unlock();
		}
		rwl.w.lock();
		try {
			if (documentFieldList != null)
				return documentFieldList;
			documentFieldList = new FieldList<Field>(returnFieldList);
			Iterator<SnippetField> it = snippetFieldList.iterator();
			while (it.hasNext())
				documentFieldList.add(new Field(it.next()));
			return documentFieldList;
		} finally {
			rwl.w.unlock();
		}
	}

	public String getDefaultOperator() {
		rwl.r.lock();
		try {
			return defaultOperator.toString();
		} finally {
			rwl.r.unlock();
		}
	}

	public void setDefaultOperator(String value) {
		rwl.w.lock();
		try {
			if ("and".equalsIgnoreCase(value))
				defaultOperator = Operator.AND;
			else if ("or".equalsIgnoreCase(value))
				defaultOperator = Operator.OR;
		} finally {
			rwl.w.unlock();
		}
	}

	public void setCollapseMode(CollapseMode mode) {
		rwl.w.lock();
		try {
			this.collapseMode = mode;
		} finally {
			rwl.w.unlock();
		}
	}

	public CollapseMode getCollapseMode() {
		rwl.r.lock();
		try {
			return this.collapseMode;
		} finally {
			rwl.r.unlock();
		}
	}

	/**
	 * @return the moreLikeThisDocQuery
	 */
	public String getMoreLikeThisDocQuery() {
		rwl.r.lock();
		try {
			return moreLikeThisDocQuery;
		} finally {
			rwl.r.unlock();
		}
	}

	/**
	 * @return the isMoreLikeThis
	 */
	public boolean isMoreLikeThis() {
		rwl.r.lock();
		try {
			return isMoreLikeThis;
		} finally {
			rwl.r.unlock();
		}
	}

	/**
	 * @param isMoreLikeThis
	 *            the isMoreLikeThis to set
	 */
	public void setMoreLikeThis(boolean isMoreLikeThis) {
		rwl.w.lock();
		try {
			this.isMoreLikeThis = isMoreLikeThis;
		} finally {
			rwl.w.unlock();
		}
	}

	/**
	 * @param moreLikeThisDocQuery
	 *            the moreLikeThisDocQuery to set
	 */
	public void setMoreLikeThisDocQuery(String moreLikeThisDocQuery) {
		rwl.w.lock();
		try {
			this.moreLikeThisDocQuery = moreLikeThisDocQuery;
		} finally {
			rwl.w.unlock();
		}
	}

	/**
	 * @return the moreLikeThisFieldList
	 */
	public FieldList<Field> getMoreLikeThisFieldList() {
		rwl.r.lock();
		try {
			return moreLikeThisFieldList;
		} finally {
			rwl.r.unlock();
		}
	}

	/**
	 * @return the moreLikeThisMinWordLen
	 */
	public int getMoreLikeThisMinWordLen() {
		rwl.r.lock();
		try {
			return moreLikeThisMinWordLen;
		} finally {
			rwl.r.unlock();
		}
	}

	/**
	 * @param moreLikeThisMinWordLen
	 *            the moreLikeThisMinWordLen to set
	 */
	public void setMoreLikeThisMinWordLen(int moreLikeThisMinWordLen) {
		rwl.w.lock();
		try {
			this.moreLikeThisMinWordLen = moreLikeThisMinWordLen;
		} finally {
			rwl.w.unlock();
		}
	}

	/**
	 * @return the moreLikeThisMaxWordLen
	 */
	public int getMoreLikeThisMaxWordLen() {
		rwl.r.lock();
		try {
			return moreLikeThisMaxWordLen;
		} finally {
			rwl.r.unlock();
		}
	}

	/**
	 * @param moreLikeThisMaxWordLen
	 *            the moreLikeThisMaxWordLen to set
	 */
	public void setMoreLikeThisMaxWordLen(int moreLikeThisMaxWordLen) {
		rwl.w.lock();
		try {
			this.moreLikeThisMaxWordLen = moreLikeThisMaxWordLen;
		} finally {
			rwl.w.unlock();
		}
	}

	/**
	 * @return the moreLikeThisMinDocFreq
	 */
	public int getMoreLikeThisMinDocFreq() {
		rwl.r.lock();
		try {
			return moreLikeThisMinDocFreq;
		} finally {
			rwl.r.unlock();
		}
	}

	/**
	 * @param moreLikeThisMinDocFreq
	 *            the moreLikeThisMinDocFreq to set
	 */
	public void setMoreLikeThisMinDocFreq(int moreLikeThisMinDocFreq) {
		rwl.w.lock();
		try {
			this.moreLikeThisMinDocFreq = moreLikeThisMinDocFreq;
		} finally {
			rwl.w.unlock();
		}
	}

	/**
	 * @return the moreLikeThisMinTermFreq
	 */
	public int getMoreLikeThisMinTermFreq() {
		rwl.r.lock();
		try {
			return moreLikeThisMinTermFreq;
		} finally {
			rwl.r.unlock();
		}
	}

	/**
	 * @param moreLikeThisMinTermFreq
	 *            the moreLikeThisMinTermFreq to set
	 */
	public void setMoreLikeThisMinTermFreq(int moreLikeThisMinTermFreq) {
		rwl.w.lock();
		try {
			this.moreLikeThisMinTermFreq = moreLikeThisMinTermFreq;
		} finally {
			rwl.w.unlock();
		}
	}

	/**
	 * @return the moreLikeThisStopWords
	 */
	public String getMoreLikeThisStopWords() {
		rwl.r.lock();
		try {
			return moreLikeThisStopWords;
		} finally {
			rwl.r.unlock();
		}
	}

	/**
	 * @param moreLikeThisStopWords
	 *            the moreLikeThisStopWords to set
	 */
	public void setMoreLikeThisStopWords(String moreLikeThisStopWords) {
		rwl.w.lock();
		try {
			this.moreLikeThisStopWords = moreLikeThisStopWords;
		} finally {
			rwl.w.unlock();
		}
	}

	public final static String[] CONTROL_CHARS = { "\\", "^", "\"", "~", ":" };

	public final static String[] RANGE_CHARS = { "(", ")", "{", "}", "[", "]" };

	public final static String[] AND_OR_NOT_CHARS = { "+", "-", "&&", "||", "!" };

	public final static String[] WILDCARD_CHARS = { "*", "?" };

	public static String escapeQuery(String query, String[] escapeChars) {
		for (String s : escapeChars) {
			String r = "";
			for (int i = 0; i < s.length(); i++)
				r += "\\" + s.charAt(i);
			query = query.replace(s, r);
		}
		return query;
	}

	public static String escapeQuery(String query) {
		query = escapeQuery(query, CONTROL_CHARS);
		query = escapeQuery(query, RANGE_CHARS);
		query = escapeQuery(query, AND_OR_NOT_CHARS);
		query = escapeQuery(query, WILDCARD_CHARS);
		return query;
	}

	public boolean isFacet() {
		rwl.r.lock();
		try {
			if (facetFieldList == null)
				return false;
			return facetFieldList.size() > 0;
		} finally {
			rwl.r.unlock();
		}
	}

	public boolean isSpellCheck() {
		rwl.r.lock();
		try {
			if (spellCheckFieldList == null)
				return false;
			return spellCheckFieldList.size() > 0;
		} finally {
			rwl.r.unlock();
		}
	}

	public long getFinalTime() {
		rwl.r.lock();
		try {
			if (finalTime != 0)
				return finalTime;
		} finally {
			rwl.r.unlock();
		}
		rwl.w.lock();
		try {
			finalTime = timer.duration();
			return finalTime;
		} finally {
			rwl.w.unlock();
		}
	}

	public Timer getTimer() {
		rwl.r.lock();
		try {
			return timer;
		} finally {
			rwl.r.unlock();
		}
	}

	/**
	 * Construit un TemplateRequest bas� sur le noeud indiqu� dans le fichier de
	 * config XML.
	 * 
	 * @param config
	 * @param xpp
	 * @param parentNode
	 * @throws XPathExpressionException
	 * @throws ParseException
	 * @throws DOMException
	 * @throws ClassNotFoundException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	public static SearchRequest fromXmlConfig(Config config, XPathParser xpp,
			Node node) throws XPathExpressionException, DOMException,
			ParseException, InstantiationException, IllegalAccessException,
			ClassNotFoundException {
		if (node == null)
			return null;
		String name = XPathParser.getAttributeString(node, "name");
		SearchRequest searchRequest = new SearchRequest(config, name,
				("yes".equalsIgnoreCase(XPathParser.getAttributeString(node,
						"allowLeadingWildcard"))),
				XPathParser.getAttributeValue(node, "phraseSlop"),
				("and".equalsIgnoreCase(XPathParser.getAttributeString(node,
						"defaultOperator"))) ? QueryParser.AND_OPERATOR
						: QueryParser.OR_OPERATOR,
				XPathParser.getAttributeValue(node, "start"),
				XPathParser.getAttributeValue(node, "rows"),
				XPathParser.getAttributeString(node, "lang"),
				xpp.getNodeString(node, "query"), null, xpp.getNodeString(node,
						"scoreFunction"), false, true, false);

		searchRequest.setCollapseMode(CollapseMode.valueOfLabel(XPathParser
				.getAttributeString(node, "collapseMode")));
		searchRequest.setCollapseField(XPathParser.getAttributeString(node,
				"collapseField"));
		searchRequest.setCollapseMax(XPathParser.getAttributeValue(node,
				"collapseMax"));

		Node mltNode = xpp.getNode(node, "moreLikeThis");
		if (mltNode != null) {
			searchRequest.setMoreLikeThis("yes".equalsIgnoreCase(XPathParser
					.getAttributeString(mltNode, "active")));
			searchRequest.setMoreLikeThisMinWordLen(XPathParser
					.getAttributeValue(mltNode, "minWordLen"));
			searchRequest.setMoreLikeThisMaxWordLen(XPathParser
					.getAttributeValue(mltNode, "maxWordLen"));
			searchRequest.setMoreLikeThisMinTermFreq(XPathParser
					.getAttributeValue(mltNode, "minTermFreq"));
			searchRequest.setMoreLikeThisMinDocFreq(XPathParser
					.getAttributeValue(mltNode, "minDocFreq"));
			searchRequest.setMoreLikeThisStopWords(XPathParser
					.getAttributeString(mltNode, "stopWords"));

			NodeList mltFieldsNodes = xpp.getNodeList(mltNode, "fields/field");
			if (mltFieldsNodes != null) {
				FieldList<Field> moreLikeThisFields = searchRequest
						.getMoreLikeThisFieldList();
				for (int i = 0; i < mltFieldsNodes.getLength(); i++) {
					Field field = Field.fromXmlConfig(mltFieldsNodes.item(i));
					if (field != null)
						moreLikeThisFields.add(field);
				}
			}
			Node mltDocQueryNode = xpp.getNode(mltNode, "docQuery");
			if (mltDocQueryNode != null)
				searchRequest.setMoreLikeThisDocQuery(xpp
						.getNodeString(mltDocQueryNode));
		}

		FieldList<Field> returnFields = searchRequest.getReturnFieldList();
		FieldList<SchemaField> fieldList = config.getSchema().getFieldList();
		Field.filterCopy(fieldList, xpp.getNodeString(node, "returnFields"),
				returnFields);
		NodeList nodes = xpp.getNodeList(node, "returnFields/field");
		for (int i = 0; i < nodes.getLength(); i++) {
			Field field = Field.fromXmlConfig(nodes.item(i));
			if (field != null)
				returnFields.add(field);
		}

		FieldList<SnippetField> snippetFields = searchRequest
				.getSnippetFieldList();
		nodes = xpp.getNodeList(node, "snippet/field");
		for (int i = 0; i < nodes.getLength(); i++)
			SnippetField.copySnippetFields(nodes.item(i), fieldList,
					snippetFields);

		FieldList<FacetField> facetFields = searchRequest.getFacetFieldList();
		nodes = xpp.getNodeList(node, "facetFields/facetField");
		for (int i = 0; i < nodes.getLength(); i++)
			FacetField.copyFacetFields(nodes.item(i), fieldList, facetFields);

		FieldList<SpellCheckField> spellCheckFields = searchRequest
				.getSpellCheckFieldList();
		nodes = xpp.getNodeList(node, "spellCheckFields/spellCheckField");
		for (int i = 0; i < nodes.getLength(); i++)
			SpellCheckField.copySpellCheckFields(nodes.item(i), fieldList,
					spellCheckFields);

		FilterList filterList = searchRequest.getFilterList();
		nodes = xpp.getNodeList(node, "filters/filter");
		for (int i = 0; i < nodes.getLength(); i++) {
			Node n = nodes.item(i);
			filterList
					.add(xpp.getNodeString(n), "yes".equals(XPathParser
							.getAttributeString(n, "negative")),
							Source.CONFIGXML);
		}

		SortList sortList = searchRequest.getSortList();
		nodes = xpp.getNodeList(node, "sort/field");
		for (int i = 0; i < nodes.getLength(); i++) {
			node = nodes.item(i);
			String textNode = xpp.getNodeString(node);
			if (textNode != null && textNode.length() > 0)
				sortList.add(textNode);
			else
				sortList.add(new SortField(node));
		}
		return searchRequest;
	}

	public void writeXmlConfig(XmlWriter xmlWriter) throws SAXException {
		xmlWriter.startElement("request", "name", requestName, "phraseSlop",
				Integer.toString(phraseSlop), "defaultOperator",
				getDefaultOperator(), "start", Integer.toString(start), "rows",
				Integer.toString(rows), "lang", lang != null ? lang.getCode()
						: null, "collapseMode", collapseMode.getLabel(),
				"collapseField", collapseField, "collapseMax", Integer
						.toString(collapseMax));

		xmlWriter.startElement("moreLikeThis", "minWordLen",
				Integer.toString(moreLikeThisMinWordLen), "maxWordLen",
				Integer.toString(moreLikeThisMaxWordLen), "minDocFreq",
				Integer.toString(moreLikeThisMinDocFreq), "minTermFreq",
				Integer.toString(moreLikeThisMinTermFreq), "stopWords",
				moreLikeThisStopWords, "active", isMoreLikeThis ? "yes" : "no");
		if (moreLikeThisFieldList.size() > 0) {
			xmlWriter.startElement("fields");
			moreLikeThisFieldList.writeXmlConfig(xmlWriter);
			xmlWriter.endElement();
		}
		if (moreLikeThisDocQuery != null && moreLikeThisDocQuery.length() > 0) {
			xmlWriter.startElement("docQuery");
			xmlWriter.textNode(moreLikeThisDocQuery);
			xmlWriter.endElement();
		}
		xmlWriter.endElement();

		if (patternQuery != null && patternQuery.trim().length() > 0) {
			xmlWriter.startElement("query");
			xmlWriter.textNode(patternQuery);
			xmlWriter.endElement();
		}

		if (returnFieldList.size() > 0) {
			xmlWriter.startElement("returnFields");
			returnFieldList.writeXmlConfig(xmlWriter);
			xmlWriter.endElement();
		}

		if (snippetFieldList.size() > 0) {
			xmlWriter.startElement("snippet");
			snippetFieldList.writeXmlConfig(xmlWriter);
			xmlWriter.endElement();
		}

		if (facetFieldList.size() > 0) {
			xmlWriter.startElement("facetFields");
			facetFieldList.writeXmlConfig(xmlWriter);
			xmlWriter.endElement();
		}

		if (spellCheckFieldList.size() > 0) {
			xmlWriter.startElement("spellCheckFields");
			spellCheckFieldList.writeXmlConfig(xmlWriter);
			xmlWriter.endElement();
		}

		if (sortList.getFieldList().size() > 0) {
			xmlWriter.startElement("sort");
			sortList.getFieldList().writeXmlConfig(xmlWriter);
			xmlWriter.endElement();
		}

		if (filterList.size() > 0) {
			xmlWriter.startElement("filters");
			filterList.writeXmlConfig(xmlWriter);
			xmlWriter.endElement();
		}

		if (scoreFunction != null && scoreFunction.trim().length() > 0) {
			xmlWriter.startElement("scoreFunction");
			xmlWriter.textNode(scoreFunction);
			xmlWriter.endElement();
		}

		xmlWriter.endElement();
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {
		requestName = External.readUTF(in);
		filterList = External.readObject(in);
		allowLeadingWildcard = in.readBoolean();
		phraseSlop = in.readInt();

		if (in.readBoolean())
			defaultOperator = Operator.OR;
		else
			defaultOperator = Operator.AND;

		snippetFieldList = External.readObject(in);
		returnFieldList = External.readObject(in);
		documentFieldList = External.readObject(in);
		facetFieldList = External.readObject(in);
		spellCheckFieldList = External.readObject(in);
		sortList = External.readObject(in);
		collapseField = External.readUTF(in);
		collapseMax = in.readInt();
		collapseMode = CollapseMode.valueOf(in.readInt());
		start = in.readInt();
		rows = in.readInt();
		lang = LanguageEnum.findByCode(External.readUTF(in));
		queryString = External.readUTF(in);
		patternQuery = External.readUTF(in);
		scoreFunction = External.readUTF(in);
		queryParsed = External.readUTF(in);
		withDocuments = in.readBoolean();
		withSortValues = in.readBoolean();
		withLogReport = in.readBoolean();
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {

		External.writeUTF(requestName, out);
		External.writeObject(filterList, out);
		out.writeBoolean(allowLeadingWildcard);
		out.writeInt(phraseSlop);
		out.writeBoolean(defaultOperator == Operator.OR);

		External.writeObject(snippetFieldList, out);
		External.writeObject(returnFieldList, out);
		External.writeObject(documentFieldList, out);
		External.writeObject(facetFieldList, out);
		External.writeObject(spellCheckFieldList, out);
		External.writeObject(sortList, out);

		External.writeUTF(collapseField, out);
		out.writeInt(collapseMax);
		out.writeInt(collapseMode.code);

		out.writeInt(start);
		out.writeInt(rows);

		if (lang == null)
			External.writeUTF(LanguageEnum.UNDEFINED.getCode(), out);
		else
			External.writeUTF(lang.getCode(), out);
		External.writeUTF(queryString, out);
		External.writeUTF(patternQuery, out);
		External.writeUTF(scoreFunction, out);
		External.writeUTF(queryParsed, out);

		out.writeBoolean(withDocuments);
		out.writeBoolean(withSortValues);
		out.writeBoolean(withLogReport);
	}

}
