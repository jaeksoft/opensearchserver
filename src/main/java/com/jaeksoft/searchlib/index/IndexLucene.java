/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008-2014 Emmanuel Keller / Jaeksoft
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
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.index.TermFreqVector;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.similar.MoreLikeThis;
import org.json.JSONException;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.analysis.PerFieldAnalyzer;
import com.jaeksoft.searchlib.cache.FieldCache;
import com.jaeksoft.searchlib.cache.FilterCache;
import com.jaeksoft.searchlib.cache.SearchCache;
import com.jaeksoft.searchlib.cache.TermVectorCache;
import com.jaeksoft.searchlib.cluster.ClusterManager;
import com.jaeksoft.searchlib.cluster.ClusterNotification;
import com.jaeksoft.searchlib.cluster.ClusterNotification.Type;
import com.jaeksoft.searchlib.cluster.VersionFile;
import com.jaeksoft.searchlib.filter.FilterAbstract;
import com.jaeksoft.searchlib.filter.FilterHits;
import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.query.ParseException;
import com.jaeksoft.searchlib.request.AbstractRequest;
import com.jaeksoft.searchlib.request.AbstractSearchRequest;
import com.jaeksoft.searchlib.result.AbstractResult;
import com.jaeksoft.searchlib.schema.FieldValue;
import com.jaeksoft.searchlib.schema.Schema;
import com.jaeksoft.searchlib.schema.SchemaField;
import com.jaeksoft.searchlib.util.ReadWriteLock;
import com.jaeksoft.searchlib.util.Timer;
import com.jaeksoft.searchlib.util.XmlWriter;

public class IndexLucene extends IndexAbstract {

	final private ReadWriteLock rwl = new ReadWriteLock();

	final private IndexDirectory indexDirectory;

	final private File configDir;

	private final ReaderInterface reader;
	private final WriterInterface writer;

	private final VersionFile versionFile;

	public IndexLucene(File configDir, IndexConfig indexConfig,
			boolean createIfNotExists) throws IOException, URISyntaxException,
			SearchLibException, JSONException {
		super(indexConfig);
		this.configDir = configDir;
		boolean bCreate = false;
		File indexDir = new File(configDir, "index");
		if (!indexDir.exists()) {
			if (!createIfNotExists) {
				indexDirectory = null;
				reader = null;
				writer = null;
				versionFile = null;
				return;
			}
			indexDir.mkdir();
			bCreate = true;
		} else
			indexDir = findIndexDirOrSub(indexDir);
		versionFile = new VersionFile(indexDir);
		URI remoteURI = indexConfig.getRemoteURI();
		indexDirectory = remoteURI == null ? new IndexDirectory(indexDir)
				: new IndexDirectory(remoteURI);
		bCreate = bCreate || indexDirectory.isEmpty();
		writer = new WriterLocal(indexConfig, this, indexDirectory);
		if (bCreate) {
			versionFile.lock();
			try {
				((WriterLocal) writer).create();
				sendNotifReloadData();
			} finally {
				versionFile.release();
			}
		}
		reader = new ReaderLocal(indexConfig, indexDirectory, true);
	}

	protected void sendNotifReloadData() {
		ClusterManager.notify(new ClusterNotification(Type.RELOAD_DATA,
				configDir));
	}

	/**
	 * Check if there is old style index sub directory
	 * 
	 * @param indexDir
	 * @return
	 */
	private File findIndexDirOrSub(File indexDir) {
		File[] dirs = indexDir
				.listFiles((FileFilter) DirectoryFileFilter.INSTANCE);
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
			if (reader != null)
				reader.close();
			indexDirectory.close();
		} finally {
			rwl.w.unlock();
		}
	}

	@Override
	public void optimize() throws SearchLibException, IOException {
		versionFile.lock();
		try {
			rwl.r.lock();
			try {
				if (writer == null)
					return;
				writer.optimize();
				if (reader != null)
					reader.reload();
				sendNotifReloadData();
			} finally {
				rwl.r.unlock();
			}
		} finally {
			versionFile.release();
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
	public int deleteDocument(Schema schema, String field, String value)
			throws SearchLibException, IOException {
		versionFile.lock();
		try {
			rwl.r.lock();
			try {
				int d = 0;
				if (writer != null) {
					d = writer.deleteDocument(schema, field, value);
					sendNotifReloadData();
				}
				return d;
			} finally {
				rwl.r.unlock();
			}
		} finally {
			versionFile.release();
		}
	}

	@Override
	public int deleteDocuments(Schema schema, String field,
			Collection<String> values) throws SearchLibException, IOException {
		versionFile.lock();
		try {
			rwl.r.lock();
			try {
				int d = 0;
				if (writer != null) {
					d = writer.deleteDocuments(schema, field, values);
					sendNotifReloadData();
				}
				return d;
			} finally {
				rwl.r.unlock();
			}
		} finally {
			versionFile.release();
		}
	}

	@Override
	public void deleteAll() throws SearchLibException, IOException {
		versionFile.lock();
		try {
			rwl.r.lock();
			try {
				if (writer != null) {
					writer.deleteAll();
					sendNotifReloadData();
				}
			} finally {
				rwl.r.unlock();
			}
		} finally {
			versionFile.release();
		}
	}

	@Override
	public int deleteDocuments(AbstractSearchRequest query)
			throws SearchLibException, IOException {
		versionFile.lock();
		try {
			rwl.r.lock();
			try {
				int d = writer.deleteDocuments(query);
				sendNotifReloadData();
				return d;
			} finally {
				rwl.r.unlock();
			}
		} finally {
			versionFile.release();
		}
	}

	@Override
	public void addUpdateInterface(UpdateInterfaces updateInterface)
			throws SearchLibException {
		rwl.r.lock();
		try {
			if (writer != null)
				writer.addUpdateInterface(updateInterface);
		} finally {
			rwl.r.unlock();
		}
	}

	@Override
	public boolean updateDocument(Schema schema, IndexDocument document)
			throws SearchLibException, IOException {
		versionFile.lock();
		try {
			rwl.r.lock();
			try {
				boolean b = false;
				if (writer != null) {
					b = writer.updateDocument(schema, document);
					sendNotifReloadData();
				}
				return b;
			} finally {
				rwl.r.unlock();
			}
		} finally {
			versionFile.release();
		}
	}

	@Override
	public int updateDocuments(Schema schema,
			Collection<IndexDocument> documents) throws SearchLibException,
			IOException {
		versionFile.lock();
		try {
			rwl.r.lock();
			try {
				int d = 0;
				if (writer != null) {
					d = writer.updateDocuments(schema, documents);
					sendNotifReloadData();
				}
				return d;
			} finally {
				rwl.r.unlock();
			}
		} finally {
			versionFile.release();
		}
	}

	@Override
	public void reload() throws SearchLibException, IOException {
		versionFile.sharedLock();
		try {
			rwl.r.lock();
			try {
				reloadNoLock();
			} finally {
				rwl.r.unlock();
			}
		} finally {
			versionFile.release();
		}
	}

	final void reloadNoLock() throws SearchLibException, IOException {
		reader.reload();
	}

	@Override
	public AbstractResult<?> request(AbstractRequest request)
			throws SearchLibException, IOException {
		versionFile.sharedLock();
		try {
			rwl.r.lock();
			try {
				if (reader != null)
					return reader.request(request);
				return null;
			} finally {
				rwl.r.unlock();
			}
		} finally {
			versionFile.release();
		}
	}

	@Override
	public String explain(AbstractRequest request, int docId, boolean bHtml)
			throws SearchLibException, IOException {
		versionFile.sharedLock();
		try {
			rwl.r.lock();
			try {
				if (reader != null)
					return reader.explain(request, docId, bHtml);
				return null;
			} finally {
				rwl.r.unlock();
			}
		} finally {
			versionFile.release();
		}
	}

	@Override
	public boolean sameIndex(ReaderInterface reader) {
		rwl.r.lock();
		try {
			if (reader == this)
				return true;
			if (reader == this.reader)
				return true;
			return false;
		} finally {
			rwl.r.unlock();
		}
	}

	@Override
	public IndexStatistics getStatistics() throws IOException,
			SearchLibException {
		rwl.r.lock();
		try {
			if (reader != null)
				return reader.getStatistics();
			return null;
		} finally {
			rwl.r.unlock();
		}
	}

	@Override
	public IndexLucene get(String name) {
		return this;
	}

	@Override
	final public int getDocFreq(final Term term) throws SearchLibException,
			IOException {
		versionFile.sharedLock();
		try {
			rwl.r.lock();
			try {
				if (reader != null)
					return reader.getDocFreq(term);
				return 0;
			} finally {
				rwl.r.unlock();
			}
		} finally {
			versionFile.release();
		}
	}

	@Override
	final public TermEnum getTermEnum() throws SearchLibException, IOException {
		versionFile.sharedLock();
		try {
			rwl.r.lock();
			try {
				if (reader != null)
					return reader.getTermEnum();
				return null;
			} finally {
				rwl.r.unlock();
			}
		} finally {
			versionFile.release();
		}
	}

	@Override
	final public TermEnum getTermEnum(final Term term)
			throws SearchLibException, IOException {
		versionFile.sharedLock();
		try {
			rwl.r.lock();
			try {
				if (reader != null)
					return reader.getTermEnum(term);
				return null;
			} finally {
				rwl.r.unlock();
			}
		} finally {
			versionFile.release();
		}
	}

	@Override
	final public TermDocs getTermDocs(final Term term)
			throws SearchLibException, IOException {
		versionFile.sharedLock();
		try {
			rwl.r.lock();
			try {
				if (reader != null)
					return reader.getTermDocs(term);
				return null;
			} finally {
				rwl.r.unlock();
			}
		} finally {
			versionFile.release();
		}
	}

	@Override
	final public TermFreqVector getTermFreqVector(final int docId,
			final String field) throws IOException, SearchLibException {
		versionFile.sharedLock();
		try {
			rwl.r.lock();
			try {
				if (reader != null)
					return reader.getTermFreqVector(docId, field);
				return null;
			} finally {
				rwl.r.unlock();
			}
		} finally {
			versionFile.release();
		}
	}

	@Override
	final public void putTermVectors(final int[] docIds, final String field,
			final Collection<String[]> termVectors) throws IOException,
			SearchLibException {
		versionFile.sharedLock();
		try {
			rwl.r.lock();
			try {
				if (reader != null)
					reader.putTermVectors(docIds, field, termVectors);
			} finally {
				rwl.r.unlock();
			}
		} finally {
			versionFile.release();
		}
	}

	@Override
	final public FieldCacheIndex getStringIndex(final String fieldName)
			throws IOException, SearchLibException {
		versionFile.sharedLock();
		try {
			rwl.r.lock();
			try {
				if (reader != null)
					return reader.getStringIndex(fieldName);
				return null;
			} finally {
				rwl.r.unlock();
			}
		} finally {
			versionFile.release();
		}
	}

	@Override
	public FilterHits getFilterHits(SchemaField defaultField,
			PerFieldAnalyzer analyzer, AbstractSearchRequest request,
			FilterAbstract<?> filter, Timer timer) throws ParseException,
			IOException, SearchLibException {
		versionFile.sharedLock();
		try {
			rwl.r.lock();
			try {
				if (reader != null)
					return reader.getFilterHits(defaultField, analyzer,
							request, filter, timer);
				return null;
			} finally {
				rwl.r.unlock();
			}
		} finally {
			versionFile.release();
		}
	}

	@Override
	public long getVersion() throws SearchLibException, IOException {
		versionFile.sharedLock();
		try {
			rwl.r.lock();
			try {
				if (reader == null)
					return 0;
				return reader.getVersion();
			} finally {
				rwl.r.unlock();
			}
		} finally {
			versionFile.release();
		}
	}

	public SearchCache getSearchCache() throws SearchLibException {
		rwl.r.lock();
		try {
			if (reader != null)
				if (reader instanceof ReaderLocal)
					return ((ReaderLocal) reader).getSearchCache();
			return null;
		} finally {
			rwl.r.unlock();
		}
	}

	public FilterCache getFilterCache() throws SearchLibException {
		rwl.r.lock();
		try {
			if (reader != null)
				if (reader instanceof ReaderLocal)
					return ((ReaderLocal) reader).getFilterCache();
			return null;
		} finally {
			rwl.r.unlock();
		}
	}

	public FieldCache getFieldCache() throws SearchLibException {
		rwl.r.lock();
		try {
			if (reader != null)
				if (reader instanceof ReaderLocal)
					return ((ReaderLocal) reader).getFieldCache();
			return null;
		} finally {
			rwl.r.unlock();
		}
	}

	public TermVectorCache getTermVectorCache() throws SearchLibException {
		rwl.r.lock();
		try {
			if (reader != null)
				if (reader instanceof ReaderLocal)
					return ((ReaderLocal) reader).getTermVectorCache();
			return null;
		} finally {
			rwl.r.unlock();
		}
	}

	@Override
	protected void writeXmlConfigIndex(XmlWriter xmlWriter) throws SAXException {
		indexConfig.writeXmlConfig(xmlWriter);
	}

	@Override
	public Collection<?> getFieldNames() throws SearchLibException, IOException {
		versionFile.sharedLock();
		try {
			rwl.r.lock();
			try {
				if (reader != null)
					return reader.getFieldNames();
				return null;
			} finally {
				rwl.r.unlock();
			}
		} finally {
			versionFile.release();
		}
	}

	@Override
	final public Map<String, FieldValue> getDocumentFields(final int docId,
			final Set<String> fieldNameSet, final Timer timer)
			throws IOException, ParseException, SyntaxError, SearchLibException {
		versionFile.sharedLock();
		try {
			rwl.r.lock();
			try {
				if (reader != null)
					return reader.getDocumentFields(docId, fieldNameSet, timer);
				return null;
			} finally {
				rwl.r.unlock();
			}
		} finally {
			versionFile.release();
		}
	}

	@Override
	public Query rewrite(Query query) throws SearchLibException {
		rwl.r.lock();
		try {
			if (reader != null)
				return reader.rewrite(query);
			return null;
		} finally {
			rwl.r.unlock();
		}
	}

	@Override
	public MoreLikeThis getMoreLikeThis() throws SearchLibException,
			IOException {
		versionFile.sharedLock();
		try {
			rwl.r.lock();
			try {
				if (reader != null)
					return reader.getMoreLikeThis();
				return null;
			} finally {
				rwl.r.unlock();
			}
		} finally {
			versionFile.release();
		}
	}

	@Override
	public void mergeData(WriterInterface source) throws SearchLibException,
			IOException {
		if (!(source instanceof IndexLucene))
			throw new SearchLibException("Unsupported operation");
		IndexLucene sourceIndex = (IndexLucene) source;
		versionFile.lock();
		try {
			rwl.r.lock();
			try {
				if (writer == null)
					return;
				sourceIndex.rwl.r.lock();
				try {
					writer.mergeData(sourceIndex.writer);
					if (reader != null)
						reader.reload();
					sendNotifReloadData();
				} finally {
					sourceIndex.rwl.r.unlock();
				}
			} finally {
				rwl.r.unlock();
			}
		} finally {
			versionFile.release();
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

}
