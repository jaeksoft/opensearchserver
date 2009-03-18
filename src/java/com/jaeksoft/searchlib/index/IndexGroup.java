/**   
 * License Agreement for Jaeksoft SearchLib Community
 *
 * Copyright (C) 2008-2009 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.index;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;

import javax.xml.xpath.XPathExpressionException;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermFreqVector;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.store.LockObtainFailedException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.request.DocumentsGroup;
import com.jaeksoft.searchlib.request.DocumentsRequest;
import com.jaeksoft.searchlib.request.SearchGroup;
import com.jaeksoft.searchlib.request.SearchRequest;
import com.jaeksoft.searchlib.result.Result;
import com.jaeksoft.searchlib.result.ResultDocuments;
import com.jaeksoft.searchlib.result.ResultGroup;
import com.jaeksoft.searchlib.schema.Schema;
import com.jaeksoft.searchlib.util.XPathParser;

public class IndexGroup extends IndexAbstract {

	private Map<String, IndexSingle> indices;

	private ExecutorService threadPool;

	public IndexGroup(File homeDir, XPathParser xpp, Node parentNode,
			boolean createIfNotExists, ExecutorService threadPool)
			throws XPathExpressionException, IOException, URISyntaxException {
		super();
		this.threadPool = threadPool;
		indices = new TreeMap<String, IndexSingle>();
		NodeList nodes = xpp.getNodeList(parentNode, "index");
		for (int i = 0; i < nodes.getLength(); i++) {
			Node node = nodes.item(i);
			IndexConfig indexConfig = new IndexConfig(xpp, node);
			indices.put(indexConfig.getName(), new IndexSingle(homeDir,
					indexConfig, createIfNotExists));
		}
	}

	public Collection<IndexSingle> getIndices() {
		return indices.values();
	}

	public int size() {
		return indices.size();
	}

	@Override
	public IndexAbstract get(String name) {
		return indices.get(name);
	}

	public void xmlInfo(PrintWriter writer) {
		writer.println("<indices>");
		for (IndexAbstract index : indices.values())
			index.xmlInfo(writer);
		writer.println("</indices>");
	}

	public void optimize(String indexName) throws CorruptIndexException,
			LockObtainFailedException, IOException, URISyntaxException {
		if (indexName == null || indexName.length() == 0) {
			for (IndexAbstract index : getIndices())
				index.optimize(null);
			return;
		}
		IndexAbstract index = get(indexName);
		if (index == null)
			return;
		index.optimize(null);
	}

	public IndexStatistics getStatistics() throws IOException {
		IndexStatistics stats = new IndexStatistics();
		for (IndexAbstract index : indices.values())
			stats.add(index.getStatistics());
		return stats;
	}

	public boolean updateDocument(Schema schema, IndexDocument document)
			throws NoSuchAlgorithmException, IOException, URISyntaxException {
		boolean updated = false;
		for (IndexAbstract index : getIndices())
			if (index.updateDocument(schema, document))
				updated = true;
		return updated;
	}

	public boolean updateDocument(String indexName, Schema schema,
			IndexDocument document) throws NoSuchAlgorithmException,
			IOException, URISyntaxException {
		IndexAbstract index = get(indexName);
		if (index == null)
			return false;
		return index.updateDocument(schema, document);

	}

	public int updateDocuments(Schema schema,
			Collection<IndexDocument> documents)
			throws NoSuchAlgorithmException, IOException, URISyntaxException {
		int count = 0;
		for (IndexAbstract index : getIndices())
			count += index.updateDocuments(schema, documents);
		return count;
	}

	public int updateDocuments(String indexName, Schema schema,
			Collection<IndexDocument> documents)
			throws NoSuchAlgorithmException, IOException, URISyntaxException {
		IndexAbstract index = get(indexName);
		if (index == null)
			return 0;
		return index.updateDocuments(indexName, schema, documents);
	}

	public boolean deleteDocument(Schema schema, String uniqueField)
			throws CorruptIndexException, LockObtainFailedException,
			IOException, URISyntaxException {
		boolean deleted = false;
		for (IndexAbstract index : getIndices())
			if (index.deleteDocument(schema, uniqueField))
				deleted = true;
		return deleted;
	}

	public boolean deleteDocument(String indexName, Schema schema,
			String uniqueField) throws CorruptIndexException,
			LockObtainFailedException, IOException, URISyntaxException {
		IndexAbstract index = get(indexName);
		if (index == null)
			return false;
		return index.deleteDocument(schema, uniqueField);
	}

	public boolean deleteDocument(String indexName, int docId)
			throws CorruptIndexException, LockObtainFailedException,
			IOException, URISyntaxException {
		IndexAbstract index = get(indexName);
		if (index == null)
			return false;
		return index.deleteDocument(docId);
	}

	public boolean deleteDocument(int docId) throws CorruptIndexException,
			LockObtainFailedException, IOException, URISyntaxException {
		boolean deleted = false;
		for (IndexAbstract index : getIndices())
			if (index.deleteDocument(docId))
				deleted = true;
		return deleted;
	}

	public int deleteDocuments(Schema schema, Collection<String> uniqueFields)
			throws CorruptIndexException, LockObtainFailedException,
			IOException, URISyntaxException {
		int count = 0;
		for (IndexAbstract index : getIndices())
			count += index.deleteDocuments(schema, uniqueFields);
		return count;
	}

	public int deleteDocuments(String indexName, Schema schema,
			Collection<String> uniqueFields) throws CorruptIndexException,
			LockObtainFailedException, IOException, URISyntaxException {
		IndexAbstract index = get(indexName);
		if (index == null)
			return 0;
		return index.deleteDocuments(schema, uniqueFields);
	}

	public int deleteDocuments(Collection<Integer> docIds)
			throws CorruptIndexException, LockObtainFailedException,
			IOException, URISyntaxException {
		int count = 0;
		for (IndexAbstract index : getIndices())
			count += index.deleteDocuments(docIds);
		return count;
	}

	public int deleteDocuments(String indexName, Collection<Integer> docIds)
			throws CorruptIndexException, LockObtainFailedException,
			IOException, URISyntaxException {
		IndexAbstract index = get(indexName);
		if (index == null)
			return 0;
		return index.deleteDocuments(docIds);
	}

	public int getDocFreq(Term term) throws IOException {
		int r = 0;
		for (IndexAbstract index : getIndices())
			r += index.getDocFreq(term);
		return r;
	}

	public TermFreqVector getTermFreqVector(int docId, String field)
			throws IOException {
		throw new RuntimeException("Not yet implemented");
	}

	public void reload(String indexName) throws IOException, URISyntaxException {
		for (IndexAbstract index : getIndices())
			index.reload(indexName);
	}

	public void swap(String indexName, long version, boolean deleteOld)
			throws IOException, URISyntaxException {
		for (IndexAbstract index : getIndices())
			index.swap(indexName, version, deleteOld);
	}

	public Result search(SearchRequest searchRequest) throws IOException,
			URISyntaxException, ParseException, SyntaxError,
			ClassNotFoundException, InterruptedException {
		String indexName = searchRequest.getIndexName();
		if (indexName != null)
			return get(indexName).search(searchRequest);
		ResultGroup resultGroup = new SearchGroup(this, searchRequest,
				threadPool).getResult();
		if (resultGroup == null)
			return null;
		if (searchRequest.isWithDocument())
			resultGroup.loadDocuments(this);
		return resultGroup;
	}

	public boolean sameIndex(ReaderInterface reader) {
		return reader == this;
	}

	@Override
	public void receive(String indexName, long version, String fileName,
			InputStream inputStream) throws IOException {
		IndexAbstract idx = get(indexName);
		if (idx == null)
			return;
		idx.receive(indexName, version, fileName, inputStream);
	}

	@Override
	public void push(String indexName, URI dest) throws URISyntaxException,
			IOException {
		IndexAbstract index = get(indexName);
		if (index != null) {
			index.push(indexName, dest);
			return;
		}
		for (IndexAbstract idx : getIndices())
			idx.push(idx.getName(), dest);
	}

	@Override
	public boolean isOnline(String indexName) {
		IndexAbstract index = get(indexName);
		if (index == null)
			return false;
		return index.isOnline(indexName);
	}

	@Override
	public boolean isReadOnly(String indexName) {
		IndexAbstract index = get(indexName);
		if (index == null)
			return false;
		return index.isReadOnly(indexName);
	}

	@Override
	public void setOnline(String indexName, boolean v) {
		IndexAbstract index = get(indexName);
		if (index == null)
			return;
		index.setOnline(indexName, v);
	}

	@Override
	public void setReadOnly(String indexName, boolean v) {
		IndexAbstract index = get(indexName);
		if (index == null)
			return;
		index.setReadOnly(indexName, v);
	}

	@Override
	public long getVersion(String indexName) {
		// TODO Auto-generated method stub
		return 0;
	}

	public long getVersion() {
		// TODO Auto-generated method stub
		return 0;
	}

	public void push(URI dest) throws URISyntaxException, IOException {
		// TODO Auto-generated method stub

	}

	public void reload() throws IOException, URISyntaxException {
		for (IndexAbstract index : getIndices())
			index.reload();
	}

	public void swap(long version, boolean deleteOld) throws IOException {
		// TODO Auto-generated method stub
		throw new RuntimeException("Operation not permitted on grouped indices");
	}

	public ResultDocuments documents(DocumentsRequest documentsRequest)
			throws IOException, ParseException, SyntaxError,
			URISyntaxException, ClassNotFoundException, InterruptedException {
		String indexName = documentsRequest.getIndexName();
		if (indexName != null)
			return get(indexName).documents(documentsRequest);
		else
			return new DocumentsGroup(this, documentsRequest, threadPool)
					.getDocuments();
	}

}
