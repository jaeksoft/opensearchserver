/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2008-2009 Emmanuel Keller / Jaeksoft
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
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.lucene.analysis.Analyzer;
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
import org.apache.lucene.search.Similarity;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.FieldCache.StringIndex;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.LockObtainFailedException;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.cache.FieldCache;
import com.jaeksoft.searchlib.cache.FilterCache;
import com.jaeksoft.searchlib.cache.SearchCache;
import com.jaeksoft.searchlib.filter.FilterCacheKey;
import com.jaeksoft.searchlib.filter.FilterHits;
import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.remote.UriWriteStream;
import com.jaeksoft.searchlib.request.DocumentRequest;
import com.jaeksoft.searchlib.request.DocumentsRequest;
import com.jaeksoft.searchlib.request.SearchRequest;
import com.jaeksoft.searchlib.result.ResultDocument;
import com.jaeksoft.searchlib.result.ResultDocuments;
import com.jaeksoft.searchlib.result.ResultSingle;
import com.jaeksoft.searchlib.schema.Field;
import com.jaeksoft.searchlib.schema.FieldList;
import com.jaeksoft.searchlib.schema.FieldValue;
import com.jaeksoft.searchlib.schema.Schema;

public class ReaderLocal extends ReaderAbstract implements ReaderInterface {

	private IndexDirectory indexDirectory;
	private IndexSearcher indexSearcher;
	private IndexReader indexReader;

	private SearchCache searchCache;
	private FilterCache filterCache;
	private FieldCache fieldCache;

	private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock(true);
	private final Lock r = rwl.readLock();
	private final Lock w = rwl.writeLock();

	private File rootDir;
	private File dataDir;

	private String similarityClass;

	private ReaderLocal(String name, File rootDir, File dataDir,
			String similarityClass) throws IOException, InstantiationException,
			IllegalAccessException, ClassNotFoundException {
		super(name);
		this.similarityClass = similarityClass;
		init(rootDir, dataDir);
	}

	private void init(File rootDir, File dataDir) throws IOException,
			InstantiationException, IllegalAccessException,
			ClassNotFoundException {
		w.lock();
		try {
			this.rootDir = rootDir;
			this.dataDir = dataDir;
			this.indexDirectory = new IndexDirectory(getName(), dataDir);
			this.indexSearcher = new IndexSearcher(indexDirectory
					.getDirectory());
			if (similarityClass != null) {
				Similarity similarity = (Similarity) Class.forName(
						similarityClass).newInstance();
				this.indexSearcher.setSimilarity(similarity);
			}
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

	public void rewrite(Query query) throws IOException {
		r.lock();
		try {
			query.rewrite(indexReader);
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

	public void close() {
		close(false);
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
			ParseException, SyntaxError, SearchLibException,
			InstantiationException, IllegalAccessException,
			ClassNotFoundException {
		return new ResultSingle(this, searchRequest);
	}

	public boolean deleteDocument(int docId) throws StaleReaderException,
			CorruptIndexException, LockObtainFailedException, IOException {
		r.lock();
		try {
			fastDeleteDocument(docId);
			return true;
		} finally {
			r.unlock();
		}
	}

	public int deleteDocuments(Collection<Integer> docIds)
			throws StaleReaderException, CorruptIndexException,
			LockObtainFailedException, IOException {
		r.lock();
		try {
			int count = 0;
			for (Integer docId : docIds) {
				fastDeleteDocument(docId);
				count++;
			}
			return count;
		} finally {
			r.unlock();
		}
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

	public FilterHits getFilterHits(Field defaultField, Analyzer analyzer,
			com.jaeksoft.searchlib.filter.Filter filter, boolean noCache)
			throws ParseException, IOException {
		r.lock();
		try {
			FilterHits filterHits;
			FilterCacheKey filterCacheKey = null;
			if (!noCache) {
				filterCacheKey = new FilterCacheKey(filter, defaultField,
						analyzer);
				filterHits = filterCache.getAndPromote(filterCacheKey);
				if (filterHits != null)
					return filterHits;
			}
			Query query = filter.getQuery(defaultField, analyzer);
			filterHits = new FilterHits(query, this);
			if (!noCache)
				filterCache.put(filterCacheKey, filterHits);
			return filterHits;
		} finally {
			r.unlock();
		}
	}

	protected void fastDeleteDocument(int docNum) throws StaleReaderException,
			CorruptIndexException, LockObtainFailedException, IOException {
		indexReader.deleteDocument(docNum);
	}

	protected void fastDeleteDocument(String fieldName, String value)
			throws StaleReaderException, CorruptIndexException,
			LockObtainFailedException, IOException {
		indexReader.deleteDocuments(new Term(fieldName, value));
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

	public void xmlInfo(PrintWriter writer) {
		r.lock();
		try {
			writer.println("<index name=\"" + indexDirectory.getName()
					+ "\" path=\"" + indexDirectory.getDirectory() + "\"/>");
		} finally {
			r.unlock();
		}
	}

	private static ReaderLocal findMostRecent(String name, File rootDir,
			String similarityClass) {
		ReaderLocal reader = null;
		for (File f : rootDir.listFiles()) {
			if (f.getName().startsWith("."))
				continue;
			try {
				ReaderLocal r = new ReaderLocal(name, rootDir, f,
						similarityClass);
				if (reader == null)
					reader = r;
				else if (r.getVersion() > reader.getVersion())
					reader = r;
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
		return reader;
	}

	private static ReaderLocal findVersion(String name, File rootDir,
			long version, String similarityClass) {
		for (File f : rootDir.listFiles()) {
			if (f.getName().startsWith("."))
				continue;
			try {
				ReaderLocal reader = new ReaderLocal(name, rootDir, f,
						similarityClass);
				if (reader.getVersion() == version)
					return reader;
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
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

	public static ReaderLocal fromConfig(File configDir,
			IndexConfig indexConfig, boolean createIfNotExists)
			throws IOException, InstantiationException, IllegalAccessException,
			ClassNotFoundException {
		if (indexConfig.getName() == null)
			return null;
		if (indexConfig.getRemoteUri() != null)
			return null;

		File indexDir = new File(configDir, indexConfig.getName());
		if (!indexDir.exists() && createIfNotExists)
			indexDir.mkdirs();

		ReaderLocal reader = ReaderLocal.findMostRecent(indexConfig.getName(),
				indexDir, indexConfig.getSimilarityClass());

		if (reader == null) {
			if (!createIfNotExists)
				return null;
			File dataDir = WriterLocal.createIndex(indexDir);
			reader = new ReaderLocal(indexConfig.getName(), indexDir, dataDir,
					indexConfig.getSimilarityClass());
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

	public void reload() throws IOException, InstantiationException,
			IllegalAccessException, ClassNotFoundException {
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
			newReader = ReaderLocal.findVersion(getName(), rootDir, version,
					similarityClass);
		else
			newReader = ReaderLocal.findMostRecent(getName(), rootDir,
					similarityClass);
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
			throws IOException, ParseException, SyntaxError,
			SearchLibException, InstantiationException, IllegalAccessException,
			ClassNotFoundException {
		boolean isDelete = searchRequest.isDelete();
		boolean isFacet = searchRequest.isFacet();
		boolean isNoCache = searchRequest.isNoCache();
		if (isDelete)
			w.lock();
		else
			r.lock();
		try {

			Schema schema = searchRequest.getConfig().getSchema();
			Field defaultField = schema.getFieldList().getDefaultField();
			Analyzer analyzer = schema.getQueryPerFieldAnalyzer(searchRequest
					.getLang());
			DocSetHitCacheKey key = new DocSetHitCacheKey(searchRequest,
					defaultField, analyzer);

			DocSetHits dsh = null;
			if (!isDelete && !isNoCache) {
				dsh = searchCache.getAndPromote(key);
				if (dsh != null)
					return dsh;
			}

			FilterHits filterHits = searchRequest.getFilterList()
					.getFilterHits(this, defaultField, analyzer, isNoCache);
			Sort sort = searchRequest.getSortList().getLuceneSort();

			dsh = new DocSetHits(this, searchRequest.getQuery(), filterHits,
					sort, isDelete, isFacet);
			if (!isDelete && !isNoCache)
				searchCache.put(key, dsh);
			if (isDelete && dsh.getDocNumFound() > 0)
				reload();
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
				FieldContentCacheKey key = new FieldContentCacheKey(field
						.getName(), docId);
				String[] values = fieldCache.getAndPromote(key);
				if (values != null)
					documentFields.add(new FieldValue(field, values));
				else
					missingField.add(field);
			}

			if (missingField.size() > 0) {
				Document document = getDocFields(docId, missingField);
				for (Field field : missingField) {
					FieldContentCacheKey key = new FieldContentCacheKey(field
							.getName(), docId);
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

	public ResultDocuments documents(DocumentsRequest documentsRequest)
			throws IOException, ParseException, SyntaxError {
		r.lock();
		try {
			DocumentRequest[] requestedDocuments = documentsRequest
					.getRequestedDocuments();
			if (requestedDocuments == null)
				return null;
			ResultDocuments documents = new ResultDocuments(
					requestedDocuments.length);
			int i = 0;
			for (DocumentRequest documentRequest : requestedDocuments)
				documents.set(i++, new ResultDocument(documentsRequest,
						documentRequest.doc, this));
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

	protected SearchCache getSearchCache() {
		r.lock();
		try {
			return searchCache;
		} finally {
			r.unlock();
		}
	}

	protected FilterCache getFilterCache() {
		r.lock();
		try {
			return filterCache;
		} finally {
			r.unlock();
		}
	}

	protected FieldCache getFieldCache() {
		r.lock();
		try {
			return fieldCache;
		} finally {
			r.unlock();
		}
	}
}
