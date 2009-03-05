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

package com.jaeksoft.searchlib;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;

import javax.naming.NamingException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.store.LockObtainFailedException;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.config.Config;
import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.index.IndexDocument;
import com.jaeksoft.searchlib.request.DocumentsRequest;
import com.jaeksoft.searchlib.request.SearchRequest;
import com.jaeksoft.searchlib.result.Result;
import com.jaeksoft.searchlib.result.ResultDocuments;
import com.jaeksoft.searchlib.util.Context;
import com.jaeksoft.searchlib.util.XPathParser;
import com.jaeksoft.searchlib.util.XmlInfo;

public class Client extends Config implements XmlInfo {

	public Client(File homeDir, File configfile, boolean createIndexIfNotExists)
			throws SearchLibException {
		super(homeDir, configfile, createIndexIfNotExists);
	}

	public Client(File configfile, boolean createIndexIfNotExists)
			throws SearchLibException {
		this(null, configfile, createIndexIfNotExists);
	}

	public Client(File configfile) throws SearchLibException {
		this(null, configfile, false);
	}

	public boolean updateDocument(IndexDocument document)
			throws NoSuchAlgorithmException, IOException, URISyntaxException {
		return getIndex().updateDocument(getSchema(), document);
	}

	public boolean updateDocument(String indexName, IndexDocument document)
			throws NoSuchAlgorithmException, IOException, URISyntaxException {
		return getIndex().updateDocument(indexName, getSchema(), document);
	}

	public int updateDocuments(Collection<IndexDocument> documents)
			throws NoSuchAlgorithmException, IOException, URISyntaxException {
		return getIndex().updateDocuments(getSchema(), documents);
	}

	public int updateDocuments(String indexName,
			Collection<IndexDocument> documents)
			throws NoSuchAlgorithmException, IOException, URISyntaxException {
		return getIndex().updateDocuments(indexName, getSchema(), documents);
	}

	public int updateXmlDocuments(String indexName, InputStream inputStream)
			throws ParserConfigurationException, SAXException, IOException,
			XPathExpressionException, NoSuchAlgorithmException,
			URISyntaxException {
		XPathParser xpp = new XPathParser(inputStream);
		NodeList nodeList = xpp.getNodeList("/index/document");
		int l = nodeList.getLength();
		Collection<IndexDocument> docList = new ArrayList<IndexDocument>();
		for (int i = 0; i < l; i++)
			docList.add(new IndexDocument(xpp, nodeList.item(i)));
		if (indexName == null)
			return updateDocuments(docList);
		else
			return updateDocuments(indexName, docList);
	}

	public int updateXmlDocuments(InputStream inputStream)
			throws XPathExpressionException, NoSuchAlgorithmException,
			ParserConfigurationException, SAXException, IOException,
			URISyntaxException {
		return updateXmlDocuments(null, inputStream);
	}

	public boolean deleteDocument(String uniqueField)
			throws CorruptIndexException, LockObtainFailedException,
			IOException, URISyntaxException {
		return getIndex().deleteDocument(getSchema(), uniqueField);
	}

	public boolean deleteDocument(String indexName, String uniqueField)
			throws CorruptIndexException, LockObtainFailedException,
			IOException, URISyntaxException {
		return getIndex().deleteDocument(indexName, getSchema(), uniqueField);
	}

	public int deleteDocuments(Collection<String> uniqueFields)
			throws CorruptIndexException, LockObtainFailedException,
			IOException, URISyntaxException {
		return getIndex().deleteDocuments(getSchema(), uniqueFields);
	}

	public int deleteDocuments(String indexName, Collection<String> uniqueFields)
			throws CorruptIndexException, LockObtainFailedException,
			IOException, URISyntaxException {
		return getIndex().deleteDocuments(indexName, getSchema(), uniqueFields);
	}

	public int getDocFreq(String uniqueField) throws IOException {
		return getIndex().getDocFreq(
				new Term(getSchema().getFieldList().getUniqueField().getName(),
						uniqueField));
	}

	public void reload() throws IOException, URISyntaxException {
		getIndex().reload(null);
	}

	/*
	 * Older version compatibility
	 */
	@Deprecated
	public void reload(boolean deleteOld) throws IOException,
			URISyntaxException {
		reload();
	}

	public Result search(SearchRequest searchRequest) throws IOException,
			ParseException, SyntaxError, URISyntaxException,
			ClassNotFoundException {
		searchRequest.setConfig(this);
		return getIndex().search(searchRequest);
	}

	public ResultDocuments documents(DocumentsRequest documentsRequest)
			throws IOException, ParseException, SyntaxError,
			URISyntaxException, ClassNotFoundException {
		return getIndex().documents(documentsRequest);
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
