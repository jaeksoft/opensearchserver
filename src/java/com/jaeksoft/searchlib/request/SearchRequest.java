/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2008-2009 Emmanuel Keller / Jaeksoft
 * 
 * http://www.open-search-server.com
 * 
 * This file is part of Jaeksoft OpenSearchServer.
 *
 * Jaeksoft OpenSearchServer is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * Jaeksoft OpenSearchServer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Jaeksoft OpenSearchServer. 
 *  If not, see <http://www.gnu.org/licenses/>.
 **/

package com.jaeksoft.searchlib.request;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Iterator;

import javax.xml.xpath.XPathExpressionException;

import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.queryParser.QueryParser.Operator;
import org.apache.lucene.search.Query;
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
import com.jaeksoft.searchlib.filter.FilterList;
import com.jaeksoft.searchlib.filter.Filter.Source;
import com.jaeksoft.searchlib.function.expression.RootExpression;
import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.index.ReaderInterface;
import com.jaeksoft.searchlib.schema.Field;
import com.jaeksoft.searchlib.schema.FieldList;
import com.jaeksoft.searchlib.schema.Schema;
import com.jaeksoft.searchlib.schema.SchemaField;
import com.jaeksoft.searchlib.snippet.SnippetField;
import com.jaeksoft.searchlib.sort.SortField;
import com.jaeksoft.searchlib.sort.SortList;
import com.jaeksoft.searchlib.spellcheck.SpellCheckField;
import com.jaeksoft.searchlib.util.External;
import com.jaeksoft.searchlib.util.Timer;
import com.jaeksoft.searchlib.util.XPathParser;
import com.jaeksoft.searchlib.util.XmlWriter;

public class SearchRequest implements Externalizable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 148522254171520640L;

	private transient QueryParser queryParser;
	private transient Query query;
	private transient Config config;
	private transient ReaderInterface reader;
	private transient Timer timer;
	private transient long finalTime;

	private String indexName;
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
	private int start;
	private int rows;
	private LanguageEnum lang;
	private String queryString;
	private String patternQuery;
	private String scoreFunction;
	private String queryParsed;
	private boolean delete;
	private boolean withDocuments;
	private boolean withSortValues;
	private boolean debug;

	public SearchRequest() {
		queryParser = null;
		config = null;
		reader = null;
		timer = null;
		finalTime = 0;
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
		this.snippetFieldList = new FieldList<SnippetField>();
		this.returnFieldList = new FieldList<Field>();
		this.sortList = new SortList();
		this.documentFieldList = null;
		this.facetFieldList = new FieldList<FacetField>();
		this.spellCheckFieldList = new FieldList<SpellCheckField>();
		this.collapseField = null;
		this.collapseMax = 2;
		this.collapseMode = CollapseMode.COLLAPSE_OFF;
		this.start = 0;
		this.rows = 10;
		this.lang = null;
		this.query = null;
		this.queryString = null;
		this.patternQuery = null;
		this.scoreFunction = null;
		this.delete = false;
		this.withDocuments = true;
		this.withSortValues = false;
		this.reader = null;
		this.queryParsed = null;
		this.timer = new Timer();
		this.finalTime = 0;
		this.debug = false;
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
		this.delete = searchRequest.delete;
		this.withDocuments = searchRequest.withDocuments;
		this.withSortValues = searchRequest.withSortValues;
		this.start = searchRequest.start;
		this.rows = searchRequest.rows;
		this.lang = searchRequest.lang;
		this.query = null;
		this.queryString = searchRequest.queryString;
		this.patternQuery = searchRequest.patternQuery;
		this.scoreFunction = searchRequest.scoreFunction;
		this.reader = searchRequest.reader;
		this.queryParsed = null;
		this.debug = searchRequest.debug;
	}

	public void reset() {
		this.queryParsed = null;
		this.query = null;
		this.queryParser = null;
	}

	private SearchRequest(Config config, String indexName, String requestName,
			boolean allowLeadingWildcard, int phraseSlop,
			QueryParser.Operator defaultOperator, int start, int rows,
			String codeLang, String patternQuery, String queryString,
			String scoreFunction, boolean delete, boolean withDocuments,
			boolean withSortValues, boolean noCache, boolean debug) {
		this(config);
		this.indexName = indexName;
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
		this.delete = delete;
		this.withDocuments = withDocuments;
		this.withSortValues = withSortValues;
		this.debug = debug;
	}

	public void init(Config config) {
		synchronized (this) {
			this.config = config;
			finalTime = 0;
			if (timer != null)
				timer.reset();
			timer = new Timer();
		}
	}

	protected QueryParser getNewQueryParser() {
		synchronized (this) {
			Schema schema = getConfig().getSchema();
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
			queryParser.setLowercaseExpandedTerms(false);
		}
	}

	public Config getConfig() {
		return this.config;
	}

	public String getRequestName() {
		return this.requestName;
	}

	public void setRequestName(String name) {
		this.requestName = name;
	}

	public int getPhraseSlop() {
		return phraseSlop;
	}

	public void setPhraseSlop(int value) {
		phraseSlop = value;
	}

	private String getFinalQuery() throws SyntaxError {
		String finalQuery;
		if (patternQuery != null && patternQuery.length() > 0)
			finalQuery = patternQuery.replace("$$", queryString);
		else
			finalQuery = queryString;

		if (finalQuery == null || finalQuery.length() == 0)
			throw new SyntaxError("No query");
		return finalQuery;
	}

	public Query getQuery() throws ParseException, SyntaxError {
		synchronized (this) {
			if (query != null)
				return query;
			getQueryParser();
			synchronized (queryParser) {
				query = queryParser.parse(getFinalQuery());
				queryParsed = query.toString();
				if (scoreFunction != null)
					query = RootExpression.getQuery(query, scoreFunction);
			}
			return query;
		}
	}

	public QueryParser getQueryParser() throws ParseException {
		synchronized (this) {
			if (queryParser != null)
				return queryParser;
			queryParser = getNewQueryParser();
			setQueryParser(this, queryParser);
			return queryParser;
		}
	}

	public String getQueryString() {
		return queryString;
	}

	public String getPatternQuery() {
		return patternQuery;
	}

	public void setPatternQuery(String value) {
		patternQuery = value;
	}

	public String getQueryParsed() throws ParseException, SyntaxError {
		getQuery();
		return queryParsed;
	}

	public void setQueryString(String q) {
		synchronized (this) {
			queryString = q;
			query = null;
		}
	}

	public String getScoreFunction() {
		return scoreFunction;
	}

	public void setScoreFunction(String v) {
		scoreFunction = v;
	}

	public FilterList getFilterList() {
		return this.filterList;
	}

	public void addFilter(String req) throws ParseException {
		this.filterList.add(req, Filter.Source.REQUEST);
	}

	public FieldList<SnippetField> getSnippetFieldList() {
		return this.snippetFieldList;
	}

	public FieldList<Field> getReturnFieldList() {
		return this.returnFieldList;
	}

	public void addReturnField(String fieldName) throws SearchLibException {
		returnFieldList.add(new Field(config.getSchema().getFieldList().get(
				fieldName)));
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

	public FieldList<SpellCheckField> getSpellCheckFieldList() {
		return this.spellCheckFieldList;
	}

	public void setCollapseField(String collapseField) {
		this.collapseField = collapseField;
	}

	public void setCollapseMax(int collapseMax) {
		this.collapseMax = collapseMax;
	}

	public String getCollapseField() {
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

	public boolean isWithSortValues() {
		return withSortValues;
	}

	public void setWithSortValues(boolean withSortValues) {
		this.withSortValues = withSortValues;
	}

	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	public boolean isDebug() {
		return debug;
	}

	public int getRows() {
		return this.rows;
	}

	public LanguageEnum getLang() {
		return this.lang;
	}

	public void setRows(int rows) {
		this.rows = rows;
	}

	public int getEnd() {
		return this.start + this.rows;
	}

	@Override
	public String toString() {
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
		sb.append(query);
		sb.append(" IndexName: ");
		sb.append(indexName);
		return sb.toString();
	}

	public void setLang(LanguageEnum lang) {
		this.lang = lang;
	}

	public FieldList<Field> getDocumentFieldList() {
		if (documentFieldList != null)
			return documentFieldList;
		documentFieldList = new FieldList<Field>(returnFieldList);
		Iterator<SnippetField> it = snippetFieldList.iterator();
		while (it.hasNext())
			documentFieldList.add(new Field(it.next()));
		return documentFieldList;
	}

	public String getDefaultOperator() {
		return defaultOperator.toString();
	}

	public void setDefaultOperator(String value) {
		if ("and".equalsIgnoreCase(value))
			defaultOperator = Operator.AND;
		else if ("or".equalsIgnoreCase(value))
			defaultOperator = Operator.OR;
	}

	public void setCollapseMode(CollapseMode mode) {
		this.collapseMode = mode;
	}

	public CollapseMode getCollapseMode() {
		return this.collapseMode;
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

	public boolean isSpellCheck() {
		if (spellCheckFieldList == null)
			return false;
		return spellCheckFieldList.size() > 0;
	}

	public long getFinalTime() {
		if (finalTime != 0)
			return finalTime;
		finalTime = timer.duration();
		return finalTime;
	}

	public Timer getTimer() {
		return timer;
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
						"scoreFunction"), false, true, false, false, false);

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
		for (int i = 0; i < nodes.getLength(); i++)
			filterList.add(xpp.getNodeString(nodes.item(i)), Source.CONFIGXML);

		SortList sortList = searchRequest.getSortList();
		nodes = xpp.getNodeList(node, "sort/field");
		for (int i = 0; i < nodes.getLength(); i++) {
			node = nodes.item(i);
			String textNode = xpp.getNodeString(node);
			if (textNode != null)
				sortList.add(textNode);
			else
				sortList.add(new SortField(node));
		}
		return searchRequest;
	}

	public void writeXmlConfig(XmlWriter xmlWriter) throws SAXException {
		xmlWriter.startElement("request", "name", requestName, "indexName",
				indexName, "phraseSlop", Integer.toString(phraseSlop),
				"defaultOperator", getDefaultOperator(), "start", Integer
						.toString(start), "rows", Integer.toString(rows),
				"lang", lang != null ? lang.getCode() : null);

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

	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {
		indexName = External.readUTF(in);
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
		delete = in.readBoolean();
		withDocuments = in.readBoolean();
		withSortValues = in.readBoolean();
		debug = in.readBoolean();
	}

	public void writeExternal(ObjectOutput out) throws IOException {

		External.writeUTF(indexName, out);
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

		External.writeUTF(lang.getCode(), out);
		External.writeUTF(queryString, out);
		External.writeUTF(patternQuery, out);
		External.writeUTF(scoreFunction, out);
		External.writeUTF(queryParsed, out);

		out.writeBoolean(delete);
		out.writeBoolean(withDocuments);
		out.writeBoolean(withSortValues);
		out.writeBoolean(debug);
	}

}
