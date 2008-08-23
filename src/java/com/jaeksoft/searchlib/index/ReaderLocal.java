/**   
 * License Agreement for Jaeksoft SearchLib Community
 *
 * Copyright (C) 2008 Emmanuel Keller / Jaeksoft
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.FieldSelector;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.StaleReaderException;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.search.FieldCache;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.HitCollector;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.FieldCache.StringIndex;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.LockObtainFailedException;
import org.w3c.dom.Node;

import com.jaeksoft.searchlib.cache.DocumentCache;
import com.jaeksoft.searchlib.cache.FilterCache;
import com.jaeksoft.searchlib.cache.SearchCache;
import com.jaeksoft.searchlib.filter.FilterHits;
import com.jaeksoft.searchlib.filter.FilterList;
import com.jaeksoft.searchlib.request.Request;
import com.jaeksoft.searchlib.result.DocumentCacheItem;
import com.jaeksoft.searchlib.result.DocumentRequestItem;
import com.jaeksoft.searchlib.result.DocumentResult;
import com.jaeksoft.searchlib.result.ResultSearch;
import com.jaeksoft.searchlib.schema.FieldList;
import com.jaeksoft.searchlib.schema.SortField;
import com.jaeksoft.searchlib.util.FileUtils;
import com.jaeksoft.searchlib.util.XPathParser;

public class ReaderLocal extends NameFilter implements ReaderInterface {

	private IndexDirectory indexDirectory;
	private IndexSearcher indexSearcher;
	private IndexReader indexReader;

	private SearchCache searchCache;
	private FilterCache filterCache;
	private DocumentCache documentCache;

	private File rootDir;

	private ReaderLocal(String name, File rootDir, File dataDir)
			throws IOException {
		super(name);
		this.rootDir = rootDir;

		this.indexDirectory = new IndexDirectory(name, dataDir);
		this.indexSearcher = new IndexSearcher(indexDirectory.getDirectory());
		this.indexReader = indexSearcher.getIndexReader();

		this.searchCache = null;
		this.filterCache = null;
		this.documentCache = null;
	}

	private void init(ReaderLocal r) {
		synchronized (this) {
			this.rootDir = r.rootDir;
			this.indexDirectory = r.indexDirectory;
			this.indexSearcher = r.indexSearcher;
			this.indexReader = r.indexReader;
		}
	}

	private void initCache(int searchCache, int filterCache, int documentCache) {
		this.searchCache = new SearchCache(searchCache);
		this.filterCache = new FilterCache(filterCache);
		this.documentCache = new DocumentCache(documentCache);
	}

	public long getVersion() {
		return indexReader.getVersion();
	}

	public TermDocs getTermDocs(String field, String term) throws IOException {
		return indexReader.termDocs(new Term(field, term));
	}

	public int getDocFreq(String field, String term) throws IOException {
		TermDocs termDocs = getTermDocs(field, term);
		int r = 0;
		while (termDocs.next())
			if (!indexReader.isDeleted(termDocs.doc()))
				r++;
		return r;
	}

	public void close() {
		synchronized (this) {
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
					indexDirectory.close();
					indexDirectory = null;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public int maxDoc() throws IOException {
		return indexSearcher.maxDoc();
	}

	public Hits search(Query query, Filter filter, Sort sort)
			throws IOException {
		if (sort == null) {
			if (filter == null)
				return indexSearcher.search(query);
			else
				return indexSearcher.search(query, filter);
		} else {
			if (filter == null)
				return indexSearcher.search(query, sort);
			else
				return indexSearcher.search(query, filter, sort);
		}
	}

	public ResultSearch search(Request request) throws IOException {
		return new ResultSearch(this, request);
	}

	public void search(Query query, Filter filter, HitCollector collector)
			throws IOException {
		if (filter == null)
			indexSearcher.search(query, collector);
		else
			indexSearcher.search(query, filter, collector);
	}

	public void deleteDocument(int docNum) throws StaleReaderException,
			CorruptIndexException, LockObtainFailedException, IOException {
		synchronized (indexReader) {
			indexReader.deleteDocument(docNum);
			flush();
		}
	}

	public Document document(int docId, FieldSelector selector)
			throws CorruptIndexException, IOException {
		synchronized (indexReader) {
			return indexReader.document(docId, selector);
		}
	}

	public StringIndex getStringIndex(String fieldName) throws IOException {
		return FieldCache.DEFAULT.getStringIndex(indexReader, fieldName);
	}

	public void xmlInfo(PrintWriter writer, HashSet<String> classDetail) {
		writer.println("<index name=\"" + indexDirectory.getName()
				+ "\" path=\"" + indexDirectory.getDirectory() + "\"/>");
	}

	private static ReaderLocal getMostRecent(String name, File rootDir,
			boolean bDeleteOld) {
		ReaderLocal reader = null;
		for (File f : rootDir.listFiles()) {
			try {
				if (f.getName().startsWith("."))
					continue;
				ReaderLocal r = new ReaderLocal(name, rootDir, f);
				if (reader == null)
					reader = r;
				else {
					ReaderLocal deleteReader = null;
					if (r.getVersion() > reader.getVersion()) {
						reader.close();
						if (bDeleteOld)
							deleteReader = reader;
						reader = r;
					} else
						deleteReader = r;
					if (deleteReader != null)
						deleteReader.indexDirectory.delete();
				}
			} catch (IOException e) {
				e.printStackTrace();
				if (bDeleteOld)
					IndexDirectory.deleteDir(f);
			}
		}
		return reader;
	}

	public static ReaderLocal fromXmlConfig(File homeDir, String indexName,
			XPathParser xpp, Node node, boolean createIfNotExists)
			throws IOException {
		String path = XPathParser.getAttributeString(node, "path");
		if (indexName == null || path == null)
			return null;

		File rootDir = new File(FileUtils.locatePath(homeDir, path));
		if (!rootDir.exists() && createIfNotExists)
			rootDir.mkdirs();
		ReaderLocal reader = ReaderLocal.getMostRecent(indexName, rootDir,
				false);

		if (reader == null) {
			if (!createIfNotExists)
				return null;
			SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
			File dataDir = new File(rootDir, df.format(new Date()));
			WriterLocal.create(dataDir);
			reader = new ReaderLocal(indexName, rootDir, dataDir);
		}

		int searchCache = XPathParser.getAttributeValue(node, "searchCache");
		int filterCache = XPathParser.getAttributeValue(node, "filterCache");
		int documentCache = XPathParser
				.getAttributeValue(node, "documentCache");
		reader.initCache(searchCache, filterCache, documentCache);
		return reader;
	}

	public Directory getDirectory() {
		synchronized (this) {
			return indexDirectory.getDirectory();
		}
	}

	public void reload(String indexName, boolean deleteOld) throws IOException {
		if (!acceptName(indexName))
			return;
		ReaderLocal newReader = ReaderLocal.getMostRecent(getName(), rootDir,
				deleteOld);
		if (newReader.getVersion() <= getVersion())
			return;
		synchronized (this) {
			close();
			init(newReader);
			searchCache.clear();
			filterCache.clear();
			documentCache.clear();
		}
	}

	public DocSetHits searchDocSet(Request request) throws IOException {
		StringBuffer cacheDshKey = new StringBuffer(request.getQuery()
				.toString());
		FilterHits filter = null;
		FilterList filterList = request.getFilterList();
		if (filterList.size() > 0) {
			String cacheFilterKey = FilterHits.toCacheKey(filterList);
			filter = filterCache.getAndPromote(cacheFilterKey);
			if (filter == null) {
				filter = new FilterHits(this, filterList);
				filterCache.put(cacheFilterKey, filter);
			}
			cacheDshKey.append('|');
			cacheDshKey.append(cacheFilterKey);
		}
		Sort sort = null;
		FieldList<SortField> sortFieldList = request.getSortFieldList();
		if (sortFieldList.size() > 0) {
			sort = SortField.getLuceneSort(sortFieldList);
			cacheDshKey.append("_");
			cacheDshKey.append(SortField.getSortKey(sortFieldList));
		}
		String cacheDshKeyStr = cacheDshKey.toString();
		DocSetHits dsh = searchCache.getAndPromote(cacheDshKeyStr);
		if (dsh == null) {
			dsh = new DocSetHits(this, request.getQuery(), filter, sort,
					request.isDelete());
			searchCache.put(cacheDshKeyStr, dsh);
		}
		return dsh;
	}

	private DocumentRequestItem document(int docId, Request request)
			throws CorruptIndexException, IOException {
		DocumentCacheItem doc = null;
		String key = DocumentCache.getKey(request.getName(), getName(), docId);
		if (key != null) {
			doc = documentCache.getAndPromote(key);
			if (doc != null)
				new DocumentRequestItem(request, doc);
		}

		Document document = document(docId, request.getDocumentFieldList());
		doc = new DocumentCacheItem(key, request, document);
		if (key != null)
			documentCache.put(key, doc);
		return new DocumentRequestItem(request, doc);
	}

	public DocumentResult documents(Request request)
			throws CorruptIndexException, IOException {
		ArrayList<Integer> docIds = request.getDocIds();
		if (docIds == null)
			return null;
		DocumentResult documentResult = new DocumentResult();
		for (int docId : docIds)
			documentResult.add(document(docId, request));
		return documentResult;
	}

	public boolean sameIndex(ReaderInterface reader) {
		if (reader == this)
			return true;
		if (reader == null)
			return true;
		return reader.sameIndex(this);
	}

	public void flush() throws IOException {
		synchronized (indexReader) {
			indexReader.flush();
		}

	}

	public IndexStatistics getStatistics() {
		return new IndexStatistics(indexReader);
	}

}
