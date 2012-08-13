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

package com.jaeksoft.searchlib;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.http.HttpException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.config.Config;
import com.jaeksoft.searchlib.crawler.web.database.CredentialItem;
import com.jaeksoft.searchlib.crawler.web.spider.ProxyHandler;
import com.jaeksoft.searchlib.index.IndexDocument;
import com.jaeksoft.searchlib.request.AbstractRequest;
import com.jaeksoft.searchlib.request.SearchRequest;
import com.jaeksoft.searchlib.result.AbstractResult;
import com.jaeksoft.searchlib.util.DomUtils;
import com.jaeksoft.searchlib.util.InfoCallback;
import com.jaeksoft.searchlib.util.Timer;

public class Client extends Config {

	public Client(File initFileOrDir, boolean createIndexIfNotExists,
			boolean disableCrawler) throws SearchLibException {
		super(initFileOrDir, null, createIndexIfNotExists, disableCrawler);
	}

	public Client(File initFileOrDir, String resourceName,
			boolean createIndexIfNotExists) throws SearchLibException {
		super(initFileOrDir, resourceName, createIndexIfNotExists, false);
	}

	public Client(File initFile) throws SearchLibException {
		this(initFile, false, false);
	}

	public boolean updateDocument(IndexDocument document)
			throws NoSuchAlgorithmException, IOException, URISyntaxException,
			SearchLibException, InstantiationException, IllegalAccessException,
			ClassNotFoundException {
		Timer timer = new Timer("Update document " + document.toString());
		try {
			checkMaxStorageLimit();
			checkMaxDocumentLimit(1);
			return getIndex().updateDocument(getSchema(), document);
		} finally {
			getStatisticsList().addUpdate(timer);
		}
	}

	public int updateDocuments(Collection<IndexDocument> documents)
			throws NoSuchAlgorithmException, IOException, URISyntaxException,
			SearchLibException, InstantiationException, IllegalAccessException,
			ClassNotFoundException {
		Timer timer = new Timer("Update " + documents.size() + " documents");
		try {
			checkMaxStorageLimit();
			checkMaxDocumentLimit(documents.size());
			return getIndex().updateDocuments(getSchema(), documents);
		} finally {
			getStatisticsList().addUpdate(timer);
		}
	}

	private final int updateDocList(int totalCount, int docCount,
			Collection<IndexDocument> docList, InfoCallback infoCallBack)
			throws NoSuchAlgorithmException, IOException, URISyntaxException,
			SearchLibException, InstantiationException, IllegalAccessException,
			ClassNotFoundException {
		checkMaxStorageLimit();
		checkMaxDocumentLimit(docList.size());
		docCount += updateDocuments(docList);
		StringBuffer sb = new StringBuffer();
		sb.append(docCount);
		sb.append(" / ");
		sb.append(totalCount);
		sb.append(" XML document(s) indexed.");
		if (infoCallBack != null)
			infoCallBack.setInfo(sb.toString());
		else
			Logging.info(sb.toString());
		docList.clear();
		return docCount;
	}

	private int updateXmlDocuments(Node document, int bufferSize,
			CredentialItem urlDefaultCredential, ProxyHandler proxyHandler,
			InfoCallback infoCallBack) throws XPathExpressionException,
			NoSuchAlgorithmException, IOException, URISyntaxException,
			SearchLibException, InstantiationException, IllegalAccessException,
			ClassNotFoundException {
		List<Node> nodeList = DomUtils.getNodes(document, "index", "document");
		Collection<IndexDocument> docList = new ArrayList<IndexDocument>(
				bufferSize);
		int docCount = 0;
		final int totalCount = nodeList.size();
		for (Node node : nodeList) {
			docList.add(new IndexDocument(this, getParserSelector(), node,
					urlDefaultCredential, proxyHandler));
			if (docList.size() == bufferSize)
				docCount = updateDocList(totalCount, docCount, docList,
						infoCallBack);
		}
		if (docList.size() > 0)
			docCount = updateDocList(totalCount, docCount, docList,
					infoCallBack);
		return docCount;
	}

	public int updateXmlDocuments(InputSource inputSource, int bufferSize,
			CredentialItem urlDefaultCredential, ProxyHandler proxyHandler,
			InfoCallback infoCallBack) throws ParserConfigurationException,
			SAXException, IOException, XPathExpressionException,
			NoSuchAlgorithmException, URISyntaxException, SearchLibException,
			InstantiationException, IllegalAccessException,
			ClassNotFoundException {
		Document doc = DomUtils.getNewDocumentBuilder(false, true).parse(
				inputSource);
		return updateXmlDocuments(doc, bufferSize, urlDefaultCredential,
				proxyHandler, infoCallBack);
	}

	public boolean deleteDocument(String uniqueField) throws SearchLibException {
		Timer timer = new Timer("Delete document " + uniqueField);
		try {
			return getIndex().deleteDocument(getSchema(), uniqueField);
		} finally {
			getStatisticsList().addDelete(timer);
		}
	}

	public int deleteDocuments(Collection<String> uniqueFields)
			throws SearchLibException {
		Timer timer = new Timer("Delete " + uniqueFields.size() + " documents");
		try {
			return getIndex().deleteDocuments(getSchema(), uniqueFields);
		} finally {
			getStatisticsList().addDelete(timer);
		}
	}

	public int deleteDocuments(SearchRequest searchRequest)
			throws SearchLibException {
		Timer timer = new Timer("Delete by query documents");
		try {
			return getIndex().deleteDocuments(searchRequest);
		} finally {
			getStatisticsList().addDelete(timer);
		}
	}

	public void optimize() throws IOException, URISyntaxException,
			SearchLibException {
		Timer timer = new Timer("Optimize");
		try {
			getIndex().optimize();
		} finally {
			getStatisticsList().addOptimize(timer);
		}
	}

	public boolean isOptimizing() {
		return getIndex().isOptimizing();
	}

	public String getOptimizationStatus() throws IOException {
		if (isOptimizing())
			return "Running";
		return Boolean.toString(getIndex().getStatistics().isOptimized());
	}

	public void deleteAll() throws SearchLibException {
		Timer timer = new Timer("DeleteAll");
		try {
			getIndex().deleteAll();
		} finally {
			getStatisticsList().addDelete(timer);
		}
	}

	public void reload() throws SearchLibException {
		Timer timer = new Timer("Reload");
		try {
			getIndex().reload();
		} finally {
			getStatisticsList().addReload(timer);
		}
	}

	/*
	 * Older version compatibility
	 */
	@Deprecated
	public void reload(boolean deleteOld) throws IOException,
			URISyntaxException, SearchLibException, InstantiationException,
			IllegalAccessException, ClassNotFoundException, HttpException {
		reload();
	}

	public AbstractResult<?> request(AbstractRequest request)
			throws SearchLibException {
		Timer timer = null;
		AbstractResult<?> result = null;
		SearchLibException exception = null;
		try {
			request.init(this);
			timer = new Timer(request.getNameType());
			result = getIndex().request(request);
			return result;
		} catch (SearchLibException e) {
			exception = e;
			throw e;
		} catch (Exception e) {
			exception = new SearchLibException(e);
			throw exception;
		} finally {
			if (timer != null) {
				timer.duration();
				if (exception != null)
					timer.setError(exception);
				getStatisticsList().addSearch(timer);
				getLogReportManager().log(request, timer, result);
			}
		}
	}

	public String explain(SearchRequest searchRequest, int docId, boolean bHtml)
			throws SearchLibException {
		return getIndex().explain(searchRequest, docId, bHtml);
	}

	protected final void checkMaxDocumentLimit(int additionalCount)
			throws SearchLibException, IOException {
		ClientFactory.INSTANCE.properties
				.checkMaxDocumentLimit(additionalCount);
	}

	protected void checkMaxStorageLimit() throws SearchLibException {
		ClientFactory.INSTANCE.properties.checkMaxStorageLimit();
	}
}
