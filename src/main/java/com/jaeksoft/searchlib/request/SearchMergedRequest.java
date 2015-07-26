/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008-2015 Emmanuel Keller / Jaeksoft
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
import java.util.TreeSet;

import javax.xml.xpath.XPathExpressionException;

import org.apache.lucene.search.Query;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.config.Config;
import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.index.ReaderInterface;
import com.jaeksoft.searchlib.query.ParseException;
import com.jaeksoft.searchlib.result.AbstractResult;
import com.jaeksoft.searchlib.result.ResultSearchMerged;
import com.jaeksoft.searchlib.util.DomUtils;
import com.jaeksoft.searchlib.util.StringUtils;
import com.jaeksoft.searchlib.util.XPathParser;
import com.jaeksoft.searchlib.util.XmlWriter;

public class SearchMergedRequest extends AbstractSearchRequest implements
		RequestInterfaces.ReturnedFieldInterface,
		RequestInterfaces.FilterListInterface {

	private final TreeSet<RemoteRequest> requests;

	public SearchMergedRequest() {
		super(null, null /* RequestTypeEnum.SearchMergedRequest */);
		requests = new TreeSet<RemoteRequest>();
	}

	public SearchMergedRequest(Config config) {
		super(config, null /* RequestTypeEnum.SearchMergedRequest */);
		requests = new TreeSet<RemoteRequest>();
	}

	@Override
	protected void setDefaultValues() {
		super.setDefaultValues();
	}

	@Override
	public void copyFrom(AbstractRequest request) {
		super.copyFrom(request);
		SearchMergedRequest searchMergedRequest = (SearchMergedRequest) request;
		requests.clear();
		requests.addAll(searchMergedRequest.requests);
	}

	public Collection<RemoteRequest> getRequests() {
		return requests;
	}

	private final static String REMOTEREQUESTS_NODE_NAME = "remoteRequests";
	private final static String REMOTEREQUEST_NODE_NAME = "remoteRequest";
	private final static String REMOTEREQUEST_REMOTEURL = "url";
	private final static String REMOTEREQUEST_INDEXNAME = "indexName";
	private final static String REMOTEREQUEST_REQUESTNAME = "requestName";

	@Override
	public void fromXmlConfigNoLock(Config config, XPathParser xpp,
			Node searchRequestNode) throws XPathExpressionException,
			DOMException, ParseException, InstantiationException,
			IllegalAccessException, ClassNotFoundException {
		super.fromXmlConfigNoLock(config, xpp, searchRequestNode);
		Node requestsNode = DomUtils.getFirstNode(searchRequestNode,
				REMOTEREQUESTS_NODE_NAME);
		if (requestsNode != null) {
			List<Node> requestNodeList = DomUtils.getNodes(requestsNode,
					REMOTEREQUEST_NODE_NAME);
			if (requestNodeList != null)
				for (Node requestNode : requestNodeList)
					requests.add(new RemoteRequest(requestNode));
		}
	}

	@Override
	public void writeSubXmlConfig(XmlWriter xmlWriter) throws SAXException {
		xmlWriter.startElement(REMOTEREQUESTS_NODE_NAME);
		for (RemoteRequest request : requests)
			request.writeXmlConfig(xmlWriter);
		xmlWriter.endElement();
	}

	@Override
	public String getInfo() {
		return null;
	}

	@Override
	public Query getQuery() throws ParseException, SyntaxError,
			SearchLibException, IOException {
		return null;
	}

	public void save(RemoteRequest oldRequest, RemoteRequest newRequest) {
		if (oldRequest != null)
			requests.remove(oldRequest);
		if (newRequest != null)
			requests.add(new RemoteRequest(newRequest));
	}

	@Override
	public AbstractResult<?> execute(ReaderInterface reader)
			throws SearchLibException {
		return new ResultSearchMerged(this);
	}

	public static class RemoteRequest implements Comparable<RemoteRequest> {

		private String remoteURL;

		private String indexName;

		private String requestName;

		public RemoteRequest() {
			remoteURL = null;
			indexName = null;
			requestName = null;
		}

		public void writeXmlConfig(XmlWriter xmlWriter) throws SAXException {
			xmlWriter.startElement(REMOTEREQUEST_NODE_NAME,
					REMOTEREQUEST_REMOTEURL, remoteURL,
					REMOTEREQUEST_INDEXNAME, indexName,
					REMOTEREQUEST_REQUESTNAME, requestName);
			xmlWriter.endElement();
		}

		public RemoteRequest(RemoteRequest request) {
			this.remoteURL = request.remoteURL;
			this.indexName = request.indexName;
			this.requestName = request.requestName;
		}

		private RemoteRequest(Node requestNode) {
			this.remoteURL = DomUtils.getAttributeText(requestNode,
					REMOTEREQUEST_REMOTEURL);
			this.indexName = DomUtils.getAttributeText(requestNode,
					REMOTEREQUEST_INDEXNAME);
			this.requestName = DomUtils.getAttributeText(requestNode,
					REMOTEREQUEST_REQUESTNAME);
		}

		/**
		 * @return the remoteURL
		 */
		public String getRemoteURL() {
			return remoteURL;
		}

		/**
		 * @param remoteURL
		 *            the remoteURL to set
		 */
		public void setRemoteURL(String remoteURL) {
			this.remoteURL = remoteURL;
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
		 * @return the requestName
		 */
		public String getRequestName() {
			return requestName;
		}

		/**
		 * @param requestName
		 *            the requestName to set
		 */
		public void setRequestName(String requestName) {
			this.requestName = requestName;
		}

		@Override
		public int compareTo(RemoteRequest o) {
			int c;
			if ((c = StringUtils.compareNullString(remoteURL, o.remoteURL)) != 0)
				return c;
			if ((c = StringUtils.compareNullString(indexName, o.indexName)) != 0)
				return c;
			return StringUtils.compareNullString(requestName, o.requestName);
		}
	}

}
