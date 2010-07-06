/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2008-2010 Emmanuel Keller / Jaeksoft
 * 
 * http://www.open-search-server.com
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
import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.index.IndexDocument;
import com.jaeksoft.searchlib.request.DocumentsRequest;
import com.jaeksoft.searchlib.request.SearchRequest;
import com.jaeksoft.searchlib.result.Result;
import com.jaeksoft.searchlib.result.ResultDocuments;
import com.jaeksoft.searchlib.statistics.StatisticsList;
import com.jaeksoft.searchlib.util.Timer;
import com.jaeksoft.searchlib.util.XPathParser;

public class Client extends Config {

	public Client(File initFileOrDir, boolean createIndexIfNotExists)
			throws SearchLibException {
		super(initFileOrDir, null, createIndexIfNotExists);
	}

	public Client(File initFileOrDir, String resourceName,
			boolean createIndexIfNotExists) throws SearchLibException {
		super(initFileOrDir, resourceName, createIndexIfNotExists);
	}

	public Client(File initFile) throws SearchLibException {
		this(initFile, false);
	}

	public boolean updateDocument(IndexDocument document)
			throws NoSuchAlgorithmException, IOException, URISyntaxException,
			SearchLibException, InstantiationException, IllegalAccessException,
			ClassNotFoundException {
		Timer timer = new Timer("Update document " + document.toString());
		try {
			return getIndex().updateDocument(getSchema(), document);
		} finally {
			getStatisticsList().addUpdate(timer);
		}
	}

	public boolean updateDocument(String indexName, IndexDocument document)
			throws NoSuchAlgorithmException, IOException, URISyntaxException,
			SearchLibException, InstantiationException, IllegalAccessException,
			ClassNotFoundException {
		Timer timer = new Timer("Update document " + document.toString());
		try {
			return getIndex().updateDocument(indexName, getSchema(), document);
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
			return getIndex().updateDocuments(getSchema(), documents);
		} finally {
			getStatisticsList().addUpdate(timer);
		}
	}

	public int updateDocuments(String indexName,
			Collection<IndexDocument> documents)
			throws NoSuchAlgorithmException, IOException, URISyntaxException,
			SearchLibException, InstantiationException, IllegalAccessException,
			ClassNotFoundException {
		Timer timer = new Timer("Update " + documents.size() + " documents");
		try {
			return getIndex()
					.updateDocuments(indexName, getSchema(), documents);
		} finally {
			getStatisticsList().addUpdate(timer);
		}
	}

	private int updateXmlDocuments(String indexName, XPathParser xpp)
			throws XPathExpressionException, NoSuchAlgorithmException,
			IOException, URISyntaxException, SearchLibException,
			InstantiationException, IllegalAccessException,
			ClassNotFoundException {
		NodeList nodeList = xpp.getNodeList("/index/document");
		int l = nodeList.getLength();
		Collection<IndexDocument> docList = new ArrayList<IndexDocument>();
		for (int i = 0; i < l; i++)
			docList.add(new IndexDocument(getParserSelector(), xpp, nodeList
					.item(i)));
		if (indexName == null)
			return updateDocuments(docList);
		else
			return updateDocuments(indexName, docList);
	}

	public int updateXmlDocuments(String indexName, InputSource inputSource)
			throws ParserConfigurationException, SAXException, IOException,
			XPathExpressionException, NoSuchAlgorithmException,
			URISyntaxException, SearchLibException, InstantiationException,
			IllegalAccessException, ClassNotFoundException {
		XPathParser xpp = new XPathParser(inputSource);
		return updateXmlDocuments(indexName, xpp);
	}

	public int updateXmlDocuments(String indexName, String xmlString)
			throws SAXException, IOException, ParserConfigurationException,
			XPathExpressionException, NoSuchAlgorithmException,
			URISyntaxException, SearchLibException, InstantiationException,
			IllegalAccessException, ClassNotFoundException {
		XPathParser xpp = new XPathParser(new InputSource(new StringReader(
				xmlString)));
		return updateXmlDocuments(indexName, xpp);
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

	public boolean deleteDocument(String indexName, String uniqueField)
			throws CorruptIndexException, LockObtainFailedException,
			IOException, URISyntaxException, SearchLibException,
			InstantiationException, IllegalAccessException,
			ClassNotFoundException, HttpException {
		Timer timer = new Timer("Delete document " + uniqueField);
		try {
			return getIndex().deleteDocument(indexName, getSchema(),
					uniqueField);
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

	public int deleteDocuments(String indexName, Collection<String> uniqueFields)
			throws CorruptIndexException, LockObtainFailedException,
			IOException, URISyntaxException, SearchLibException,
			InstantiationException, IllegalAccessException,
			ClassNotFoundException {
		Timer timer = new Timer("Delete " + uniqueFields.size() + " documents");
		try {
			return getIndex().deleteDocuments(indexName, getSchema(),
					uniqueFields);
		} finally {
			getStatisticsList().addDelete(timer);
		}
	}

	public void optimize(String indexName) throws IOException,
			URISyntaxException, SearchLibException, InstantiationException,
			IllegalAccessException, ClassNotFoundException, HttpException {
		Timer timer = new Timer("Optimize " + indexName);
		try {
			getIndex().optimize(indexName);
		} finally {
			getStatisticsList().addOptimize(timer);
		}
	}

	public void reload(String indexName) throws IOException,
			URISyntaxException, SearchLibException, InstantiationException,
			IllegalAccessException, ClassNotFoundException, HttpException {
		Timer timer = new Timer("Reload " + indexName);
		try {
			getIndex().reload(indexName);
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
		reload(null);
	}

	public Result search(SearchRequest searchRequest) throws IOException,
			ParseException, SyntaxError, URISyntaxException,
			ClassNotFoundException, InterruptedException, SearchLibException,
			InstantiationException, IllegalAccessException {
		Timer timer = null;
		Exception exception = null;
		try {
			searchRequest.init(this);
			timer = searchRequest.getTimer();
			Result result = getIndex().search(searchRequest);
			return result;
		} catch (IOException e) {
			exception = e;
			throw e;
		} catch (ParseException e) {
			exception = e;
			throw e;
		} catch (SyntaxError e) {
			exception = e;
			throw e;
		} catch (URISyntaxException e) {
			exception = e;
			throw e;
		} catch (ClassNotFoundException e) {
			exception = e;
			throw e;
		} catch (InterruptedException e) {
			exception = e;
			throw e;
		} catch (SearchLibException e) {
			exception = e;
			throw e;
		} catch (InstantiationException e) {
			exception = e;
			throw e;
		} catch (IllegalAccessException e) {
			exception = e;
			throw e;
		} finally {
			if (timer != null) {
				if (exception != null)
					timer.setError(exception);
				getStatisticsList().addSearch(timer);
			}
		}
	}

	public String explain(SearchRequest searchRequest, int docId)
			throws IOException, ParseException, SyntaxError, SearchLibException {
		Timer timer = null;
		Exception exception = null;
		try {
			searchRequest.init(this);
			timer = searchRequest.getTimer();
			return getIndex().explain(searchRequest, docId);
		} catch (IOException e) {
			exception = e;
			throw e;
		} catch (ParseException e) {
			exception = e;
			throw e;
		} catch (SyntaxError e) {
			exception = e;
			throw e;
		} finally {
			if (timer != null) {
				if (exception != null)
					timer.setError(exception);
				getStatisticsList().addSearch(timer);
			}
		}
	}

	public ResultDocuments documents(DocumentsRequest documentsRequest)
			throws IOException, ParseException, SyntaxError,
			URISyntaxException, ClassNotFoundException, InterruptedException,
			SearchLibException, IllegalAccessException, InstantiationException {
		return getIndex().documents(documentsRequest);
	}

	public void close() {
		try {
			getFileCrawlMaster().abort();
			getWebCrawlMaster().abort();
			getFileCrawlMaster().waitForEnd();
			getWebCrawlMaster().waitForEnd();
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			getIndex().close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			StatisticsList statList = getStatisticsList();
			if (statList != null)
				statList.save(getStatStorage());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
