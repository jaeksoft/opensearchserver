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

import com.jaeksoft.searchlib.ClientCatalog;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.analysis.PerFieldAnalyzer;
import com.jaeksoft.searchlib.filter.FilterAbstract;
import com.jaeksoft.searchlib.filter.FilterHits;
import com.jaeksoft.searchlib.filter.FilterListExecutor;
import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.query.ParseException;
import com.jaeksoft.searchlib.request.AbstractLocalSearchRequest;
import com.jaeksoft.searchlib.request.AbstractRequest;
import com.jaeksoft.searchlib.result.AbstractResult;
import com.jaeksoft.searchlib.schema.FieldValue;
import com.jaeksoft.searchlib.schema.FieldValueItem;
import com.jaeksoft.searchlib.schema.FieldValueOriginEnum;
import com.jaeksoft.searchlib.schema.SchemaField;
import com.jaeksoft.searchlib.spellcheck.SpellCheckCache;
import com.jaeksoft.searchlib.util.IOUtils;
import com.jaeksoft.searchlib.util.Timer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.FieldSelector;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiReader;
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
import org.apache.lucene.search.similar.MoreLikeThis;
import org.apache.lucene.search.spell.LuceneDictionary;
import org.apache.lucene.search.spell.SpellChecker;
import org.apache.lucene.store.Directory;
import org.apache.lucene.util.ReaderUtil;
import org.roaringbitmap.RoaringBitmap;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;

public class ReaderLocal extends ReaderAbstract implements ReaderInterface, Closeable {

	private final IndexDirectory indexDirectory;
	private final SpellCheckCache spellCheckCache;
	private final DocSetHitsCache docSetHitsCache;

	private final IndexSearcher indexSearcher;
	private final IndexReader indexReader;
	private final IndexReader[] indexReaders;
	private final IndexDirectory[] indexDirectories;
	private final AtomicInteger references;

	ReaderLocal(IndexConfig indexConfig, IndexDirectory indexDirectory) throws IOException, SearchLibException {
		super(indexConfig);
		spellCheckCache = new SpellCheckCache(100);
		docSetHitsCache = new DocSetHitsCache(indexConfig);
		this.indexDirectory = indexDirectory;
		references = new AtomicInteger(0);
		acquire();
		Directory directory = indexDirectory.getDirectory();
		if (directory == null)
			throw new IOException("The directory is closed");
		if (indexConfig.isMulti()) {
			List<String> indexList = indexConfig.getIndexList();
			indexDirectories = new IndexDirectory[indexList.size()];
			indexReaders = new IndexReader[indexList.size()];
			int i = 0;
			for (String indexName : indexList) {
				IndexDirectory indexDir =
						new IndexDirectory(new File(ClientCatalog.getClient(indexName).getDirectory(), "index"));
				indexDirectories[i] = indexDir;
				indexReaders[i++] = IndexReader.open(indexDir.getDirectory());
			}
			indexReader = new MultiReader(indexReaders);
		} else {
			indexReaders = null;
			indexDirectories = null;
			indexReader = IndexReader.open(directory);
		}
		indexSearcher = new IndexSearcher(indexReader);

		Similarity similarity = indexConfig.getNewSimilarityInstance();
		if (similarity != null)
			indexSearcher.setSimilarity(similarity);
	}

	void acquire() {
		references.incrementAndGet();
	}

	void release() {
		if (references.decrementAndGet() <= 0)
			doClose();
	}

	private void doClose() {
		if (indexSearcher != null) {
			IOUtils.closeQuietly(indexSearcher);
		}
		if (indexReader != null) {
			org.apache.lucene.search.FieldCache.DEFAULT.purge(indexReader);
			IOUtils.closeQuietly(indexReader);
		}
		if (indexReaders != null) {
			for (IndexReader ir : indexReaders) {
				org.apache.lucene.search.FieldCache.DEFAULT.purge(ir);
				IOUtils.closeQuietly(ir);
			}
		}
		if (indexDirectories != null) {
			for (IndexDirectory id : indexDirectories)
				id.close();
		}
	}

	@Override
	public void close() throws IOException {
		release();
	}

	@Override
	public long getVersion() {
		if (indexConfig.isMulti())
			return 0L;
		return indexReader.getVersion();
	}

	@Override
	public TermDocs getTermDocs(Term term) throws IOException {
		return indexReader.termDocs(term);
	}

	@Override
	public TermPositions getTermPositions() throws IOException {
		return indexReader.termPositions();
	}

	@Override
	public TermFreqVector getTermFreqVector(final int docId, final String field) throws IOException {
		return indexReader.getTermFreqVector(docId, field);
	}

	final public boolean isDeletedNoLock(final int docId) {
		return indexReader.isDeleted(docId);
	}

	public void putTermFreqVectors(final int[] docIds, final String field,
			final Collection<TermFreqVector> termFreqVectors) throws IOException {
		if (termFreqVectors == null || docIds == null || docIds.length == 0)
			return;
		for (int docId : docIds)
			termFreqVectors.add(indexReader.getTermFreqVector(docId, field));
	}

	@Override
	public void putTermVectors(int[] docIds, String field, Collection<String[]> termVectors) throws IOException {
		if (docIds == null || docIds.length == 0 || field == null || termVectors == null)
			return;
		List<TermFreqVector> termFreqVectors = new ArrayList<TermFreqVector>(docIds.length);
		putTermFreqVectors(docIds, field, termFreqVectors);
		for (TermFreqVector termFreqVector : termFreqVectors)
			termVectors.add(termFreqVector.getTerms());
	}

	@Override
	public int getDocFreq(Term term) throws SearchLibException {
		try {
			return indexSearcher.docFreq(term);
		} catch (IOException e) {
			throw new SearchLibException(e);
		}
	}

	@Override
	public TermEnum getTermEnum() throws SearchLibException {
		try {
			return indexReader.terms();
		} catch (IOException e) {
			throw new SearchLibException(e);
		}
	}

	@Override
	public TermEnum getTermEnum(Term term) throws SearchLibException {
		try {
			return indexReader.terms(term);
		} catch (IOException e) {
			throw new SearchLibException(e);
		}
	}

	@Override
	public Collection<?> getFieldNames() {
		return ReaderUtil.getIndexedFields(indexReader);
	}

	@Override
	public MoreLikeThis getMoreLikeThis() {
		return new MoreLikeThis(indexReader);
	}

	@Override
	public Query rewrite(Query query) throws SearchLibException {
		try {
			return query.rewrite(indexReader);
		} catch (IOException e) {
			throw new SearchLibException(e);
		}
	}

	@Override
	public int maxDoc() throws IOException {
		return indexSearcher.maxDoc();
	}

	@Override
	public int numDocs() {
		return indexReader.numDocs();
	}

	@Override
	public String explain(AbstractRequest request, int docId, boolean bHtml) throws SearchLibException {
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
		}
	}

	@Override
	public void search(Query query, Filter filter, Collector collector) throws IOException {
		if (filter == null)
			indexSearcher.search(query, collector);
		else
			indexSearcher.search(query, filter, collector);
	}

	@Override
	public FilterHits getFilterHits(SchemaField defaultField, PerFieldAnalyzer analyzer,
			AbstractLocalSearchRequest request, FilterAbstract<?> filter, Timer timer)
			throws ParseException, IOException, SearchLibException, SyntaxError {
		return filter.getFilterHits(defaultField, analyzer, request, timer);
	}

	final public Document getDocFields(final int docId, final Set<String> fieldNameSet) throws IOException {
		FieldSelector selector = new FieldSelectors.SetFieldSelector(fieldNameSet);
		return indexReader.document(docId, selector);
	}

	final public List<Document> getDocFields(final int[] docIds, final Set<String> fieldNameSet) throws IOException {
		if (docIds == null || docIds.length == 0)
			return null;
		List<Document> documents = new ArrayList<Document>(docIds.length);
		FieldSelector selector = new FieldSelectors.SetFieldSelector(fieldNameSet);
		for (int docId : docIds)
			documents.add(indexReader.document(docId, selector));
		return documents;

	}

	final private StringIndex getStringIndexNoLock(String fieldName) throws IOException {
		return org.apache.lucene.search.FieldCache.DEFAULT.getStringIndex(indexReader, fieldName);
	}

	@Override
	final public FieldCacheIndex getStringIndex(final String fieldName) throws IOException {
		StringIndex si = getStringIndexNoLock(fieldName);
		return new FieldCacheIndex(si.lookup, si.order);
	}

	@Override
	public String[] getDocTerms(final String fieldName) throws SearchLibException, IOException {
		StringIndex si = getStringIndexNoLock(fieldName);
		RoaringBitmap bitSet = new RoaringBitmap();
		for (int doc = 0; doc < si.order.length; doc++) {
			if (!indexReader.isDeleted(doc)) {
				bitSet.add(si.order[doc]);
			}
		}
		String[] result = new String[bitSet.getCardinality()];
		int i = 0;
		int j = 0;
		for (String term : si.lookup)
			if (bitSet.contains(i++))
				result[j++] = term;
		return result;
	}

	public LuceneDictionary getLuceneDirectionary(String fieldName) {
		return new LuceneDictionary(indexReader, fieldName);
	}

	public void xmlInfo(PrintWriter writer) {
		writer.println("<index  path=\"" + indexDirectory.getDirectory() + "\"/>");
	}

	@Override
	public DocSetHits searchDocSet(AbstractLocalSearchRequest searchRequest, Timer timer)
			throws IOException, ParseException, SyntaxError, SearchLibException {
		try {
			FilterHits filterHits = new FilterListExecutor(searchRequest, timer).getFilterHits();
			DocSetHits dsh = new DocSetHits(this, searchRequest, filterHits);
			return docSetHitsCache.getAndJoin(dsh, timer);
		} catch (Exception e) {
			throw new SearchLibException(e);
		}
	}

	@Override
	final public LinkedHashMap<String, FieldValue> getDocumentStoredField(final int docId) throws IOException {
		LinkedHashMap<String, FieldValue> documentFields = new LinkedHashMap<String, FieldValue>();
		Document doc = indexReader.document(docId, FieldSelectors.LoadFieldSelector.INSTANCE);
		String currentFieldName = null;
		FieldValue currentFieldValue = null;
		for (Fieldable field : doc.getFields()) {
			if (!field.isStored())
				continue;
			FieldValue fieldValue = null;
			String fieldName = field.name();
			if (currentFieldName != null && currentFieldName.equals(fieldName))
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
			currentFieldValue.addValue(new FieldValueItem(FieldValueOriginEnum.STORAGE, field.stringValue()));
		}
		return documentFields;
	}

	@Override
	final public LinkedHashMap<String, FieldValue> getDocumentFields(final int docId,
			final LinkedHashSet<String> fieldNameSet, final Timer timer)
			throws IOException, ParseException, SyntaxError {

		LinkedHashMap<String, FieldValue> documentFields = new LinkedHashMap<String, FieldValue>();
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
					FieldValueItem[] valueItems = FieldValueItem.buildArray(fieldables);
					documentFields.put(fieldName, new FieldValue(fieldName, valueItems));
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
					FieldValueItem[] valueItems =
							FieldValueItem.buildArray(FieldValueOriginEnum.TERM_VECTOR, tfv.getTerms());
					documentFields.put(fieldName, new FieldValue(fieldName, valueItems));
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
						FieldValueItem[] valueItems =
								FieldValueItem.buildArray(FieldValueOriginEnum.STRING_INDEX, term);
						documentFields.put(fieldName, new FieldValue(fieldName, valueItems));
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
	}

	public Set<FieldValue> getTermsVectorFields(int docId, Set<String> fieldNameSet) throws IOException {
		Set<FieldValue> fieldValueList = new HashSet<FieldValue>();
		for (String fieldName : fieldNameSet) {
			TermFreqVector termFreqVector = indexReader.getTermFreqVector(docId, fieldName);
			if (termFreqVector == null)
				continue;
			String[] terms = termFreqVector.getTerms();
			if (terms == null)
				continue;
			FieldValueItem[] fieldValueItem = new FieldValueItem[terms.length];
			int i = 0;
			for (String term : terms)
				fieldValueItem[i++] = new FieldValueItem(FieldValueOriginEnum.TERM_VECTOR, term);
			fieldValueList.add(new FieldValue(fieldName, fieldValueItem));
		}
		return fieldValueList;
	}

	public Set<FieldValue> getTerms(int docId, Set<String> fieldNameSet) throws IOException {
		TermPositions termPosition = indexReader.termPositions();
		Set<FieldValue> fieldValueSet = new HashSet<FieldValue>();
		for (String fieldName : fieldNameSet) {
			List<FieldValueItem> fieldValueItemList = new ArrayList<FieldValueItem>();
			TermEnum termEnum = indexReader.terms(new Term(fieldName, ""));
			if (termEnum == null)
				continue;
			Term term = termEnum.term();
			if (!term.field().equals(fieldName))
				continue;
			do {
				term = termEnum.term();
				if (!term.field().equals(fieldName))
					break;
				termPosition.seek(term);
				if (!termPosition.skipTo(docId) || termPosition.doc() != docId)
					continue;
				fieldValueItemList.add(new FieldValueItem(FieldValueOriginEnum.TERM_ENUM, term.text()));
			} while (termEnum.next());
			termEnum.close();
			if (fieldValueItemList.size() > 0)
				fieldValueSet.add(new FieldValue(fieldName, fieldValueItemList));
		}
		return fieldValueSet;
	}

	@Override
	public boolean sameIndex(ReaderInterface reader) {
		if (reader == this)
			return true;
		if (reader == null)
			return true;
		return reader.sameIndex(this);
	}

	@Override
	public IndexStatistics getStatistics() {
		return new IndexStatistics(indexReader);
	}

	public SpellChecker getSpellChecker(String fieldName) throws IOException, SearchLibException {
		return spellCheckCache.get(this, fieldName);
	}

	protected DocSetHitsCache getDocSetHitsCache() {
		return docSetHitsCache;
	}

	@Override
	public AbstractResult<?> request(AbstractRequest request) throws SearchLibException {
		return request.execute(this);
	}

}
