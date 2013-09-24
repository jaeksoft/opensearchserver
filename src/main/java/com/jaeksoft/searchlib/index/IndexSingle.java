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

package com.jaeksoft.searchlib.index;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
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
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.cache.FieldCache;
import com.jaeksoft.searchlib.cache.FilterCache;
import com.jaeksoft.searchlib.cache.SearchCache;
import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.query.ParseException;
import com.jaeksoft.searchlib.request.AbstractRequest;
import com.jaeksoft.searchlib.request.AbstractSearchRequest;
import com.jaeksoft.searchlib.result.AbstractResult;
import com.jaeksoft.searchlib.schema.FieldValue;
import com.jaeksoft.searchlib.schema.Schema;
import com.jaeksoft.searchlib.util.ReadWriteLock;
import com.jaeksoft.searchlib.util.Timer;
import com.jaeksoft.searchlib.util.XmlWriter;

public class IndexSingle extends IndexAbstract {

	final private ReadWriteLock rwl = new ReadWriteLock();

	private IndexDirectory indexDirectory = null;

	private ReaderInterface reader = null;
	private WriterInterface writer = null;

	private volatile boolean online;

	public IndexSingle(File configDir, IndexConfig indexConfig,
			boolean createIfNotExists) throws IOException, URISyntaxException,
			InstantiationException, IllegalAccessException,
			ClassNotFoundException {
		super(indexConfig);
		this.online = true;

		boolean bCreate = false;
		File indexDir = new File(configDir, "index");
		if (!indexDir.exists()) {
			if (!createIfNotExists)
				return;
			indexDir.mkdir();
			bCreate = true;
		} else
			indexDir = findIndexDirOrSub(indexDir);
		indexDirectory = new IndexDirectory(indexDir);
		writer = new WriterLocal(indexConfig, this, indexDirectory);
		if (bCreate)
			((WriterLocal) writer).create();
		reader = new ReaderLocal(indexConfig, indexDirectory);
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
			if (reader != null)
				reader.reload();
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
	public int deleteDocument(Schema schema, String field, String value)
			throws SearchLibException {
		rwl.r.lock();
		try {
			checkOnline(true);
			if (writer != null)
				return writer.deleteDocument(schema, field, value);
			else
				return 0;
		} finally {
			rwl.r.unlock();
		}
	}

	@Override
	public int deleteDocuments(Schema schema, String field,
			Collection<String> values) throws SearchLibException {
		rwl.r.lock();
		try {
			checkOnline(true);
			if (writer != null)
				return writer.deleteDocuments(schema, field, values);
			else
				return 0;
		} finally {
			rwl.r.unlock();
		}
	}

	@Override
	public void deleteAll() throws SearchLibException {
		rwl.r.lock();
		try {
			checkOnline(true);
			if (writer != null)
				writer.deleteAll();
		} finally {
			rwl.r.unlock();
		}
	}

	@Override
	public int deleteDocuments(AbstractSearchRequest query)
			throws SearchLibException {
		rwl.r.lock();
		try {
			checkOnline(true);
			return writer.deleteDocuments(query);
		} finally {
			rwl.r.unlock();
		}
	}

	@Override
	public void addBeforeUpdate(BeforeUpdateInterface beforeUpdate)
			throws SearchLibException {
		rwl.r.lock();
		try {
			if (writer != null)
				writer.addBeforeUpdate(beforeUpdate);
		} finally {
			rwl.r.unlock();
		}
	}

	@Override
	public boolean updateDocument(Schema schema, IndexDocument document)
			throws SearchLibException {
		rwl.r.lock();
		try {
			checkOnline(true);
			if (writer != null)
				return writer.updateDocument(schema, document);
			else
				return false;
		} finally {
			rwl.r.unlock();
		}
	}

	@Override
	public int updateDocuments(Schema schema,
			Collection<IndexDocument> documents) throws SearchLibException {
		rwl.r.lock();
		try {
			checkOnline(true);
			if (writer != null)
				return writer.updateDocuments(schema, documents);
			else
				return 0;
		} finally {
			rwl.r.unlock();
		}
	}

	@Override
	public void reload() throws SearchLibException {
		rwl.r.lock();
		try {
			checkOnline(true);
			reader.reload();
		} finally {
			rwl.r.unlock();
		}
	}

	@Override
	public AbstractResult<?> request(AbstractRequest request)
			throws SearchLibException {
		rwl.r.lock();
		try {
			checkOnline(true);
			if (reader != null)
				return reader.request(request);
			return null;
		} finally {
			rwl.r.unlock();
		}
	}

	@Override
	public String explain(AbstractRequest request, int docId, boolean bHtml)
			throws SearchLibException {
		rwl.r.lock();
		try {
			checkOnline(true);
			if (reader != null)
				return reader.explain(request, docId, bHtml);
			return null;
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
			checkOnline(true);
			if (reader != null)
				return reader.getStatistics();
			return null;
		} finally {
			rwl.r.unlock();
		}
	}

	@Override
	public IndexSingle get(String name) {
		return this;
	}

	@Override
	public int getDocFreq(Term term) throws SearchLibException {
		rwl.r.lock();
		try {
			checkOnline(true);
			if (reader != null)
				return reader.getDocFreq(term);
			return 0;
		} finally {
			rwl.r.unlock();
		}
	}

	@Override
	public TermEnum getTermEnum() throws SearchLibException {
		rwl.r.lock();
		try {
			checkOnline(true);
			if (reader != null)
				return reader.getTermEnum();
			return null;
		} finally {
			rwl.r.unlock();
		}
	}

	@Override
	public TermEnum getTermEnum(Term term) throws SearchLibException {
		rwl.r.lock();
		try {
			checkOnline(true);
			if (reader != null)
				return reader.getTermEnum(term);
			return null;
		} finally {
			rwl.r.unlock();
		}
	}

	@Override
	public TermDocs getTermDocs(Term term) throws SearchLibException,
			IOException {
		rwl.r.lock();
		try {
			checkOnline(true);
			if (reader != null)
				return reader.getTermDocs(term);
			return null;
		} finally {
			rwl.r.unlock();
		}
	}

	@Override
	public TermFreqVector getTermFreqVector(int docId, String field)
			throws IOException, SearchLibException {
		rwl.r.lock();
		try {
			checkOnline(true);
			if (reader != null)
				return reader.getTermFreqVector(docId, field);
			return null;
		} finally {
			rwl.r.unlock();
		}
	}

	@Override
	public boolean isOnline() {
		rwl.r.lock();
		try {
			return online;
		} finally {
			rwl.r.unlock();
		}
	}

	@Override
	public void setOnline(boolean v) {
		rwl.w.lock();
		try {
			online = v;
		} finally {
			rwl.w.unlock();
		}
	}

	@Override
	public long getVersion() throws SearchLibException {
		rwl.r.lock();
		try {
			checkOnline(true);
			if (reader == null)
				return 0;
			return reader.getVersion();
		} finally {
			rwl.r.unlock();
		}
	}

	public SearchCache getSearchCache() throws SearchLibException {
		rwl.r.lock();
		try {
			checkOnline(true);
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
			checkOnline(true);
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
			checkOnline(true);
			if (reader != null)
				if (reader instanceof ReaderLocal)
					return ((ReaderLocal) reader).getFieldCache();
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
	public Collection<?> getFieldNames() throws SearchLibException {
		rwl.r.lock();
		try {
			checkOnline(true);
			if (reader != null)
				return reader.getFieldNames();
			return null;
		} finally {
			rwl.r.unlock();
		}
	}

	@Override
	public Map<String, FieldValue> getDocumentFields(int docId,
			Set<String> fieldNameSet, Timer timer) throws IOException,
			ParseException, SyntaxError, SearchLibException {
		rwl.r.lock();
		try {
			checkOnline(true);
			if (reader != null)
				return reader.getDocumentFields(docId, fieldNameSet, timer);
			return null;
		} finally {
			rwl.r.unlock();
		}
	}

	@Override
	public Query rewrite(Query query) throws SearchLibException {
		rwl.r.lock();
		try {
			checkOnline(true);
			if (reader != null)
				return reader.rewrite(query);
			return null;
		} finally {
			rwl.r.unlock();
		}
	}

	@Override
	public MoreLikeThis getMoreLikeThis() throws SearchLibException {
		rwl.r.lock();
		try {
			checkOnline(true);
			if (reader != null)
				return reader.getMoreLikeThis();
			return null;
		} finally {
			rwl.r.unlock();
		}
	}

	@Override
	public void mergeData(WriterInterface source) throws SearchLibException {
		if (!(source instanceof IndexSingle))
			throw new SearchLibException("Unsupported operation");
		IndexSingle sourceIndex = (IndexSingle) source;
		rwl.r.lock();
		try {
			if (writer == null)
				return;
			sourceIndex.rwl.r.lock();
			try {
				writer.mergeData(sourceIndex.writer);
				if (reader != null)
					reader.reload();
			} finally {
				sourceIndex.rwl.r.unlock();
			}
		} finally {
			rwl.r.unlock();
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
