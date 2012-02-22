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

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.http.HttpException;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.store.LockObtainFailedException;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.config.Config;
import com.jaeksoft.searchlib.crawler.web.database.CredentialItem;
import com.jaeksoft.searchlib.crawler.web.spider.ProxyHandler;
import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.index.IndexDocument;
import com.jaeksoft.searchlib.request.AbstractRequest;
import com.jaeksoft.searchlib.request.DocumentsRequest;
import com.jaeksoft.searchlib.request.SearchRequest;
import com.jaeksoft.searchlib.result.AbstractResult;
import com.jaeksoft.searchlib.result.ResultDocument;
import com.jaeksoft.searchlib.util.Timer;
import com.jaeksoft.searchlib.util.XPathParser;

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
			checkMaxDocumentLimit(documents.size());
			return getIndex().updateDocuments(getSchema(), documents);
		} finally {
			getStatisticsList().addUpdate(timer);
		}
	}

	private final int updateBuffer(Collection<IndexDocument> docList,
			int docCount, int docTotal) throws SearchLibException, IOException,
			NoSuchAlgorithmException, URISyntaxException,
			InstantiationException, IllegalAccessException,
			ClassNotFoundException {
		checkMaxDocumentLimit(docList.size());
		docCount += updateDocuments(docList);
		docList.clear();
		return docCount;
	}

	private int updateXmlDocuments(XPathParser xpp, int bufferSize,
			CredentialItem urlDefaultCredential, ProxyHandler proxyHandler)
			throws XPathExpressionException, NoSuchAlgorithmException,
			IOException, URISyntaxException, SearchLibException,
			InstantiationException, IllegalAccessException,
			ClassNotFoundException {
		NodeList nodeList = xpp.getNodeList("/index/document");
		int l = nodeList.getLength();
		Collection<IndexDocument> docList = new ArrayList<IndexDocument>(
				bufferSize);
		int docCount = 0;
		for (int i = 0; i < l; i++) {
			docList.add(new IndexDocument(this, getParserSelector(), nodeList
					.item(i), urlDefaultCredential, proxyHandler));
			if (docList.size() == bufferSize)
				docCount = updateBuffer(docList, docCount, l);

		}
		if (docList.size() > 0)
			docCount = updateBuffer(docList, docCount, l);
		return docCount;
	}

	public int updateXmlDocuments(InputSource inputSource, int bufferSize,
			CredentialItem urlDefaultCredential, ProxyHandler proxyHandler)
			throws ParserConfigurationException, SAXException, IOException,
			XPathExpressionException, NoSuchAlgorithmException,
			URISyntaxException, SearchLibException, InstantiationException,
			IllegalAccessException, ClassNotFoundException {
		XPathParser xpp = new XPathParser(inputSource);
		return updateXmlDocuments(xpp, bufferSize, urlDefaultCredential,
				proxyHandler);
	}

	public boolean deleteDocument(String uniqueField)
			throws CorruptIndexException, LockObtainFailedException,
			IOException, URISyntaxException, SearchLibException,
			InstantiationException, IllegalAccessException,
			ClassNotFoundException, HttpException {
		Timer timer = new Timer("Delete document " + uniqueField);
		try {
			return getIndex().deleteDocument(getSchema(), uniqueField);
		} finally {
			getStatisticsList().addDelete(timer);
		}
	}

	public int deleteDocuments(Collection<String> uniqueFields)
			throws CorruptIndexException, LockObtainFailedException,
			IOException, URISyntaxException, SearchLibException,
			InstantiationException, IllegalAccessException,
			ClassNotFoundException {
		Timer timer = new Timer("Delete " + uniqueFields.size() + " documents");
		try {
			return getIndex().deleteDocuments(getSchema(), uniqueFields);
		} finally {
			getStatisticsList().addDelete(timer);
		}
	}

	public int deleteDocuments(SearchRequest searchRequest)
			throws SearchLibException, CorruptIndexException, IOException,
			InstantiationException, IllegalAccessException,
			ClassNotFoundException, ParseException, SyntaxError,
			URISyntaxException, InterruptedException {
		Timer timer = new Timer("Delete by query documents");
		try {
			return getIndex().deleteDocuments(searchRequest);
		} finally {
			getStatisticsList().addDelete(timer);
		}
	}

	public void optimize() throws IOException, URISyntaxException,
			SearchLibException, InstantiationException, IllegalAccessException,
			ClassNotFoundException, HttpException {
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
			timer = request.getTimer();
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
				if (exception != null)
					timer.setError(exception);
				getStatisticsList().addSearch(timer);
				getLogReportManager().log(request, timer, result);
			}
		}
	}

	public String explain(SearchRequest searchRequest, int docId, boolean bHtml)
			throws SearchLibException {
		Timer timer = null;
		SearchLibException exception = null;
		try {
			searchRequest.init(this);
			timer = searchRequest.getTimer();
			return getIndex().explain(searchRequest, docId, bHtml);
		} catch (SearchLibException e) {
			exception = e;
		} finally {
			if (timer != null) {
				if (exception != null)
					timer.setError(exception);
				getStatisticsList().addSearch(timer);
			}
			if (exception != null)
				throw exception;
		}
		return null;
	}

	public ResultDocument[] documents(DocumentsRequest documentsRequest)
			throws IOException, ParseException, SyntaxError,
			URISyntaxException, ClassNotFoundException, InterruptedException,
			SearchLibException, IllegalAccessException, InstantiationException {
		return getIndex().documents(documentsRequest);
	}

	protected final void checkMaxDocumentLimit(int additionalCount)
			throws SearchLibException, IOException {
		if (ClientFactory.INSTANCE.properties.getMaxDocumentLimit() == 0)
			return;
		ClientFactory.INSTANCE.properties.checkMaxDocumentLimit(ClientCatalog
				.countAllDocuments() + additionalCount);
	}

}
