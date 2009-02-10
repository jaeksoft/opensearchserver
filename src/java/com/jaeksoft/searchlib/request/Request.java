/**   
 * License Agreement for Jaeksoft SearchLib Community
 *
 * Copyright (C) 2008 Emmanuel Keller / Jaeksoft
 * 
 * http://www.jaeksoft.com
 * 
 * This file is part of Jaeksoft SearchLib Community.
 *
 * Jaeksoft SearchLib Community is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * Jaeksoft SearchLib Community is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Jaeksoft SearchLib Community. 
 *  If not, see <http://www.gnu.org/licenses/>.
 **/

package com.jaeksoft.searchlib.request;

import java.io.PrintWriter;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Iterator;

import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.queryParser.QueryParser.Operator;
import org.apache.lucene.search.Query;

import com.jaeksoft.searchlib.config.Config;
import com.jaeksoft.searchlib.facet.FacetField;
import com.jaeksoft.searchlib.filter.Filter;
import com.jaeksoft.searchlib.filter.FilterList;
import com.jaeksoft.searchlib.function.expression.RootExpression;
import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.highlight.HighlightField;
import com.jaeksoft.searchlib.index.ReaderInterface;
import com.jaeksoft.searchlib.schema.Field;
import com.jaeksoft.searchlib.schema.FieldList;
import com.jaeksoft.searchlib.schema.Schema;
import com.jaeksoft.searchlib.sort.SortList;
import com.jaeksoft.searchlib.util.XmlInfo;

public class Request implements XmlInfo, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 148522254171520640L;

	private transient Request sourceRequest;
	private transient QueryParser queryParser;
	private transient Config config;
	private transient ReaderInterface reader;

	private String name;
	private FilterList filterList;
	private boolean allowLeadingWildcard;
	private int phraseSlop;
	private QueryParser.Operator defaultOperator;
	private FieldList<HighlightField> highlightFieldList;
	private FieldList<Field> returnFieldList;
	private FieldList<Field> documentFieldList;
	private FieldList<FacetField> facetFieldList;
	private SortList sortList;
	private Field collapseField;
	private int collapseMax;
	private boolean collapseActive;
	private int start;
	private int rows;
	private String lang;
	private String queryString;
	private String scoreFunction;
	private Query query;
	private String queryParsed;
	private boolean delete;
	private boolean withDocuments;

	public Request(Config config) {
		this.sourceRequest = null;
		this.config = config;
		this.name = null;
		this.filterList = new FilterList(this.config);
		this.queryParser = null;
		this.allowLeadingWildcard = false;
		this.phraseSlop = 10;
		this.defaultOperator = Operator.OR;
		this.highlightFieldList = new FieldList<HighlightField>();
		this.returnFieldList = new FieldList<Field>();
		this.sortList = new SortList();
		this.documentFieldList = null;
		this.facetFieldList = new FieldList<FacetField>();
		this.collapseField = null;
		this.collapseMax = 2;
		this.collapseActive = false;
		this.start = 0;
		this.rows = 10;
		this.lang = null;
		this.query = null;
		this.queryString = null;
		this.scoreFunction = null;
		this.delete = false;
		this.withDocuments = true;
		this.reader = null;
		this.queryParsed = null;
	}

	protected Request(Request request) {
		this(request.config);
		this.sourceRequest = request;
		this.name = request.name;
		this.filterList = new FilterList(request.filterList);
		this.queryParser = null;
		this.allowLeadingWildcard = request.allowLeadingWildcard;
		this.phraseSlop = request.phraseSlop;
		this.defaultOperator = request.defaultOperator;
		this.highlightFieldList = new FieldList<HighlightField>(
				request.highlightFieldList);
		this.returnFieldList = new FieldList<Field>(request.returnFieldList);
		this.sortList = new SortList(request.sortList);
		this.documentFieldList = null;
		if (request.documentFieldList != null)
			this.documentFieldList = new FieldList<Field>(
					request.documentFieldList);
		this.facetFieldList = new FieldList<FacetField>(request.facetFieldList);
		this.collapseField = request.collapseField;
		this.collapseMax = request.collapseMax;
		this.collapseActive = request.collapseActive;
		this.delete = request.delete;
		this.withDocuments = request.withDocuments;
		this.start = request.start;
		this.rows = request.rows;
		this.lang = request.lang;
		this.query = request.query;
		this.queryString = request.queryString;
		this.scoreFunction = request.scoreFunction;
		this.reader = request.reader;
		this.queryParsed = null;
	}

	protected Request(Config config, String name, boolean allowLeadingWildcard,
			int phraseSlop, QueryParser.Operator defaultOperator, int start,
			int rows, String lang, String queryString,
			String highlightQueryString, String scoreFunction,
			boolean forceLocal, boolean delete, boolean withDocuments) {
		this(config);
		this.name = name;
		this.allowLeadingWildcard = allowLeadingWildcard;
		this.phraseSlop = phraseSlop;
		this.defaultOperator = defaultOperator;
		this.start = start;
		this.rows = rows;
		this.lang = lang;
		this.queryString = queryString;
		if (scoreFunction != null)
			if (scoreFunction.trim().length() == 0)
				scoreFunction = null;
		this.scoreFunction = scoreFunction;
		this.delete = delete;
		this.withDocuments = withDocuments;
	}

	public void setConfig(Config config) {
		this.config = config;
	}

	@Override
	public Request clone() {
		return new Request(this);
	}

	protected QueryParser getNewQueryParser() {
		synchronized (this) {
			Schema schema = this.getConfig().getSchema();
			return new QueryParser(schema.getFieldList().getDefaultField()
					.getName(), schema.getQueryPerFieldAnalyzer(getLang()));
		}
	}

	protected QueryParser getNewHighlightQueryParser() {
		synchronized (this) {
			Schema schema = this.getConfig().getSchema();
			return new QueryParser(schema.getFieldList().getDefaultField()
					.getName(), schema.getQueryPerFieldAnalyzer(getLang()));
		}
	}

	private static void setQueryParser(Request req, QueryParser queryParser) {
		synchronized (queryParser) {
			queryParser.setAllowLeadingWildcard(req.allowLeadingWildcard);
			queryParser.setPhraseSlop(req.phraseSlop);
			queryParser.setDefaultOperator(req.defaultOperator);
		}
	}

	public Config getConfig() {
		return this.config;
	}

	public String getName() {
		return this.name;
	}

	public Query getQuery() throws ParseException, SyntaxError {
		synchronized (this) {
			if (query != null)
				return query;
			if (queryParser == null) {
				queryParser = getNewQueryParser();
				setQueryParser(this, queryParser);
			}
			synchronized (queryParser) {
				query = queryParser.parse(queryString);
				queryParsed = query.toString();
				if (scoreFunction != null)
					query = RootExpression.getQuery(query, scoreFunction);
			}
			return query;
		}
	}

	public String getQueryString() {
		return queryString;
	}

	public String getQueryParsed() throws ParseException, SyntaxError {
		getQuery();
		return queryParsed;
	}

	protected void setQueryStringNotEscaped(String q) {
		queryString = q;
	}

	public void setQueryString(String q) {
		synchronized (this) {
			queryString = q;
			query = null;
		}
	}

	public Request getSourceRequest() {
		return this.sourceRequest;
	}

	public FilterList getFilterList() {
		return this.filterList;
	}

	public void addFilter(String req) throws ParseException {
		this.filterList.add(req, Filter.Source.REQUEST);
	}

	public FieldList<HighlightField> getHighlightFieldList() {
		return this.highlightFieldList;
	}

	public FieldList<Field> getReturnFieldList() {
		return this.returnFieldList;
	}

	public SortList getSortList() {
		return this.sortList;
	}

	public void addSort(String fieldName, boolean desc) {
		sortList.add(fieldName, desc);
	}

	public FieldList<FacetField> getFacetFieldList() {
		return this.facetFieldList;
	}

	public void setCollapseField(Field collapseField) {
		this.collapseField = collapseField;
	}

	public void setCollapseMax(int collapseMax) {
		this.collapseMax = collapseMax;
	}

	public Field getCollapseField() {
		return this.collapseField;
	}

	public int getCollapseMax() {
		return this.collapseMax;
	}

	public int getStart() {
		return this.start;
	}

	public void setStart(int start) {
		this.start = start;
	}

	public boolean isDelete() {
		return this.delete;
	}

	public void setDelete(boolean delete) {
		this.delete = delete;
	}

	public boolean isWithDocument() {
		return this.withDocuments;
	}

	public void setWithDocument(boolean withDocuments) {
		this.withDocuments = withDocuments;
	}

	public int getRows() {
		return this.rows;
	}

	public String getLang() {
		return this.lang;
	}

	public void setRows(int rows) {
		this.rows = rows;
	}

	public int getEnd() {
		return this.start + this.rows;
	}

	public void xmlInfo(PrintWriter writer, HashSet<String> classDetail) {
		writer.println("<request name=\"" + name + "\" defaultOperator=\""
				+ defaultOperator + "\" start=\"" + start + "\" rows=\"" + rows
				+ "\">");
		writer.println("<query>" + queryString + "</query>");
		if (returnFieldList.size() > 0)
			writer.println("<returnsField>" + returnFieldList.toString()
					+ "</returnsField>");
		writer.println("</request>");

	}

	public void setLang(String p) {
		this.lang = p;
	}

	public FieldList<Field> getDocumentFieldList() {
		if (documentFieldList != null)
			return documentFieldList;
		documentFieldList = new FieldList<Field>(returnFieldList);
		Iterator<HighlightField> it = highlightFieldList.iterator();
		while (it.hasNext())
			documentFieldList.add(new Field(it.next()));
		return documentFieldList;
	}

	public void setCollapseActive(boolean active) {
		this.collapseActive = active;
	}

	public boolean getCollapseActive() {
		return this.collapseActive;
	}

	public static String escapeQuery(String query) {
		String[] escapedString = { "\\", "+", "-", "&&", "||", "!", "(", ")",
				"{", "}", "[", "]", "^", "\"", "~", "*", "?", ":" };
		for (String s : escapedString)
			query = query.replace(s, "\\" + s);
		return query;
	}

	public boolean isFacet() {
		if (facetFieldList == null)
			return false;
		return facetFieldList.size() > 0;
	}

}
