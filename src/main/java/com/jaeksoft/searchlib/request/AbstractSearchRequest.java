/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008-2013 Emmanuel Keller / Jaeksoft
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
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.xpath.XPathExpressionException;

import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.util.Version;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.analysis.LanguageEnum;
import com.jaeksoft.searchlib.analysis.PerFieldAnalyzer;
import com.jaeksoft.searchlib.collapse.CollapseFunctionField;
import com.jaeksoft.searchlib.collapse.CollapseParameters;
import com.jaeksoft.searchlib.config.Config;
import com.jaeksoft.searchlib.facet.FacetField;
import com.jaeksoft.searchlib.facet.FacetFieldList;
import com.jaeksoft.searchlib.filter.FilterAbstract;
import com.jaeksoft.searchlib.filter.FilterList;
import com.jaeksoft.searchlib.filter.GeoFilter;
import com.jaeksoft.searchlib.filter.QueryFilter;
import com.jaeksoft.searchlib.filter.RelativeDateFilter;
import com.jaeksoft.searchlib.filter.TermFilter;
import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.geo.GeoParameters;
import com.jaeksoft.searchlib.index.ReaderInterface;
import com.jaeksoft.searchlib.index.ReaderLocal;
import com.jaeksoft.searchlib.join.JoinList;
import com.jaeksoft.searchlib.query.ParseException;
import com.jaeksoft.searchlib.result.AbstractResult;
import com.jaeksoft.searchlib.result.ResultSearchSingle;
import com.jaeksoft.searchlib.schema.Schema;
import com.jaeksoft.searchlib.schema.SchemaField;
import com.jaeksoft.searchlib.schema.SchemaFieldList;
import com.jaeksoft.searchlib.scoring.AdvancedScore;
import com.jaeksoft.searchlib.snippet.SnippetField;
import com.jaeksoft.searchlib.snippet.SnippetFieldList;
import com.jaeksoft.searchlib.sort.SortField;
import com.jaeksoft.searchlib.sort.SortFieldList;
import com.jaeksoft.searchlib.util.DomUtils;
import com.jaeksoft.searchlib.util.XPathParser;
import com.jaeksoft.searchlib.util.XmlWriter;
import com.jaeksoft.searchlib.web.ServletTransaction;
import com.jaeksoft.searchlib.webservice.query.search.SearchQueryAbstract.OperatorEnum;

public abstract class AbstractSearchRequest extends AbstractRequest implements
		RequestInterfaces.ReturnedFieldInterface,
		RequestInterfaces.FilterListInterface {

	private transient Query boostedComplexQuery;
	private transient Query snippetSimpleQuery;

	protected transient PerFieldAnalyzer analyzer;

	protected transient QueryParser queryParser;

	private FilterList filterList;
	private JoinList joinList;
	private boolean allowLeadingWildcard;
	protected int phraseSlop;
	protected OperatorEnum defaultOperator;
	private SnippetFieldList snippetFieldList;
	private ReturnFieldList returnFieldList;
	private FacetFieldList facetFieldList;
	private List<BoostQuery> boostingQueries;
	private SortFieldList sortFieldList;
	private String collapseField;
	private int collapseMax;
	private CollapseParameters.Mode collapseMode;
	private CollapseParameters.Type collapseType;
	private Set<CollapseFunctionField> collapseFunctionFields;
	private int start;
	private int rows;
	private LanguageEnum lang;
	private String queryString;
	private AdvancedScore advancedScore;
	private String queryParsed;
	private boolean withSortValues;
	protected boolean emptyReturnsAll;
	private final GeoParameters geoParameters = new GeoParameters();

	protected AbstractSearchRequest(Config config, RequestTypeEnum type) {
		super(config, type);
	}

	@Override
	protected void setDefaultValues() {
		super.setDefaultValues();
		this.queryParser = null;
		this.queryParsed = null;
		this.filterList = new FilterList(this.config);
		this.joinList = new JoinList(this.config);
		this.allowLeadingWildcard = false;
		this.phraseSlop = 10;
		this.defaultOperator = OperatorEnum.OR;
		this.snippetFieldList = new SnippetFieldList();
		this.returnFieldList = new ReturnFieldList();
		this.sortFieldList = new SortFieldList();
		this.facetFieldList = new FacetFieldList();
		this.boostingQueries = new ArrayList<BoostQuery>(0);

		this.collapseField = null;
		this.collapseMax = 2;
		this.collapseMode = CollapseParameters.Mode.OFF;
		this.collapseType = CollapseParameters.Type.OPTIMIZED;
		this.collapseFunctionFields = null;

		this.start = 0;
		this.rows = 10;
		this.lang = null;
		this.snippetSimpleQuery = null;
		this.boostedComplexQuery = null;
		this.analyzer = null;
		this.queryString = null;
		this.advancedScore = null;
		this.withSortValues = false;
		this.queryParsed = null;
		this.emptyReturnsAll = true;
	}

	@Override
	public void copyFrom(AbstractRequest request) {
		super.copyFrom(request);
		AbstractSearchRequest searchRequest = (AbstractSearchRequest) request;
		this.filterList = new FilterList(searchRequest.filterList);
		this.joinList = new JoinList(searchRequest.joinList);
		this.allowLeadingWildcard = searchRequest.allowLeadingWildcard;
		this.phraseSlop = searchRequest.phraseSlop;
		this.defaultOperator = searchRequest.defaultOperator;
		this.snippetFieldList = new SnippetFieldList(
				searchRequest.snippetFieldList);
		this.returnFieldList = new ReturnFieldList(
				searchRequest.returnFieldList);
		this.sortFieldList = new SortFieldList(searchRequest.sortFieldList);
		this.facetFieldList = new FacetFieldList(searchRequest.facetFieldList);
		this.boostingQueries = new ArrayList<BoostQuery>(
				searchRequest.boostingQueries.size());
		for (BoostQuery boostQuery : searchRequest.boostingQueries)
			this.boostingQueries.add(new BoostQuery(boostQuery));

		this.collapseField = searchRequest.collapseField;
		this.collapseMax = searchRequest.collapseMax;
		this.collapseMode = searchRequest.collapseMode;
		this.collapseType = searchRequest.collapseType;
		this.collapseFunctionFields = CollapseFunctionField
				.duplicate(searchRequest.collapseFunctionFields);
		this.geoParameters.set(searchRequest.geoParameters);

		this.withSortValues = searchRequest.withSortValues;
		this.start = searchRequest.start;
		this.rows = searchRequest.rows;
		this.lang = searchRequest.lang;
		this.snippetSimpleQuery = null;
		this.boostedComplexQuery = null;
		this.analyzer = null;
		this.queryString = searchRequest.queryString;
		this.advancedScore = AdvancedScore.copy(searchRequest.advancedScore);
		this.queryParsed = null;
		this.emptyReturnsAll = searchRequest.emptyReturnsAll;
	}

	@Override
	protected void resetNoLock() {
		this.queryParser = null;
		this.queryParsed = null;
		this.snippetSimpleQuery = null;
		this.boostedComplexQuery = null;
		this.analyzer = null;
		if (snippetFieldList != null)
			for (SnippetField snippetField : snippetFieldList)
				snippetField.reset();
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

	public boolean getEmptyReturnsAll() {
		rwl.r.lock();
		try {
			return emptyReturnsAll;
		} finally {
			rwl.r.unlock();
		}
	}

	public void setEmptyReturnsAll(boolean value) {
		rwl.w.lock();
		try {
			emptyReturnsAll = value;
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
			getQueryParser();
			checkAnalyzer();
			boostedComplexQuery = newComplexQuery(queryString);
			if (boostedComplexQuery == null)
				boostedComplexQuery = new BooleanQuery();
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

	public String getQueryString() {
		rwl.r.lock();
		try {
			return queryString;
		} finally {
			rwl.r.unlock();
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
			snippetSimpleQuery = null;
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

	public void addTermFilter(String field, String term, boolean negative) {
		rwl.w.lock();
		try {
			this.filterList.add(new TermFilter(field, term, negative,
					FilterAbstract.Source.REQUEST, null));
		} finally {
			rwl.w.unlock();
		}

	}

	final public void removeFilterSource(FilterAbstract.Source source) {
		rwl.w.lock();
		try {
			List<FilterAbstract<?>> toRemoveList = new ArrayList<FilterAbstract<?>>(
					0);
			for (FilterAbstract<?> filterAbstract : filterList)
				if (filterAbstract.getSource() == source)
					toRemoveList.add(filterAbstract);
			for (FilterAbstract<?> filterAbstract : toRemoveList)
				filterList.remove(filterAbstract);
		} finally {
			rwl.w.unlock();
		}
	}

	public SnippetFieldList getSnippetFieldList() {
		rwl.r.lock();
		try {
			return this.snippetFieldList;
		} finally {
			rwl.r.unlock();
		}
	}

	@Override
	public ReturnFieldList getReturnFieldList() {
		rwl.r.lock();
		try {
			return this.returnFieldList;
		} finally {
			rwl.r.unlock();
		}
	}

	private SchemaField getCheckSchemaField(SchemaFieldList schemaFieldList,
			String fieldName) throws SearchLibException {
		SchemaField schemaField = schemaFieldList.get(fieldName);
		if (schemaField == null)
			throw new SearchLibException("Returned field: The field: "
					+ fieldName + " does not exist");
		return schemaField;
	}

	private void addReturnFieldNoLock(SchemaFieldList schemaFieldList,
			String fieldName) throws SearchLibException {
		returnFieldList.put(new ReturnField(getCheckSchemaField(
				schemaFieldList, fieldName).getName()));
	}

	@Override
	public void addReturnField(String fieldName) throws SearchLibException {
		rwl.w.lock();
		try {
			addReturnFieldNoLock(config.getSchema().getFieldList(), fieldName);
		} finally {
			rwl.w.unlock();
		}
	}

	public SortFieldList getSortFieldList() {
		rwl.r.lock();
		try {
			return this.sortFieldList;
		} finally {
			rwl.r.unlock();
		}
	}

	public boolean isScoreRequired() {
		rwl.r.lock();
		try {
			return this.sortFieldList.isScore();
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
			sortFieldList.put(new SortField(fieldName, desc));
		} finally {
			rwl.w.unlock();
		}
	}

	public FacetFieldList getFacetFieldList() {
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

	public Collection<CollapseFunctionField> getCollapseFunctionFields() {
		rwl.r.lock();
		try {
			return this.collapseFunctionFields;
		} finally {
			rwl.r.unlock();
		}
	}

	public void addCollapseFunctionField(CollapseFunctionField functionField) {
		if (functionField == null)
			return;
		rwl.w.lock();
		try {
			if (collapseFunctionFields == null)
				collapseFunctionFields = new HashSet<CollapseFunctionField>();
			collapseFunctionFields
					.add(new CollapseFunctionField(functionField));
		} finally {
			rwl.w.unlock();
		}
	}

	public void removeCollapseFunctionField(CollapseFunctionField functionField) {
		rwl.w.lock();
		try {
			collapseFunctionFields.remove(functionField);
			if (collapseFunctionFields.size() == 0)
				collapseFunctionFields = null;
		} finally {
			rwl.w.unlock();
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
			StringBuilder sb = new StringBuilder();
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

	public String getDefaultOperator() {
		rwl.r.lock();
		try {
			return defaultOperator.toString();
		} finally {
			rwl.r.unlock();
		}
	}

	public void setDefaultOperator(String value) {
		setDefaultOperator(OperatorEnum.find(value));
	}

	public void setDefaultOperator(OperatorEnum operator) {
		rwl.w.lock();
		try {
			defaultOperator = operator;
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

	public GeoParameters getGeoParameters() {
		return geoParameters;
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

	@Override
	protected void fromXmlConfigNoLock(Config config, XPathParser xpp,
			Node requestNode) throws XPathExpressionException,
			InstantiationException, IllegalAccessException, DOMException,
			ParseException, ClassNotFoundException {
		super.fromXmlConfigNoLock(config, xpp, requestNode);
		allowLeadingWildcard = "yes".equalsIgnoreCase(XPathParser
				.getAttributeString(requestNode, "allowLeadingWildcard"));
		setPhraseSlop(XPathParser.getAttributeValue(requestNode, "phraseSlop"));
		setDefaultOperator(XPathParser.getAttributeString(requestNode,
				"defaultOperator"));
		setStart(XPathParser.getAttributeValue(requestNode, "start"));
		setRows(XPathParser.getAttributeValue(requestNode, "rows"));
		setLang(LanguageEnum.findByCode(XPathParser.getAttributeString(
				requestNode, "lang")));
		setEmptyReturnsAll(!"no".equalsIgnoreCase(DomUtils.getAttributeText(
				requestNode, "emtpyReturnsAll")));

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
		setCollapseMax(XPathParser
				.getAttributeValue(requestNode, "collapseMax"));

		NodeList nodes = xpp.getNodeList(requestNode, "collapseFunction");
		for (int i = 0; i < nodes.getLength(); i++)
			addCollapseFunctionField(CollapseFunctionField.fromXmlConfig(nodes
					.item(i)));

		Node geoNode = xpp.getNode(requestNode, "geoParameters");
		if (geoNode != null)
			geoParameters.set(geoNode);

		Node bqNode = xpp.getNode(requestNode, "boostingQueries");
		if (bqNode != null)
			BoostQuery.loadFromXml(xpp, bqNode, boostingQueries);

		SchemaFieldList fieldList = config.getSchema().getFieldList();
		returnFieldList.filterCopy(fieldList,
				xpp.getNodeString(requestNode, "returnFields"));
		nodes = xpp.getNodeList(requestNode, "returnFields/field");
		for (int i = 0; i < nodes.getLength(); i++) {
			ReturnField field = ReturnField.fromXmlConfig(nodes.item(i));
			if (field != null)
				returnFieldList.put(field);
		}

		nodes = xpp.getNodeList(requestNode, "snippet/field");
		for (int i = 0; i < nodes.getLength(); i++)
			SnippetField.copySnippetFields(nodes.item(i), fieldList,
					snippetFieldList);

		nodes = xpp.getNodeList(requestNode, "facetFields/facetField");
		for (int i = 0; i < nodes.getLength(); i++)
			FacetField
					.copyFacetFields(nodes.item(i), fieldList, facetFieldList);

		nodes = xpp.getNodeList(requestNode, "filters/*");
		for (int i = 0; i < nodes.getLength(); i++) {
			Node node = nodes.item(i);
			String nodeName = node.getNodeName();
			if ("filter".equals(nodeName))
				filterList.add(new QueryFilter(xpp, node));
			else if ("geofilter".equals(nodeName))
				filterList.add(new GeoFilter(xpp, node));
			else if ("relativeDateFilter".equals(nodeName))
				filterList.add(new RelativeDateFilter(xpp, node));
		}

		nodes = xpp.getNodeList(requestNode, "joins/join");
		for (int i = 0; i < nodes.getLength(); i++)
			joinList.add(xpp, nodes.item(i));

		nodes = xpp.getNodeList(requestNode, "sort/field");
		for (int i = 0; i < nodes.getLength(); i++) {
			Node node = nodes.item(i);
			String textNode = xpp.getNodeString(node, false);
			if (textNode != null && textNode.length() > 0)
				sortFieldList.put(new SortField(textNode, false));
			else
				sortFieldList.put(new SortField(node));
		}
	}

	protected abstract void writeSubXmlConfig(XmlWriter xmlWriter)
			throws SAXException;

	@Override
	public final void writeXmlConfig(XmlWriter xmlWriter) throws SAXException {
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
					"collapseMax", Integer.toString(collapseMax),
					"emptyReturnsAll", emptyReturnsAll ? "yes" : "no");

			if (collapseFunctionFields != null)
				for (CollapseFunctionField functionField : collapseFunctionFields)
					functionField.writeXmlConfig(xmlWriter, "collapseFunction");

			geoParameters.writeXmlConfig(xmlWriter, "geoParameters");

			if (boostingQueries.size() > 0) {
				xmlWriter.startElement("boostingQueries");
				for (BoostQuery boostQuery : boostingQueries)
					boostQuery.writeXmlConfig(xmlWriter);
				xmlWriter.endElement();
			}

			writeSubXmlConfig(xmlWriter);

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

			if (sortFieldList.size() > 0) {
				xmlWriter.startElement("sort");
				sortFieldList.writeXmlConfig(xmlWriter);
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
	public void setFromServletNoLock(ServletTransaction transaction)
			throws SyntaxError, SearchLibException {
		String p;
		Integer i;

		SchemaFieldList schemaFieldList = config.getSchema().getFieldList();

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
			setCollapseField(schemaFieldList.get(p).getName());

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

		filterList.addFromServlet(transaction, null);

		if ((values = transaction.getParameterValues("rf")) != null) {
			for (String value : values)
				if (value != null) {
					value = value.trim();
					if (value.length() > 0)
						addReturnFieldNoLock(schemaFieldList, value.trim());
				}
		}

		if ((values = transaction.getParameterValues("hl")) != null) {
			for (String value : values)
				snippetFieldList.put(new SnippetField(getCheckSchemaField(
						schemaFieldList, value).getName()));
		}

		if ((values = transaction.getParameterValues("fl")) != null) {
			for (String value : values)
				returnFieldList.put(new ReturnField(getCheckSchemaField(
						schemaFieldList, value).getName()));
		}

		if ((values = transaction.getParameterValues("sort")) != null) {
			for (String value : values)
				sortFieldList.put(new SortField(value));
		}

		for (int j = 1; j <= 10; j++) {
			p = transaction.getParameterString("sort" + j);
			if (p == null)
				break;
			sortFieldList.put(new SortField(p));
		}

		if ((values = transaction.getParameterValues("facet")) != null) {
			for (String value : values)
				facetFieldList.put(FacetField.buildFacetField(value, false,
						false));
		}
		if ((values = transaction.getParameterValues("facet.collapse")) != null) {
			for (String value : values)
				facetFieldList.put(FacetField.buildFacetField(value, false,
						true));
		}
		if ((values = transaction.getParameterValues("facet.multi")) != null) {
			for (String value : values)
				facetFieldList.put(FacetField.buildFacetField(value, true,
						false));
		}
		if ((values = transaction.getParameterValues("facet.multi.collapse")) != null) {
			for (String value : values)
				facetFieldList.put(FacetField
						.buildFacetField(value, true, true));
		}

		geoParameters.setFromServlet(transaction);
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
	public abstract String getInfo();

}
