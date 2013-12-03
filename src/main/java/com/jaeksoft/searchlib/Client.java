/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008-2013 Emmanuel Keller / Jaeksoft
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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.io.IOUtils;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermEnum;
import org.w3c.dom.Node;

import com.jaeksoft.searchlib.analysis.LanguageEnum;
import com.jaeksoft.searchlib.config.Config;
import com.jaeksoft.searchlib.crawler.web.database.CredentialItem;
import com.jaeksoft.searchlib.crawler.web.spider.HttpDownloader;
import com.jaeksoft.searchlib.index.IndexDocument;
import com.jaeksoft.searchlib.index.IndexStatistics;
import com.jaeksoft.searchlib.request.AbstractRequest;
import com.jaeksoft.searchlib.request.AbstractSearchRequest;
import com.jaeksoft.searchlib.result.AbstractResult;
import com.jaeksoft.searchlib.schema.Schema;
import com.jaeksoft.searchlib.schema.SchemaField;
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

	/**
	 * Insert or update a document in the index. If an unique key is defined in
	 * the schema, the document is updated if it already exists.
	 * 
	 * @param document
	 * @return
	 * @throws IOException
	 * @throws SearchLibException
	 */
	public boolean updateDocument(IndexDocument document)
			throws SearchLibException, IOException {
		Timer timer = new Timer("Update document " + document.toString());
		try {
			checkMaxStorageLimit();
			checkMaxDocumentLimit();
			Schema schema = getSchema();
			document.prepareCopyOf(schema);
			return getIndexAbstract().updateDocument(schema, document);
		} finally {
			getStatisticsList().addUpdate(timer);
		}
	}

	public int updateDocuments(Collection<IndexDocument> documents)
			throws IOException, SearchLibException {
		Timer timer = new Timer("Update " + documents.size() + " documents");
		try {
			checkMaxStorageLimit();
			checkMaxDocumentLimit();
			Schema schema = getSchema();
			for (IndexDocument document : documents)
				document.prepareCopyOf(schema);
			return getIndexAbstract().updateDocuments(schema, documents);
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
		checkMaxDocumentLimit();
		docCount += updateDocuments(docList);
		StringBuilder sb = new StringBuilder();
		sb.append(docCount);
		if (totalCount > 0) {
			sb.append(" / ");
			sb.append(totalCount);
		}
		sb.append(" document(s) updated.");
		if (infoCallBack != null)
			infoCallBack.setInfo(sb.toString());
		else
			Logging.info(sb.toString());
		docList.clear();
		return docCount;
	}

	public int updateXmlDocuments(Node document, int bufferSize,
			CredentialItem urlDefaultCredential, HttpDownloader httpDownloader,
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
					urlDefaultCredential, httpDownloader));
			if (docList.size() == bufferSize)
				docCount = updateDocList(totalCount, docCount, docList,
						infoCallBack);
		}
		if (docList.size() > 0)
			docCount = updateDocList(totalCount, docCount, docList,
					infoCallBack);
		return docCount;
	}

	public int updateTextDocuments(StreamSource streamSource, String charset,
			Integer bufferSize, String capturePattern, Integer langPosition,
			List<String> fieldList, InfoCallback infoCallBack)
			throws SearchLibException, IOException, NoSuchAlgorithmException,
			URISyntaxException, InstantiationException, IllegalAccessException,
			ClassNotFoundException {
		if (capturePattern == null)
			throw new SearchLibException("No capture pattern");
		if (fieldList == null || fieldList.size() == 0)
			throw new SearchLibException("empty field list");
		String[] fields = fieldList.toArray(new String[fieldList.size()]);
		Matcher matcher = Pattern.compile(capturePattern).matcher("");
		BufferedReader br = null;
		Reader reader = null;
		SchemaField uniqueSchemaField = getSchema().getFieldList()
				.getUniqueField();
		String uniqueField = uniqueSchemaField != null ? uniqueSchemaField
				.getName() : null;
		if (charset == null)
			charset = "UTF-8";
		if (bufferSize == null)
			bufferSize = 50;
		try {
			Collection<IndexDocument> docList = new ArrayList<IndexDocument>(
					bufferSize);
			reader = streamSource.getReader();
			if (reader == null)
				reader = new InputStreamReader(streamSource.getInputStream(),
						charset);
			br = reader instanceof BufferedReader ? (BufferedReader) reader
					: new BufferedReader(reader);
			String line;
			int docCount = 0;
			IndexDocument lastDocument = null;
			String lastUniqueValue = null;
			while ((line = br.readLine()) != null) {
				matcher.reset(line);
				if (!matcher.matches())
					continue;
				LanguageEnum lang = LanguageEnum.UNDEFINED;
				int matcherGroupCount = matcher.groupCount();
				if (langPosition != null && matcherGroupCount >= langPosition)
					lang = LanguageEnum.findByNameOrCode(matcher
							.group(langPosition));
				IndexDocument document = new IndexDocument(lang);
				int i = matcherGroupCount < fields.length ? matcherGroupCount
						: fields.length;
				String uniqueValue = null;
				while (i > 0) {
					String value = matcher.group(i--);
					String f = fields[i];
					document.add(f, value, 1.0F);
					if (f.equals(uniqueField))
						uniqueValue = value;
				}
				// Consecutive documents with same uniqueKey value are merged
				// (multivalued)
				if (uniqueField != null && lastDocument != null
						&& uniqueValue != null
						&& uniqueValue.equals(lastUniqueValue)) {
					lastDocument.addIfNotAlreadyHere(document);
					continue;
				}
				docList.add(document);
				if (docList.size() == bufferSize)
					docCount = updateDocList(0, docCount, docList, infoCallBack);
				lastUniqueValue = uniqueValue;
				lastDocument = document;
			}
			if (docList.size() > 0)
				docCount = updateDocList(0, docCount, docList, infoCallBack);
			return docCount;
		} finally {
			if (br != null)
				if (br != reader)
					IOUtils.closeQuietly(br);
		}
	}

	private final int deleteUniqueKeyList(int totalCount, int docCount,
			Collection<String> deleteList, InfoCallback infoCallBack)
			throws SearchLibException {
		docCount += deleteDocuments(null, deleteList);
		StringBuilder sb = new StringBuilder();
		sb.append(docCount);
		sb.append(" / ");
		sb.append(totalCount);
		sb.append(" XML document(s) deleted.");
		if (infoCallBack != null)
			infoCallBack.setInfo(sb.toString());
		else
			Logging.info(sb.toString());
		deleteList.clear();
		return docCount;
	}

	public int deleteXmlDocuments(Node xmlDoc, int bufferSize,
			InfoCallback infoCallBack) throws XPathExpressionException,
			NoSuchAlgorithmException, IOException, URISyntaxException,
			SearchLibException, InstantiationException, IllegalAccessException,
			ClassNotFoundException {
		List<Node> deleteNodeList = DomUtils
				.getNodes(xmlDoc, "index", "delete");
		Collection<String> deleteList = new ArrayList<String>(bufferSize);
		int deleteCount = 0;
		final int totalCount = deleteNodeList.size();
		for (Node deleteNode : deleteNodeList) {
			List<Node> uniqueKeyNodeList = DomUtils.getNodes(deleteNode,
					"uniquekey");
			for (Node uniqueKeyNode : uniqueKeyNodeList) {
				deleteList.add(uniqueKeyNode.getTextContent());
				if (deleteList.size() == bufferSize)
					deleteCount = deleteUniqueKeyList(totalCount, deleteCount,
							deleteList, infoCallBack);
			}
		}
		if (deleteList.size() > 0)
			deleteCount = deleteUniqueKeyList(totalCount, deleteCount,
					deleteList, infoCallBack);
		return deleteCount;
	}

	public int deleteDocument(String field, String value)
			throws SearchLibException {
		Timer timer = new Timer("Delete document " + field + ": " + value);
		try {
			return getIndexAbstract().deleteDocument(getSchema(), field, value);
		} finally {
			getStatisticsList().addDelete(timer);
		}
	}

	public int deleteDocuments(String field, Collection<String> values)
			throws SearchLibException {
		Timer timer = new Timer("Delete " + values.size() + " documents("
				+ field + ")");
		try {
			return getIndexAbstract().deleteDocuments(getSchema(), field,
					values);
		} finally {
			getStatisticsList().addDelete(timer);
		}
	}

	public int deleteDocuments(AbstractSearchRequest searchRequest)
			throws SearchLibException {
		Timer timer = new Timer("Delete by query documents");
		try {
			return getIndexAbstract().deleteDocuments(searchRequest);
		} finally {
			getStatisticsList().addDelete(timer);
		}
	}

	public void optimize() throws SearchLibException {
		Timer timer = new Timer("Optimize");
		try {
			getIndexAbstract().optimize();
		} finally {
			getStatisticsList().addOptimize(timer);
		}
	}

	public boolean isOptimizing() {
		return getIndexAbstract().isOptimizing();
	}

	public String getOptimizationStatus() throws IOException,
			SearchLibException {
		if (!isOnline())
			return "Unknown";
		if (isOptimizing())
			return "Running";
		IndexStatistics stats = getIndexAbstract().getStatistics();
		if (stats == null)
			return null;
		return Boolean.toString(stats.isOptimized());
	}

	public String getMergeStatus() {
		if (!isOnline())
			return "Unknown";
		return isMerging() ? "Merging" : null;
	}

	public boolean isMerging() {
		return getIndexAbstract().isMerging();
	}

	public void deleteAll() throws SearchLibException {
		Timer timer = new Timer("DeleteAll");
		try {
			getIndexAbstract().deleteAll();
		} finally {
			getStatisticsList().addDelete(timer);
		}
	}

	public void reload() throws SearchLibException {
		Timer timer = new Timer("Reload");
		try {
			getIndexAbstract().reload();
		} finally {
			getStatisticsList().addReload(timer);
		}
	}

	public void setOnline(boolean online) throws SearchLibException {
		if (online == getIndexAbstract().isOnline())
			return;
		getIndexAbstract().setOnline(online);
	}

	public boolean isOnline() {
		return getIndexAbstract().isOnline();
	}

	public AbstractResult<?> request(AbstractRequest request)
			throws SearchLibException {
		Timer timer = null;
		AbstractResult<?> result = null;
		SearchLibException exception = null;
		try {
			request.init(this);
			timer = new Timer(request.getNameType());
			result = getIndexAbstract().request(request);
			return result;
		} catch (SearchLibException e) {
			exception = e;
			throw e;
		} catch (Exception e) {
			exception = new SearchLibException(e);
			throw exception;
		} finally {
			if (timer != null) {
				timer.getDuration();
				if (exception != null)
					timer.setError(exception);
				getStatisticsList().addSearch(timer);
				getLogReportManager().log(request, timer, result);
			}
		}
	}

	public String explain(AbstractRequest request, int docId, boolean bHtml)
			throws SearchLibException {
		return getIndexAbstract().explain(request, docId, bHtml);
	}

	protected final void checkMaxDocumentLimit() throws SearchLibException,
			IOException {
		ClientFactory.INSTANCE.properties.checkMaxDocumentLimit();
	}

	protected void checkMaxStorageLimit() throws SearchLibException {
		ClientFactory.INSTANCE.properties.checkMaxStorageLimit();
	}

	public IndexStatistics getStatistics() throws IOException,
			SearchLibException {
		return getIndexAbstract().getStatistics();
	}

	public TermEnum getTermEnum(Term term) throws SearchLibException {
		return getIndexAbstract().getTermEnum(term);
	}

	private final static String REPL_CHECK_FILENAME = "repl.check";

	public boolean isTrueReplicate() {
		return new File(this.getDirectory(), REPL_CHECK_FILENAME).exists();
	}

	public void writeReplCheck() throws IOException {
		new File(this.getDirectory(), REPL_CHECK_FILENAME).createNewFile();
	}

	public void removeReplCheck() {
		new File(this.getDirectory(), REPL_CHECK_FILENAME).delete();
	}

	public void mergeData(Client sourceClient) throws SearchLibException {
		getIndexAbstract().mergeData(sourceClient.getIndexAbstract());
	}
}
