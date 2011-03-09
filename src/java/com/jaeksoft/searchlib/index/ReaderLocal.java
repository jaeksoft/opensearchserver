/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2008-2010 Emmanuel Keller / Jaeksoft
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

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.FieldSelector;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexReader.FieldOption;
import org.apache.lucene.index.StaleReaderException;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.index.TermFreqVector;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.FieldCache.StringIndex;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Similarity;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similar.MoreLikeThis;
import org.apache.lucene.search.spell.LuceneDictionary;
import org.apache.lucene.search.spell.SpellChecker;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.LockObtainFailedException;

import com.jaeksoft.searchlib.Logging;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.cache.FieldCache;
import com.jaeksoft.searchlib.cache.FilterCache;
import com.jaeksoft.searchlib.cache.SearchCache;
import com.jaeksoft.searchlib.cache.SpellCheckerCache;
import com.jaeksoft.searchlib.filter.FilterHits;
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
import com.jaeksoft.searchlib.util.ReadWriteLock;

public class ReaderLocal extends ReaderAbstract implements ReaderInterface {

	final private ReadWriteLock rwl = new ReadWriteLock();

	private IndexDirectory indexDirectory;
	private IndexSearcher indexSearcher;
	private IndexReader indexReader;

	private SearchCache searchCache;
	private FilterCache filterCache;
	private FieldCache fieldCache;
	private SpellCheckerCache spellCheckerCache;

	private File rootDir;
	private File dataDir;

	private boolean readOnly;
	private String similarityClass;

	private ReaderLocal(File rootDir, File dataDir, String similarityClass,
			boolean readOnly) throws IOException, InstantiationException,
			IllegalAccessException, ClassNotFoundException {
		this.similarityClass = similarityClass;
		this.readOnly = readOnly;
		init(rootDir, dataDir);
	}

	private void init(File rootDir, File dataDir) throws IOException,
			InstantiationException, IllegalAccessException,
			ClassNotFoundException {
		rwl.w.lock();
		try {
			this.rootDir = rootDir;
			this.dataDir = dataDir;
			this.indexDirectory = new IndexDirectory("index", dataDir);
			this.indexReader = IndexReader.open(indexDirectory.getDirectory(),
					readOnly);
			this.indexSearcher = new IndexSearcher(indexReader);
			if (similarityClass != null) {
				Similarity similarity = (Similarity) Class.forName(
						similarityClass).newInstance();
				this.indexSearcher.setSimilarity(similarity);
			}
		} finally {
			rwl.w.unlock();
		}
	}

	private void init(ReaderLocal r) {
		rwl.w.lock();
		try {
			this.rootDir = r.rootDir;
			this.dataDir = r.dataDir;
			this.indexDirectory = r.indexDirectory;
			this.indexSearcher = r.indexSearcher;
			this.indexReader = r.indexReader;
		} finally {
			rwl.w.unlock();
		}
	}

	private void initCache(int searchCache, int filterCache, int fieldCache) {
		rwl.w.lock();
		try {
			this.searchCache = new SearchCache(searchCache);
			this.filterCache = new FilterCache(filterCache);
			this.fieldCache = new FieldCache(fieldCache);
			// TODO replace value 100 by number of field in schema
			this.spellCheckerCache = new SpellCheckerCache(100);
		} finally {
			rwl.w.unlock();
		}
	}

	private void resetCache() {
		rwl.w.lock();
		try {
			searchCache.clear();
			filterCache.clear();
			fieldCache.clear();
			spellCheckerCache.clear();
		} finally {
			rwl.w.unlock();
		}
	}

	protected File getRootDir() {
		rwl.r.lock();
		try {
			return rootDir;
		} finally {
			rwl.r.unlock();
		}
	}

	protected File getDatadir() {
		rwl.r.lock();
		try {
			return dataDir;
		} finally {
			rwl.r.unlock();
		}
	}

	@Override
	public long getVersion() {
		rwl.r.lock();
		try {
			return indexReader.getVersion();
		} finally {
			rwl.r.unlock();
		}
	}

	public TermDocs getTermDocs(Term term) throws IOException {
		rwl.r.lock();
		try {
			return indexReader.termDocs(term);
		} finally {
			rwl.r.unlock();
		}
	}

	@Override
	public TermFreqVector getTermFreqVector(int docId, String field)
			throws SearchLibException {
		rwl.r.lock();
		try {
			return indexReader.getTermFreqVector(docId, field);
		} catch (IOException e) {
			throw new SearchLibException(e);
		} finally {
			rwl.r.unlock();
		}
	}

	@Override
	public int getDocFreq(Term term) throws SearchLibException {
		rwl.r.lock();
		try {
			return indexSearcher.docFreq(term);
		} catch (IOException e) {
			throw new SearchLibException(e);
		} finally {
			rwl.r.unlock();
		}
	}

	@Override
	public TermEnum getTermEnum() throws SearchLibException {
		rwl.r.lock();
		try {
			return indexReader.terms();
		} catch (IOException e) {
			throw new SearchLibException(e);
		} finally {
			rwl.r.unlock();
		}
	}

	@Override
	public TermEnum getTermEnum(String field, String term)
			throws SearchLibException {
		rwl.r.lock();
		try {
			return indexReader.terms(new Term(field, term));
		} catch (IOException e) {
			throw new SearchLibException(e);
		} finally {
			rwl.r.unlock();
		}
	}

	@Override
	public Collection<?> getFieldNames() {
		rwl.r.lock();
		try {
			return indexReader.getFieldNames(FieldOption.ALL);
		} finally {
			rwl.r.unlock();
		}
	}

	@Override
	public MoreLikeThis getMoreLikeThis() {
		rwl.r.lock();
		try {
			return new MoreLikeThis(indexReader);
		} finally {
			rwl.r.unlock();
		}
	}

	@Override
	public Query rewrite(Query query) throws SearchLibException {
		rwl.r.lock();
		try {
			return query.rewrite(indexReader);
		} catch (IOException e) {
			throw new SearchLibException(e);
		} finally {
			rwl.r.unlock();
		}
	}

	public void close(boolean bDeleteDirectory) {
		rwl.w.lock();
		try {
			if (indexSearcher != null) {
				indexSearcher.close();
				indexSearcher = null;
			}
			if (indexReader != null) {
				org.apache.lucene.search.FieldCache.DEFAULT.purge(indexReader);
				indexReader.close();
				indexReader = null;
			}
			if (indexDirectory != null) {
				if (bDeleteDirectory)
					indexDirectory.delete();
				indexDirectory.close();
				indexDirectory = null;
			}
		} catch (IOException e) {
			Logging.logger.warn(e.getMessage(), e);
		} finally {
			rwl.w.unlock();
		}
	}

	@Override
	public void close() {
		close(false);
	}

	public int maxDoc() throws IOException {
		rwl.r.lock();
		try {
			return indexSearcher.maxDoc();
		} finally {
			rwl.r.unlock();
		}
	}

	public int numDocs() {
		rwl.r.lock();
		try {
			return indexReader.numDocs();
		} finally {
			rwl.r.unlock();
		}
	}

	public TopDocs search(Query query, Filter filter, Sort sort, int nTop)
			throws IOException {
		rwl.r.lock();
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
			rwl.r.unlock();
		}
	}

	@Override
	public ResultSingle search(SearchRequest searchRequest)
			throws SearchLibException {
		try {
			return new ResultSingle(this, searchRequest);
		} catch (IOException e) {
			throw new SearchLibException(e);
		} catch (ParseException e) {
			throw new SearchLibException(e);
		} catch (SyntaxError e) {
			throw new SearchLibException(e);
		} catch (InstantiationException e) {
			throw new SearchLibException(e);
		} catch (IllegalAccessException e) {
			throw new SearchLibException(e);
		} catch (ClassNotFoundException e) {
			throw new SearchLibException(e);
		}
	}

	@Override
	public String explain(SearchRequest searchRequest, int docId)
			throws SearchLibException {
		rwl.r.lock();
		try {
			Explanation explanation = indexSearcher.explain(
					searchRequest.getQuery(), docId);
			return explanation.toString();
		} catch (IOException e) {
			throw new SearchLibException(e);
		} catch (ParseException e) {
			throw new SearchLibException(e);
		} catch (SyntaxError e) {
			throw new SearchLibException(e);
		} finally {
			rwl.r.unlock();
		}
	}

	public void search(Query query, Filter filter, Collector collector)
			throws IOException {
		rwl.r.lock();
		try {
			if (filter == null)
				indexSearcher.search(query, collector);
			else
				indexSearcher.search(query, filter, collector);
		} finally {
			rwl.r.unlock();
		}
	}

	public FilterHits getFilterHits(Field defaultField, Analyzer analyzer,
			com.jaeksoft.searchlib.filter.Filter filter) throws ParseException,
			IOException {
		rwl.r.lock();
		try {
			return filterCache.get(this, defaultField, analyzer, filter);
		} finally {
			rwl.r.unlock();
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

	public Document getDocFields(int docId, FieldSelector selector)
			throws CorruptIndexException, IOException {
		rwl.r.lock();
		try {
			return indexReader.document(docId, selector);
		} catch (IllegalArgumentException e) {
			throw e;
		} finally {
			rwl.r.unlock();
		}
	}

	public StringIndex getStringIndex(String fieldName) throws IOException {
		rwl.r.lock();
		try {
			return org.apache.lucene.search.FieldCache.DEFAULT.getStringIndex(
					indexReader, fieldName);
		} finally {
			rwl.r.unlock();
		}
	}

	public LuceneDictionary getLuceneDirectionary(String fieldName) {
		rwl.r.lock();
		try {
			return new LuceneDictionary(indexReader, fieldName);
		} finally {
			rwl.r.unlock();
		}
	}

	public void xmlInfo(PrintWriter writer) {
		rwl.r.lock();
		try {
			writer.println("<index name=\"" + indexDirectory.getName()
					+ "\" path=\"" + indexDirectory.getDirectory() + "\"/>");
		} finally {
			rwl.r.unlock();
		}
	}

	private static ReaderLocal findMostRecent(File rootDir,
			String similarityClass, boolean readOnly) {
		ReaderLocal reader = null;
		for (File f : rootDir.listFiles()) {
			if (f.getName().startsWith("."))
				continue;
			try {
				ReaderLocal r = new ReaderLocal(rootDir, f, similarityClass,
						readOnly);
				if (reader == null)
					reader = r;
				else if (r.getVersion() > reader.getVersion())
					reader = r;
			} catch (IOException e) {
				Logging.logger.error(e.getMessage(), e);
			} catch (InstantiationException e) {
				Logging.logger.error(e.getMessage(), e);
			} catch (IllegalAccessException e) {
				Logging.logger.error(e.getMessage(), e);
			} catch (ClassNotFoundException e) {
				Logging.logger.error(e.getMessage(), e);
			}
		}
		return reader;
	}

	private static ReaderLocal findVersion(File rootDir, long version,
			String similarityClass, boolean readOnly) {
		for (File f : rootDir.listFiles()) {
			if (f.getName().startsWith("."))
				continue;
			try {
				ReaderLocal reader = new ReaderLocal(rootDir, f,
						similarityClass, readOnly);
				if (reader.getVersion() == version)
					return reader;
			} catch (IOException e) {
				Logging.logger.error(e.getMessage(), e);
			} catch (InstantiationException e) {
				Logging.logger.error(e.getMessage(), e);
			} catch (IllegalAccessException e) {
				Logging.logger.error(e.getMessage(), e);
			} catch (ClassNotFoundException e) {
				Logging.logger.error(e.getMessage(), e);
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

		if (indexConfig.getRemoteUri() != null)
			return null;

		File indexDir = new File(configDir, "index");
		if (!indexDir.exists() && createIfNotExists)
			indexDir.mkdirs();

		ReaderLocal reader = ReaderLocal.findMostRecent(indexDir,
				indexConfig.getSimilarityClass(), indexConfig.getReadOnly());

		if (reader == null) {
			if (!createIfNotExists)
				return null;
			File dataDir = WriterLocal.createIndex(indexDir);
			reader = new ReaderLocal(indexDir, dataDir,
					indexConfig.getSimilarityClass(), indexConfig.getReadOnly());
		}

		reader.initCache(indexConfig.getSearchCache(),
				indexConfig.getFilterCache(), indexConfig.getFieldCache());
		return reader;
	}

	public Directory getDirectory() {
		rwl.r.lock();
		try {
			return indexDirectory.getDirectory();
		} finally {
			rwl.r.unlock();
		}
	}

	@Override
	public void reload() throws SearchLibException {
		rwl.w.lock();
		try {
			close(false);
			init(rootDir, dataDir);
			resetCache();
		} catch (InstantiationException e) {
			throw new SearchLibException(e);
		} catch (IllegalAccessException e) {
			throw new SearchLibException(e);
		} catch (ClassNotFoundException e) {
			throw new SearchLibException(e);
		} catch (IOException e) {
			throw new SearchLibException(e);
		} finally {
			rwl.w.unlock();
		}
	}

	@Override
	public void swap(long version, boolean deleteOld) {
		ReaderLocal newReader = null;
		if (version > 0)
			newReader = ReaderLocal.findVersion(rootDir, version,
					similarityClass, readOnly);
		else
			newReader = ReaderLocal.findMostRecent(rootDir, similarityClass,
					readOnly);
		if (newReader == null)
			return;
		rwl.w.lock();
		try {
			close(false);
			init(newReader);
			resetCache();
			if (deleteOld)
				deleteAllOthers();
		} finally {
			rwl.w.unlock();
		}

	}

	public DocSetHits searchDocSet(SearchRequest searchRequest)
			throws IOException, ParseException, SyntaxError,
			SearchLibException, InstantiationException, IllegalAccessException,
			ClassNotFoundException {
		rwl.r.lock();
		try {

			Schema schema = searchRequest.getConfig().getSchema();
			Field defaultField = schema.getFieldList().getDefaultField();
			Analyzer analyzer = schema.getQueryPerFieldAnalyzer(searchRequest
					.getLang());

			return searchCache.get(this, searchRequest, schema, defaultField,
					analyzer);

		} finally {
			rwl.r.unlock();
		}
	}

	public DocSetHits newDocSetHits(SearchRequest searchRequest, Schema schema,
			Field defaultField, Analyzer analyzer) throws IOException,
			ParseException, SyntaxError, InstantiationException,
			IllegalAccessException, ClassNotFoundException, SearchLibException {

		boolean isFacet = searchRequest.isFacet();

		FilterHits filterHits = searchRequest.getFilterList().getFilterHits(
				this, defaultField, analyzer);
		Sort sort = searchRequest.getSortList().getLuceneSort();

		DocSetHits dsh = new DocSetHits(this, searchRequest.getQuery(),
				filterHits, sort, isFacet);
		return dsh;
	}

	public FieldList<FieldValue> getDocumentFields(int docId,
			FieldList<Field> fieldList) throws CorruptIndexException,
			IOException, ParseException, SyntaxError {
		rwl.r.lock();
		try {
			return fieldCache.get(this, docId, fieldList);
		} finally {
			rwl.r.unlock();
		}
	}

	@Override
	public ResultDocument[] documents(DocumentsRequest documentsRequest)
			throws SearchLibException {
		rwl.r.lock();
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
		} catch (IOException e) {
			throw new SearchLibException(e);
		} catch (ParseException e) {
			throw new SearchLibException(e);
		} catch (SyntaxError e) {
			throw new SearchLibException(e);
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
			if (reader == null)
				return true;
			return reader.sameIndex(this);
		} finally {
			rwl.r.unlock();
		}
	}

	@Override
	public IndexStatistics getStatistics() {
		rwl.r.lock();
		try {
			return new IndexStatistics(indexReader);
		} finally {
			rwl.r.unlock();
		}
	}

	private void pushFile(File file, URI uri) throws URISyntaxException,
			IOException {
		StringBuffer query = new StringBuffer();
		query.append("?version=");
		query.append(getVersion());
		query.append("&fileName=");
		query.append(file.getName());
		uri = new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost(),
				uri.getPort(), uri.getPath() + "/push", query.toString(),
				uri.getFragment());
		UriWriteStream uws = null;
		try {
			uws = new UriWriteStream(uri, file);
		} finally {
			if (uws != null)
				uws.close();
		}
	}

	@Override
	public void push(URI dest) throws SearchLibException {
		rwl.r.lock();
		try {
			File[] files = dataDir.listFiles();
			for (File file : files) {
				if (file.getName().charAt(0) == '.')
					continue;
				pushFile(file, dest);
			}
		} catch (URISyntaxException e) {
			throw new SearchLibException(e);
		} catch (IOException e) {
			throw new SearchLibException(e);
		} finally {
			rwl.r.unlock();
		}

	}

	public SpellChecker getSpellChecker(String fieldName) throws IOException {
		rwl.r.lock();
		try {
			return spellCheckerCache.get(this, fieldName);
		} finally {
			rwl.r.unlock();
		}
	}

	protected SearchCache getSearchCache() {
		rwl.r.lock();
		try {
			return searchCache;
		} finally {
			rwl.r.unlock();
		}
	}

	protected FilterCache getFilterCache() {
		rwl.r.lock();
		try {
			return filterCache;
		} finally {
			rwl.r.unlock();
		}
	}

	protected FieldCache getFieldCache() {
		rwl.r.lock();
		try {
			return fieldCache;
		} finally {
			rwl.r.unlock();
		}
	}

}
