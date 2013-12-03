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
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.SerialMergeScheduler;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Similarity;
import org.apache.lucene.store.AlreadyClosedException;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.util.Version;

import com.jaeksoft.searchlib.ClientFactory;
import com.jaeksoft.searchlib.Logging;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.SearchLibException.UniqueKeyMissing;
import com.jaeksoft.searchlib.analysis.Analyzer;
import com.jaeksoft.searchlib.analysis.CompiledAnalyzer;
import com.jaeksoft.searchlib.analysis.LanguageEnum;
import com.jaeksoft.searchlib.analysis.PerFieldAnalyzer;
import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.query.ParseException;
import com.jaeksoft.searchlib.request.AbstractSearchRequest;
import com.jaeksoft.searchlib.schema.FieldValueItem;
import com.jaeksoft.searchlib.schema.Schema;
import com.jaeksoft.searchlib.schema.SchemaField;
import com.jaeksoft.searchlib.util.SimpleLock;

public class WriterLucene extends WriterAbstract {

	private final SimpleLock lock = new SimpleLock();

	private final IndexDirectory indexDirectory;

	private final IndexLucene indexLucene;

	protected WriterLucene(IndexConfig indexConfig, IndexLucene indexLucene,
			IndexDirectory indexDirectory) throws IOException {
		super(indexConfig);
		this.indexLucene = indexLucene;
		this.indexDirectory = indexDirectory;
	}

	public IndexWriter close(IndexWriter indexWriter) {
		if (indexWriter == null)
			return null;
		try {
			indexWriter.close();
			return null;
		} catch (AlreadyClosedException e) {
			Logging.warn(e.getMessage(), e);
			return null;
		} catch (IOException e) {
			Logging.warn(e.getMessage(), e);
			return indexWriter;
		} finally {
			indexDirectory.unlock();
		}
	}

	@Override
	public void close() {
	}

	public final void create() throws CorruptIndexException,
			LockObtainFailedException, IOException, SearchLibException {
		IndexWriter indexWriter = null;
		lock.rl.lock();
		try {
			indexWriter = open(true);
			indexWriter = close(indexWriter);
		} finally {
			lock.rl.unlock();
			close(indexWriter);
		}
	}

	private final IndexWriter open(boolean create)
			throws CorruptIndexException, LockObtainFailedException,
			IOException, SearchLibException {
		IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_36,
				null);
		config.setOpenMode(create ? OpenMode.CREATE_OR_APPEND : OpenMode.APPEND);
		config.setMergeScheduler(new SerialMergeScheduler());
		Similarity similarity = indexConfig.getNewSimilarityInstance();
		if (similarity != null)
			config.setSimilarity(similarity);
		Logging.debug("WriteLocal open " + indexDirectory.getDirectory());
		return new IndexWriter(indexDirectory.getDirectory(), config);
	}

	private IndexWriter open() throws IOException, SearchLibException {
		IndexWriter indexWriter = open(false);
		return indexWriter;
	}

	@Deprecated
	public void addDocument(Document document) throws IOException,
			SearchLibException {
		IndexWriter indexWriter = null;
		lock.rl.lock();
		try {
			indexWriter = open();
			indexWriter.addDocument(document);
			indexWriter = close(indexWriter);
		} finally {
			lock.rl.unlock();
			close(indexWriter);
		}
	}

	private boolean updateDocNoLock(SchemaField uniqueField,
			IndexWriter indexWriter, Schema schema, IndexDocument document)
			throws IOException, NoSuchAlgorithmException, SearchLibException {
		if (!acceptDocument(document))
			return false;

		if (beforeUpdateList != null)
			for (BeforeUpdateInterface beforeUpdate : beforeUpdateList)
				beforeUpdate.update(schema, document);

		Document doc = getLuceneDocument(schema, document);
		PerFieldAnalyzer pfa = schema.getIndexPerFieldAnalyzer(document
				.getLang());

		if (uniqueField != null) {
			String uniqueFieldName = uniqueField.getName();
			String uniqueFieldValue = doc.get(uniqueFieldName);
			if (uniqueFieldValue == null)
				throw new UniqueKeyMissing(uniqueFieldName);
			indexWriter.updateDocument(new Term(uniqueFieldName,
					uniqueFieldValue), doc, pfa);
		} else
			indexWriter.addDocument(doc, pfa);
		return true;
	}

	private boolean updateDocumentNoLock(Schema schema, IndexDocument document)
			throws SearchLibException {
		IndexWriter indexWriter = null;
		try {
			indexWriter = open();
			SchemaField uniqueField = schema.getFieldList().getUniqueField();
			boolean updated = updateDocNoLock(uniqueField, indexWriter, schema,
					document);
			indexWriter = close(indexWriter);
			if (updated)
				indexLucene.reload();
			return updated;
		} catch (IOException e) {
			throw new SearchLibException(e);
		} catch (NoSuchAlgorithmException e) {
			throw new SearchLibException(e);
		} finally {
			close(indexWriter);
		}
	}

	@Override
	public boolean updateDocument(Schema schema, IndexDocument document)
			throws SearchLibException {
		lock.rl.lock();
		try {
			return updateDocumentNoLock(schema, document);
		} finally {
			lock.rl.unlock();
		}
	}

	private int updateDocumentsNoLock(Schema schema,
			Collection<IndexDocument> documents) throws SearchLibException {
		IndexWriter indexWriter = null;
		try {
			int count = 0;
			indexWriter = open();
			SchemaField uniqueField = schema.getFieldList().getUniqueField();
			for (IndexDocument document : documents)
				if (updateDocNoLock(uniqueField, indexWriter, schema, document))
					count++;
			indexWriter = close(indexWriter);
			if (count > 0)
				indexLucene.reload();
			return count;
		} catch (IOException e) {
			throw new SearchLibException(e);
		} catch (NoSuchAlgorithmException e) {
			throw new SearchLibException(e);
		} finally {
			close(indexWriter);
		}
	}

	@Override
	public int updateDocuments(Schema schema,
			Collection<IndexDocument> documents) throws SearchLibException {
		lock.rl.lock();
		try {
			return updateDocumentsNoLock(schema, documents);
		} finally {
			lock.rl.unlock();
		}
	}

	private static Document getLuceneDocument(Schema schema,
			IndexDocument document) throws IOException, SearchLibException {
		schema.getIndexPerFieldAnalyzer(document.getLang());
		Document doc = new Document();
		LanguageEnum lang = document.getLang();
		for (FieldContent fieldContent : document) {
			String fieldName = fieldContent.getField();
			SchemaField field = schema.getFieldList().get(fieldName);
			if (field != null) {
				Analyzer analyzer = schema.getAnalyzer(field, lang);
				@SuppressWarnings("resource")
				CompiledAnalyzer compiledAnalyzer = (analyzer == null) ? null
						: analyzer.getIndexAnalyzer();
				for (FieldValueItem valueItem : fieldContent.getValues()) {
					if (valueItem == null)
						continue;
					String value = valueItem.getValue();
					if (value == null)
						continue;
					if (compiledAnalyzer != null)
						if (!compiledAnalyzer.isAnyToken(fieldName, value))
							continue;
					doc.add(field.getLuceneField(value, valueItem.getBoost()));
				}
			}
		}
		return doc;
	}

	private void optimizeNoLock() throws SearchLibException {
		IndexWriter indexWriter = null;
		try {
			indexWriter = open();
			indexWriter.optimize(true);
			indexWriter = close(indexWriter);
		} catch (IOException e) {
			throw new SearchLibException(e);
		} finally {
			close(indexWriter);
		}
	}

	@Override
	public void optimize() throws SearchLibException {
		lock.rl.lock();
		try {
			setOptimizing(true);
			optimizeNoLock();
		} finally {
			setOptimizing(false);
			lock.rl.unlock();
		}
	}

	private int deleteDocumentNoLock(String field, String value)
			throws SearchLibException {
		IndexWriter indexWriter = null;
		try {
			int l = indexLucene.getStatistics().getNumDocs();
			indexWriter = open();
			indexWriter.deleteDocuments(new Term(field, value));
			indexWriter = close(indexWriter);
			indexLucene.reload();
			l = l - indexLucene.getStatistics().getNumDocs();
			return l;
		} catch (IOException e) {
			throw new SearchLibException(e);
		} finally {
			close(indexWriter);
		}
	}

	private String getSchemaFieldOrUnique(Schema schema, String field) {
		SchemaField schemaField = field != null ? schema.getFieldList().get(
				field) : schema.getFieldList().getUniqueField();
		return schemaField == null ? null : schemaField.getName();
	}

	@Override
	public int deleteDocument(Schema schema, String field, String value)
			throws SearchLibException {
		field = getSchemaFieldOrUnique(schema, field);
		if (field == null)
			return 0;
		lock.rl.lock();
		try {
			return deleteDocumentNoLock(field, value);
		} finally {
			lock.rl.unlock();
		}
	}

	private int deleteDocumentsNoLock(Schema schema, Term[] terms)
			throws SearchLibException {
		IndexWriter indexWriter = null;
		try {
			int l = indexLucene.getStatistics().getNumDocs();
			indexWriter = open();
			indexWriter.deleteDocuments(terms);
			indexWriter = close(indexWriter);
			if (terms.length > 0)
				indexLucene.reload();
			l = l - indexLucene.getStatistics().getNumDocs();
			return l;
		} catch (IOException e) {
			throw new SearchLibException(e);
		} finally {
			close(indexWriter);
		}
	}

	@Override
	public int deleteDocuments(Schema schema, String field,
			Collection<String> values) throws SearchLibException {
		field = getSchemaFieldOrUnique(schema, field);
		if (field == null)
			return 0;
		int countNonNullValues = 0;
		for (String value : values)
			if (value != null)
				countNonNullValues++;
		if (countNonNullValues == 0)
			return 0;
		int maxClauseCount = ClientFactory.INSTANCE
				.getBooleanQueryMaxClauseCount().getValue();

		Iterator<String> valueIterator = values.iterator();

		int count = 0;
		while (valueIterator.hasNext()) {
			List<String> termList = new ArrayList<String>();
			while (valueIterator.hasNext() && termList.size() < maxClauseCount) {
				String value = valueIterator.next();
				if (value == null)
					continue;
				termList.add(value);
			}
			if (termList.size() == 0)
				continue;
			Term[] terms = new Term[termList.size()];
			int i = 0;
			for (String value : termList)
				terms[i++] = new Term(field, value);
			lock.rl.lock();
			try {
				count += deleteDocumentsNoLock(schema, terms);
			} finally {
				lock.rl.unlock();
			}
		}
		return count;
	}

	private int deleteDocumentsNoLock(AbstractSearchRequest query)
			throws SearchLibException {
		IndexWriter indexWriter = null;
		try {
			int l = indexLucene.getStatistics().getNumDocs();
			indexWriter = open();
			indexWriter.deleteDocuments(query.getQuery());
			indexWriter = close(indexWriter);
			indexLucene.reload();
			l = l - indexLucene.getStatistics().getNumDocs();
			return l;
		} catch (IOException e) {
			throw new SearchLibException(e);
		} catch (ParseException e) {
			throw new SearchLibException(e);
		} catch (SyntaxError e) {
			throw new SearchLibException(e);
		} finally {
			close(indexWriter);
		}
	}

	@Override
	public int deleteDocuments(AbstractSearchRequest query)
			throws SearchLibException {
		lock.rl.lock();
		try {
			return deleteDocumentsNoLock(query);
		} finally {
			lock.rl.unlock();
		}
	}

	private void deleteAllNoLock() throws SearchLibException {
		IndexWriter indexWriter = null;
		try {
			indexWriter = open();
			indexWriter.deleteAll();
			indexWriter = close(indexWriter);
			indexLucene.reload();
		} catch (CorruptIndexException e) {
			throw new SearchLibException(e);
		} catch (LockObtainFailedException e) {
			throw new SearchLibException(e);
		} catch (IOException e) {
			throw new SearchLibException(e);
		} finally {
			close(indexWriter);
		}
	}

	@Override
	public void deleteAll() throws SearchLibException {
		lock.rl.lock();
		try {
			deleteAllNoLock();
		} finally {
			lock.rl.unlock();
		}
	}

	private void mergeNoLock(IndexDirectory directory)
			throws SearchLibException {
		IndexWriter indexWriter = null;
		try {
			indexWriter = open();
			indexWriter.addIndexes(directory.getDirectory());
			indexWriter = close(indexWriter);
			indexLucene.reload();
		} catch (IOException e) {
			throw new SearchLibException(e);
		} finally {
			close(indexWriter);
		}

	}

	@Override
	public void mergeData(WriterInterface source) throws SearchLibException {
		WriterLucene sourceWriter = null;
		if (!(source instanceof WriterLucene))
			throw new SearchLibException("Unsupported operation");
		sourceWriter = (WriterLucene) source;
		lock.rl.lock();
		try {
			sourceWriter.lock.rl.lock();
			sourceWriter.setMergingSource(true);
			setMergingTarget(true);
			try {
				mergeNoLock(sourceWriter.indexDirectory);
			} finally {
				sourceWriter.lock.rl.unlock();
			}
		} finally {
			if (sourceWriter != null)
				sourceWriter.setMergingSource(false);
			setMergingTarget(false);
			lock.rl.unlock();
		}
	}

}
