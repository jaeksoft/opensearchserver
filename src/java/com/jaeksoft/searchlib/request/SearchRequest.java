/**   
 * License Agreement for Jaeksoft SearchLib Community
 *
 * Copyright (C) 2008-2009 Emmanuel Keller / Jaeksoft
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

import javax.xml.xpath.XPathExpressionException;

import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.queryParser.QueryParser.Operator;
import org.apache.lucene.search.Query;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.jaeksoft.searchlib.config.Config;
import com.jaeksoft.searchlib.facet.FacetField;
import com.jaeksoft.searchlib.filter.Filter;
import com.jaeksoft.searchlib.filter.FilterList;
import com.jaeksoft.searchlib.filter.Filter.Source;
import com.jaeksoft.searchlib.function.expression.RootExpression;
import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.highlight.HighlightField;
import com.jaeksoft.searchlib.index.ReaderInterface;
import com.jaeksoft.searchlib.schema.Field;
import com.jaeksoft.searchlib.schema.FieldList;
import com.jaeksoft.searchlib.schema.Schema;
import com.jaeksoft.searchlib.schema.SchemaField;
import com.jaeksoft.searchlib.sort.SortList;
import com.jaeksoft.searchlib.util.Timer;
import com.jaeksoft.searchlib.util.XPathParser;
import com.jaeksoft.searchlib.util.XmlInfo;

public class SearchRequest implements XmlInfo, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 148522254171520640L;

	private transient QueryParser queryParser;
	private transient Config config;
	private transient ReaderInterface reader;
	private transient Timer timer;

	private String indexName;
	private String requestName;
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
	private String patternQuery;
	private String scoreFunction;
	private Query query;
	private String queryParsed;
	private boolean delete;
	private boolean withDocuments;
	private long finalTime;

	public SearchRequest() {
	}

	public SearchRequest(Config config) {
		this.config = config;
		this.indexName = null;
		this.requestName = null;
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
		this.patternQuery = null;
		this.scoreFunction = null;
		this.delete = false;
		this.withDocuments = true;
		this.reader = null;
		this.queryParsed = null;
		this.timer = new Timer();
		this.finalTime = 0;
	}

	public SearchRequest(SearchRequest searchRequest) {
		this(searchRequest.config);
		this.indexName = searchRequest.indexName;
		this.requestName = searchRequest.requestName;
		this.filterList = new FilterList(searchRequest.filterList);
		this.queryParser = null;
		this.allowLeadingWildcard = searchRequest.allowLeadingWildcard;
		this.phraseSlop = searchRequest.phraseSlop;
		this.defaultOperator = searchRequest.defaultOperator;
		this.highlightFieldList = new FieldList<HighlightField>(
				searchRequest.highlightFieldList);
		this.returnFieldList = new FieldList<Field>(
				searchRequest.returnFieldList);
		this.sortList = new SortList(searchRequest.sortList);
		this.documentFieldList = null;
		if (searchRequest.documentFieldList != null)
			this.documentFieldList = new FieldList<Field>(
					searchRequest.documentFieldList);
		this.facetFieldList = new FieldList<FacetField>(
				searchRequest.facetFieldList);
		this.collapseField = searchRequest.collapseField;
		this.collapseMax = searchRequest.collapseMax;
		this.collapseActive = searchRequest.collapseActive;
		this.delete = searchRequest.delete;
		this.withDocuments = searchRequest.withDocuments;
		this.start = searchRequest.start;
		this.rows = searchRequest.rows;
		this.lang = searchRequest.lang;
		this.query = searchRequest.query;
		this.queryString = searchRequest.queryString;
		this.patternQuery = searchRequest.patternQuery;
		this.scoreFunction = searchRequest.scoreFunction;
		this.reader = searchRequest.reader;
		this.queryParsed = null;
	}

	private SearchRequest(Config config, String indexName, String requestName,
			boolean allowLeadingWildcard, int phraseSlop,
			QueryParser.Operator defaultOperator, int start, int rows,
			String lang, String patternQuery, String queryString,
			String scoreFunction, boolean delete, boolean withDocuments) {
		this(config);
		this.indexName = indexName;
		this.requestName = requestName;
		this.allowLeadingWildcard = allowLeadingWildcard;
		this.phraseSlop = phraseSlop;
		this.defaultOperator = defaultOperator;
		this.start = start;
		this.rows = rows;
		this.lang = lang;
		this.queryString = queryString;
		this.patternQuery = patternQuery;
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

	private static void setQueryParser(SearchRequest searchRequest,
			QueryParser queryParser) {
		synchronized (queryParser) {
			queryParser
					.setAllowLeadingWildcard(searchRequest.allowLeadingWildcard);
			queryParser.setPhraseSlop(searchRequest.phraseSlop);
			queryParser.setDefaultOperator(searchRequest.defaultOperator);
		}
	}

	public Config getConfig() {
		return this.config;
	}

	public String getRequestName() {
		return this.requestName;
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

	public void setQueryString(String q) {
		synchronized (this) {
			if (patternQuery != null)
				queryString = patternQuery.replace("$$", q);
			else
				queryString = q;
			query = null;
		}
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
		this.collapseField = new Field(collapseField);
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

	public void setEnd(int end) {
		if (end < start)
			end = start;
		this.rows = end - this.start;
	}

	public void xmlInfo(PrintWriter writer, HashSet<String> classDetail) {
		writer.println("<request name=\"" + requestName
				+ "\" defaultOperator=\"" + defaultOperator + "\" start=\""
				+ start + "\" rows=\"" + rows + "\">");
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

	public long getFinalTime() {
		if (finalTime != 0)
			return finalTime;
		finalTime = timer.duration();
		return finalTime;
	}

	public String getIndexName() {
		return indexName;
	}

	public void setIndexName(String indexName) {
		this.indexName = indexName;
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
		String indexName = XPathParser.getAttributeString(node, "indexName");
		SearchRequest searchRequest = new SearchRequest(config, indexName,
				name, false, XPathParser.getAttributeValue(node, "phraseSlop"),
				("and".equals(XPathParser.getAttributeString(node,
						"defaultOperator"))) ? QueryParser.AND_OPERATOR
						: QueryParser.OR_OPERATOR, XPathParser
						.getAttributeValue(node, "start"), XPathParser
						.getAttributeValue(node, "rows"), XPathParser
						.getAttributeString(node, "lang"), xpp.getNodeString(
						node, "query"), null, xpp.getNodeString(node,
						"scoreFunction"), false, true);

		FieldList<Field> returnFields = searchRequest.getReturnFieldList();
		FieldList<SchemaField> fieldList = config.getSchema().getFieldList();
		Field.filterCopy(fieldList, xpp.getNodeString(node, "returnFields"),
				returnFields);

		FieldList<HighlightField> highlightFields = searchRequest
				.getHighlightFieldList();
		NodeList nodes = xpp.getNodeList(node, "highlighting/field");
		for (int i = 0; i < nodes.getLength(); i++)
			HighlightField.copyHighlightFields(nodes.item(i), fieldList,
					highlightFields);

		FieldList<FacetField> facetFields = searchRequest.getFacetFieldList();
		nodes = xpp.getNodeList(node, "facetFields/facetField");
		for (int i = 0; i < nodes.getLength(); i++)
			FacetField.copyFacetFields(nodes.item(i), fieldList, facetFields);

		FilterList filterList = searchRequest.getFilterList();
		nodes = xpp.getNodeList(node, "filters/filter");
		for (int i = 0; i < nodes.getLength(); i++)
			filterList.add(xpp.getNodeString(nodes.item(i)), Source.CONFIGXML);

		SortList sortList = searchRequest.getSortList();
		nodes = xpp.getNodeList(node, "sort/field");
		for (int i = 0; i < nodes.getLength(); i++)
			sortList.add(xpp.getNodeString(nodes.item(i)));
		return searchRequest;
	}

}
