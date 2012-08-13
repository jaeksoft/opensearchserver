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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.xpath.XPathExpressionException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.queryParser.QueryParser.Operator;
import org.apache.lucene.search.Query;
import org.apache.lucene.util.Version;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.analysis.LanguageEnum;
import com.jaeksoft.searchlib.collapse.CollapseParameters;
import com.jaeksoft.searchlib.config.Config;
import com.jaeksoft.searchlib.facet.FacetField;
import com.jaeksoft.searchlib.filter.FilterAbstract;
import com.jaeksoft.searchlib.filter.FilterList;
import com.jaeksoft.searchlib.filter.GeoFilter;
import com.jaeksoft.searchlib.filter.QueryFilter;
import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.index.ReaderInterface;
import com.jaeksoft.searchlib.index.ReaderLocal;
import com.jaeksoft.searchlib.join.JoinList;
import com.jaeksoft.searchlib.query.ParseException;
import com.jaeksoft.searchlib.result.AbstractResult;
import com.jaeksoft.searchlib.result.ResultSearchSingle;
import com.jaeksoft.searchlib.schema.Field;
import com.jaeksoft.searchlib.schema.FieldList;
import com.jaeksoft.searchlib.schema.Schema;
import com.jaeksoft.searchlib.schema.SchemaField;
import com.jaeksoft.searchlib.schema.SchemaFieldList;
import com.jaeksoft.searchlib.scoring.AdvancedScore;
import com.jaeksoft.searchlib.snippet.SnippetField;
import com.jaeksoft.searchlib.sort.SortField;
import com.jaeksoft.searchlib.sort.SortList;
import com.jaeksoft.searchlib.util.StringUtils;
import com.jaeksoft.searchlib.util.XPathParser;
import com.jaeksoft.searchlib.util.XmlWriter;
import com.jaeksoft.searchlib.web.ServletTransaction;

public class SearchRequest extends AbstractRequest implements
		RequestInterfaces.ReturnedFieldInterface,
		RequestInterfaces.FilterListInterface {

	private transient QueryParser queryParser;
	private transient Query boostedComplexQuery;
	private transient Query snippetComplexQuery;
	private transient Query primitiveQuery;

	private transient Analyzer analyzer;

	private FilterList filterList;
	private JoinList joinList;
	private boolean allowLeadingWildcard;
	private int phraseSlop;
	private QueryParser.Operator defaultOperator;
	private FieldList<SnippetField> snippetFieldList;
	private FieldList<Field> returnFieldList;
	private FieldList<Field> documentFieldList;
	private FieldList<FacetField> facetFieldList;
	private List<BoostQuery> boostingQueries;
	private SortList sortList;
	private String collapseField;
	private int collapseMax;
	private CollapseParameters.Mode collapseMode;
	private CollapseParameters.Type collapseType;
	private int start;
	private int rows;
	private LanguageEnum lang;
	private String queryString;
	private String patternQuery;
	private AdvancedScore advancedScore;
	private String queryParsed;
	private boolean withSortValues;

	public SearchRequest() {
	}

	public SearchRequest(Config config) {
		super(config);
	}

	@Override
	protected void setDefaultValues() {
		super.setDefaultValues();
		this.filterList = new FilterList(this.config);
		this.joinList = new JoinList(this.config);
		this.queryParser = null;
		this.allowLeadingWildcard = false;
		this.phraseSlop = 10;
		this.defaultOperator = Operator.OR;
		this.snippetFieldList = new FieldList<SnippetField>();
		this.returnFieldList = new FieldList<Field>();
		this.sortList = new SortList();
		this.documentFieldList = null;
		this.facetFieldList = new FieldList<FacetField>();
		this.boostingQueries = new ArrayList<BoostQuery>(0);

		this.collapseField = null;
		this.collapseMax = 2;
		this.collapseMode = CollapseParameters.Mode.OFF;
		this.collapseType = CollapseParameters.Type.OPTIMIZED;

		this.start = 0;
		this.rows = 10;
		this.lang = null;
		this.snippetComplexQuery = null;
		this.boostedComplexQuery = null;
		this.primitiveQuery = null;
		this.analyzer = null;
		this.queryString = null;
		this.patternQuery = null;
		this.advancedScore = null;
		this.withSortValues = false;
		this.queryParsed = null;
	}

	@Override
	public void copyFrom(AbstractRequest request) {
		super.copyFrom(request);
		SearchRequest searchRequest = (SearchRequest) request;
		this.filterList = new FilterList(searchRequest.filterList);
		this.joinList = new JoinList(searchRequest.joinList);
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
		this.boostingQueries = new ArrayList<BoostQuery>(
				searchRequest.boostingQueries.size());
		for (BoostQuery boostQuery : searchRequest.boostingQueries)
			this.boostingQueries.add(new BoostQuery(boostQuery));

		this.collapseField = searchRequest.collapseField;
		this.collapseMax = searchRequest.collapseMax;
		this.collapseMode = searchRequest.collapseMode;
		this.collapseType = searchRequest.collapseType;

		this.withSortValues = searchRequest.withSortValues;
		this.start = searchRequest.start;
		this.rows = searchRequest.rows;
		this.lang = searchRequest.lang;
		this.snippetComplexQuery = null;
		this.boostedComplexQuery = null;
		this.primitiveQuery = null;
		this.analyzer = null;
		this.queryString = searchRequest.queryString;
		this.patternQuery = searchRequest.patternQuery;
		this.advancedScore = AdvancedScore.copy(searchRequest.advancedScore);
		this.queryParsed = null;
	}

	@Override
	public void reset() {
		rwl.w.lock();
		try {
			this.queryParsed = null;
			this.snippetComplexQuery = null;
			this.boostedComplexQuery = null;
			this.primitiveQuery = null;
			this.queryParser = null;
			this.analyzer = null;
		} finally {
			rwl.w.unlock();
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
			finalQuery = patternQuery;
			if (finalQuery.contains("$$$$")) {
				String escQuery = replaceControlChars(queryString);
				finalQuery = finalQuery.replace("$$$$", escQuery);
			}
			if (patternQuery.contains("$$$")) {
				String escQuery = escapeQuery(queryString);
				finalQuery = finalQuery.replace("$$$", escQuery);
			}
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
			getQuery();
			primitiveQuery = config.getIndex().rewrite(snippetComplexQuery);
			return primitiveQuery;
		} finally {
			rwl.w.unlock();
		}
	}

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

			queryParser = getQueryParser();
			String fq = getFinalQuery();
			if (fq == null)
				return null;
			try {
				boostedComplexQuery = queryParser.parse(fq);
			} catch (org.apache.lucene.queryParser.ParseException e) {
				throw new ParseException(e);
			}
			snippetComplexQuery = boostedComplexQuery;
			if (advancedScore != null && !advancedScore.isEmpty())
				boostedComplexQuery = advancedScore
						.getNewQuery(boostedComplexQuery);
			for (BoostQuery boostQuery : boostingQueries)
				boostedComplexQuery = boostQuery.getNewQuery(
						boostedComplexQuery, queryParser);
			queryParsed = boostedComplexQuery.toString();
			return boostedComplexQuery;
		} finally {
			rwl.w.unlock();
		}
	}

	public void setBoostedComplexQuery(Query query) {
		rwl.w.lock();
		try {
			boostedComplexQuery = query;
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
			boostedComplexQuery = null;
			snippetComplexQuery = null;
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
			boostedComplexQuery = null;
			snippetComplexQuery = null;
			primitiveQuery = null;
		} finally {
			rwl.w.unlock();
		}
	}

	public AdvancedScore getAdvancedScore() {
		rwl.r.lock();
		try {
			return advancedScore;
		} finally {
			rwl.r.unlock();
		}
	}

	public void setAdvancedScore(AdvancedScore advancedScore) {
		rwl.w.lock();
		try {
			this.advancedScore = advancedScore;
		} finally {
			rwl.w.unlock();
		}
	}

	public JoinList getJoinList() {
		rwl.r.lock();
		try {
			return this.joinList;
		} finally {
			rwl.r.unlock();
		}
	}

	@Override
	public FilterList getFilterList() {
		rwl.r.lock();
		try {
			return this.filterList;
		} finally {
			rwl.r.unlock();
		}
	}

	@Override
	public void addFilter(String req, boolean negative) throws ParseException {
		rwl.w.lock();
		try {
			this.filterList.add(new QueryFilter(req, negative,
					FilterAbstract.Source.REQUEST, null));
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

	@Override
	public FieldList<Field> getReturnFieldList() {
		rwl.r.lock();
		try {
			return this.returnFieldList;
		} finally {
			rwl.r.unlock();
		}
	}

	@Override
	public void addReturnField(String fieldName) {
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

	public boolean isScoreRequired() {
		rwl.r.lock();
		try {
			return this.sortList.isScore();
		} finally {
			rwl.r.unlock();
		}
	}

	public boolean isJoin() {
		rwl.r.lock();
		try {
			if (joinList == null)
				return false;
			return joinList.size() > 0;
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
			sb.append(getRequestName());
			sb.append(" DefaultOperator: ");
			sb.append(defaultOperator);
			sb.append(" Start: ");
			sb.append(start);
			sb.append(" Rows: ");
			sb.append(rows);
			sb.append(" Query: ");
			sb.append(boostedComplexQuery);
			sb.append(" Facet: " + getFacetFieldList().toString());
			if (getCollapseMode() != CollapseParameters.Mode.OFF)
				sb.append(" Collapsing Mode: " + getCollapseMode() + " Type: "
						+ getCollapseType() + " Field: " + getCollapseField()
						+ "(" + getCollapseMax() + ")");
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

	public void setCollapseMode(CollapseParameters.Mode mode) {
		rwl.w.lock();
		try {
			this.collapseMode = mode;
		} finally {
			rwl.w.unlock();
		}
	}

	public CollapseParameters.Mode getCollapseMode() {
		rwl.r.lock();
		try {
			return this.collapseMode;
		} finally {
			rwl.r.unlock();
		}
	}

	public void setCollapseType(CollapseParameters.Type type) {
		rwl.w.lock();
		try {
			this.collapseType = type;
		} finally {
			rwl.w.unlock();
		}
	}

	public CollapseParameters.Type getCollapseType() {
		rwl.r.lock();
		try {
			return this.collapseType;
		} finally {
			rwl.r.unlock();
		}
	}

	public final static String[] CONTROL_CHARS = { "\\", "^", "\"", "~", ":" };

	public final static String[] RANGE_CHARS = { "(", ")", "{", "}", "[", "]" };

	public final static String[] AND_OR_NOT_CHARS = { "+", "-", "&&", "||", "!" };

	public final static String[] WILDCARD_CHARS = { "*", "?" };

	final public static String escapeQuery(String query, String[] escapeChars) {
		for (String s : escapeChars) {
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < s.length(); i++) {
				sb.append('\\');
				sb.append(s.charAt(i));
			}
			query = query.replace(s, sb.toString());
		}
		return query;
	}

	final public static String escapeQuery(String query) {
		query = escapeQuery(query, CONTROL_CHARS);
		query = escapeQuery(query, RANGE_CHARS);
		query = escapeQuery(query, AND_OR_NOT_CHARS);
		query = escapeQuery(query, WILDCARD_CHARS);
		return query;
	}

	final public static String replaceControlChars(String query,
			String[] controlChars, String replaceChars) {
		for (String s : controlChars)
			query = query.replace(s, replaceChars);
		return query;
	}

	final public static String replaceControlChars(String query) {
		query = replaceControlChars(query, CONTROL_CHARS, " ");
		query = replaceControlChars(query, RANGE_CHARS, " ");
		query = replaceControlChars(query, AND_OR_NOT_CHARS, " ");
		query = replaceControlChars(query, WILDCARD_CHARS, " ");
		return StringUtils.replaceConsecutiveSpaces(query, " ");
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

	public BoostQuery[] getBoostingQueries() {
		rwl.r.lock();
		try {
			BoostQuery[] queries = new BoostQuery[boostingQueries.size()];
			return boostingQueries.toArray(queries);
		} finally {
			rwl.r.unlock();
		}
	}

	public void setBoostingQuery(BoostQuery oldOne, BoostQuery newOne) {
		rwl.w.lock();
		try {
			if (oldOne != null) {
				if (newOne == null)
					boostingQueries.remove(oldOne);
				else
					oldOne.copyFrom(newOne);
			} else {
				if (newOne != null)
					boostingQueries.add(newOne);
			}
		} finally {
			rwl.w.unlock();
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
	@Override
	public void fromXmlConfig(Config config, XPathParser xpp, Node requestNode)
			throws XPathExpressionException, DOMException, ParseException,
			InstantiationException, IllegalAccessException,
			ClassNotFoundException {
		rwl.w.lock();
		try {
			super.fromXmlConfig(config, xpp, requestNode);
			allowLeadingWildcard = "yes".equalsIgnoreCase(XPathParser
					.getAttributeString(requestNode, "allowLeadingWildcard"));
			setPhraseSlop(XPathParser.getAttributeValue(requestNode,
					"phraseSlop"));
			setDefaultOperator(XPathParser.getAttributeString(requestNode,
					"defaultOperator"));
			setStart(XPathParser.getAttributeValue(requestNode, "start"));
			setRows(XPathParser.getAttributeValue(requestNode, "rows"));
			setLang(LanguageEnum.findByCode(XPathParser.getAttributeString(
					requestNode, "lang")));
			setPatternQuery(xpp.getNodeString(requestNode, "query"));

			AdvancedScore advancedScore = AdvancedScore.fromXmlConfig(xpp,
					requestNode);
			if (advancedScore != null)
				setAdvancedScore(advancedScore);

			setCollapseMode(CollapseParameters.Mode.valueOfLabel(XPathParser
					.getAttributeString(requestNode, "collapseMode")));
			setCollapseType(CollapseParameters.Type.valueOfLabel(XPathParser
					.getAttributeString(requestNode, "collapseType")));
			setCollapseField(XPathParser.getAttributeString(requestNode,
					"collapseField"));
			setCollapseMax(XPathParser.getAttributeValue(requestNode,
					"collapseMax"));

			Node bqNode = xpp.getNode(requestNode, "boostingQueries");
			if (bqNode != null)
				BoostQuery.loadFromXml(xpp, bqNode, boostingQueries);

			FieldList<SchemaField> fieldList = config.getSchema()
					.getFieldList();
			Field.filterCopy(fieldList,
					xpp.getNodeString(requestNode, "returnFields"),
					returnFieldList);
			NodeList nodes = xpp.getNodeList(requestNode, "returnFields/field");
			for (int i = 0; i < nodes.getLength(); i++) {
				Field field = Field.fromXmlConfig(nodes.item(i));
				if (field != null)
					returnFieldList.add(field);
			}

			nodes = xpp.getNodeList(requestNode, "snippet/field");
			for (int i = 0; i < nodes.getLength(); i++)
				SnippetField.copySnippetFields(nodes.item(i), fieldList,
						snippetFieldList);

			nodes = xpp.getNodeList(requestNode, "facetFields/facetField");
			for (int i = 0; i < nodes.getLength(); i++)
				FacetField.copyFacetFields(nodes.item(i), fieldList,
						facetFieldList);

			nodes = xpp.getNodeList(requestNode, "filters/*");
			for (int i = 0; i < nodes.getLength(); i++) {
				Node node = nodes.item(i);
				String nodeName = node.getNodeName();
				if ("filter".equals(nodeName))
					filterList.add(new QueryFilter(xpp, node));
				else if ("geofilter".equals(nodeName))
					filterList.add(new GeoFilter(xpp, node));
			}

			nodes = xpp.getNodeList(requestNode, "joins/join");
			for (int i = 0; i < nodes.getLength(); i++)
				joinList.add(xpp, nodes.item(i));

			nodes = xpp.getNodeList(requestNode, "sort/field");
			for (int i = 0; i < nodes.getLength(); i++) {
				Node node = nodes.item(i);
				String textNode = xpp.getNodeString(node);
				if (textNode != null && textNode.length() > 0)
					sortList.add(textNode);
				else
					sortList.add(new SortField(node));
			}
		} finally {
			rwl.w.unlock();
		}
	}

	@Override
	public void writeXmlConfig(XmlWriter xmlWriter) throws SAXException {
		rwl.r.lock();
		try {
			xmlWriter.startElement(XML_NODE_REQUEST, XML_ATTR_NAME,
					getRequestName(), XML_ATTR_TYPE, getType().name(),
					"phraseSlop", Integer.toString(phraseSlop),
					"defaultOperator", getDefaultOperator(), "start",
					Integer.toString(start), "rows", Integer.toString(rows),
					"lang", lang != null ? lang.getCode() : null,
					"collapseMode", collapseMode.getLabel(), "collapseType",
					collapseType.getLabel(), "collapseField", collapseField,
					"collapseMax", Integer.toString(collapseMax));

			if (boostingQueries.size() > 0) {
				xmlWriter.startElement("boostingQueries");
				for (BoostQuery boostQuery : boostingQueries)
					boostQuery.writeXmlConfig(xmlWriter);
				xmlWriter.endElement();
			}

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

			if (joinList.size() > 0) {
				xmlWriter.startElement("joins");
				joinList.writeXmlConfig(xmlWriter);
				xmlWriter.endElement();
			}

			if (advancedScore != null)
				advancedScore.writeXmlConfig(xmlWriter);

			xmlWriter.endElement();
		} finally {
			rwl.r.unlock();
		}
	}

	@Override
	public RequestTypeEnum getType() {
		return RequestTypeEnum.SearchRequest;
	}

	@Override
	public void setFromServlet(ServletTransaction transaction)
			throws SyntaxError {
		rwl.w.lock();
		try {
			String p;
			Integer i;

			SchemaFieldList shemaFieldList = config.getSchema().getFieldList();

			if ((p = transaction.getParameterString("query")) != null)
				setQueryString(p);
			else if ((p = transaction.getParameterString("q")) != null)
				setQueryString(p);

			if ((i = transaction.getParameterInteger("start")) != null)
				setStart(i);

			if ((i = transaction.getParameterInteger("rows")) != null)
				setRows(i);

			if ((p = transaction.getParameterString("lang")) != null)
				setLang(LanguageEnum.findByCode(p));

			if ((p = transaction.getParameterString("collapse.mode")) != null)
				setCollapseMode(CollapseParameters.Mode.valueOfLabel(p));

			if ((p = transaction.getParameterString("collapse.type")) != null)
				setCollapseType(CollapseParameters.Type.valueOfLabel(p));

			if ((p = transaction.getParameterString("collapse.field")) != null)
				setCollapseField(shemaFieldList.get(p).getName());

			if ((i = transaction.getParameterInteger("collapse.max")) != null)
				setCollapseMax(i);

			if ((p = transaction.getParameterString("log")) != null)
				setLogReport(true);

			if ((p = transaction.getParameterString("operator")) != null)
				setDefaultOperator(p);

			if (joinList != null)
				joinList.setFromServlet(transaction);

			if (filterList != null)
				filterList.setFromServlet(transaction);

			if (isLogReport()) {
				for (int j = 1; j <= 10; j++) {
					p = transaction.getParameterString("log" + j);
					if (p == null)
						break;
					addCustomLog(p);
				}
			}

			if ((p = transaction.getParameterString("timer.minTime")) != null)
				setTimerMinTime(Integer.parseInt(p));

			if ((p = transaction.getParameterString("timer.maxDepth")) != null)
				setTimerMaxDepth(Integer.parseInt(p));

			String[] values;

			if ((values = transaction.getParameterValues("fq")) != null) {
				for (String value : values)
					if (value != null)
						if (value.trim().length() > 0)
							filterList.add(new QueryFilter(value, false,
									FilterAbstract.Source.REQUEST, null));
			}

			if ((values = transaction.getParameterValues("fqn")) != null) {
				for (String value : values)
					if (value != null)
						if (value.trim().length() > 0)
							filterList.add(new QueryFilter(value, true,
									FilterAbstract.Source.REQUEST, null));
			}

			if ((values = transaction.getParameterValues("rf")) != null) {
				for (String value : values)
					if (value != null)
						if (value.trim().length() > 0)
							returnFieldList.add(new Field(shemaFieldList
									.get(value)));
			}

			if ((values = transaction.getParameterValues("hl")) != null) {
				for (String value : values)
					snippetFieldList.add(new SnippetField(shemaFieldList.get(
							value).getName()));
			}

			if ((values = transaction.getParameterValues("fl")) != null) {
				for (String value : values)
					returnFieldList.add(shemaFieldList.get(value));
			}

			if ((values = transaction.getParameterValues("sort")) != null) {
				for (String value : values)
					sortList.add(value);
			}

			if ((values = transaction.getParameterValues("facet")) != null) {
				for (String value : values)
					facetFieldList.add(FacetField.buildFacetField(value, false,
							false));
			}
			if ((values = transaction.getParameterValues("facet.collapse")) != null) {
				for (String value : values)
					facetFieldList.add(FacetField.buildFacetField(value, false,
							true));
			}
			if ((values = transaction.getParameterValues("facet.multi")) != null) {
				for (String value : values)
					facetFieldList.add(FacetField.buildFacetField(value, true,
							false));
			}
			if ((values = transaction
					.getParameterValues("facet.multi.collapse")) != null) {
				for (String value : values)
					facetFieldList.add(FacetField.buildFacetField(value, true,
							true));
			}

		} finally {
			rwl.w.unlock();
		}

	}

	@Override
	public AbstractResult<?> execute(ReaderInterface reader)
			throws SearchLibException {
		try {
			return new ResultSearchSingle((ReaderLocal) reader, this);
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

	@Override
	public String getInfo() {
		rwl.r.lock();
		try {
			return patternQuery;
		} finally {
			rwl.r.unlock();
		}
	}

}
