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
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

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
import com.jaeksoft.searchlib.schema.FieldValue;
import com.jaeksoft.searchlib.schema.Schema;
import com.jaeksoft.searchlib.sort.SortField;
import com.jaeksoft.searchlib.sort.SortList;
import com.jaeksoft.searchlib.util.XmlInfo;

public class Request implements XmlInfo {

	private Request sourceRequest;
	private String name;
	private FilterList filterList;
	private QueryParser queryParser;
	private QueryParser highlightQueryParser;
	private boolean allowLeadingWildcard;
	private int phraseSlop;
	private QueryParser.Operator defaultOperator;
	private FieldList<HighlightField> highlightFieldList;
	private FieldList<FieldValue> returnFieldList;
	private FieldList<FieldValue> documentFieldList;
	private FieldList<FacetField> facetFieldList;
	private SortList sortList;
	private Field collapseField;
	private int collapseMax;
	private boolean collapseActive;
	private int start;
	private int rows;
	private String lang;
	private String queryString;
	private String highlightQueryString;
	private String scoreFunction;
	private Query query;
	private String queryParsed;
	private Query highlightQuery;
	private transient Config config;
	private boolean forceLocal;
	private List<Integer> docIds;
	private transient ReaderInterface reader;
	private boolean delete;
	private boolean withDocuments;

	public Request(Config config) {
		this.sourceRequest = null;
		this.config = config;
		this.name = null;
		this.filterList = new FilterList(this.config);
		this.queryParser = null;
		this.highlightQueryParser = null;
		this.allowLeadingWildcard = false;
		this.phraseSlop = 10;
		this.defaultOperator = Operator.OR;
		this.highlightFieldList = new FieldList<HighlightField>();
		this.returnFieldList = new FieldList<FieldValue>();
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
		this.docIds = null;
		this.highlightQueryString = null;
		this.queryString = null;
		this.scoreFunction = null;
		this.forceLocal = false;
		this.delete = false;
		this.withDocuments = false;
		this.reader = null;
		this.queryParsed = null;
		this.highlightQuery = null;
	}

	protected Request(Request request) {
		this(request.config);
		this.sourceRequest = request;
		this.name = request.name;
		this.filterList = new FilterList(request.filterList);
		this.queryParser = null;
		this.highlightQueryParser = null;
		this.allowLeadingWildcard = request.allowLeadingWildcard;
		this.phraseSlop = request.phraseSlop;
		this.defaultOperator = request.defaultOperator;
		this.highlightFieldList = new FieldList<HighlightField>(
				request.highlightFieldList);
		this.returnFieldList = new FieldList<FieldValue>(
				request.returnFieldList);
		this.sortList = new SortList(request.sortList);
		this.documentFieldList = null;
		if (request.documentFieldList != null)
			this.documentFieldList = new FieldList<FieldValue>(
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
		this.highlightQuery = request.highlightQuery;
		this.docIds = null;
		if (request.docIds != null)
			this.docIds = new ArrayList<Integer>(request.docIds);
		this.queryString = request.queryString;
		this.highlightQueryString = request.highlightQueryString;
		this.scoreFunction = request.scoreFunction;
		this.forceLocal = request.forceLocal;
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
		this.highlightQueryString = highlightQueryString;
		if (scoreFunction != null)
			if (scoreFunction.trim().length() == 0)
				scoreFunction = null;
		this.scoreFunction = scoreFunction;
		this.forceLocal = forceLocal;
		this.delete = delete;
		this.withDocuments = withDocuments;
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

	public ReaderInterface getReader() {
		return reader;
	}

	public void setReader(ReaderInterface reader) {
		this.reader = reader;
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

	public Query getHighlightQuery() throws ParseException {
		synchronized (this) {
			if (highlightQuery != null)
				return highlightQuery;
			if (highlightQueryParser == null) {
				highlightQueryParser = getNewHighlightQueryParser();
				setQueryParser(this, highlightQueryParser);
			}
			synchronized (highlightQueryParser) {
				highlightQuery = highlightQueryParser
						.parse(highlightQueryString);
			}
			return highlightQuery;
		}
	}

	public String getQueryString() {
		return queryString;
	}

	public String getHighlightQueryString() {
		return highlightQueryString;
	}

	public String getQueryParsed() throws ParseException, SyntaxError {
		getQuery();
		return queryParsed;
	}

	protected void setQueryStringNotEscaped(String q) {
		queryString = q;
	}

	protected void setHighlightQueryStringNotEscaped(String q) {
		highlightQueryString = q;
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

	public FieldList<FieldValue> getReturnFieldList() {
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

	public boolean getForceLocal() {
		return this.forceLocal;
	}

	public void setForceLocal(boolean forceLocal) {
		this.forceLocal = forceLocal;
	}

	public String getUrlQueryString() throws UnsupportedEncodingException {
		StringBuffer sb = new StringBuffer();
		sb.append("qt=");
		sb.append(name);
		sb.append("&start=");
		sb.append(start);
		sb.append("&rows=");
		sb.append(rows);
		sb.append("&q=");
		sb.append(URLEncoder.encode(queryString, "UTF-8"));
		for (Filter f : filterList) {
			if (f.getSource() == Filter.Source.REQUEST) {
				sb.append("&fq=");
				sb.append(URLEncoder.encode(f.getQueryString(), "UTF-8"));
			}
		}
		if (reader != null) {
			sb.append("&search=");
			sb.append(URLEncoder.encode(reader.getName(), "UTF-8"));
		}
		if (forceLocal)
			sb.append("&forceLocal");
		if (collapseField != null) {
			sb.append("&collapse.field=");
			sb.append(URLEncoder.encode(collapseField.getName(), "UTF-8"));
			sb.append("&collapse.max=");
			sb.append(this.collapseMax);
		}
		if (this.collapseActive)
			sb.append("&collapse.active=true");
		for (SortField f : sortList.getFieldList()) {
			sb.append("&sort=");
			if (f.isDesc())
				sb.append("-");
			sb.append(f.getName());
		}
		if (this.delete)
			sb.append("&delete");
		if (this.withDocuments)
			sb.append("&withDocs");
		if (docIds != null) {
			for (int docId : docIds) {
				sb.append("&docId=");
				sb.append(docId);
			}
		}
		return sb.toString();
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

	public FieldList<FieldValue> getDocumentFieldList() {
		if (documentFieldList != null)
			return documentFieldList;
		documentFieldList = new FieldList<FieldValue>(returnFieldList);
		Iterator<HighlightField> it = highlightFieldList.iterator();
		while (it.hasNext())
			documentFieldList.add(new FieldValue(it.next()));
		return documentFieldList;
	}

	public void addDocId(ReaderInterface reader, int docId) {
		if (!reader.sameIndex(this.reader))
			return;
		if (docIds == null)
			docIds = new ArrayList<Integer>();
		docIds.add(docId);
	}

	public List<Integer> getDocIds() {
		return this.docIds;
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
