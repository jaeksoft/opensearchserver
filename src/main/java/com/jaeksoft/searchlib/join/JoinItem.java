/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2012-2013 Emmanuel Keller / Jaeksoft
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

import java.io.IOException;
import java.util.List;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.ClientCatalog;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.filter.FilterAbstract;
import com.jaeksoft.searchlib.filter.FilterList;
import com.jaeksoft.searchlib.index.FieldCacheIndex;
import com.jaeksoft.searchlib.index.ReaderLocal;
import com.jaeksoft.searchlib.request.AbstractRequest;
import com.jaeksoft.searchlib.request.AbstractSearchRequest;
import com.jaeksoft.searchlib.result.ResultSearchSingle;
import com.jaeksoft.searchlib.result.collector.DocIdInterface;
import com.jaeksoft.searchlib.result.collector.JoinDocCollector;
import com.jaeksoft.searchlib.util.Timer;
import com.jaeksoft.searchlib.util.XPathParser;
import com.jaeksoft.searchlib.util.XmlWriter;
import com.jaeksoft.searchlib.web.ServletTransaction;

public class JoinItem implements Comparable<JoinItem> {

	public static enum JoinType {
		INNER, OUTER;

		private final String label = name().toLowerCase();

		final public static JoinType find(String name) {
			for (JoinType type : values())
				if (type.name().equalsIgnoreCase(name))
					return type;
			return INNER;
		}

		public String getLabel() {
			return label;
		}
	}

	private String indexName;

	private String queryTemplate;

	private FilterList filterList;

	private String queryString;

	private String localField;

	private String foreignField;

	private String paramPosition;

	private boolean returnFields;

	private boolean returnScores;

	private boolean returnFacets;

	private JoinType type;

	public JoinItem() {
		indexName = null;
		queryTemplate = null;
		queryString = null;
		localField = null;
		foreignField = null;
		paramPosition = null;
		returnFields = false;
		returnScores = false;
		returnFacets = false;
		type = JoinType.INNER;
		filterList = new FilterList();
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
		target.paramPosition = paramPosition;
		target.returnFields = returnFields;
		target.returnScores = returnScores;
		target.returnFacets = returnFacets;
		target.type = type;
		target.filterList = new FilterList(filterList);
	}

	public JoinItem(XPathParser xpp, Node node) throws XPathExpressionException {
		indexName = XPathParser.getAttributeString(node, ATTR_NAME_INDEXNAME);
		queryTemplate = XPathParser.getAttributeString(node,
				ATTR_NAME_QUERYTEMPLATE);
		queryString = xpp.getNodeString(node, false);
		localField = XPathParser.getAttributeString(node, ATTR_NAME_LOCALFIELD);
		foreignField = XPathParser.getAttributeString(node,
				ATTR_NAME_FOREIGNFIELD);
		returnFields = Boolean.parseBoolean(XPathParser.getAttributeString(
				node, ATTR_NAME_RETURNFIELDS));
		returnScores = Boolean.parseBoolean(XPathParser.getAttributeString(
				node, ATTR_NAME_RETURNSCORES));
		returnFacets = Boolean.parseBoolean(XPathParser.getAttributeString(
				node, ATTR_NAME_RETURNFACETS));
		type = JoinType.find(XPathParser.getAttributeString(node,
				ATTR_NAME_TYPE));
		filterList = new FilterList();
	}

	public final String NODE_NAME_JOIN = "join";
	public final String ATTR_NAME_INDEXNAME = "indexName";
	public final String ATTR_NAME_QUERYTEMPLATE = "queryTemplate";
	public final String ATTR_NAME_LOCALFIELD = "localField";
	public final String ATTR_NAME_FOREIGNFIELD = "foreignField";
	public final String ATTR_NAME_RETURNFIELDS = "returnFields";
	public final String ATTR_NAME_RETURNSCORES = "returnScores";
	public final String ATTR_NAME_RETURNFACETS = "returnFacets";
	public final String ATTR_NAME_TYPE = "type";

	public void writeXmlConfig(XmlWriter xmlWriter) throws SAXException {
		xmlWriter.startElement(NODE_NAME_JOIN, ATTR_NAME_INDEXNAME, indexName,
				ATTR_NAME_QUERYTEMPLATE, queryTemplate, ATTR_NAME_LOCALFIELD,
				localField, ATTR_NAME_FOREIGNFIELD, foreignField,
				ATTR_NAME_RETURNFIELDS, Boolean.toString(returnFields),
				ATTR_NAME_RETURNSCORES, Boolean.toString(returnScores),
				ATTR_NAME_RETURNFACETS, Boolean.toString(returnFacets),
				ATTR_NAME_TYPE, type.name());
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

	@Override
	public int compareTo(JoinItem o) {
		int c = 0;
		if ((c = indexName.compareTo(o.indexName)) != 0)
			return c;
		if ((c = queryTemplate.compareTo(o.queryTemplate)) != 0)
			return c;
		if ((c = queryString.compareTo(o.queryString)) != 0)
			return c;
		if ((c = localField.compareTo(o.localField)) != 0)
			return c;
		if ((c = foreignField.compareTo(o.foreignField)) != 0)
			return c;
		return 0;
	}

	public void setParamPosition(int position) {
		StringBuffer sb = new StringBuffer("jq");
		sb.append(position);
		paramPosition = sb.toString();
	}

	public String getParamPosition() {
		return paramPosition;
	}

	/**
	 * @return the returnFields
	 */
	public boolean isReturnFields() {
		return returnFields;
	}

	/**
	 * @param returnFields
	 *            the returnFields to set
	 */
	public void setReturnFields(boolean returnFields) {
		this.returnFields = returnFields;
	}

	/**
	 * @return the returnScores
	 */
	public boolean isReturnScores() {
		return returnScores;
	}

	/**
	 * @param returnScores
	 *            the returnScores to set
	 */
	public void setReturnScores(boolean returnScores) {
		this.returnScores = returnScores;
	}

	/**
	 * @return the returnFacets
	 */
	public boolean isReturnFacets() {
		return returnFacets;
	}

	/**
	 * @param returnFacets
	 *            the returnFacets to set
	 */
	public void setReturnFacets(boolean returnFacets) {
		this.returnFacets = returnFacets;
	}

	/**
	 * @return the type
	 */
	public JoinType getType() {
		return type;
	}

	/**
	 * @param type
	 *            the type to set
	 */
	public void setType(JoinType type) {
		this.type = type;
	}

	public DocIdInterface apply(ReaderLocal reader, DocIdInterface docs,
			int joinResultSize, JoinResult joinResult,
			List<JoinFacet> joinFacets, Timer timer) throws SearchLibException {
		try {
			Client client = ClientCatalog.getClient(indexName);
			if (client == null)
				throw new SearchLibException("No client found: " + indexName);
			AbstractRequest request = client.getNewRequest(queryTemplate);
			if (request == null)
				throw new SearchLibException(
						"The request template was not found: " + queryTemplate);
			if (!(request instanceof AbstractSearchRequest))
				throw new SearchLibException(
						"The request template is not a Search request: "
								+ queryTemplate);
			FieldCacheIndex localStringIndex = reader
					.getStringIndex(localField);
			if (localStringIndex == null)
				throw new SearchLibException(
						"No string index found for the local field: "
								+ localField);
			AbstractSearchRequest searchRequest = (AbstractSearchRequest) request;
			// TODO Why ??
			if (searchRequest.getRows() == 0)
				searchRequest.setRows(1);
			searchRequest.setQueryString(queryString);
			for (FilterAbstract<?> filter : filterList)
				searchRequest.getFilterList().add(filter);
			String joinResultName = "join " + joinResult.getParamPosition();
			Timer t = new Timer(timer, joinResultName + " foreign search");
			ResultSearchSingle resultSearch = (ResultSearchSingle) client
					.request(searchRequest);
			t.getDuration();
			joinResult.setForeignResult(resultSearch);
			if (searchRequest.isFacet()) {
				if (returnFacets)
					joinFacets.add(new JoinFacet(joinResult, searchRequest
							.getFacetFieldList(), resultSearch));
				searchRequest.getFacetFieldList().clear();
			}

			FieldCacheIndex foreignFieldIndex = resultSearch.getReader()
					.getStringIndex(foreignField);
			if (foreignFieldIndex == null)
				throw new SearchLibException(
						"No string index found for the foreign field: "
								+ foreignField);
			t = new Timer(timer, joinResultName + " join");
			DocIdInterface joinDocs = JoinDocCollector.join(docs,
					localStringIndex, resultSearch.getDocs(),
					foreignFieldIndex, joinResultSize, joinResult.joinPosition,
					t, returnScores, type);
			t.getDuration();
			return joinDocs;
		} catch (IOException e) {
			throw new SearchLibException(e);
		}
	}

	public void setFromServlet(ServletTransaction transaction) {
		String q = transaction.getParameterString(paramPosition);
		if (q != null)
			setQueryString(q);
		filterList.addFromServlet(transaction, paramPosition + ".");
	}

	public void setParam(String param) {
		if (param != null)
			setQueryString(param);
	}
}
