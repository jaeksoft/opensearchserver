/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2012 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.join;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.util.XPathParser;
import com.jaeksoft.searchlib.util.XmlWriter;

public class JoinItem {

	private String indexName;

	private String queryTemplate;

	private String queryString;

	private String localField;

	private String foreignField;

	public JoinItem() {
		indexName = null;
		queryTemplate = null;
		queryString = null;
		localField = null;
		foreignField = null;
	}

	public JoinItem(JoinItem source) {
		source.copyTo(this);
	}

	public void copyTo(JoinItem target) {
		target.indexName = indexName;
		target.queryTemplate = queryTemplate;
		target.queryString = queryString;
		target.localField = localField;
		target.foreignField = foreignField;
	}

	public JoinItem(XPathParser xpp, Node node) throws XPathExpressionException {
		indexName = XPathParser.getAttributeString(node, ATTR_NAME_INDEXNAME);
		queryTemplate = XPathParser.getAttributeString(node,
				ATTR_NAME_QUERYTEMPLATE);
		queryString = xpp.getNodeString(node);
		localField = XPathParser.getAttributeString(node, ATTR_NAME_LOCALFIELD);
		foreignField = XPathParser.getAttributeString(node,
				ATTR_NAME_FOREIGNFIELD);
	}

	public final String NODE_NAME_JOIN = "join";
	public final String ATTR_NAME_INDEXNAME = "indexName";
	public final String ATTR_NAME_QUERYTEMPLATE = "queryTemplate";
	public final String ATTR_NAME_LOCALFIELD = "localField";
	public final String ATTR_NAME_FOREIGNFIELD = "foreignField";

	public void writeXmlConfig(XmlWriter xmlWriter) throws SAXException {
		xmlWriter.startElement(NODE_NAME_JOIN, ATTR_NAME_INDEXNAME, indexName,
				ATTR_NAME_QUERYTEMPLATE, queryTemplate, ATTR_NAME_LOCALFIELD,
				localField, ATTR_NAME_FOREIGNFIELD, foreignField);
		xmlWriter.textNode(queryString);
		xmlWriter.endElement();
	}

	/**
	 * @return the indexName
	 */
	public String getIndexName() {
		return indexName;
	}

	/**
	 * @param indexName
	 *            the indexName to set
	 */
	public void setIndexName(String indexName) {
		this.indexName = indexName;
	}

	/**
	 * @return the queryTemplate
	 */
	public String getQueryTemplate() {
		return queryTemplate;
	}

	/**
	 * @param queryTemplate
	 *            the queryTemplate to set
	 */
	public void setQueryTemplate(String queryTemplate) {
		this.queryTemplate = queryTemplate;
	}

	/**
	 * @return the queryString
	 */
	public String getQueryString() {
		return queryString;
	}

	/**
	 * @param queryString
	 *            the queryString to set
	 */
	public void setQueryString(String queryString) {
		this.queryString = queryString;
	}

	/**
	 * @return the localField
	 */
	public String getLocalField() {
		return localField;
	}

	/**
	 * @param localField
	 *            the localField to set
	 */
	public void setLocalField(String localField) {
		this.localField = localField;
	}

	/**
	 * @return the foreignField
	 */
	public String getForeignField() {
		return foreignField;
	}

	/**
	 * @param foreignField
	 *            the foreignField to set
	 */
	public void setForeignField(String foreignField) {
		this.foreignField = foreignField;
	}

}
