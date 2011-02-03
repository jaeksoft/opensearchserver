/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2008-2011 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.index;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;

import javax.xml.xpath.XPathExpressionException;

import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.index.TermFreqVector;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.similar.MoreLikeThis;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.SearchLibException;
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
import com.jaeksoft.searchlib.util.XmlWriter;

public class IndexGroup extends IndexAbstract {

	private Map<String, IndexSingle> indices;

	private ExecutorService threadPool;

	public IndexGroup(File homeDir, XPathParser xpp, Node parentNode,
			boolean createIfNotExists, ExecutorService threadPool)
			throws XPathExpressionException, IOException, URISyntaxException,
			InstantiationException, IllegalAccessException,
			ClassNotFoundException {
		super(null);
		this.threadPool = threadPool;
		indices = new TreeMap<String, IndexSingle>();
		NodeList nodes = xpp.getNodeList(parentNode, "index");
		for (int i = 0; i < nodes.getLength(); i++) {
			Node node = nodes.item(i);
			IndexConfig indexConfig = new IndexConfig(xpp, node);
			indices.put(null, new IndexSingle(homeDir, indexConfig,
					createIfNotExists));
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

	@Override
	public void close() {
		for (IndexAbstract index : indices.values())
			index.close();
	}

	@Override
	public void optimize() throws SearchLibException {
		for (IndexAbstract index : getIndices())
			index.optimize();
	}

	@Override
	public IndexStatistics getStatistics() throws IOException {
		IndexStatistics stats = new IndexStatistics();
		for (IndexAbstract index : indices.values())
			stats.add(index.getStatistics());
		return stats;
	}

	@Override
	public void addBeforeUpdate(BeforeUpdateInterface beforeUpdate)
			throws SearchLibException {
		for (IndexAbstract index : getIndices())
			index.addBeforeUpdate(beforeUpdate);

	}

	@Override
	public boolean updateDocument(Schema schema, IndexDocument document)
			throws SearchLibException {
		boolean updated = false;
		for (IndexAbstract index : getIndices())
			if (index.updateDocument(schema, document))
				updated = true;
		return updated;
	}

	@Override
	public int updateDocuments(Schema schema,
			Collection<IndexDocument> documents) throws SearchLibException {
		int count = 0;
		for (IndexAbstract index : getIndices())
			count += index.updateDocuments(schema, documents);
		return count;
	}

	@Override
	public boolean deleteDocument(Schema schema, String uniqueField)
			throws SearchLibException {
		boolean deleted = false;
		for (IndexAbstract index : getIndices())
			if (index.deleteDocument(schema, uniqueField))
				deleted = true;
		return deleted;
	}

	@Override
	public int deleteDocuments(Schema schema, Collection<String> uniqueFields)
			throws SearchLibException {
		int count = 0;
		for (IndexAbstract index : getIndices())
			count += index.deleteDocuments(schema, uniqueFields);
		return count;
	}

	@Override
	public int getDocFreq(Term term) throws SearchLibException {
		int r = 0;
		for (IndexAbstract index : getIndices())
			r += index.getDocFreq(term);
		return r;
	}

	@Override
	public TermFreqVector getTermFreqVector(int docId, String field)
			throws SearchLibException {
		throw new RuntimeException("Not yet implemented");
	}

	@Override
	public Result search(SearchRequest searchRequest) throws SearchLibException {
		ResultGroup resultGroup;
		try {
			resultGroup = new SearchGroup(this, searchRequest, threadPool)
					.getResult();
			if (resultGroup == null)
				return null;
			if (searchRequest.isWithDocument())
				resultGroup.loadDocuments(this);
			return resultGroup;
		} catch (IOException e) {
			throw new SearchLibException(e);
		} catch (URISyntaxException e) {
			throw new SearchLibException(e);
		} catch (ParseException e) {
			throw new SearchLibException(e);
		} catch (SyntaxError e) {
			throw new SearchLibException(e);
		} catch (ClassNotFoundException e) {
			throw new SearchLibException(e);
		} catch (InterruptedException e) {
			throw new SearchLibException(e);
		} catch (IllegalAccessException e) {
			throw new SearchLibException(e);
		} catch (InstantiationException e) {
			throw new SearchLibException(e);
		}
	}

	@Override
	public boolean sameIndex(ReaderInterface reader) {
		return reader == this;
	}

	@Override
	public void push(URI dest) throws SearchLibException {
		throw new RuntimeException("Not implemented");
	}

	@Override
	public boolean isOnline() {
		int i = 0;
		for (IndexAbstract index : getIndices())
			if (index.isOnline())
				i++;
		return i == getIndices().size();
	}

	@Override
	public boolean isReadOnly() {
		for (IndexAbstract index : getIndices())
			if (index.isReadOnly())
				return true;
		return false;
	}

	@Override
	public void setOnline(boolean v) {
		for (IndexAbstract index : getIndices())
			index.setOnline(v);
	}

	@Override
	public void setReadOnly(boolean v) {
		for (IndexAbstract index : getIndices())
			index.setReadOnly(v);
	}

	@Override
	public long getVersion() {
		throw new RuntimeException("Not implemented");
	}

	@Override
	public void reload() throws SearchLibException {
		for (IndexAbstract index : getIndices())
			index.reload();
	}

	@Override
	public void swap(long version, boolean deleteOld) {
		throw new RuntimeException("Operation not permitted on grouped indices");
	}

	@Override
	public ResultDocuments documents(DocumentsRequest documentsRequest)
			throws SearchLibException {
		try {
			return new DocumentsGroup(this, documentsRequest, threadPool)
					.getDocuments();
		} catch (IOException e) {
			throw new SearchLibException(e);
		} catch (URISyntaxException e) {
			throw new SearchLibException(e);
		} catch (ParseException e) {
			throw new SearchLibException(e);
		} catch (SyntaxError e) {
			throw new SearchLibException(e);
		} catch (ClassNotFoundException e) {
			throw new SearchLibException(e);
		} catch (InterruptedException e) {
			throw new SearchLibException(e);
		} catch (IllegalAccessException e) {
			throw new SearchLibException(e);
		} catch (InstantiationException e) {
			throw new SearchLibException(e);
		}
	}

	@Override
	protected void writeXmlConfigIndex(XmlWriter xmlWriter) throws SAXException {
		for (IndexSingle index : getIndices())
			index.writeXmlConfig(xmlWriter);
	}

	@Override
	public TermEnum getTermEnum() throws SearchLibException {
		throw new RuntimeException("Not yet implemented");
	}

	@Override
	public Collection<?> getFieldNames() {
		throw new RuntimeException("Not yet implemented");
	}

	@Override
	public TermEnum getTermEnum(String field, String term)
			throws SearchLibException {
		throw new RuntimeException("Not yet implemented");
	}

	@Override
	public String explain(SearchRequest searchRequest, int docId) {
		throw new RuntimeException("Not yet implemented");
	}

	@Override
	public int deleteDocuments(SearchRequest query) throws SearchLibException {
		throw new RuntimeException("Not yet implemented");
	}

	@Override
	public Query rewrite(Query query) throws SearchLibException {
		throw new RuntimeException("Not yet implemented");
	}

	@Override
	public MoreLikeThis getMoreLikeThis() {
		throw new RuntimeException("Not yet implemented");
	}

}
