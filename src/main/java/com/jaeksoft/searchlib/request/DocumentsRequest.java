/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2012-2014 Emmanuel Keller / Jaeksoft
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
import java.util.List;

import javax.xml.xpath.XPathExpressionException;

import org.apache.lucene.search.Query;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.config.Config;
import com.jaeksoft.searchlib.index.ReaderInterface;
import com.jaeksoft.searchlib.index.ReaderLocal;
import com.jaeksoft.searchlib.query.ParseException;
import com.jaeksoft.searchlib.result.AbstractResult;
import com.jaeksoft.searchlib.result.ResultDocuments;
import com.jaeksoft.searchlib.schema.SchemaFieldList;
import com.jaeksoft.searchlib.util.StringUtils;
import com.jaeksoft.searchlib.util.XPathParser;
import com.jaeksoft.searchlib.util.XmlWriter;
import com.jaeksoft.searchlib.web.ServletTransaction;

public class DocumentsRequest extends AbstractRequest implements
		RequestInterfaces.ReturnedFieldInterface {

	private List<Integer> docList;
	private List<String> uniqueKeyList;
	private ReturnFieldList returnFieldList;

	public DocumentsRequest() {
		super(null, RequestTypeEnum.DocumentsRequest);
	}

	public DocumentsRequest(Config config) {
		super(config, RequestTypeEnum.DocumentsRequest);
	}

	@Override
	protected void setDefaultValues() {
		super.setDefaultValues();
		this.docList = new ArrayList<Integer>(0);
		this.uniqueKeyList = new ArrayList<String>(0);
		this.returnFieldList = new ReturnFieldList();
	}

	@Override
	public void copyFrom(AbstractRequest request) {
		super.copyFrom(request);
		DocumentsRequest docsRequest = (DocumentsRequest) request;
		this.docList = new ArrayList<Integer>(docsRequest.docList);
		this.uniqueKeyList = new ArrayList<String>(docsRequest.uniqueKeyList);
		this.returnFieldList = new ReturnFieldList(docsRequest.returnFieldList);
	}

	@Override
	public Query getQuery() throws SearchLibException, IOException {
		return null;
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

	@Override
	public void addReturnField(String fieldName) {
		rwl.w.lock();
		try {
			returnFieldList.put(new ReturnField(fieldName));
		} finally {
			rwl.w.unlock();
		}
	}

	public void addReturnFields(Collection<String> returnFields) {
		if (returnFields == null)
			return;
		rwl.w.lock();
		try {
			for (String returnField : returnFields)
				returnFieldList.put(new ReturnField(returnField));
		} finally {
			rwl.w.unlock();
		}
	}

	public void addUniqueKeys(Collection<String> uniqueKeys) {
		if (uniqueKeys == null)
			return;
		rwl.w.lock();
		try {
			for (String uniqueKey : uniqueKeys)
				uniqueKeyList.add(uniqueKey);
		} finally {
			rwl.w.unlock();
		}
	}

	@Override
	public void fromXmlConfigNoLock(Config config, XPathParser xpp,
			Node requestNode) throws XPathExpressionException, DOMException,
			ParseException, InstantiationException, IllegalAccessException,
			ClassNotFoundException {
		super.fromXmlConfigNoLock(config, xpp, requestNode);
		NodeList nodes = xpp.getNodeList(requestNode, "docs/doc");
		if (nodes != null) {
			for (int i = 0; i < nodes.getLength(); i++) {
				Node n = nodes.item(i);
				if (n != null) {
					int id = XPathParser.getAttributeValue(n, "id");
					docList.add(id);
				}
			}
		}

		nodes = xpp.getNodeList(requestNode, "uniqueKeys/key");
		if (nodes != null) {
			for (int i = 0; i < nodes.getLength(); i++) {
				Node n = nodes.item(i);
				if (n != null)
					uniqueKeyList.add(xpp.getNodeString(n, false));
			}
		}

		SchemaFieldList fieldList = config.getSchema().getFieldList();
		returnFieldList.filterCopy(fieldList,
				xpp.getNodeString(requestNode, "returnFields"));
		nodes = xpp.getNodeList(requestNode, "returnFields/field");
		for (int i = 0; i < nodes.getLength(); i++) {
			ReturnField field = ReturnField.fromXmlConfig(nodes.item(i));
			if (field != null)
				returnFieldList.put(field);
		}
	}

	@Override
	public void writeXmlConfig(XmlWriter xmlWriter) throws SAXException {
		rwl.r.lock();
		try {
			xmlWriter.startElement(XML_NODE_REQUEST, XML_ATTR_NAME,
					getRequestName(), XML_ATTR_TYPE, getType().name());

			if (docList.size() > 0) {
				xmlWriter.startElement("docs");
				for (Integer id : docList) {
					xmlWriter.startElement("doc", "id", id.toString());
					xmlWriter.endElement();
				}
				xmlWriter.endElement();
			}
			if (uniqueKeyList.size() > 0) {
				xmlWriter.startElement("uniqueKeys");
				for (String key : uniqueKeyList) {
					xmlWriter.startElement("key");
					xmlWriter.textNode(key);
					xmlWriter.endElement();
				}
				xmlWriter.endElement();
			}
			if (returnFieldList.size() > 0) {
				xmlWriter.startElement("returnFields");
				returnFieldList.writeXmlConfig(xmlWriter);
				xmlWriter.endElement();
			}
			xmlWriter.endElement();
		} finally {
			rwl.r.unlock();
		}
	}

	@Override
	final public void setFromServletNoLock(
			final ServletTransaction transaction, final String prefix) {
		String[] values;

		SchemaFieldList shemaFieldList = config.getSchema().getFieldList();

		if ((values = transaction.getParameterValues(StringUtils.fastConcat(
				prefix, "rf"))) != null) {
			for (String value : values)
				if (value != null)
					if (value.trim().length() > 0)
						returnFieldList.put(new ReturnField(shemaFieldList.get(
								value).getName()));
		}

		if ((values = transaction.getParameterValues(StringUtils.fastConcat(
				prefix, "id"))) != null) {
			for (String value : values)
				if (value != null)
					if (value.trim().length() > 0)
						docList.add(Integer.parseInt(value));
		}
		if ((values = transaction.getParameterValues(StringUtils.fastConcat(
				prefix, "uk"))) != null) {
			for (String value : values)
				if (value != null) {
					value = value.trim();
					if (value.length() > 0)
						uniqueKeyList.add(value);
				}
		}
	}

	@Override
	protected void resetNoLock() {
	}

	@Override
	public AbstractResult<AbstractRequest> execute(ReaderInterface reader)
			throws SearchLibException {
		try {
			return new ResultDocuments((ReaderLocal) reader, this);
		} catch (IOException e) {
			throw new SearchLibException(e);
		}
	}

	@Override
	public String getInfo() {
		rwl.r.lock();
		try {
			StringBuilder sb = new StringBuilder();
			sb.append("Fields:");
			sb.append(returnFieldList.toString());
			sb.append(" - Document id(s):");
			for (Integer id : docList) {
				sb.append(" [");
				sb.append(id);
				sb.append(']');
			}
			sb.append(" - Unique key(s):");
			for (String uniq : uniqueKeyList) {
				sb.append(" [");
				sb.append(uniq);
				sb.append(']');
			}
			return sb.toString();
		} finally {
			rwl.r.unlock();
		}
	}

	public List<Integer> getDocList() {
		rwl.r.lock();
		try {
			return docList;
		} finally {
			rwl.r.unlock();
		}
	}

	public List<String> getUniqueKeyList() {
		rwl.r.lock();
		try {
			return uniqueKeyList;
		} finally {
			rwl.r.unlock();
		}
	}

}
