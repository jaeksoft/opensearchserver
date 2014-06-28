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
import java.util.Collection;

import org.apache.commons.collections.CollectionUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.SerialMergeScheduler;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Similarity;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.util.Version;

import com.jaeksoft.searchlib.Logging;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.SearchLibException.UniqueKeyMissing;
import com.jaeksoft.searchlib.analysis.AbstractAnalyzer;
import com.jaeksoft.searchlib.analysis.Analyzer;
import com.jaeksoft.searchlib.analysis.CompiledAnalyzer;
import com.jaeksoft.searchlib.analysis.IndexDocumentAnalyzer;
import com.jaeksoft.searchlib.analysis.LanguageEnum;
import com.jaeksoft.searchlib.analysis.PerFieldAnalyzer;
import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.query.ParseException;
import com.jaeksoft.searchlib.request.AbstractRequest;
import com.jaeksoft.searchlib.request.AbstractSearchRequest;
import com.jaeksoft.searchlib.request.DocumentsRequest;
import com.jaeksoft.searchlib.result.ResultDocuments;
import com.jaeksoft.searchlib.schema.FieldValueItem;
import com.jaeksoft.searchlib.schema.Schema;
import com.jaeksoft.searchlib.schema.SchemaField;
import com.jaeksoft.searchlib.schema.SchemaFieldList;
import com.jaeksoft.searchlib.util.IOUtils;
import com.jaeksoft.searchlib.util.SimpleLock;
import com.jaeksoft.searchlib.webservice.query.document.IndexDocumentResult;
import com.jaeksoft.searchlib.webservice.query.document.IndexDocumentResult.IndexField;
import com.jaeksoft.searchlib.webservice.query.document.IndexDocumentResult.IndexTerm;

public class WriterLocal extends WriterAbstract {

	private final SimpleLock lock = new SimpleLock();

	private IndexDirectory indexDirectory;

	private IndexLucene indexSingle;

	protected WriterLocal(IndexConfig indexConfig, IndexLucene indexSingle,
			IndexDirectory indexDirectory) throws IOException {
		super(indexConfig);
		this.indexSingle = indexSingle;
		this.indexDirectory = indexDirectory;
	}

	private void close(IndexWriter indexWriter) {
		if (indexWriter == null)
			return;
		try {
			indexWriter.close();
		} catch (Throwable e) {
			Logging.warn(e);
		} finally {
			indexDirectory.unlock();
		}
	}

	public final void create() throws CorruptIndexException,
			LockObtainFailedException, IOException, SearchLibException {
		IndexWriter indexWriter = null;
		lock.rl.lock();
		try {
			indexWriter = open(true);
			close(indexWriter);
			indexWriter = null;
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
			close(indexWriter);
			indexWriter = null;
		} finally {
			lock.rl.unlock();
			close(indexWriter);
		}
	}

	final private boolean updateDocNoLock(SchemaField uniqueField,
			IndexWriter indexWriter, Schema schema, IndexDocument document)
			throws IOException, NoSuchAlgorithmException, SearchLibException {
		if (!acceptDocument(document))
			return false;

		if (beforeUpdateList != null)
			for (UpdateInterfaces.Before beforeUpdate : beforeUpdateList)
				beforeUpdate.update(schema, document);

		Document doc = getLuceneDocument(schema, document);
		PerFieldAnalyzer pfa = schema.getIndexPerFieldAnalyzer(document
				.getLang());

		updateDocNoLock(uniqueField, indexWriter, pfa, doc);
		return true;
	}

	final private void updateDocNoLock(SchemaField uniqueField,
			IndexWriter indexWriter, AbstractAnalyzer analyzer, Document doc)
			throws UniqueKeyMissing, CorruptIndexException, IOException {
		if (uniqueField != null) {
			String uniqueFieldName = uniqueField.getName();
			String uniqueFieldValue = doc.get(uniqueFieldName);
			if (uniqueFieldValue == null)
				throw new UniqueKeyMissing(uniqueFieldName);
			indexWriter.updateDocument(new Term(uniqueFieldName,
					uniqueFieldValue), doc, analyzer);
		} else
			indexWriter.addDocument(doc, analyzer);
	}

	private boolean updateDocumentNoLock(Schema schema, IndexDocument document)
			throws SearchLibException {
		IndexWriter indexWriter = null;
		try {
			indexWriter = open();
			SchemaField uniqueField = schema.getFieldList().getUniqueField();
			boolean updated = updateDocNoLock(uniqueField, indexWriter, schema,
					document);
			close(indexWriter);
			indexWriter = null;
			if (updated) {
				indexSingle.reloadNoLock();
				if (afterUpdateList != null)
					for (UpdateInterfaces.After afterUpdate : afterUpdateList)
						afterUpdate.update(document);
			}
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
			close(indexWriter);
			indexWriter = null;
			if (count > 0) {
				indexSingle.reloadNoLock();
				if (afterUpdateList != null)
					for (UpdateInterfaces.After afterUpdate : afterUpdateList)
						afterUpdate.update(documents);
			}
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

	private int updateIndexDocumentsNoLock(Schema schema,
			Collection<IndexDocumentResult> documents)
			throws SearchLibException {
		IndexWriter indexWriter = null;
		try {
			int count = 0;
			indexWriter = open();
			SchemaField uniqueField = schema.getFieldList().getUniqueField();
			for (IndexDocumentResult document : documents) {
				Document doc = getLuceneDocument(schema, document);
				IndexDocumentAnalyzer analyzer = new IndexDocumentAnalyzer(
						document);
				updateDocNoLock(uniqueField, indexWriter, analyzer, doc);
				count++;
			}
			close(indexWriter);
			indexWriter = null;
			if (count > 0)
				indexSingle.reload();
			return count;
		} catch (IOException e) {
			throw new SearchLibException(e);
		} finally {
			close(indexWriter);
		}
	}

	@Override
	public int updateIndexDocuments(Schema schema,
			Collection<IndexDocumentResult> documents)
			throws SearchLibException {
		lock.rl.lock();
		try {
			return updateIndexDocumentsNoLock(schema, documents);
		} finally {
			lock.rl.unlock();
		}
	}

	final private static Document getLuceneDocument(Schema schema,
			IndexDocument document) throws IOException, SearchLibException {
		schema.getIndexPerFieldAnalyzer(document.getLang());
		Document doc = new Document();
		LanguageEnum lang = document.getLang();
		SchemaFieldList schemaFieldList = schema.getFieldList();
		for (FieldContent fieldContent : document) {
			String fieldName = fieldContent.getField();
			SchemaField field = schemaFieldList.get(fieldName);
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

	final private static Document getLuceneDocument(Schema schema,
			IndexDocumentResult document) {
		if (CollectionUtils.isEmpty(document.fields))
			return null;
		SchemaFieldList schemaFieldList = schema.getFieldList();
		Document doc = new Document();
		for (IndexField indexField : document.fields) {
			SchemaField field = schemaFieldList.get(indexField.field);
			if (field == null)
				continue;
			if (indexField.stored != null) {
				for (String value : indexField.stored)
					doc.add(field.getLuceneField(value, null));
			} else {
				if (indexField.terms != null)
					for (IndexTerm term : indexField.terms)
						doc.add(field.getLuceneField(term.t, null));
			}
		}
		return doc;
	}

	private void optimizeNoLock() throws SearchLibException {
		IndexWriter indexWriter = null;
		try {
			indexWriter = open();
			indexWriter.optimize(true);
			close(indexWriter);
			indexWriter = null;
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

	@SuppressWarnings("deprecation")
	private int deleteDocumentsNoLock(int[] ids) throws IOException,
			SearchLibException {
		if (ids == null || ids.length == 0)
			return 0;
		IndexReader indexReader = null;
		try {
			int l = indexSingle.getStatistics().getNumDocs();
			indexReader = IndexReader
					.open(indexDirectory.getDirectory(), false);
			for (int id : ids)
				if (!indexReader.isDeleted(id))
					indexReader.deleteDocument(id);
			indexReader.close();
			indexReader = null;
			indexSingle.reload();
			l = l - indexSingle.getStatistics().getNumDocs();
			return l;
		} finally {
			IOUtils.close(indexReader);
		}
	}

	private int deleteDocumentsNoLock(AbstractRequest request)
			throws SearchLibException {
		try {
			int[] ids = null;
			if (request instanceof AbstractSearchRequest) {
				DocSetHits dsh = indexSingle.searchDocSet(
						(AbstractSearchRequest) request, null);
				if (dsh != null)
					ids = dsh.getIds();
			} else if (request instanceof DocumentsRequest) {
				ResultDocuments result = (ResultDocuments) indexSingle
						.request(request);
				if (result != null)
					ids = result.getDocIdArray();
			}
			return deleteDocumentsNoLock(ids);
		} catch (IOException e) {
			throw new SearchLibException(e);
		} catch (ParseException e) {
			throw new SearchLibException(e);
		} catch (SyntaxError e) {
			throw new SearchLibException(e);
		}
	}

	@Override
	public int deleteDocuments(AbstractRequest request)
			throws SearchLibException {
		lock.rl.lock();
		try {
			return deleteDocumentsNoLock(request);
		} finally {
			lock.rl.unlock();
		}
	}

	private void deleteAllNoLock() throws SearchLibException {
		IndexWriter indexWriter = null;
		try {
			indexWriter = open();
			indexWriter.deleteAll();
			close(indexWriter);
			indexWriter = null;
			indexSingle.reloadNoLock();
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
			close(indexWriter);
			indexWriter = null;
			indexSingle.reloadNoLock();
		} catch (IOException e) {
			throw new SearchLibException(e);
		} finally {
			close(indexWriter);
		}

	}

	@Override
	public void mergeData(WriterInterface source) throws SearchLibException {
		WriterLocal sourceWriter = null;
		if (!(source instanceof WriterLocal))
			throw new SearchLibException("Unsupported operation");
		sourceWriter = (WriterLocal) source;
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
