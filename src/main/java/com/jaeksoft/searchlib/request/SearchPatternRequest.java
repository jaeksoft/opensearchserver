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

import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Query;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.config.Config;
import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.query.ParseException;
import com.jaeksoft.searchlib.query.QueryUtils;
import com.jaeksoft.searchlib.util.XPathParser;
import com.jaeksoft.searchlib.util.XmlWriter;

public class SearchPatternRequest extends AbstractSearchRequest implements
		RequestInterfaces.ReturnedFieldInterface,
		RequestInterfaces.FilterListInterface {

	private String snippetPatternQuery;
	private String searchPatternQuery;

	public SearchPatternRequest() {
		super(null, RequestTypeEnum.SearchRequest);
	}

	public SearchPatternRequest(Config config) {
		super(config, RequestTypeEnum.SearchRequest);
	}

	@Override
	protected void setDefaultValues() {
		super.setDefaultValues();
		this.snippetPatternQuery = null;
		this.searchPatternQuery = null;
	}

	@Override
	public void copyFrom(AbstractRequest request) {
		super.copyFrom(request);
		SearchPatternRequest searchRequest = (SearchPatternRequest) request;
		this.searchPatternQuery = searchRequest.searchPatternQuery;
		this.snippetPatternQuery = searchRequest.snippetPatternQuery;
	}

	private final static String getFinalQuery(String patternQuery,
			String queryString) {
		if (patternQuery == null || patternQuery.length() == 0
				|| queryString == null)
			return queryString;
		String finalQuery = patternQuery;
		if (finalQuery.contains("$$$$")) {
			String escQuery = QueryUtils.replaceControlChars(queryString);
			finalQuery = finalQuery.replace("$$$$", escQuery);
		}
		if (patternQuery.contains("$$$")) {
			String escQuery = QueryUtils.escapeQuery(queryString);
			finalQuery = finalQuery.replace("$$$", escQuery);
		}
		finalQuery = finalQuery.replace("$$", queryString);
		return finalQuery;
	}

	@Override
	protected Query newSnippetQuery(String queryString) throws IOException,
			ParseException, SyntaxError, SearchLibException {
		if (emptyReturnsAll && StringUtils.isEmpty(queryString))
			queryString = "*:*";
		String q = snippetPatternQuery == null
				|| snippetPatternQuery.length() == 0 ? searchPatternQuery
				: snippetPatternQuery;
		String fq = getFinalQuery(q, queryString);
		if (fq == null)
			return null;
		Query complexQuery = getParsedQuery(queryParser, fq);
		return config.getIndexAbstract().rewrite(complexQuery);
	}

	private final static Query getParsedQuery(QueryParser queryParser,
			String finalQuery) throws ParseException {
		try {
			return queryParser.parse(finalQuery);
		} catch (org.apache.lucene.queryParser.ParseException e) {
			throw new ParseException(e);
		}
	}

	@Override
	public Query newComplexQuery(String queryString) throws ParseException,
			SyntaxError, SearchLibException, IOException {
		if (emptyReturnsAll && StringUtils.isEmpty(queryString))
			queryString = "*:*";
		String fq = getFinalQuery(searchPatternQuery, queryString);
		if (fq == null)
			return null;
		return getParsedQuery(queryParser, fq);
	}

	public String getPatternQuery() {
		rwl.r.lock();
		try {
			return searchPatternQuery;
		} finally {
			rwl.r.unlock();
		}
	}

	public void setPatternQuery(String value) {
		rwl.w.lock();
		try {
			searchPatternQuery = value;
			resetNoLock();
		} finally {
			rwl.w.unlock();
		}
	}

	public String getSnippetPatternQuery() {
		rwl.r.lock();
		try {
			return snippetPatternQuery;
		} finally {
			rwl.r.unlock();
		}
	}

	public void setSnippetPatternQuery(String value) {
		rwl.w.lock();
		try {
			snippetPatternQuery = value;
			resetNoLock();
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
	public void fromXmlConfigNoLock(Config config, XPathParser xpp,
			Node requestNode) throws XPathExpressionException, DOMException,
			ParseException, InstantiationException, IllegalAccessException,
			ClassNotFoundException {
		super.fromXmlConfigNoLock(config, xpp, requestNode);
		setPatternQuery(xpp.getNodeString(requestNode, "query"));
		setSnippetPatternQuery(xpp.getNodeString(requestNode, "snippetQuery"));
	}

	@Override
	public void writeSubXmlConfig(XmlWriter xmlWriter) throws SAXException {
		if (searchPatternQuery != null
				&& searchPatternQuery.trim().length() > 0) {
			xmlWriter.startElement("query");
			xmlWriter.textNode(searchPatternQuery);
			xmlWriter.endElement();
		}

		if (snippetPatternQuery != null
				&& snippetPatternQuery.trim().length() > 0) {
			xmlWriter.startElement("snippetQuery");
			xmlWriter.textNode(snippetPatternQuery);
			xmlWriter.endElement();
		}
	}

	@Override
	public String getInfo() {
		rwl.r.lock();
		try {
			return searchPatternQuery;
		} finally {
			rwl.r.unlock();
		}
	}

	@Override
	protected Query newRuntimeFilter(String queryString) throws ParseException,
			SyntaxError, SearchLibException, IOException {
		return null;
	}

}
