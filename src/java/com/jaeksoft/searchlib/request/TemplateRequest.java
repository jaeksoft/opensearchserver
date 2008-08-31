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

import javax.xml.xpath.XPathExpressionException;

import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.queryParser.QueryParser.Operator;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.jaeksoft.searchlib.config.Config;
import com.jaeksoft.searchlib.facet.FacetField;
import com.jaeksoft.searchlib.filter.FilterList;
import com.jaeksoft.searchlib.filter.Filter.Source;
import com.jaeksoft.searchlib.highlight.HighlightField;
import com.jaeksoft.searchlib.schema.FieldList;
import com.jaeksoft.searchlib.schema.FieldValue;
import com.jaeksoft.searchlib.schema.Schema;
import com.jaeksoft.searchlib.schema.SchemaField;
import com.jaeksoft.searchlib.util.XPathParser;

public class TemplateRequest extends Request {

	/**
	 * Constructeur de base.
	 * 
	 * @param config
	 * @param name
	 * @param allowLeadingWildcard
	 * @param phraseSlop
	 * @param defaultOperator
	 * @param start
	 * @param rows
	 * @param queryString
	 * @param forceLocal
	 */
	public TemplateRequest(Config config, String name,
			boolean allowLeadingWildcard, int phraseSlop,
			Operator defaultOperator, int start, int rows, String lang,
			String queryString, boolean forceLocal, boolean delete) {
		super(config, name, allowLeadingWildcard, phraseSlop, defaultOperator,
				start, rows, lang, queryString, forceLocal, delete);
	}

	/**
	 * Contructeur permettant de cloner.
	 * 
	 * @param req
	 */
	private TemplateRequest(TemplateRequest req) {
		super(req);
	}

	@Override
	protected QueryParser getNewQueryParser() {
		synchronized (this) {
			Schema schema = this.getConfig().getSchema();
			return new QueryParser(schema.getFieldList().getDefaultField()
					.getName(), schema.getQueryPerFieldAnalyzer(getLang()));
		}
	}

	@Override
	protected QueryParser getNewHighlightQueryParser() {
		synchronized (this) {
			Schema schema = this.getConfig().getSchema();
			return new QueryParser(schema.getFieldList().getDefaultField()
					.getName(), schema.getHighlightPerFieldAnalyzer(getLang()));
		}
	}

	/**
	 * Retourne un nouvel objet clon�.
	 */
	@Override
	public Request clone() {
		return new TemplateRequest(this);
	}

	@Override
	public void setQueryString(String q) throws ParseException {
		setQueryStringNotEscaped(getSourceRequest().getQueryString().replace(
				"$$", q));
	}

	/**
	 * Construit un TemplateRequest bas� sur le noeud indiqu� dans le
	 * fichier de config XML.
	 * 
	 * @param config
	 * @param xpp
	 * @param parentNode
	 * @throws XPathExpressionException
	 * @throws ParseException
	 * @throws DOMException
	 */
	public static TemplateRequest fromXmlConfig(Config config, XPathParser xpp,
			Node node) throws XPathExpressionException, DOMException,
			ParseException {
		if (node == null)
			return null;
		String name = XPathParser.getAttributeString(node, "name");
		Node queryNode = xpp.getNode(node, "query");
		TemplateRequest templateRequest = new TemplateRequest(config, name,
				false, XPathParser.getAttributeValue(node, "phraseSlop"),
				("and".equals(XPathParser.getAttributeString(node,
						"defaultOperator"))) ? QueryParser.AND_OPERATOR
						: QueryParser.OR_OPERATOR, XPathParser
						.getAttributeValue(node, "start"), XPathParser
						.getAttributeValue(node, "rows"), XPathParser
						.getAttributeString(node, "lang"), xpp
						.getNodeString(queryNode), false, false);

		FieldList<FieldValue> returnFields = templateRequest
				.getReturnFieldList();
		FieldList<SchemaField> fieldList = config.getSchema().getFieldList();
		FieldValue.filterCopy(fieldList, xpp
				.getNodeString(node, "returnFields"), returnFields);

		FieldList<HighlightField> highlightFields = templateRequest
				.getHighlightFieldList();
		NodeList nodes = xpp.getNodeList(node, "highlighting/field");
		for (int i = 0; i < nodes.getLength(); i++)
			HighlightField.copyHighlightFields(nodes.item(i), fieldList,
					highlightFields);

		FieldList<FacetField> facetFields = templateRequest.getFacetFieldList();
		nodes = xpp.getNodeList(node, "facetFields/facetField");
		for (int i = 0; i < nodes.getLength(); i++)
			FacetField.copyFacetFields(nodes.item(i), fieldList, facetFields);

		FilterList filterList = templateRequest.getFilterList();
		nodes = xpp.getNodeList(node, "filters/filter");
		for (int i = 0; i < nodes.getLength(); i++)
			filterList.add(node.getTextContent(), Source.CONFIGXML);
		return templateRequest;
	}

}
