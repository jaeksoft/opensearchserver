/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2008 Emmanuel Keller / Jaeksoft
 * 
 * http://www.jaeksoft.com
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

package com.jaeksoft.searchlib;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;

import javax.naming.NamingException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.store.LockObtainFailedException;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.config.Config;
import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.index.IndexDocument;
import com.jaeksoft.searchlib.request.DocumentsRequest;
import com.jaeksoft.searchlib.request.SearchRequest;
import com.jaeksoft.searchlib.result.Result;
import com.jaeksoft.searchlib.result.ResultDocuments;
import com.jaeksoft.searchlib.util.Context;
import com.jaeksoft.searchlib.util.Timer;
import com.jaeksoft.searchlib.util.XPathParser;
import com.jaeksoft.searchlib.util.XmlInfo;

public class Client extends Config implements XmlInfo {

	public Client(File initFile, boolean createIndexIfNotExists)
			throws SearchLibException {
		super(initFile, createIndexIfNotExists);
	}

	public Client(File initFile) throws SearchLibException {
		this(initFile, false);
	}

	public boolean updateDocument(IndexDocument document)
			throws NoSuchAlgorithmException, IOException, URISyntaxException {
		Timer timer = new Timer();
		try {
			return getIndex().updateDocument(getSchema(), document);
		} finally {
			statisticsList.addUpdate(timer);
		}
	}

	public boolean updateDocument(String indexName, IndexDocument document)
			throws NoSuchAlgorithmException, IOException, URISyntaxException {
		Timer timer = new Timer();
		try {
			return getIndex().updateDocument(indexName, getSchema(), document);
		} finally {
			statisticsList.addUpdate(timer);
		}
	}

	public int updateDocuments(Collection<IndexDocument> documents)
			throws NoSuchAlgorithmException, IOException, URISyntaxException {
		Timer timer = new Timer();
		try {
			return getIndex().updateDocuments(getSchema(), documents);
		} finally {
			statisticsList.addUpdate(timer);
		}
	}

	public int updateDocuments(String indexName,
			Collection<IndexDocument> documents)
			throws NoSuchAlgorithmException, IOException, URISyntaxException {
		Timer timer = new Timer();
		try {
			return getIndex()
					.updateDocuments(indexName, getSchema(), documents);
		} finally {
			statisticsList.addUpdate(timer);
		}
	}

	private int updateXmlDocuments(String indexName, XPathParser xpp)
			throws XPathExpressionException, NoSuchAlgorithmException,
			IOException, URISyntaxException, SearchLibException {
		NodeList nodeList = xpp.getNodeList("/index/document");
		int l = nodeList.getLength();
		Collection<IndexDocument> docList = new ArrayList<IndexDocument>();
		for (int i = 0; i < l; i++)
			docList.add(new IndexDocument(xpp, nodeList.item(i),
					getBasketCache()));
		if (indexName == null)
			return updateDocuments(docList);
		else
			return updateDocuments(indexName, docList);
	}

	public int updateXmlDocuments(String indexName, InputSource inputSource)
			throws ParserConfigurationException, SAXException, IOException,
			XPathExpressionException, NoSuchAlgorithmException,
			URISyntaxException, SearchLibException {
		XPathParser xpp = new XPathParser(inputSource);
		return updateXmlDocuments(indexName, xpp);
	}

	public int updateXmlDocuments(String indexName, String xmlString)
			throws SAXException, IOException, ParserConfigurationException,
			XPathExpressionException, NoSuchAlgorithmException,
			URISyntaxException, SearchLibException {
		XPathParser xpp = new XPathParser(new InputSource(new StringReader(
				xmlString)));
		return updateXmlDocuments(indexName, xpp);
	}

	public boolean deleteDocument(String uniqueField)
			throws CorruptIndexException, LockObtainFailedException,
			IOException, URISyntaxException {
		Timer timer = new Timer();
		try {
			return getIndex().deleteDocument(getSchema(), uniqueField);
		} finally {
			statisticsList.addDelete(timer);
		}
	}

	public boolean deleteDocument(String indexName, String uniqueField)
			throws CorruptIndexException, LockObtainFailedException,
			IOException, URISyntaxException {
		Timer timer = new Timer();
		try {
			return getIndex().deleteDocument(indexName, getSchema(),
					uniqueField);
		} finally {
			statisticsList.addDelete(timer);
		}
	}

	public boolean deleteDocument(int docId) throws CorruptIndexException,
			LockObtainFailedException, IOException, URISyntaxException {
		Timer timer = new Timer();
		try {
			return getIndex().deleteDocument(docId);
		} finally {
			statisticsList.addDelete(timer);
		}
	}

	public boolean deleteDocument(String indexName, int docId)
			throws CorruptIndexException, LockObtainFailedException,
			IOException, URISyntaxException {
		Timer timer = new Timer();
		try {
			return getIndex().deleteDocument(indexName, docId);
		} finally {
			statisticsList.addDelete(timer);
		}
	}

	public int deleteDocuments(Collection<String> uniqueFields)
			throws CorruptIndexException, LockObtainFailedException,
			IOException, URISyntaxException {
		Timer timer = new Timer();
		try {
			return getIndex().deleteDocuments(getSchema(), uniqueFields);
		} finally {
			statisticsList.addDelete(timer);
		}
	}

	public int deleteDocuments(String indexName, Collection<String> uniqueFields)
			throws CorruptIndexException, LockObtainFailedException,
			IOException, URISyntaxException {
		Timer timer = new Timer();
		try {
			return getIndex().deleteDocuments(indexName, getSchema(),
					uniqueFields);
		} finally {
			statisticsList.addDelete(timer);
		}
	}

	public int deleteDocumentsById(Collection<Integer> docIds)
			throws CorruptIndexException, LockObtainFailedException,
			IOException, URISyntaxException {
		Timer timer = new Timer();
		try {
			return getIndex().deleteDocuments(docIds);
		} finally {
			statisticsList.addDelete(timer);
		}
	}

	public int deleteDocumentsbyId(String indexName, Collection<Integer> docIds)
			throws CorruptIndexException, LockObtainFailedException,
			IOException, URISyntaxException {
		Timer timer = new Timer();
		try {
			return getIndex().deleteDocuments(indexName, docIds);
		} finally {
			statisticsList.addDelete(timer);
		}
	}

	public void optimize(String indexName) throws IOException,
			URISyntaxException {
		Timer timer = new Timer();
		try {
			getIndex().optimize(indexName);
		} finally {
			statisticsList.addOptimize(timer);
		}
	}

	public void reload(String indexName) throws IOException, URISyntaxException {
		Timer timer = new Timer();
		try {
			getIndex().reload(indexName);
		} finally {
			statisticsList.addReload(timer);
		}
	}

	/*
	 * Older version compatibility
	 */
	@Deprecated
	public void reload(boolean deleteOld) throws IOException,
			URISyntaxException {
		reload(null);
	}

	public Result search(SearchRequest searchRequest) throws IOException,
			ParseException, SyntaxError, URISyntaxException,
			ClassNotFoundException, InterruptedException {
		searchRequest.init(this);
		Result result = getIndex().search(searchRequest);
		statisticsList.addSearch(searchRequest.getTimer());
		return result;
	}

	public ResultDocuments documents(DocumentsRequest documentsRequest)
			throws IOException, ParseException, SyntaxError,
			URISyntaxException, ClassNotFoundException, InterruptedException {
		return getIndex().documents(documentsRequest);
	}

	public void close() {
		getIndex().close();
	}

	private static volatile Client INSTANCE;

	public static Client getFileInstance(File configFile)
			throws SearchLibException {
		if (INSTANCE == null) {
			synchronized (Client.class) {
				if (INSTANCE == null)
					INSTANCE = new Client(configFile, true);
			}
		}
		return INSTANCE;
	}

	public static Client getWebAppInstance() throws SearchLibException,
			NamingException {
		if (INSTANCE == null) {
			String contextPath = "java:comp/env/JaeksoftSearchServer/configfile";
			File configFile = new File((String) Context.get(contextPath));
			getFileInstance(configFile);
		}
		return INSTANCE;
	}

}
