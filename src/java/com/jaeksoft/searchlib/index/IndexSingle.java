/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2008 Emmanuel Keller / Jaeksoft
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
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermFreqVector;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.store.LockObtainFailedException;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.cache.FieldCache;
import com.jaeksoft.searchlib.cache.FilterCache;
import com.jaeksoft.searchlib.cache.SearchCache;
import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.request.DocumentsRequest;
import com.jaeksoft.searchlib.request.SearchRequest;
import com.jaeksoft.searchlib.result.Result;
import com.jaeksoft.searchlib.result.ResultDocuments;
import com.jaeksoft.searchlib.schema.Schema;
import com.jaeksoft.searchlib.util.XmlWriter;

public class IndexSingle extends IndexAbstract {

	private ReaderInterface reader = null;
	private WriterInterface writer = null;

	private volatile boolean online;

	private volatile boolean readonly;

	public IndexSingle(File configDir, IndexConfig indexConfig,
			boolean createIfNotExists) throws IOException, URISyntaxException,
			InstantiationException, IllegalAccessException,
			ClassNotFoundException {
		super(indexConfig);
		online = true;
		readonly = false;
		if (indexConfig.getNativeOSSE()) {
			reader = new ReaderNativeOSSE(configDir, indexConfig);
			writer = new WriterNativeOSSE(configDir, indexConfig,
					(ReaderNativeOSSE) reader);
		} else if (indexConfig.getRemoteUri() == null) {
			reader = ReaderLocal.fromConfig(configDir, indexConfig,
					createIfNotExists);
			writer = new WriterLocal(indexConfig, (ReaderLocal) reader);
		} else {
			reader = new ReaderRemote(indexConfig);
			writer = new WriterRemote(indexConfig);
		}
	}

	@Override
	public void close() {
		w.lock();
		try {
			if (reader != null)
				reader.close();
		} finally {
			w.unlock();
		}
	}

	public void optimize(String indexName) throws CorruptIndexException,
			LockObtainFailedException, IOException, URISyntaxException,
			InstantiationException, IllegalAccessException,
			ClassNotFoundException {
		if (!online)
			throw new IOException("Index is offline");
		if (readonly)
			throw new IOException("Index is read only");
		r.lock();
		try {
			if (writer != null)
				writer.optimize(indexName);
		} finally {
			r.unlock();
		}
		reload(indexName);
	}

	public boolean deleteDocument(Schema schema, String uniqueField)
			throws CorruptIndexException, LockObtainFailedException,
			IOException, URISyntaxException, InstantiationException,
			IllegalAccessException, ClassNotFoundException {
		if (!online)
			throw new IOException("Index is offline");
		if (readonly)
			throw new IOException("Index is read only");
		r.lock();
		try {
			if (writer != null)
				return writer.deleteDocument(schema, uniqueField);
			else
				return false;
		} finally {
			r.unlock();
		}
	}

	public boolean deleteDocument(String indexName, Schema schema,
			String uniqueField) throws CorruptIndexException,
			LockObtainFailedException, IOException, URISyntaxException,
			InstantiationException, IllegalAccessException,
			ClassNotFoundException {
		if (!online)
			throw new IOException("Index is offline");
		if (readonly)
			throw new IOException("Index is read only");
		r.lock();
		try {
			if (writer != null)
				return writer.deleteDocument(indexName, schema, uniqueField);
			else
				return false;
		} finally {
			r.unlock();
		}
	}

	public boolean deleteDocument(int docId) throws IOException,
			URISyntaxException {
		if (!online)
			throw new IOException("Index is offline");
		if (readonly)
			throw new IOException("Index is read only");
		r.lock();
		try {
			if (reader != null)
				return reader.deleteDocument(docId);
			else
				return false;
		} finally {
			r.unlock();
		}
	}

	public boolean deleteDocument(String indexName, int docId)
			throws IOException, URISyntaxException {
		if (!online)
			throw new IOException("Index is offline");
		if (readonly)
			throw new IOException("Index is read only");
		r.lock();
		try {
			if (reader != null)
				return reader.deleteDocument(indexName, docId);
			else
				return false;
		} finally {
			r.unlock();
		}
	}

	public int deleteDocuments(Schema schema, Collection<String> uniqueFields)
			throws CorruptIndexException, LockObtainFailedException,
			IOException, URISyntaxException, InstantiationException,
			IllegalAccessException, ClassNotFoundException {
		if (!online)
			throw new IOException("Index is offline");
		if (readonly)
			throw new IOException("Index is read only");
		r.lock();
		try {
			if (writer != null)
				return writer.deleteDocuments(schema, uniqueFields);
			else
				return 0;
		} finally {
			r.unlock();
		}
	}

	public int deleteDocuments(String indexName, Schema schema,
			Collection<String> uniqueFields) throws CorruptIndexException,
			LockObtainFailedException, IOException, URISyntaxException,
			InstantiationException, IllegalAccessException,
			ClassNotFoundException {
		if (!online)
			throw new IOException("Index is offline");
		if (readonly)
			throw new IOException("Index is read only");
		r.lock();
		try {
			if (writer != null)
				return writer.deleteDocuments(indexName, schema, uniqueFields);
			else
				return 0;
		} finally {
			r.unlock();
		}
	}

	public int deleteDocuments(Collection<Integer> docIds)
			throws CorruptIndexException, LockObtainFailedException,
			IOException, URISyntaxException {
		if (!online)
			throw new IOException("Index is offline");
		if (readonly)
			throw new IOException("Index is read only");
		r.lock();
		try {
			if (reader != null)
				return reader.deleteDocuments(docIds);
			else
				return 0;
		} finally {
			r.unlock();
		}
	}

	public int deleteDocuments(String indexName, Collection<Integer> docIds)
			throws CorruptIndexException, LockObtainFailedException,
			IOException, URISyntaxException {
		if (!online)
			throw new IOException("Index is offline");
		if (readonly)
			throw new IOException("Index is read only");
		r.lock();
		try {
			if (reader != null)
				return reader.deleteDocuments(indexName, docIds);
			else
				return 0;
		} finally {
			r.unlock();
		}
	}

	public boolean updateDocument(Schema schema, IndexDocument document)
			throws NoSuchAlgorithmException, IOException, URISyntaxException,
			InstantiationException, IllegalAccessException,
			ClassNotFoundException {
		if (!online)
			throw new IOException("Index is offline");
		if (readonly)
			throw new IOException("Index is read only");
		r.lock();
		try {
			if (writer != null)
				return writer.updateDocument(schema, document);
			else
				return false;
		} finally {
			r.unlock();
		}
	}

	public boolean updateDocument(String indexName, Schema schema,
			IndexDocument document) throws NoSuchAlgorithmException,
			IOException, URISyntaxException, InstantiationException,
			IllegalAccessException, ClassNotFoundException {
		if (!online)
			throw new IOException("Index is offline");
		if (readonly)
			throw new IOException("Index is read only");
		r.lock();
		try {
			if (writer != null)
				return writer.updateDocument(indexName, schema, document);
			else
				return false;
		} finally {
			r.unlock();
		}
	}

	public int updateDocuments(Schema schema,
			Collection<IndexDocument> documents)
			throws NoSuchAlgorithmException, IOException, URISyntaxException,
			InstantiationException, IllegalAccessException,
			ClassNotFoundException {
		if (!online)
			throw new IOException("Index is offline");
		if (readonly)
			throw new IOException("Index is read only");
		r.lock();
		try {
			if (writer != null)
				return writer.updateDocuments(schema, documents);
			else
				return 0;
		} finally {
			r.unlock();
		}
	}

	public int updateDocuments(String indexName, Schema schema,
			Collection<IndexDocument> documents)
			throws NoSuchAlgorithmException, IOException, URISyntaxException,
			InstantiationException, IllegalAccessException,
			ClassNotFoundException {
		if (!online)
			throw new IOException("Index is offline");
		if (readonly)
			throw new IOException("Index is read only");
		r.lock();
		try {
			if (writer != null)
				return writer.updateDocuments(indexName, schema, documents);
			else
				return 0;
		} finally {
			r.unlock();
		}
	}

	public void reload() throws IOException, URISyntaxException,
			InstantiationException, IllegalAccessException,
			ClassNotFoundException {
		if (!online)
			throw new IOException("Index is offline");
		if (readonly)
			throw new IOException("Index is read only");
		w.lock();
		try {
			if (reader != null)
				reader.reload();
		} finally {
			w.unlock();
		}
	}

	@Override
	public void reload(String indexName) throws IOException,
			URISyntaxException, InstantiationException, IllegalAccessException,
			ClassNotFoundException {
		if (!acceptNameOrEmpty(indexName))
			return;
		reload();
	}

	public void swap(long version, boolean deleteOld) throws IOException,
			URISyntaxException {
		if (!online)
			throw new IOException("Index is offline");
		if (readonly)
			throw new IOException("Index is read only");
		w.lock();
		try {
			if (reader != null)
				reader.swap(version, deleteOld);
		} finally {
			w.unlock();
		}
	}

	@Override
	public void swap(String indexName, long version, boolean deleteOld)
			throws IOException, URISyntaxException {
		if (!acceptNameOrEmpty(indexName))
			return;
		swap(version, deleteOld);
	}

	public Result search(SearchRequest searchRequest) throws IOException,
			URISyntaxException, ParseException, SyntaxError,
			ClassNotFoundException, InterruptedException, SearchLibException,
			InstantiationException, IllegalAccessException {
		if (!online)
			throw new IOException("Index is offline");
		r.lock();
		try {
			if (!acceptNameOrEmpty(searchRequest.getIndexName()))
				return null;
			if (reader != null)
				return reader.search(searchRequest);
			return null;
		} finally {
			r.unlock();
		}
	}

	public ResultDocuments documents(DocumentsRequest documentsRequest)
			throws IOException, ParseException, SyntaxError,
			URISyntaxException, ClassNotFoundException, InterruptedException,
			SearchLibException, IllegalAccessException, InstantiationException {
		if (!online)
			throw new IOException("Index is offline");
		r.lock();
		try {
			if (reader != null)
				return reader.documents(documentsRequest);
		} finally {
			r.unlock();
		}
		return null;
	}

	public boolean sameIndex(ReaderInterface reader) {
		r.lock();
		try {
			if (reader == this)
				return true;
			if (reader == this.reader)
				return true;
			return false;
		} finally {
			r.unlock();
		}
	}

	public IndexStatistics getStatistics() throws IOException {
		if (!online)
			throw new IOException("Index is offline");
		r.lock();
		try {
			if (reader != null)
				return reader.getStatistics();
			return null;
		} finally {
			r.unlock();
		}
	}

	@Override
	public IndexSingle get(String name) {
		return this;
	}

	public int getDocFreq(Term term) throws IOException {
		if (!online)
			throw new IOException("Index is offline");
		r.lock();
		try {
			if (reader != null)
				return reader.getDocFreq(term);
			return 0;
		} finally {
			r.unlock();
		}
	}

	public TermFreqVector getTermFreqVector(int docId, String field)
			throws IOException {
		if (!online)
			throw new IOException("Index is offline");
		r.lock();
		try {
			if (reader != null)
				return reader.getTermFreqVector(docId, field);
			return null;
		} finally {
			r.unlock();
		}
	}

	public void push(URI dest) throws URISyntaxException, IOException {
		if (reader == null)
			return;
		boolean oldReadOnly;
		w.lock();
		try {
			oldReadOnly = readonly;
			readonly = true;
		} finally {
			w.unlock();
		}
		r.lock();
		try {
			reader.push(dest);
		} finally {
			r.unlock();
		}
		readonly = oldReadOnly;
	}

	@Override
	public void push(String indexName, URI dest) throws URISyntaxException,
			IOException {
		if (!acceptOnlyRightName(indexName))
			return;
		push(dest);
	}

	@Override
	public void receive(String indexName, long version, String fileName,
			InputStream inputStream) throws IOException {
		if (reader == null)
			return;
		if (!acceptOnlyRightName(indexName))
			return;
		WriterLocal.receiveIndexFile(((ReaderLocal) reader).getRootDir(),
				version, fileName, inputStream);
	}

	@Override
	public boolean isOnline(String indexName) {
		return online;
	}

	@Override
	public boolean isReadOnly(String indexName) {
		return readonly;
	}

	@Override
	public void setOnline(String indexName, boolean v) {
		if (!acceptNameOrEmpty(indexName))
			return;
		online = v;
	}

	@Override
	public void setReadOnly(String indexName, boolean v) {
		if (!acceptNameOrEmpty(indexName))
			return;
		readonly = v;
	}

	public long getVersion() {
		if (reader == null)
			return 0;
		return reader.getVersion();
	}

	@Override
	public long getVersion(String indexName) {
		if (!online)
			return 0;
		if (!acceptNameOrEmpty(indexName))
			return 0;
		return getVersion();
	}

	public SearchCache getSearchCache() {
		r.lock();
		try {
			if (reader != null)
				if (reader instanceof ReaderLocal)
					return ((ReaderLocal) reader).getSearchCache();
			return null;
		} finally {
			r.unlock();
		}
	}

	public FilterCache getFilterCache() {
		r.lock();
		try {
			if (reader != null)
				if (reader instanceof ReaderLocal)
					return ((ReaderLocal) reader).getFilterCache();
			return null;
		} finally {
			r.unlock();
		}
	}

	public FieldCache getFieldCache() {
		r.lock();
		try {
			if (reader != null)
				if (reader instanceof ReaderLocal)
					return ((ReaderLocal) reader).getFieldCache();
			return null;
		} finally {
			r.unlock();
		}
	}

	@Override
	protected void writeXmlConfigIndex(XmlWriter xmlWriter) throws SAXException {
		indexConfig.writeXmlConfig(xmlWriter);
	}

}
