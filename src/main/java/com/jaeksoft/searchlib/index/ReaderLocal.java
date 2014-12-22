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

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.FieldSelector;
import org.apache.lucene.document.Fieldable;
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
import com.jaeksoft.searchlib.cache.SpellCheckerCache;
import com.jaeksoft.searchlib.filter.FilterAbstract;
import com.jaeksoft.searchlib.filter.FilterHits;
import com.jaeksoft.searchlib.filter.FilterList;
import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.index.DocSetHits.Params;
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
import com.jaeksoft.searchlib.util.bitset.BitSetFactory;
import com.jaeksoft.searchlib.util.bitset.BitSetInterface;

public class ReaderLocal extends ReaderAbstract {

	final private ReadWriteLock rwl = new ReadWriteLock();

	private final IndexDirectory indexDirectory;
	private IndexSearcher indexSearcher;
	private IndexReader indexReader;

	private SpellCheckerCache spellCheckerCache;

	public ReaderLocal(IndexConfig indexConfig, IndexDirectory indexDirectory,
			boolean bOnline) throws IOException, SearchLibException {
		super(indexConfig);
		indexSearcher = null;
		indexReader = null;
		this.indexDirectory = indexDirectory;
		if (bOnline)
			openNoLock();
	}

	private void openNoLock() throws IOException, SearchLibException {
		if (this.indexReader != null && this.indexSearcher != null)
			return;
		this.indexReader = IndexReader.open(indexDirectory.getDirectory());
		indexSearcher = new IndexSearcher(indexReader);

		Similarity similarity = indexConfig.getNewSimilarityInstance();
		if (similarity != null)
			indexSearcher.setSimilarity(similarity);
		// TODO replace value 100 by number of field in schema
		this.spellCheckerCache = new SpellCheckerCache(100);
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
	public TermPositions getTermPositions() throws IOException {
		rwl.r.lock();
		try {
			return indexReader.termPositions();
		} finally {
			rwl.r.unlock();
		}
	}

	@Override
	public TermFreqVector getTermFreqVector(final int docId, final String field)
			throws IOException {
		rwl.r.lock();
		try {
			return indexReader.getTermFreqVector(docId, field);
		} finally {
			rwl.r.unlock();
		}
	}

	final public boolean isDeletedNoLock(final int docId) {
		return indexReader.isDeleted(docId);
	}

	public void putTermFreqVectors(final int[] docIds, final String field,
			final Collection<TermFreqVector> termFreqVectors)
			throws IOException {
		if (termFreqVectors == null || docIds == null || docIds.length == 0)
			return;
		rwl.r.lock();
		try {
			for (int docId : docIds)
				termFreqVectors
						.add(indexReader.getTermFreqVector(docId, field));
		} finally {
			rwl.r.unlock();
		}
	}

	@Override
	public void putTermVectors(int[] docIds, String field,
			Collection<String[]> termVectors) throws IOException {
		if (docIds == null || docIds.length == 0 || field == null
				|| termVectors == null)
			return;
		rwl.r.lock();
		try {
			List<TermFreqVector> termFreqVectors = new ArrayList<TermFreqVector>(
					docIds.length);
			putTermFreqVectors(docIds, field, termFreqVectors);
			for (TermFreqVector termFreqVector : termFreqVectors)
				termVectors.add(termFreqVector.getTerms());
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
	public final long maxDoc() throws IOException {
		rwl.r.lock();
		try {
			return indexSearcher.maxDoc();
		} finally {
			rwl.r.unlock();
		}
	}

	@Override
	public final long numDocs() {
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
			IOException, SearchLibException, SyntaxError {
		rwl.r.lock();
		try {
			return filter.getFilterHits(defaultField, analyzer, request, timer);
		} finally {
			rwl.r.unlock();
		}
	}

	final public Document getDocFields(final int docId,
			final Set<String> fieldNameSet) throws IOException {
		rwl.r.lock();
		try {
			FieldSelector selector = new FieldSelectors.SetFieldSelector(
					fieldNameSet);
			return indexReader.document(docId, selector);
		} catch (IllegalArgumentException e) {
			throw e;
		} finally {
			rwl.r.unlock();
		}
	}

	final public List<Document> getDocFields(final int[] docIds,
			final Set<String> fieldNameSet) throws IOException {
		if (docIds == null || docIds.length == 0)
			return null;
		List<Document> documents = new ArrayList<Document>(docIds.length);
		rwl.r.lock();
		try {
			FieldSelector selector = new FieldSelectors.SetFieldSelector(
					fieldNameSet);
			for (int docId : docIds)
				documents.add(indexReader.document(docId, selector));
			return documents;
		} catch (IllegalArgumentException e) {
			throw e;
		} finally {
			rwl.r.unlock();
		}
	}

	final private StringIndex getStringIndexNoLock(String fieldName)
			throws IOException {
		return org.apache.lucene.search.FieldCache.DEFAULT.getStringIndex(
				indexReader, fieldName);
	}

	@Override
	final public FieldCacheIndex getStringIndex(final String fieldName)
			throws IOException {
		rwl.r.lock();
		try {
			StringIndex si = getStringIndexNoLock(fieldName);
			return new FieldCacheIndex(indexReader.getVersion(), si.lookup,
					si.order);
		} finally {
			rwl.r.unlock();
		}
	}

	@Override
	public String[] getDocTerms(final String fieldName)
			throws SearchLibException, IOException {
		rwl.r.lock();
		try {
			StringIndex si = getStringIndexNoLock(fieldName);
			BitSetInterface bitSet = BitSetFactory.INSTANCE
					.newInstance(si.order.length);
			for (int doc = 0; doc < si.order.length; doc++) {
				if (!indexReader.isDeleted(doc)) {
					bitSet.set(si.order[doc]);
				}
			}
			String[] result = new String[(int) bitSet.cardinality()];
			int i = 0;
			int j = 0;
			for (String term : si.lookup)
				if (bitSet.get(i++))
					result[j++] = term;
			return result;
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
			spellCheckerCache.clear();
			openNoLock();
		} catch (IOException e) {
			throw new SearchLibException(e);
		} finally {
			rwl.w.unlock();
		}
	}

	@Override
	public DocSetHits searchDocSet(AbstractSearchRequest searchRequest,
			Timer timer) throws IOException, ParseException, SyntaxError,
			SearchLibException {
		rwl.r.lock();
		try {
			Schema schema = searchRequest.getConfig().getSchema();
			SchemaField defaultField = schema.getFieldList().getDefaultField();
			PerFieldAnalyzer analyzer = searchRequest.getAnalyzer();
			FilterList filterList = searchRequest.getFilterList();
			FilterHits filterHits = filterList == null ? null
					: filterList.getFilterHits(defaultField, analyzer,
							searchRequest, timer);
			return new DocSetHits(new Params(this, searchRequest, filterHits),
					timer);
		} finally {
			rwl.r.unlock();
		}
	}

	@Override
	final public Map<String, FieldValue> getDocumentStoredField(final int docId)
			throws IOException {
		rwl.r.lock();
		try {
			Map<String, FieldValue> documentFields = new TreeMap<String, FieldValue>();
			Document doc = indexReader.document(docId,
					FieldSelectors.LoadFieldSelector.INSTANCE);
			String currentFieldName = null;
			FieldValue currentFieldValue = null;
			for (Fieldable field : doc.getFields()) {
				if (!field.isStored())
					continue;
				FieldValue fieldValue = null;
				String fieldName = field.name();
				if (currentFieldName != null
						&& currentFieldName.equals(fieldName))
					fieldValue = currentFieldValue;
				else {
					fieldValue = documentFields.get(fieldName);
					if (fieldValue == null) {
						fieldValue = new FieldValue(fieldName);
						documentFields.put(fieldName, fieldValue);
					}
					currentFieldName = fieldName;
					currentFieldValue = fieldValue;
				}
				currentFieldValue.addValue(new FieldValueItem(
						FieldValueOriginEnum.STORAGE, field.stringValue()));
			}
			return documentFields;
		} catch (IllegalArgumentException e) {
			throw e;
		} finally {
			rwl.r.unlock();
		}
	}

	@Override
	final public Map<String, FieldValue> getDocumentFields(final int docId,
			final Set<String> fieldNameSet, final Timer timer)
			throws IOException, ParseException, SyntaxError {
		rwl.r.lock();
		try {

			Map<String, FieldValue> documentFields = new TreeMap<String, FieldValue>();
			Set<String> vectorField = null;
			Set<String> indexedField = null;
			Set<String> missingField = null;

			Timer t = new Timer(timer, "Field from store");

			// Check missing fields from store
			if (fieldNameSet != null && fieldNameSet.size() > 0) {
				vectorField = new TreeSet<String>();
				Document document = getDocFields(docId, fieldNameSet);
				for (String fieldName : fieldNameSet) {
					Fieldable[] fieldables = document.getFieldables(fieldName);
					if (fieldables != null && fieldables.length > 0) {
						FieldValueItem[] valueItems = FieldValueItem
								.buildArray(fieldables);
						documentFields.put(fieldName, new FieldValue(fieldName,
								valueItems));
					} else
						vectorField.add(fieldName);
				}
			}

			t.end(null);

			t = new Timer(timer, "Field from vector");

			// Check missing fields from vector
			if (vectorField != null && vectorField.size() > 0) {
				indexedField = new TreeSet<String>();
				for (String fieldName : vectorField) {
					TermFreqVector tfv = getTermFreqVector(docId, fieldName);
					if (tfv != null) {
						FieldValueItem[] valueItems = FieldValueItem
								.buildArray(FieldValueOriginEnum.TERM_VECTOR,
										tfv.getTerms());
						documentFields.put(fieldName, new FieldValue(fieldName,
								valueItems));
					} else
						indexedField.add(fieldName);
				}
			}

			t.end(null);

			t = new Timer(timer, "Field from StringIndex");

			// Check missing fields from StringIndex
			if (indexedField != null && indexedField.size() > 0) {
				missingField = new TreeSet<String>();
				for (String fieldName : indexedField) {
					FieldCacheIndex stringIndex = getStringIndex(fieldName);
					if (stringIndex != null) {
						String term = stringIndex.lookup[stringIndex.order[docId]];
						if (term != null) {
							FieldValueItem[] valueItems = FieldValueItem
									.buildArray(
											FieldValueOriginEnum.STRING_INDEX,
											term);
							documentFields.put(fieldName, new FieldValue(
									fieldName, valueItems));
							continue;
						}
					}
					missingField.add(fieldName);
				}
			}

			t.end(null);

			if (missingField != null && missingField.size() > 0)
				for (String fieldName : missingField)
					documentFields.put(fieldName, new FieldValue(fieldName));
			return documentFields;
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

	public SpellChecker getSpellChecker(String fieldName) throws IOException,
			SearchLibException {
		rwl.r.lock();
		try {
			return spellCheckerCache.get(this, fieldName);
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
