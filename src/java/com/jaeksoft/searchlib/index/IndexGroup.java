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

package com.jaeksoft.searchlib.index;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.LinkedHashMap;

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

	private LinkedHashMap<String, IndexSingle> indices;

	public IndexGroup(File homeDir, XPathParser xpp, Node parentNode,
			boolean createIfNotExists) throws XPathExpressionException,
			IOException, URISyntaxException {
		super();
		indices = new LinkedHashMap<String, IndexSingle>();
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

	public void updateDocument(Schema schema, IndexDocument document)
			throws NoSuchAlgorithmException, IOException, URISyntaxException {
		for (IndexAbstract index : getIndices())
			index.updateDocument(schema, document);
	}

	public void updateDocument(String indexName, Schema schema,
			IndexDocument document) throws NoSuchAlgorithmException,
			IOException, URISyntaxException {
		IndexAbstract index = get(indexName);
		if (index == null)
			return;
		index.updateDocument(schema, document);

	}

	public void updateDocuments(Schema schema,
			Collection<IndexDocument> documents)
			throws NoSuchAlgorithmException, IOException, URISyntaxException {
		for (IndexAbstract index : getIndices())
			index.updateDocuments(schema, documents);
	}

	public void updateDocuments(String indexName, Schema schema,
			Collection<IndexDocument> documents)
			throws NoSuchAlgorithmException, IOException, URISyntaxException {
		IndexAbstract index = get(indexName);
		if (index == null)
			return;
		index.updateDocuments(indexName, schema, documents);
	}

	public void deleteDocument(Schema schema, String uniqueField)
			throws CorruptIndexException, LockObtainFailedException,
			IOException, URISyntaxException {
		for (IndexAbstract index : getIndices())
			index.deleteDocument(schema, uniqueField);
	}

	public void deleteDocument(String indexName, Schema schema,
			String uniqueField) throws CorruptIndexException,
			LockObtainFailedException, IOException, URISyntaxException {
		IndexAbstract index = get(indexName);
		if (index == null)
			return;
		index.deleteDocument(schema, uniqueField);
	}

	public void deleteDocuments(Schema schema, Collection<String> uniqueFields)
			throws CorruptIndexException, LockObtainFailedException,
			IOException, URISyntaxException {
		for (IndexAbstract index : getIndices())
			index.deleteDocuments(schema, uniqueFields);
	}

	public void deleteDocuments(String indexName, Schema schema,
			Collection<String> uniqueFields) throws CorruptIndexException,
			LockObtainFailedException, IOException, URISyntaxException {
		IndexAbstract index = get(indexName);
		if (index == null)
			return;
		index.deleteDocuments(schema, uniqueFields);
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
			ClassNotFoundException {
		String indexName = searchRequest.getIndexName();
		if (indexName != null)
			return get(indexName).search(searchRequest);
		ResultGroup resultGroup = new SearchGroup(this, searchRequest)
				.getResult();
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
			URISyntaxException, ClassNotFoundException {
		String indexName = documentsRequest.getIndexName();
		if (indexName != null)
			return get(indexName).documents(documentsRequest);
		else
			return new DocumentsGroup(this, documentsRequest).getDocuments();
	}

}
