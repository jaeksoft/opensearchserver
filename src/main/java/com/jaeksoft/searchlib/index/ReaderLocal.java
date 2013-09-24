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

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.FieldSelector;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.index.TermFreqVector;
import org.apache.lucene.index.TermPositions;
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
import org.apache.lucene.util.ReaderUtil;

import com.jaeksoft.searchlib.Logging;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.analysis.PerFieldAnalyzer;
import com.jaeksoft.searchlib.cache.FieldCache;
import com.jaeksoft.searchlib.cache.FilterCache;
import com.jaeksoft.searchlib.cache.SearchCache;
import com.jaeksoft.searchlib.cache.SpellCheckerCache;
import com.jaeksoft.searchlib.filter.FilterAbstract;
import com.jaeksoft.searchlib.filter.FilterHits;
import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.query.ParseException;
import com.jaeksoft.searchlib.request.AbstractRequest;
import com.jaeksoft.searchlib.request.AbstractSearchRequest;
import com.jaeksoft.searchlib.result.AbstractResult;
import com.jaeksoft.searchlib.schema.FieldValue;
import com.jaeksoft.searchlib.schema.FieldValueItem;
import com.jaeksoft.searchlib.schema.FieldValueOriginEnum;
import com.jaeksoft.searchlib.schema.Schema;
import com.jaeksoft.searchlib.schema.SchemaField;
import com.jaeksoft.searchlib.util.ReadWriteLock;
import com.jaeksoft.searchlib.util.Timer;

public class ReaderLocal extends ReaderAbstract {

	final private ReadWriteLock rwl = new ReadWriteLock();

	private final IndexDirectory indexDirectory;
	private IndexSearcher indexSearcher;
	private IndexReader indexReader;

	private SearchCache searchCache;
	private FilterCache filterCache;
	private FieldCache fieldCache;
	private SpellCheckerCache spellCheckerCache;

	private String similarityClass;

	public ReaderLocal(IndexConfig indexConfig, IndexDirectory indexDirectory)
			throws IOException, InstantiationException, IllegalAccessException,
			ClassNotFoundException {
		super(indexConfig);
		indexSearcher = null;
		indexReader = null;
		this.similarityClass = indexConfig.getSimilarityClass();
		this.indexDirectory = indexDirectory;
		init();
	}

	private void init() throws InstantiationException, IllegalAccessException,
			ClassNotFoundException, IOException {
		this.indexReader = IndexReader.open(indexDirectory.getDirectory());
		indexSearcher = new IndexSearcher(indexReader);
		if (similarityClass != null) {
			Similarity similarity = (Similarity) Class.forName(similarityClass)
					.newInstance();
			indexSearcher.setSimilarity(similarity);
		}
		this.searchCache = new SearchCache(indexConfig);
		this.filterCache = new FilterCache(indexConfig);
		this.fieldCache = new FieldCache(indexConfig);
		// TODO replace value 100 by number of field in schema
		this.spellCheckerCache = new SpellCheckerCache(100);
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

	@Override
	public long getVersion() {
		rwl.r.lock();
		try {
			return indexReader.getVersion();
		} finally {
			rwl.r.unlock();
		}
	}

	@Override
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
			throws IOException {
		rwl.r.lock();
		try {
			return indexReader.getTermFreqVector(docId, field);
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
	public TermEnum getTermEnum(Term term) throws SearchLibException {
		rwl.r.lock();
		try {
			return indexReader.terms(term);
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
			return ReaderUtil.getIndexedFields(indexReader);
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

	private void closeNoLock() throws IOException {
		if (indexSearcher != null) {
			indexSearcher.close();
			indexSearcher = null;
		}
		if (indexReader != null) {
			org.apache.lucene.search.FieldCache.DEFAULT.purge(indexReader);
			indexReader.close();
			indexReader = null;
		}
	}

	@Override
	public void close() {
		rwl.w.lock();
		try {
			closeNoLock();
		} catch (IOException e) {
			Logging.warn(e.getMessage(), e);
		} finally {
			rwl.w.unlock();
		}
	}

	@Override
	public final int maxDoc() throws IOException {
		rwl.r.lock();
		try {
			return indexSearcher.maxDoc();
		} finally {
			rwl.r.unlock();
		}
	}

	@Override
	public final int numDocs() {
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
	public String explain(AbstractRequest request, int docId, boolean bHtml)
			throws SearchLibException {
		rwl.r.lock();
		try {
			Query query = request.getQuery();
			if (query == null)
				return "No explanation available";
			Explanation explanation = indexSearcher.explain(query, docId);
			if (bHtml)
				return explanation.toHtml();
			else
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

	@Override
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

	@Override
	public FilterHits getFilterHits(SchemaField defaultField,
			PerFieldAnalyzer analyzer, AbstractSearchRequest request,
			FilterAbstract<?> filter, Timer timer) throws ParseException,
			IOException {
		rwl.r.lock();
		try {
			return filterCache.get(this, filter, defaultField, analyzer,
					request, timer);
		} finally {
			rwl.r.unlock();
		}
	}

	public Document getDocFields(int docId, Set<String> fieldNameSet)
			throws IOException {
		rwl.r.lock();
		try {
			FieldSelector selector = new SetFieldSelector(fieldNameSet);
			return indexReader.document(docId, selector);
		} catch (IllegalArgumentException e) {
			throw e;
		} finally {
			rwl.r.unlock();
		}
	}

	@Override
	public FieldCacheIndex getStringIndex(String fieldName) throws IOException {
		rwl.r.lock();
		try {
			StringIndex si = org.apache.lucene.search.FieldCache.DEFAULT
					.getStringIndex(indexReader, fieldName);
			return new FieldCacheIndex(indexReader.getVersion(), si.lookup,
					si.order);
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
			writer.println("<index  path=\"" + indexDirectory.getDirectory()
					+ "\"/>");
		} finally {
			rwl.r.unlock();
		}
	}

	@Override
	public void reload() throws SearchLibException {
		rwl.w.lock();
		try {
			closeNoLock();
			init();
			resetCache();
		} catch (IOException e) {
			throw new SearchLibException(e);
		} catch (InstantiationException e) {
			throw new SearchLibException(e);
		} catch (IllegalAccessException e) {
			throw new SearchLibException(e);
		} catch (ClassNotFoundException e) {
			throw new SearchLibException(e);
		} finally {
			rwl.w.unlock();
		}
	}

	public DocSetHits searchDocSet(AbstractSearchRequest searchRequest,
			Timer timer) throws IOException, ParseException, SyntaxError,
			SearchLibException, InstantiationException, IllegalAccessException,
			ClassNotFoundException {
		rwl.r.lock();
		try {

			Schema schema = searchRequest.getConfig().getSchema();
			SchemaField defaultField = schema.getFieldList().getDefaultField();

			return searchCache.get(this, searchRequest, schema, defaultField,
					timer);

		} finally {
			rwl.r.unlock();
		}
	}

	public DocSetHits newDocSetHits(AbstractSearchRequest searchRequest,
			Schema schema, SchemaField defaultField, PerFieldAnalyzer analyzer,
			Timer timer) throws IOException, ParseException, SyntaxError,
			InstantiationException, IllegalAccessException,
			ClassNotFoundException, SearchLibException {

		FilterHits filterHits = searchRequest.getFilterList().getFilterHits(
				this, defaultField, analyzer, searchRequest, timer);

		DocSetHits dsh = new DocSetHits(this, searchRequest.getQuery(),
				filterHits, searchRequest.getSortFieldList(), timer);
		return dsh;
	}

	@Override
	public Map<String, FieldValue> getDocumentFields(int docId,
			Set<String> fieldNameSet, Timer timer) throws IOException,
			ParseException, SyntaxError {
		rwl.r.lock();
		try {
			return fieldCache.get(this, docId, fieldNameSet, timer);
		} finally {
			rwl.r.unlock();
		}
	}

	public Set<FieldValue> getTermsVectorFields(int docId,
			Set<String> fieldNameSet) throws IOException {
		rwl.r.lock();
		try {
			Set<FieldValue> fieldValueList = new HashSet<FieldValue>();
			for (String fieldName : fieldNameSet) {
				TermFreqVector termFreqVector = indexReader.getTermFreqVector(
						docId, fieldName);
				if (termFreqVector == null)
					continue;
				String[] terms = termFreqVector.getTerms();
				if (terms == null)
					continue;
				FieldValueItem[] fieldValueItem = new FieldValueItem[terms.length];
				int i = 0;
				for (String term : terms)
					fieldValueItem[i++] = new FieldValueItem(
							FieldValueOriginEnum.TERM_VECTOR, term);
				fieldValueList.add(new FieldValue(fieldName, fieldValueItem));
			}
			return fieldValueList;
		} finally {
			rwl.r.unlock();
		}
	}

	public Set<FieldValue> getTerms(int docId, Set<String> fieldNameSet)
			throws IOException {
		rwl.r.lock();
		try {
			TermPositions termPosition = indexReader.termPositions();
			Set<FieldValue> fieldValueSet = new HashSet<FieldValue>();
			for (String fieldName : fieldNameSet) {
				List<FieldValueItem> fieldValueItemList = new ArrayList<FieldValueItem>();
				TermEnum termEnum = indexReader.terms(new Term(fieldName, ""));
				Term term = termEnum.term();
				if (termEnum == null || !term.field().equals(fieldName))
					continue;
				do {
					term = termEnum.term();
					if (!term.field().equals(fieldName))
						break;
					termPosition.seek(term);
					if (!termPosition.skipTo(docId)
							|| termPosition.doc() != docId)
						continue;
					fieldValueItemList.add(new FieldValueItem(
							FieldValueOriginEnum.TERM_ENUM, term.text()));
				} while (termEnum.next());
				termEnum.close();
				if (fieldValueItemList.size() > 0)
					fieldValueSet.add(new FieldValue(fieldName,
							fieldValueItemList));
			}
			return fieldValueSet;
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

	@Override
	public AbstractResult<?> request(AbstractRequest request)
			throws SearchLibException {
		rwl.r.lock();
		try {
			return request.execute(this);
		} finally {
			rwl.r.unlock();
		}
	}

}
