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
import java.util.List;
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
import com.jaeksoft.searchlib.request.DocumentsRequest;
import com.jaeksoft.searchlib.result.AbstractResult;
import com.jaeksoft.searchlib.result.ResultDocuments;
import com.jaeksoft.searchlib.schema.FieldValue;
import com.jaeksoft.searchlib.schema.Schema;
import com.jaeksoft.searchlib.schema.SchemaField;
import com.jaeksoft.searchlib.util.IOUtils;
import com.jaeksoft.searchlib.util.Timer;
import com.jaeksoft.searchlib.util.XmlWriter;
import com.jaeksoft.searchlib.webservice.query.document.IndexDocumentResult;

public class IndexSingle extends IndexAbstract {

	final private IndexDirectory indexDirectory;

	private volatile ReaderLocal _reader;
	private final WriterLocal writer;

	private volatile boolean online;

	protected List<UpdateInterfaces.Before> beforeUpdateList = null;
	protected List<UpdateInterfaces.After> afterUpdateList = null;
	protected List<UpdateInterfaces.Delete> afterDeleteList = null;

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
			writer = new WriterLocal(indexConfig, indexDirectory);
			if (bCreate)
				((WriterLocal) writer).create();
		} else {
			writer = null;
		}
		_reader = new ReaderLocal(indexConfig, indexDirectory);
	}

	@Override
	public synchronized void addUpdateInterface(UpdateInterfaces updateInterface) {
		if (updateInterface == null)
			return;
		if (updateInterface instanceof UpdateInterfaces.Before) {
			if (beforeUpdateList == null)
				beforeUpdateList = new ArrayList<UpdateInterfaces.Before>(1);
			beforeUpdateList.add((UpdateInterfaces.Before) updateInterface);
		}
		if (updateInterface instanceof UpdateInterfaces.After) {
			if (afterUpdateList == null)
				afterUpdateList = new ArrayList<UpdateInterfaces.After>(1);
			afterUpdateList.add((UpdateInterfaces.After) updateInterface);
		}
		if (updateInterface instanceof UpdateInterfaces.Delete) {
			if (afterDeleteList == null)
				afterDeleteList = new ArrayList<UpdateInterfaces.Delete>(1);
			afterDeleteList.add((UpdateInterfaces.Delete) updateInterface);
		}
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
		if (_reader != null)
			IOUtils.close(_reader);
		_reader = null;
		indexDirectory.close();
	}

	private void checkOnline(boolean online) throws SearchLibException {
		if (this.online != online)
			throw new SearchLibException("Index is offline");
	}

	@Override
	public void deleteAll() throws SearchLibException {
		checkOnline(true);
		if (writer == null)
			return;
		writer.deleteAll();
		reloadNoLock();
	}

	private int[] getIds(AbstractRequest request) throws IOException, ParseException, SyntaxError, SearchLibException {
		ReaderLocal reader = acquire();
		try {
			if (request instanceof AbstractLocalSearchRequest) {
				DocSetHits dsh = reader.searchDocSet((AbstractLocalSearchRequest) request, null);
				if (dsh != null)
					return dsh.getIds();
			} else if (request instanceof DocumentsRequest) {
				ResultDocuments result = (ResultDocuments) reader.request(request);
				if (result != null)
					return result.getDocIdArray();
			}
			return null;
		} finally {
			release(reader);
		}
	}

	@Override
	public int deleteDocuments(AbstractRequest request) throws SearchLibException {
		try {
			checkOnline(true);
			if (writer == null)
				return 0;
			int[] ids = getIds(request);
			if (ids == null || ids.length == 0)
				return 0;
			int res = writer.deleteDocuments(request);
			reloadNoLock();
			return res;
		} catch (IOException | ParseException | SyntaxError e) {
			throw new SearchLibException(e);
		}
	}

	private void beforeUpdate(Schema schema, IndexDocument document) throws SearchLibException {
		if (beforeUpdateList == null)
			return;
		for (UpdateInterfaces.Before beforeUpdate : beforeUpdateList)
			beforeUpdate.update(schema, document);
	}

	private void afterUpdate(IndexDocument document) throws SearchLibException {
		if (afterUpdateList == null)
			return;
		for (UpdateInterfaces.After afterUpdate : afterUpdateList)
			afterUpdate.update(document);
	}

	@Override
	public boolean updateDocument(Schema schema, IndexDocument document) throws SearchLibException {
		checkOnline(true);
		if (writer == null)
			return false;
		beforeUpdate(schema, document);
		if (!writer.updateDocument(schema, document))
			return false;
		reloadNoLock();
		afterUpdate(document);
		return true;
	}

	@Override
	public int updateDocuments(Schema schema, Collection<IndexDocument> documents) throws SearchLibException {
		checkOnline(true);
		if (writer == null)
			return 0;
		for (IndexDocument document : documents)
			beforeUpdate(schema, document);
		int res = writer.updateDocuments(schema, documents);
		reloadNoLock();
		for (IndexDocument document : documents)
			afterUpdate(document);
		return res;
	}

	@Override
	public int updateIndexDocuments(Schema schema, Collection<IndexDocumentResult> documents)
			throws SearchLibException {
		checkOnline(true);
		if (writer == null)
			return 0;
		int res = writer.updateIndexDocuments(schema, documents);
		reloadNoLock();
		return res;
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
		checkOnline(true);
		ReaderLocal reader = acquire();
		try {
			reloadNoLock();
		} finally {
			release(reader);
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
		checkOnline(true);
		ReaderLocal reader = acquire();
		try {
			return reader.request(request);
		} finally {
			release(reader);
		}
	}

	@Override
	public String explain(AbstractRequest request, int docId, boolean bHtml) throws SearchLibException {
		checkOnline(true);
		ReaderLocal reader = acquire();
		try {
			return reader.explain(request, docId, bHtml);
		} finally {
			release(reader);
		}
	}

	@Override
	public boolean sameIndex(ReaderInterface reader) {
		if (reader == this)
			return true;
		if (reader == this._reader)
			return true;
		return false;
	}

	@Override
	public IndexStatistics getStatistics() throws IOException, SearchLibException {
		checkOnline(true);
		ReaderLocal reader = acquire();
		try {
			return reader.getStatistics();
		} finally {
			release(reader);
		}
	}

	@Override
	public IndexSingle get(String name) {
		return this;
	}

	@Override
	final public int getDocFreq(final Term term) throws SearchLibException {
		checkOnline(true);
		ReaderLocal reader = acquire();
		try {
			return reader.getDocFreq(term);
		} finally {
			release(reader);
		}
	}

	@Override
	final public TermEnum getTermEnum() throws SearchLibException {
		checkOnline(true);
		ReaderLocal reader = acquire();
		try {
			return reader.getTermEnum();
		} finally {
			release(reader);
		}
	}

	@Override
	final public TermEnum getTermEnum(final Term term) throws SearchLibException {
		checkOnline(true);
		ReaderLocal reader = acquire();
		try {
			return reader.getTermEnum(term);
		} finally {
			release(reader);
		}
	}

	@Override
	final public TermDocs getTermDocs(final Term term) throws SearchLibException, IOException {
		checkOnline(true);
		ReaderLocal reader = acquire();
		try {
			return reader.getTermDocs(term);
		} finally {
			release(reader);
		}
	}

	@Override
	final public TermPositions getTermPositions() throws IOException, SearchLibException {
		checkOnline(true);
		ReaderLocal reader = acquire();
		try {
			return reader.getTermPositions();
		} finally {
			release(reader);
		}
	}

	@Override
	final public TermFreqVector getTermFreqVector(final int docId, final String field)
			throws IOException, SearchLibException {
		checkOnline(true);
		ReaderLocal reader = acquire();
		try {
			return reader.getTermFreqVector(docId, field);
		} finally {
			release(reader);
		}
	}

	@Override
	final public void putTermVectors(final int[] docIds, final String field, final Collection<String[]> termVectors)
			throws IOException, SearchLibException {
		checkOnline(true);
		ReaderLocal reader = acquire();
		try {
			reader.putTermVectors(docIds, field, termVectors);
		} finally {
			release(reader);
		}
	}

	@Override
	final public FieldCacheIndex getStringIndex(final String fieldName) throws IOException, SearchLibException {
		checkOnline(true);
		ReaderLocal reader = acquire();
		try {
			return reader.getStringIndex(fieldName);
		} finally {
			release(reader);
		}
	}

	@Override
	public FilterHits getFilterHits(SchemaField defaultField, PerFieldAnalyzer analyzer,
			AbstractLocalSearchRequest request, FilterAbstract<?> filter, Timer timer)
					throws ParseException, IOException, SearchLibException, SyntaxError {
		checkOnline(true);
		ReaderLocal reader = acquire();
		try {
			return reader.getFilterHits(defaultField, analyzer, request, filter, timer);
		} finally {
			release(reader);
		}
	}

	@Override
	public DocSetHits searchDocSet(AbstractLocalSearchRequest searchRequest, Timer timer)
			throws IOException, ParseException, SyntaxError, SearchLibException {
		checkOnline(true);
		ReaderLocal reader = acquire();
		try {
			return reader.searchDocSet(searchRequest, timer);
		} finally {
			release(reader);
		}
	}

	@Override
	final public boolean isOnline() {
		return online;
	}

	@Override
	public synchronized void setOnline(boolean v) throws SearchLibException {
		if (v == online)
			return;
		online = v;
		if (v)
			reloadNoLock();
		else {
			IOUtils.close(_reader);
			_reader = null;
		}
	}

	@Override
	public long getVersion() throws SearchLibException {
		checkOnline(true);
		ReaderLocal reader = acquire();
		try {
			return reader.getVersion();
		} finally {
			release(reader);
		}
	}

	public DocSetHitsCache getSearchCache() throws SearchLibException {
		checkOnline(true);
		ReaderLocal reader = acquire();
		try {
			return reader.getDocSetHitsCache();
		} finally {
			release(reader);
		}
	}

	@Override
	public String[] getDocTerms(String field) throws SearchLibException, IOException {
		checkOnline(true);
		ReaderLocal reader = acquire();
		try {
			return reader.getDocTerms(field);
		} finally {
			release(reader);
		}
	}

	@Override
	protected void writeXmlConfigIndex(XmlWriter xmlWriter) throws SAXException {
		indexConfig.writeXmlConfig(xmlWriter);
	}

	@Override
	public Collection<?> getFieldNames() throws SearchLibException {
		checkOnline(true);
		ReaderLocal reader = acquire();
		try {
			return reader.getFieldNames();
		} finally {
			release(reader);
		}
	}

	@Override
	final public LinkedHashMap<String, FieldValue> getDocumentFields(final int docId,
			final LinkedHashSet<String> fieldNameSet, final Timer timer)
					throws IOException, ParseException, SyntaxError, SearchLibException {
		checkOnline(true);
		ReaderLocal reader = acquire();
		try {
			return reader.getDocumentFields(docId, fieldNameSet, timer);
		} finally {
			release(reader);
		}
	}

	@Override
	public LinkedHashMap<String, FieldValue> getDocumentStoredField(final int docId)
			throws IOException, SearchLibException {
		checkOnline(true);
		ReaderLocal reader = acquire();
		try {
			return reader.getDocumentStoredField(docId);
		} finally {
			release(reader);
		}
	}

	@Override
	public Query rewrite(Query query) throws SearchLibException {
		checkOnline(true);
		ReaderLocal reader = acquire();
		try {
			return reader.rewrite(query);
		} finally {
			release(reader);
		}
	}

	@Override
	public MoreLikeThis getMoreLikeThis() throws SearchLibException {
		checkOnline(true);
		ReaderLocal reader = acquire();
		try {
			return reader.getMoreLikeThis();
		} finally {
			release(reader);
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
