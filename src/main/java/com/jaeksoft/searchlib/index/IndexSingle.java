/**
 * License Agreement for OpenSearchServer
 * <p/>
 * Copyright (C) 2008-2015 Emmanuel Keller / Jaeksoft
 * <p/>
 * http://www.open-search-server.com
 * <p/>
 * This file is part of OpenSearchServer.
 * <p/>
 * OpenSearchServer is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p/>
 * OpenSearchServer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU General Public License
 * along with OpenSearchServer.
 * If not, see <http://www.gnu.org/licenses/>.
 **/

package com.jaeksoft.searchlib.index;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.ClientCatalog;
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
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.lucene.index.*;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.similar.MoreLikeThis;
import org.json.JSONException;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

public class IndexSingle extends IndexAbstract {

	final private IndexDirectory indexDirectory;

	private volatile ReaderLocal _reader;
	private final WriterLocal writer;

	private volatile boolean online;

	private final Set<UpdateInterfaces.Before> beforeUpdateSet = new HashSet<>();
	private final Set<UpdateInterfaces.After> afterUpdateSet = new HashSet<>();
	private final Set<UpdateInterfaces.Delete> afterDeleteSet = new HashSet<>();
	private final Set<UpdateInterfaces.Reload> afterReloadSet = new HashSet<>();
	private volatile UpdateInterfaces.Before[] _beforeUpdateArray = null;
	private volatile UpdateInterfaces.After[] _afterUpdateArray = null;
	private volatile UpdateInterfaces.Delete[] _afterDeleteArray = null;
	private volatile UpdateInterfaces.Reload[] _afterReloadArray = null;

	private final List<IndexAbstract> reloadIndexList;
	private final UpdateInterfaces.Reload reloadUpdateInterface = new UpdateInterfaces.Reload() {
		@Override
		public void reload() throws SearchLibException {
			IndexSingle.this.reload();
		}
	};

	public IndexSingle(File configDir, IndexConfig indexConfig, boolean createIfNotExists)
			throws IOException, URISyntaxException, SearchLibException, JSONException {
		super(indexConfig);
		this.online = true;
		boolean bCreate = false;
		File indexDir = new File(configDir, "index");
		if (!indexDir.exists()) {
			if (!createIfNotExists) {
				indexDirectory = null;
				_reader = null;
				writer = null;
				reloadIndexList = null;
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
			reloadIndexList = null;
		} else {
			writer = null;
			reloadIndexList = new ArrayList<>();
		}
		_reader = new ReaderLocal(indexConfig, indexDirectory);
		eventUpdateInterface();
	}

	private void emptyReloadEvents() {
		for (IndexAbstract index : reloadIndexList)
			index.removeUpdateInterface(reloadUpdateInterface);
		reloadIndexList.clear();
	}

	private void subscribeReloadEvents() throws SearchLibException {
		for (String indexName : indexConfig.getIndexList()) {
			Client client = ClientCatalog.getClient(indexName);
			if (client == null)
				continue;
			IndexAbstract index = client.getIndex();
			reloadIndexList.add(index);
			index.addUpdateInterface(reloadUpdateInterface);
		}
	}

	private void eventUpdateInterface() throws SearchLibException {
		if (reloadIndexList == null)
			return;
		synchronized (reloadIndexList) {
			emptyReloadEvents();
			subscribeReloadEvents();
		}
	}

	private void generateUpdateInterfaceArrays() {
		synchronized (beforeUpdateSet) {
			_beforeUpdateArray = beforeUpdateSet.isEmpty() ?
					null :
					beforeUpdateSet.toArray(new UpdateInterfaces.Before[beforeUpdateSet.size()]);
		}
		synchronized (afterUpdateSet) {
			_afterUpdateArray = afterUpdateSet.isEmpty() ?
					null :
					afterUpdateSet.toArray(new UpdateInterfaces.After[afterUpdateSet.size()]);
		}
		synchronized (afterDeleteSet) {
			_afterDeleteArray = afterDeleteSet.isEmpty() ?
					null :
					afterDeleteSet.toArray(new UpdateInterfaces.Delete[afterDeleteSet.size()]);
		}
		synchronized (afterReloadSet) {
			_afterReloadArray = afterReloadSet.isEmpty() ?
					null :
					afterReloadSet.toArray(new UpdateInterfaces.Reload[afterReloadSet.size()]);
		}
	}

	@Override
	public void addUpdateInterface(UpdateInterfaces updateInterface) {
		if (updateInterface == null)
			return;
		if (updateInterface instanceof UpdateInterfaces.Before) {
			synchronized (beforeUpdateSet) {
				beforeUpdateSet.add((UpdateInterfaces.Before) updateInterface);
			}
		}
		if (updateInterface instanceof UpdateInterfaces.After) {
			synchronized (afterUpdateSet) {
				afterUpdateSet.add((UpdateInterfaces.After) updateInterface);
			}
		}
		if (updateInterface instanceof UpdateInterfaces.Delete) {
			synchronized (afterDeleteSet) {
				afterDeleteSet.add((UpdateInterfaces.Delete) updateInterface);
			}
		}
		if (updateInterface instanceof UpdateInterfaces.Reload) {
			synchronized (afterReloadSet) {
				afterReloadSet.add((UpdateInterfaces.Reload) updateInterface);
			}
		}
		generateUpdateInterfaceArrays();
	}

	@Override
	public void removeUpdateInterface(UpdateInterfaces updateInterface) {
		if (updateInterface == null)
			return;
		if (updateInterface instanceof UpdateInterfaces.Before) {
			synchronized (beforeUpdateSet) {
				beforeUpdateSet.remove((UpdateInterfaces.Before) updateInterface);
			}
		}
		if (updateInterface instanceof UpdateInterfaces.After) {
			synchronized (afterUpdateSet) {
				afterUpdateSet.remove((UpdateInterfaces.After) updateInterface);
			}
		}
		if (updateInterface instanceof UpdateInterfaces.Delete) {
			synchronized (afterDeleteSet) {
				afterDeleteSet.remove((UpdateInterfaces.Delete) updateInterface);
			}
		}
		if (updateInterface instanceof UpdateInterfaces.Reload) {
			synchronized (afterReloadSet) {
				afterReloadSet.remove((UpdateInterfaces.Reload) updateInterface);
			}
		}
		generateUpdateInterfaceArrays();
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
		if (reloadIndexList != null) {
			synchronized (reloadIndexList) {
				emptyReloadEvents();
			}
		}
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
			int res = writer.deleteDocuments(ids);
			reloadNoLock();
			return res;
		} catch (IOException | ParseException | SyntaxError e) {
			throw new SearchLibException(e);
		}
	}

	private void beforeUpdate(Schema schema, IndexDocument document) throws SearchLibException {
		UpdateInterfaces.Before[] array = _beforeUpdateArray;
		if (array == null)
			return;
		for (UpdateInterfaces.Before beforeUpdate : array)
			beforeUpdate.update(schema, document);
	}

	private void afterUpdate(IndexDocument document) throws SearchLibException {
		UpdateInterfaces.After[] array = _afterUpdateArray;
		if (array == null)
			return;
		for (UpdateInterfaces.After afterUpdate : array)
			afterUpdate.update(document);
	}

	private void afterReload() throws SearchLibException {
		UpdateInterfaces.Reload[] array = _afterReloadArray;
		if (array == null)
			return;
		for (UpdateInterfaces.Reload afterReload : array)
			afterReload.reload();
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

	private synchronized void reloadNoLock() throws SearchLibException {
		ReaderLocal oldReader = _reader;
		try {
			_reader = new ReaderLocal(indexConfig, indexDirectory);
		} catch (IOException e) {
			throw new SearchLibException(e);
		}
		if (oldReader != null)
			IOUtils.closeQuietly(oldReader);
		afterReload();
	}

	@Override
	public void reload() throws SearchLibException {
		checkOnline(true);
		eventUpdateInterface();
		ReaderLocal reader = acquire();
		try {
			reloadNoLock();
		} finally {
			release(reader);
		}
	}

	private synchronized ReaderLocal acquire() {
		ReaderLocal reader = _reader;
		reader.acquire();
		return reader;
	}

	private synchronized void release(ReaderLocal reader) {
		reader.release();
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

	@Override
	public void mergeData(WriterInterface source) throws SearchLibException {
		if (!(source instanceof IndexSingle))
			throw new SearchLibException("Unsupported operation");
		if (writer == null)
			return;
		final IndexSingle sourceIndex = (IndexSingle) source;
		ReaderLocal reader = sourceIndex.acquire();
		try {
			writer.mergeData(sourceIndex.writer);
			reloadNoLock();
		} finally {
			release(reader);
		}
	}

	@Override
	public boolean isMerging() {
		return writer != null && writer.isMerging();
	}

}
