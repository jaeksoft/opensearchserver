/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2013 Emmanuel Keller / Jaeksoft
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
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.search.Query;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.analysis.Analyzer;
import com.jaeksoft.searchlib.analysis.CompiledAnalyzer;
import com.jaeksoft.searchlib.analysis.FilterFactory;
import com.jaeksoft.searchlib.analysis.filter.DeduplicateTokenFilter;
import com.jaeksoft.searchlib.analysis.filter.IndexLookupFilter;
import com.jaeksoft.searchlib.analysis.filter.RemoveIncludedTermFilter;
import com.jaeksoft.searchlib.analysis.filter.ShingleFilter;
import com.jaeksoft.searchlib.analysis.tokenizer.TokenizerFactory;
import com.jaeksoft.searchlib.config.Config;
import com.jaeksoft.searchlib.index.ReaderInterface;
import com.jaeksoft.searchlib.query.ParseException;
import com.jaeksoft.searchlib.result.AbstractResult;
import com.jaeksoft.searchlib.result.ResultNamedEntityExtraction;
import com.jaeksoft.searchlib.util.DomUtils;
import com.jaeksoft.searchlib.util.XPathParser;
import com.jaeksoft.searchlib.util.XmlWriter;
import com.jaeksoft.searchlib.web.ServletTransaction;

public class NamedEntityExtractionRequest extends AbstractRequest {

	private String text;

	private String searchRequest;

	private String namedEntityField;

	private Set<String> returnedFields;

	public NamedEntityExtractionRequest() {
		super(null, RequestTypeEnum.NamedEntityExtractionRequest);
	}

	public NamedEntityExtractionRequest(Config config) {
		super(config, RequestTypeEnum.NamedEntityExtractionRequest);
	}

	@Override
	protected void setDefaultValues() {
		super.setDefaultValues();
		this.text = null;
		this.searchRequest = null;
		this.namedEntityField = null;
		this.returnedFields = null;
	}

	@Override
	public void copyFrom(AbstractRequest request) {
		super.copyFrom(request);
		NamedEntityExtractionRequest neeRequest = (NamedEntityExtractionRequest) request;
		this.text = neeRequest.text;
		this.searchRequest = neeRequest.searchRequest;
		this.namedEntityField = neeRequest.namedEntityField;
		this.returnedFields = neeRequest.returnedFields == null ? null
				: new TreeSet<String>(neeRequest.returnedFields);
	}

	public void addReturnedField(String returnedField) {
		if (StringUtils.isEmpty(returnedField))
			return;
		if (returnedFields == null)
			returnedFields = new TreeSet<String>();
		returnedFields.add(returnedField);
	}

	public void removeReturnedField(String returnedField) {
		if (StringUtils.isEmpty(returnedField))
			return;
		if (returnedFields == null)
			return;
		returnedFields.remove(returnedField);
	}

	public Collection<String> getReturnedFields() {
		return returnedFields;
	}

	public void setReturnedFields(List<String> returnedFields) {
		this.returnedFields.clear();
		for (String returnedField : returnedFields)
			addReturnedField(returnedField);
	}

	@Override
	public Query getQuery() throws SearchLibException, IOException {
		return null;
	}

	private final static String ATTR_SEARCH_REQUEST = "searchRequest";
	private final static String ATTR_NAMED_ENTITY_FIELD = "namedEntityField";
	private final static String NODE_TEXT = "text";
	private final static String NODE_RETURNED_FIELD = "returnedField";
	private final static String ATTR_NAME_FIELD = "name";

	@Override
	public void fromXmlConfigNoLock(Config config, XPathParser xpp,
			Node requestNode) throws XPathExpressionException, DOMException,
			ParseException, InstantiationException, IllegalAccessException,
			ClassNotFoundException {
		super.fromXmlConfigNoLock(config, xpp, requestNode);
		searchRequest = DomUtils.getAttributeText(requestNode,
				ATTR_SEARCH_REQUEST);
		namedEntityField = DomUtils.getAttributeText(requestNode,
				ATTR_NAMED_ENTITY_FIELD);
		Node textNode = DomUtils.getFirstNode(requestNode, NODE_TEXT);
		if (textNode == null)
			text = DomUtils.getText(requestNode);
		else
			text = DomUtils.getText(textNode);
		List<Node> returnedNodes = DomUtils.getNodes(requestNode,
				NODE_RETURNED_FIELD);
		if (returnedNodes != null)
			for (Node returnedNode : returnedNodes)
				addReturnedField(DomUtils.getAttributeText(returnedNode,
						ATTR_NAME_FIELD));

	}

	@Override
	public void writeXmlConfig(XmlWriter xmlWriter) throws SAXException {
		rwl.r.lock();
		try {
			xmlWriter.startElement(XML_NODE_REQUEST, XML_ATTR_NAME,
					getRequestName(), XML_ATTR_TYPE, getType().name(),
					ATTR_SEARCH_REQUEST, searchRequest,
					ATTR_NAMED_ENTITY_FIELD, namedEntityField);
			if (returnedFields != null)
				for (String returnedField : returnedFields) {
					xmlWriter.startElement(NODE_RETURNED_FIELD,
							ATTR_NAME_FIELD, returnedField);
					xmlWriter.endElement();
				}
			if (!StringUtils.isEmpty(text)) {
				xmlWriter.startElement(NODE_TEXT);
				xmlWriter.textNode(text);
				xmlWriter.endElement();
			}
			xmlWriter.endElement();
		} finally {
			rwl.r.unlock();
		}
	}

	@Override
	public void setFromServletNoLock(ServletTransaction transaction) {
		String value = null;
		if ((value = transaction.getParameterString("text")) != null)
			text = value;
		if ((value = transaction.getParameterString("searchRequest")) != null)
			searchRequest = value;
		if ((value = transaction.getParameterString("namedEntityField")) != null)
			namedEntityField = value;

	}

	@Override
	protected void resetNoLock() {
	}

	@Override
	public AbstractResult<AbstractRequest> execute(ReaderInterface reader)
			throws SearchLibException {
		try {
			AbstractSearchRequest abstractSearchRequest = (AbstractSearchRequest) config
					.getNewRequest(searchRequest);
			if (abstractSearchRequest == null)
				throw new SearchLibException("Request not found: "
						+ searchRequest);
			TreeSet<String> fieldNameSet = new TreeSet<String>();
			abstractSearchRequest.getReturnFieldList().populate(fieldNameSet);
			Analyzer analyzer = new Analyzer(config);
			analyzer.setTokenizer(TokenizerFactory.create(config,
					"StandardTokenizer"));
			ShingleFilter shingleFilter = FilterFactory.create(config,
					ShingleFilter.class);
			shingleFilter.setProperties(" ", 1, 5);
			analyzer.add(shingleFilter);
			analyzer.add(FilterFactory.create(config,
					DeduplicateTokenFilter.class));
			IndexLookupFilter ilf = FilterFactory.create(config,
					IndexLookupFilter.class);
			addReturnedField(namedEntityField);
			ilf.setProperties(config.getIndexName(), searchRequest,
					namedEntityField, StringUtils.join(returnedFields, '|'));
			analyzer.add(ilf);
			RemoveIncludedTermFilter ritf = FilterFactory.create(config,
					RemoveIncludedTermFilter.class);
			ritf.setProperties(namedEntityField, true);
			analyzer.add(ritf);
			CompiledAnalyzer compiledAnalyzer = analyzer.getQueryAnalyzer();
			ResultNamedEntityExtraction result = new ResultNamedEntityExtraction(
					this);
			compiledAnalyzer.populate(text, result);
			return result;
		} catch (IOException e) {
			throw new SearchLibException(e);
		}
	}

	@Override
	public String getInfo() {
		rwl.r.lock();
		try {
			StringBuffer sb = new StringBuffer();
			sb.append("SearchRequest:");
			if (searchRequest != null)
				sb.append(searchRequest);
			sb.append(" - NamedEntityField:");
			if (namedEntityField != null)
				sb.append(namedEntityField);
			return sb.toString();
		} finally {
			rwl.r.unlock();
		}
	}

	/**
	 * @return the text
	 */
	public String getText() {
		return text;
	}

	/**
	 * @param text
	 *            the text to set
	 */
	public void setText(String text) {
		this.text = text;
	}

	/**
	 * @return the searchRequest
	 */
	public String getSearchRequest() {
		return searchRequest;
	}

	/**
	 * @param searchRequest
	 *            the searchRequest to set
	 */
	public void setSearchRequest(String searchRequest) {
		this.searchRequest = searchRequest;
	}

	/**
	 * @return the namedEntityField
	 */
	public String getNamedEntityField() {
		return namedEntityField;
	}

	/**
	 * @param namedEntityField
	 *            the namedEntityField to set
	 */
	public void setNamedEntityField(String namedEntityField) {
		this.namedEntityField = namedEntityField;
		addReturnedField(namedEntityField);
	}

}
