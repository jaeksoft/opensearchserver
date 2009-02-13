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
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.FieldSelector;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.StaleReaderException;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.index.TermFreqVector;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.HitCollector;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.FieldCache.StringIndex;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.LockObtainFailedException;

import com.jaeksoft.searchlib.cache.FieldCache;
import com.jaeksoft.searchlib.cache.FilterCache;
import com.jaeksoft.searchlib.cache.SearchCache;
import com.jaeksoft.searchlib.filter.FilterHits;
import com.jaeksoft.searchlib.filter.FilterList;
import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.remote.UriWriteStream;
import com.jaeksoft.searchlib.request.DocumentRequest;
import com.jaeksoft.searchlib.request.DocumentsRequest;
import com.jaeksoft.searchlib.request.SearchRequest;
import com.jaeksoft.searchlib.result.ResultDocument;
import com.jaeksoft.searchlib.result.ResultSingle;
import com.jaeksoft.searchlib.schema.Field;
import com.jaeksoft.searchlib.schema.FieldList;
import com.jaeksoft.searchlib.schema.FieldValue;
import com.jaeksoft.searchlib.schema.Schema;
import com.jaeksoft.searchlib.sort.SortList;
import com.jaeksoft.searchlib.util.FileUtils;

public class ReaderLocal extends NameFilter implements ReaderInterface {

	private IndexDirectory indexDirectory;
	private IndexSearcher indexSearcher;
	private IndexReader indexReader;

	private SearchCache searchCache;
	private FilterCache filterCache;
	private FieldCache fieldCache;

	private final ReadWriteLock rwl = new ReentrantReadWriteLock();
	private final Lock r = rwl.readLock();
	private final Lock w = rwl.writeLock();

	private File rootDir;
	private File dataDir;

	private ReaderLocal(String name, File rootDir, File dataDir)
			throws IOException {
		super(name);
		init(rootDir, dataDir);
	}

	private void init(File rootDir, File dataDir) throws IOException {
		w.lock();
		try {
			this.rootDir = rootDir;
			this.dataDir = dataDir;
			this.indexDirectory = new IndexDirectory(getName(), dataDir);
			this.indexSearcher = new IndexSearcher(indexDirectory
					.getDirectory());
			this.indexReader = indexSearcher.getIndexReader();
		} finally {
			w.unlock();
		}
	}

	private void init(ReaderLocal r) {
		w.lock();
		try {
			this.rootDir = r.rootDir;
			this.dataDir = r.dataDir;
			this.indexDirectory = r.indexDirectory;
			this.indexSearcher = r.indexSearcher;
			this.indexReader = r.indexReader;
		} finally {
			w.unlock();
		}
	}

	private void initCache(int searchCache, int filterCache, int fieldCache) {
		w.lock();
		try {
			this.searchCache = new SearchCache(searchCache);
			this.filterCache = new FilterCache(filterCache);
			this.fieldCache = new FieldCache(fieldCache);
		} finally {
			w.unlock();
		}
	}

	private void resetCache() {
		w.lock();
		try {
			searchCache.clear();
			filterCache.clear();
			fieldCache.clear();
		} finally {
			w.unlock();
		}
	}

	protected File getRootDir() {
		r.lock();
		try {
			return rootDir;
		} finally {
			r.unlock();
		}
	}

	protected File getDatadir() {
		r.lock();
		try {
			return dataDir;
		} finally {
			r.unlock();
		}
	}

	public long getVersion() {
		r.lock();
		try {
			return indexReader.getVersion();
		} finally {
			r.unlock();
		}
	}

	public TermDocs getTermDocs(Term term) throws IOException {
		r.lock();
		try {
			return indexReader.termDocs(term);
		} finally {
			r.unlock();
		}
	}

	public TermFreqVector getTermFreqVector(int docId, String field)
			throws IOException {
		r.lock();
		try {
			return indexReader.getTermFreqVector(docId, field);
		} finally {
			r.unlock();
		}
	}

	public int getDocFreq(Term term) throws IOException {
		r.lock();
		try {
			return indexSearcher.docFreq(term);
		} finally {
			r.unlock();
		}
	}

	public void close(boolean bDeleteDirectory) {
		w.lock();
		try {
			if (indexReader != null) {
				indexReader.close();
				indexReader = null;
			}
			if (indexSearcher != null) {
				indexSearcher.close();
				indexSearcher = null;
			}
			if (indexDirectory != null) {
				if (bDeleteDirectory)
					indexDirectory.delete();
				indexDirectory.close();
				indexDirectory = null;
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			w.unlock();
		}
	}

	public int maxDoc() throws IOException {
		r.lock();
		try {
			return indexSearcher.maxDoc();
		} finally {
			r.unlock();
		}
	}

	public int numDocs() {
		r.lock();
		try {
			return indexReader.numDocs();
		} finally {
			r.unlock();
		}
	}

	public TopDocs search(Query query, Filter filter, Sort sort, int nTop)
			throws IOException {
		r.lock();
		try {
			if (sort == null) {
				if (filter == null)
					return indexSearcher.search(query, nTop);
				else
					return indexSearcher.search(query, filter, nTop);
			} else {
				return indexSearcher.search(query, filter, nTop, sort);
			}
		} finally {
			r.unlock();
		}
	}

	public ResultSingle search(SearchRequest searchRequest) throws IOException,
			ParseException, SyntaxError {
		return new ResultSingle(this, searchRequest);
	}

	public void search(Query query, Filter filter, HitCollector collector)
			throws IOException {
		r.lock();
		try {
			if (filter == null)
				indexSearcher.search(query, collector);
			else
				indexSearcher.search(query, filter, collector);
		} finally {
			r.unlock();
		}
	}

	protected void deleteDocument(int docNum) throws StaleReaderException,
			CorruptIndexException, LockObtainFailedException, IOException {
		indexReader.deleteDocument(docNum);
	}

	private Document getDocFields(int docId, FieldSelector selector)
			throws CorruptIndexException, IOException {
		r.lock();
		try {
			return indexReader.document(docId, selector);
		} catch (IllegalArgumentException e) {
			throw e;
		} finally {
			r.unlock();
		}
	}

	public StringIndex getStringIndex(String fieldName) throws IOException {
		r.lock();
		try {
			return org.apache.lucene.search.FieldCache.DEFAULT.getStringIndex(
					indexReader, fieldName);
		} finally {
			r.unlock();
		}
	}

	public void xmlInfo(PrintWriter writer, HashSet<String> classDetail) {
		r.lock();
		try {
			writer.println("<index name=\"" + indexDirectory.getName()
					+ "\" path=\"" + indexDirectory.getDirectory() + "\"/>");
		} finally {
			r.unlock();
		}
	}

	private static ReaderLocal findMostRecent(String name, File rootDir) {
		ReaderLocal reader = null;
		for (File f : rootDir.listFiles()) {
			if (f.getName().startsWith("."))
				continue;
			try {
				ReaderLocal r = new ReaderLocal(name, rootDir, f);
				if (reader == null)
					reader = r;
				else if (r.getVersion() > reader.getVersion())
					reader = r;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return reader;
	}

	private static ReaderLocal findVersion(String name, File rootDir,
			long version) {
		for (File f : rootDir.listFiles()) {
			if (f.getName().startsWith("."))
				continue;
			try {
				ReaderLocal reader = new ReaderLocal(name, rootDir, f);
				if (reader.getVersion() == version)
					return reader;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	private void deleteAllOthers() {
		for (File f : rootDir.listFiles()) {
			if (f.getName().startsWith("."))
				continue;
			if (f.equals(dataDir))
				continue;
			IndexDirectory.deleteDir(f);
		}
	}

	public static ReaderLocal fromConfig(File homeDir, IndexConfig indexConfig,
			boolean createIfNotExists) throws IOException {
		if (indexConfig.getName() == null || indexConfig.getPath() == null)
			return null;

		File rootDir = new File(FileUtils.locatePath(homeDir, indexConfig
				.getPath()));
		if (!rootDir.exists() && createIfNotExists)
			rootDir.mkdirs();
		ReaderLocal reader = ReaderLocal.findMostRecent(indexConfig.getName(),
				rootDir);

		if (reader == null) {
			if (!createIfNotExists)
				return null;
			File dataDir = WriterLocal.createIndex(rootDir);
			reader = new ReaderLocal(indexConfig.getName(), rootDir, dataDir);
		}

		reader.initCache(indexConfig.getSearchCache(), indexConfig
				.getFilterCache(), indexConfig.getFieldCache());
		return reader;
	}

	public Directory getDirectory() {
		r.lock();
		try {
			return indexDirectory.getDirectory();
		} finally {
			r.unlock();
		}
	}

	public void reload() throws IOException {
		w.lock();
		try {
			close(false);
			init(rootDir, dataDir);
			resetCache();
		} finally {
			w.unlock();
		}
	}

	public void swap(long version, boolean deleteOld) throws IOException {
		ReaderLocal newReader = null;
		if (version > 0)
			newReader = ReaderLocal.findVersion(getName(), rootDir, version);
		else
			newReader = ReaderLocal.findMostRecent(getName(), rootDir);
		if (newReader == null)
			return;
		w.lock();
		try {
			close(false);
			init(newReader);
			resetCache();
			if (deleteOld)
				deleteAllOthers();
		} finally {
			w.unlock();
		}

	}

	public DocSetHits searchDocSet(SearchRequest searchRequest)
			throws IOException, ParseException, SyntaxError {
		boolean isDelete = searchRequest.isDelete();
		boolean isFacet = searchRequest.isFacet();
		if (isDelete)
			w.lock();
		else
			r.lock();
		try {
			StringBuffer cacheDshKey = new StringBuffer(searchRequest
					.getQueryParsed());
			FilterHits filter = null;
			FilterList filterList = searchRequest.getFilterList();
			if (filterList.size() > 0) {
				String cacheFilterKey = FilterHits.toCacheKey(filterList);
				filter = filterCache.getAndPromote(cacheFilterKey);
				if (filter == null) {
					Schema schema = searchRequest.getConfig().getSchema();
					filter = new FilterHits(schema.getFieldList()
							.getDefaultField(), schema
							.getQueryPerFieldAnalyzer(searchRequest.getLang()),
							this, filterList);
					filterCache.put(cacheFilterKey, filter);
				}
				cacheDshKey.append('|');
				cacheDshKey.append(cacheFilterKey);
			}
			SortList sortList = searchRequest.getSortList();
			Sort sort = sortList.getLuceneSort();
			if (sort != null) {
				cacheDshKey.append("_");
				cacheDshKey.append(sortList.getSortKey());
			}
			if (isFacet)
				cacheDshKey.append("|facet");
			String cacheDshKeyStr = cacheDshKey.toString();
			DocSetHits dsh = null;
			if (!isDelete)
				dsh = searchCache.getAndPromote(cacheDshKeyStr);
			if (dsh == null) {
				dsh = new DocSetHits(this, searchRequest.getQuery(), filter,
						sort, isDelete, isFacet);
				if (!isDelete)
					searchCache.put(cacheDshKeyStr, dsh);
				else if (dsh.getDocNumFound() > 0)
					reload();
			}
			return dsh;
		} finally {
			if (isDelete)
				w.unlock();
			else
				r.unlock();
		}
	}

	public FieldList<FieldValue> getDocumentFields(int docId,
			FieldList<Field> fieldList) throws CorruptIndexException,
			IOException, ParseException, SyntaxError {
		r.lock();
		try {

			FieldList<FieldValue> documentFields = new FieldList<FieldValue>();

			FieldList<Field> missingField = new FieldList<Field>();

			for (Field field : fieldList) {
				String key = FieldCache.getKey(field, docId);
				String[] values = fieldCache.getAndPromote(key);
				if (values != null)
					documentFields.add(new FieldValue(field, values));
				else
					missingField.add(field);
			}

			if (missingField.size() > 0) {
				Document document = getDocFields(docId, missingField);
				for (Field field : missingField) {
					String key = FieldCache.getKey(field, docId);
					String[] values = document.getValues(field.getName());
					if (values != null) {
						documentFields.add(new FieldValue(field, values));
						fieldCache.put(key, values);
					}
				}
			}
			return documentFields;

		} finally {
			r.unlock();
		}
	}

	public ResultDocument[] documents(DocumentsRequest documentsRequest)
			throws IOException, ParseException, SyntaxError {
		r.lock();
		try {
			DocumentRequest[] requestedDocuments = documentsRequest
					.getRequestedDocuments();
			if (requestedDocuments == null)
				return null;
			ResultDocument[] documents = new ResultDocument[requestedDocuments.length];
			int i = 0;
			for (DocumentRequest documentRequest : requestedDocuments)
				documents[i++] = new ResultDocument(documentsRequest,
						documentRequest.doc, this);
			return documents;
		} finally {
			r.unlock();
		}
	}

	public boolean sameIndex(ReaderInterface reader) {
		r.lock();
		try {
			if (reader == this)
				return true;
			if (reader == null)
				return true;
			return reader.sameIndex(this);
		} finally {
			r.unlock();
		}
	}

	public IndexStatistics getStatistics() {
		r.lock();
		try {
			return new IndexStatistics(indexReader);
		} finally {
			r.unlock();
		}
	}

	private void pushFile(File file, URI uri) throws URISyntaxException,
			IOException {
		StringBuffer query = new StringBuffer("?indexName=");
		query.append(getName());
		query.append("&version=");
		query.append(getVersion());
		query.append("&fileName=");
		query.append(file.getName());
		uri = new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost(), uri
				.getPort(), uri.getPath() + "/push", query.toString(), uri
				.getFragment());
		UriWriteStream uws = null;
		try {
			uws = new UriWriteStream(uri, file);
		} finally {
			if (uws != null)
				uws.close();
		}
	}

	public void push(URI dest) throws URISyntaxException, IOException {
		r.lock();
		try {
			File[] files = dataDir.listFiles();
			for (File file : files) {
				if (file.getName().charAt(0) == '.')
					continue;
				pushFile(file, dest);
			}
		} finally {
			r.unlock();
		}

	}

}
