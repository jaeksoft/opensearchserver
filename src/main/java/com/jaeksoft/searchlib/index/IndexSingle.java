/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008-2015 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.index;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.index.TermFreqVector;
import org.apache.lucene.index.TermPositions;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.similar.MoreLikeThis;
import org.json.JSONException;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.analysis.PerFieldAnalyzer;
import com.jaeksoft.searchlib.filter.FilterAbstract;
import com.jaeksoft.searchlib.filter.FilterHits;
import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.query.ParseException;
import com.jaeksoft.searchlib.request.AbstractLocalSearchRequest;
import com.jaeksoft.searchlib.request.AbstractRequest;
import com.jaeksoft.searchlib.result.AbstractResult;
import com.jaeksoft.searchlib.schema.FieldValue;
import com.jaeksoft.searchlib.schema.Schema;
import com.jaeksoft.searchlib.schema.SchemaField;
import com.jaeksoft.searchlib.util.IOUtils;
import com.jaeksoft.searchlib.util.ReadWriteLock;
import com.jaeksoft.searchlib.util.Timer;
import com.jaeksoft.searchlib.util.XmlWriter;
import com.jaeksoft.searchlib.webservice.query.document.IndexDocumentResult;

public class IndexSingle extends IndexAbstract {

	final private ReadWriteLock rwl = new ReadWriteLock();

	final private IndexDirectory indexDirectory;

	private volatile ReaderLocal _reader;
	private final WriterInterface writer;

	private volatile boolean online;

	private final Set<ReaderLocal> closeableReaders;

	public IndexSingle(File configDir, IndexConfig indexConfig, boolean createIfNotExists)
			throws IOException, URISyntaxException, SearchLibException, JSONException {
		super(indexConfig);
		this.online = true;
		closeableReaders = new HashSet<ReaderLocal>();
		boolean bCreate = false;
		File indexDir = new File(configDir, "index");
		if (!indexDir.exists()) {
			if (!createIfNotExists) {
				indexDirectory = null;
				_reader = null;
				writer = null;
				return;
			}
			indexDir.mkdir();
			bCreate = true;
		} else
			indexDir = findIndexDirOrSub(indexDir);
		URI remoteURI = indexConfig.getRemoteURI();
		indexDirectory = remoteURI == null ? new IndexDirectory(indexDir) : new IndexDirectory(remoteURI);
		bCreate = bCreate || indexDirectory.isEmpty();
		if (!indexConfig.isMulti()) {
			writer = new WriterLocal(indexConfig, this, indexDirectory);
			if (bCreate)
				((WriterLocal) writer).create();
		} else {
			writer = null;
		}
		_reader = new ReaderLocal(indexConfig, indexDirectory);
	}

	/**
	 * Check if there is old style index sub directory
	 * 
	 * @param indexDir
	 * @return
	 */
	private File findIndexDirOrSub(File indexDir) {
		File[] dirs = indexDir.listFiles((FileFilter) DirectoryFileFilter.INSTANCE);
		if (dirs == null)
			return indexDir;
		if (dirs.length == 0)
			return indexDir;
		return dirs[dirs.length - 1];
	}

	@Override
	public void close() {
		rwl.w.lock();
		try {
			if (_reader != null)
				IOUtils.close(_reader);
			_reader = null;
			indexDirectory.close();
		} finally {
			rwl.w.unlock();
		}
	}

	private void checkOnline(boolean online) throws SearchLibException {
		if (this.online != online)
			throw new SearchLibException("Index is offline");
	}

	@Override
	public void optimize() throws SearchLibException {
		rwl.r.lock();
		try {
			checkOnline(true);
			if (writer == null)
				return;
			writer.optimize();
			reload();
		} finally {
			rwl.r.unlock();
		}
	}

	@Override
	public boolean isOptimizing() {
		rwl.r.lock();
		try {
			return writer != null ? writer.isOptimizing() : false;
		} finally {
			rwl.r.unlock();
		}
	}

	@Override
	public void deleteAll() throws SearchLibException {
		rwl.r.lock();
		try {
			checkOnline(true);
			if (writer == null)
				return;
			writer.deleteAll();
			reload();
		} finally {
			rwl.r.unlock();
		}
	}

	@Override
	public int deleteDocuments(AbstractRequest request) throws SearchLibException {
		rwl.r.lock();
		try {
			checkOnline(true);
			if (writer == null)
				return 0;
			int res = writer.deleteDocuments(request);
			reload();
			return res;
		} finally {
			rwl.r.unlock();
		}
	}

	@Override
	public void addUpdateInterface(UpdateInterfaces updateInterface) throws SearchLibException {
		rwl.r.lock();
		try {
			if (writer != null)
				writer.addUpdateInterface(updateInterface);
		} finally {
			rwl.r.unlock();
		}
	}

	@Override
	public boolean updateDocument(Schema schema, IndexDocument document) throws SearchLibException {
		rwl.r.lock();
		try {
			checkOnline(true);
			if (writer == null)
				return false;
			if (!writer.updateDocument(schema, document))
				return false;
			reload();
			return true;
		} finally {
			rwl.r.unlock();
		}
	}

	@Override
	public int updateDocuments(Schema schema, Collection<IndexDocument> documents) throws SearchLibException {
		rwl.r.lock();
		try {
			checkOnline(true);
			if (writer == null)
				return 0;
			int res = writer.updateDocuments(schema, documents);
			reload();
			return res;
		} finally {
			rwl.r.unlock();
		}
	}

	@Override
	public int updateIndexDocuments(Schema schema, Collection<IndexDocumentResult> documents)
			throws SearchLibException {
		rwl.r.lock();
		try {
			checkOnline(true);
			if (writer == null)
				return 0;
			int res = writer.updateIndexDocuments(schema, documents);
			reload();
			return res;
		} finally {
			rwl.r.unlock();
		}
	}

	private void reloadNoLock() throws SearchLibException {
		synchronized (closeableReaders) {
			if (_reader != null)
				closeableReaders.add(_reader);
		}
		try {
			_reader = new ReaderLocal(indexConfig, indexDirectory);
			closeCloseables();
		} catch (IOException e) {
			throw new SearchLibException(e);
		}
	}

	@Override
	public void reload() throws SearchLibException {
		rwl.r.lock();
		try {
			checkOnline(true);
			ReaderLocal reader = acquire();
			try {
				reloadNoLock();
			} finally {
				release(reader);
			}
		} finally {
			rwl.r.unlock();
		}
	}

	private synchronized ReaderLocal acquire() {
		return _reader.acquire();
	}

	private synchronized void release(ReaderLocal reader) {
		reader.release();
		closeCloseables();
	}

	@Override
	public AbstractResult<?> request(AbstractRequest request) throws SearchLibException {
		rwl.r.lock();
		try {
			checkOnline(true);
			ReaderLocal reader = acquire();
			try {
				return reader.request(request);
			} finally {
				release(reader);
			}
		} finally {
			rwl.r.unlock();
		}
	}

	@Override
	public String explain(AbstractRequest request, int docId, boolean bHtml) throws SearchLibException {
		rwl.r.lock();
		try {
			checkOnline(true);
			ReaderLocal reader = acquire();
			try {
				return reader.explain(request, docId, bHtml);
			} finally {
				release(reader);
			}
		} finally {
			rwl.r.unlock();
		}
	}

	@Override
	public boolean sameIndex(ReaderInterface reader) {
		rwl.r.lock();
		try {
			if (reader == this)
				return true;
			if (reader == this._reader)
				return true;
			return false;
		} finally {
			rwl.r.unlock();
		}
	}

	@Override
	public IndexStatistics getStatistics() throws IOException, SearchLibException {
		rwl.r.lock();
		try {
			checkOnline(true);
			ReaderLocal reader = acquire();
			try {
				return reader.getStatistics();
			} finally {
				release(reader);
			}
		} finally {
			rwl.r.unlock();
		}
	}

	@Override
	public IndexSingle get(String name) {
		return this;
	}

	@Override
	final public int getDocFreq(final Term term) throws SearchLibException {
		rwl.r.lock();
		try {
			checkOnline(true);
			ReaderLocal reader = acquire();
			try {
				return reader.getDocFreq(term);
			} finally {
				release(reader);
			}
		} finally {
			rwl.r.unlock();
		}
	}

	@Override
	final public TermEnum getTermEnum() throws SearchLibException {
		rwl.r.lock();
		try {
			checkOnline(true);
			ReaderLocal reader = acquire();
			try {
				return reader.getTermEnum();
			} finally {
				release(reader);
			}
		} finally {
			rwl.r.unlock();
		}
	}

	@Override
	final public TermEnum getTermEnum(final Term term) throws SearchLibException {
		rwl.r.lock();
		try {
			checkOnline(true);
			ReaderLocal reader = acquire();
			try {
				return reader.getTermEnum(term);
			} finally {
				release(reader);
			}
		} finally {
			rwl.r.unlock();
		}
	}

	@Override
	final public TermDocs getTermDocs(final Term term) throws SearchLibException, IOException {
		rwl.r.lock();
		try {
			checkOnline(true);
			ReaderLocal reader = acquire();
			try {
				return reader.getTermDocs(term);
			} finally {
				release(reader);
			}
		} finally {
			rwl.r.unlock();
		}
	}

	@Override
	final public TermPositions getTermPositions() throws IOException, SearchLibException {
		rwl.r.lock();
		try {
			checkOnline(true);
			ReaderLocal reader = acquire();
			try {
				return reader.getTermPositions();
			} finally {
				release(reader);
			}
		} finally {
			rwl.r.unlock();
		}
	}

	@Override
	final public TermFreqVector getTermFreqVector(final int docId, final String field)
			throws IOException, SearchLibException {
		rwl.r.lock();
		try {
			checkOnline(true);
			ReaderLocal reader = acquire();
			try {
				return reader.getTermFreqVector(docId, field);
			} finally {
				release(reader);
			}
		} finally {
			rwl.r.unlock();
		}
	}

	@Override
	final public void putTermVectors(final int[] docIds, final String field, final Collection<String[]> termVectors)
			throws IOException, SearchLibException {
		rwl.r.lock();
		try {
			checkOnline(true);
			ReaderLocal reader = acquire();
			try {
				reader.putTermVectors(docIds, field, termVectors);
			} finally {
				release(reader);
			}
		} finally {
			rwl.r.unlock();
		}
	}

	@Override
	final public FieldCacheIndex getStringIndex(final String fieldName) throws IOException, SearchLibException {
		rwl.r.lock();
		try {
			checkOnline(true);
			ReaderLocal reader = acquire();
			try {
				return reader.getStringIndex(fieldName);
			} finally {
				release(reader);
			}
		} finally {
			rwl.r.unlock();
		}

	}

	@Override
	public FilterHits getFilterHits(SchemaField defaultField, PerFieldAnalyzer analyzer,
			AbstractLocalSearchRequest request, FilterAbstract<?> filter, Timer timer)
					throws ParseException, IOException, SearchLibException, SyntaxError {
		rwl.r.lock();
		try {
			checkOnline(true);
			ReaderLocal reader = acquire();
			try {
				return reader.getFilterHits(defaultField, analyzer, request, filter, timer);
			} finally {
				release(reader);
			}
		} finally {
			rwl.r.unlock();
		}
	}

	@Override
	public DocSetHits searchDocSet(AbstractLocalSearchRequest searchRequest, Timer timer)
			throws IOException, ParseException, SyntaxError, SearchLibException {
		rwl.r.lock();
		try {
			checkOnline(true);
			ReaderLocal reader = acquire();
			try {
				return reader.searchDocSet(searchRequest, timer);
			} finally {
				release(reader);
			}
		} finally {
			rwl.r.unlock();
		}
	}

	@Override
	final public boolean isOnline() {
		rwl.r.lock();
		try {
			return online;
		} finally {
			rwl.r.unlock();
		}
	}

	@Override
	public void setOnline(boolean v) throws SearchLibException {
		rwl.r.lock();
		try {
			if (v == online)
				return;
		} finally {
			rwl.r.unlock();
		}
		rwl.w.lock();
		try {
			if (v == online)
				return;
			online = v;
			if (v)
				reload();
			else {
				IOUtils.close(_reader);
				_reader = null;
			}
		} finally {
			rwl.w.unlock();
		}
	}

	@Override
	public long getVersion() throws SearchLibException {
		rwl.r.lock();
		try {
			checkOnline(true);
			ReaderLocal reader = acquire();
			try {
				return reader.getVersion();
			} finally {
				release(reader);
			}
		} finally {
			rwl.r.unlock();
		}
	}

	public DocSetHitsCache getSearchCache() throws SearchLibException {
		rwl.r.lock();
		try {
			checkOnline(true);
			ReaderLocal reader = acquire();
			try {
				return reader.getDocSetHitsCache();
			} finally {
				release(reader);
			}
		} finally {
			rwl.r.unlock();
		}
	}

	@Override
	public String[] getDocTerms(String field) throws SearchLibException, IOException {
		rwl.r.lock();
		try {
			checkOnline(true);
			ReaderLocal reader = acquire();
			try {
				return reader.getDocTerms(field);
			} finally {
				release(reader);
			}
		} finally {
			rwl.r.unlock();
		}
	}

	@Override
	protected void writeXmlConfigIndex(XmlWriter xmlWriter) throws SAXException {
		indexConfig.writeXmlConfig(xmlWriter);
	}

	@Override
	public Collection<?> getFieldNames() throws SearchLibException {
		rwl.r.lock();
		try {
			checkOnline(true);
			ReaderLocal reader = acquire();
			try {
				return reader.getFieldNames();
			} finally {
				release(reader);
			}
		} finally {
			rwl.r.unlock();
		}
	}

	@Override
	final public LinkedHashMap<String, FieldValue> getDocumentFields(final int docId,
			final LinkedHashSet<String> fieldNameSet, final Timer timer)
					throws IOException, ParseException, SyntaxError, SearchLibException {
		rwl.r.lock();
		try {
			checkOnline(true);
			ReaderLocal reader = acquire();
			try {
				return reader.getDocumentFields(docId, fieldNameSet, timer);
			} finally {
				release(reader);
			}
		} finally {
			rwl.r.unlock();
		}
	}

	@Override
	public LinkedHashMap<String, FieldValue> getDocumentStoredField(final int docId)
			throws IOException, SearchLibException {
		rwl.r.lock();
		try {
			checkOnline(true);
			ReaderLocal reader = acquire();
			try {
				return reader.getDocumentStoredField(docId);
			} finally {
				release(reader);
			}
		} finally {
			rwl.r.unlock();
		}
	}

	@Override
	public Query rewrite(Query query) throws SearchLibException {
		rwl.r.lock();
		try {
			checkOnline(true);
			ReaderLocal reader = acquire();
			try {
				return reader.rewrite(query);
			} finally {
				release(reader);
			}
		} finally {
			rwl.r.unlock();
		}
	}

	@Override
	public MoreLikeThis getMoreLikeThis() throws SearchLibException {
		rwl.r.lock();
		try {
			checkOnline(true);
			ReaderLocal reader = acquire();
			try {
				return reader.getMoreLikeThis();
			} finally {
				release(reader);
			}
		} finally {
			rwl.r.unlock();
		}
	}

	@Override
	public void mergeData(WriterInterface source) throws SearchLibException {
		if (!(source instanceof IndexSingle))
			throw new SearchLibException("Unsupported operation");
		IndexSingle sourceIndex = (IndexSingle) source;
		rwl.w.lock();
		try {
			if (writer == null)
				return;
			sourceIndex.rwl.r.lock();
			try {
				writer.mergeData(sourceIndex.writer);
				reloadNoLock();
			} finally {
				sourceIndex.rwl.r.unlock();
			}
		} finally {
			rwl.w.unlock();
		}
	}

	@Override
	public boolean isMerging() {
		rwl.r.lock();
		try {
			return writer != null ? writer.isMerging() : false;
		} finally {
			rwl.r.unlock();
		}
	}

	private void closeCloseables() {
		synchronized (closeableReaders) {
			if (closeableReaders.isEmpty())
				return;
			final ArrayList<ReaderLocal> list = new ArrayList<ReaderLocal>();
			for (ReaderLocal readerLocal : closeableReaders)
				if (readerLocal.references.get() == 0)
					list.add(readerLocal);
			for (ReaderLocal readerLocal : list) {
				closeableReaders.remove(readerLocal);
				IOUtils.close(readerLocal);
			}
		}
	}

}
