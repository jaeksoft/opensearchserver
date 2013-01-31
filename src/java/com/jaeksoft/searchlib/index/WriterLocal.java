/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008-2012 Emmanuel Keller / Jaeksoft
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

import org.apache.lucene.analysis.PerFieldAnalyzerWrapper;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.SerialMergeScheduler;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.AlreadyClosedException;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.util.Version;

import com.jaeksoft.searchlib.Logging;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.analysis.Analyzer;
import com.jaeksoft.searchlib.analysis.CompiledAnalyzer;
import com.jaeksoft.searchlib.analysis.LanguageEnum;
import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.query.ParseException;
import com.jaeksoft.searchlib.request.SearchRequest;
import com.jaeksoft.searchlib.schema.FieldValueItem;
import com.jaeksoft.searchlib.schema.Schema;
import com.jaeksoft.searchlib.schema.SchemaField;
import com.jaeksoft.searchlib.util.SimpleLock;

public class WriterLocal extends WriterAbstract {

	private final SimpleLock lock = new SimpleLock();

	private IndexDirectory indexDirectory;

	private IndexSingle indexSingle;

	protected WriterLocal(IndexConfig indexConfig, IndexSingle indexSingle,
			IndexDirectory indexDirectory) throws IOException {
		super(indexConfig);
		this.indexSingle = indexSingle;
		this.indexDirectory = indexDirectory;
	}

	private IndexWriter close(IndexWriter indexWriter) {
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

	public final void create() throws CorruptIndexException,
			LockObtainFailedException, IOException {
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
			IOException {
		IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_36,
				null);
		config.setOpenMode(create ? OpenMode.CREATE_OR_APPEND : OpenMode.APPEND);
		config.setMergeScheduler(new SerialMergeScheduler());
		Logging.debug("WriteLocal open " + indexDirectory.getDirectory());
		return new IndexWriter(indexDirectory.getDirectory(),
				new IndexWriterConfig(Version.LUCENE_36, null));
	}

	private IndexWriter open() throws IOException, InstantiationException,
			IllegalAccessException, ClassNotFoundException {
		IndexWriter indexWriter = open(false);
		return indexWriter;
	}

	@Deprecated
	public void addDocument(Document document) throws IOException,
			InstantiationException, IllegalAccessException,
			ClassNotFoundException {
		IndexWriter indexWriter = null;
		lock.rl.lock();
		try {
			indexWriter = open();
			indexWriter.addDocument(document);
			indexWriter = close(indexWriter);
		} finally {
			close(indexWriter);
			lock.rl.unlock();
		}
	}

	private boolean updateDocNoLock(IndexWriter indexWriter, Schema schema,
			IndexDocument document) throws IOException,
			NoSuchAlgorithmException, SearchLibException {
		if (!acceptDocument(document))
			return false;

		if (beforeUpdateList != null)
			for (BeforeUpdateInterface beforeUpdate : beforeUpdateList)
				beforeUpdate.update(schema, document);

		Document doc = getLuceneDocument(schema, document);
		PerFieldAnalyzerWrapper pfa = schema.getIndexPerFieldAnalyzer(document
				.getLang());

		SchemaField uniqueField = schema.getFieldList().getUniqueField();
		if (uniqueField != null) {
			String uniqueFieldName = uniqueField.getName();
			String uniqueFieldValue = doc.get(uniqueFieldName);
			if (uniqueFieldValue == null)
				throw new SearchLibException("The unique value is missing ("
						+ uniqueFieldName + ")");
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
			boolean updated = updateDocNoLock(indexWriter, schema, document);
			indexWriter = close(indexWriter);
			if (updated)
				indexSingle.reload();
			return updated;
		} catch (IOException e) {
			throw new SearchLibException(e);
		} catch (InstantiationException e) {
			throw new SearchLibException(e);
		} catch (IllegalAccessException e) {
			throw new SearchLibException(e);
		} catch (ClassNotFoundException e) {
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
			for (IndexDocument document : documents)
				if (updateDocNoLock(indexWriter, schema, document))
					count++;
			indexWriter = close(indexWriter);
			if (count > 0)
				indexSingle.reload();
			return count;
		} catch (IOException e) {
			throw new SearchLibException(e);
		} catch (InstantiationException e) {
			throw new SearchLibException(e);
		} catch (IllegalAccessException e) {
			throw new SearchLibException(e);
		} catch (ClassNotFoundException e) {
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
			optimizing = true;
			indexWriter.optimize(true);
			optimizing = false;
			indexWriter = close(indexWriter);
		} catch (IOException e) {
			throw new SearchLibException(e);
		} catch (InstantiationException e) {
			throw new SearchLibException(e);
		} catch (IllegalAccessException e) {
			throw new SearchLibException(e);
		} catch (ClassNotFoundException e) {
			throw new SearchLibException(e);
		} finally {
			optimizing = false;
			close(indexWriter);
		}
	}

	@Override
	public void optimize() throws SearchLibException {
		lock.rl.lock();
		try {
			optimizeNoLock();
		} finally {
			lock.rl.unlock();
		}
	}

	private int deleteDocumentNoLock(String field, String value)
			throws SearchLibException {
		IndexWriter indexWriter = null;
		try {
			int l = indexSingle.getStatistics().getNumDeletedDocs();
			indexWriter = open();
			indexWriter.deleteDocuments(new Term(field, value));
			indexWriter = close(indexWriter);
			indexSingle.reload();
			l = indexSingle.getStatistics().getNumDeletedDocs() - l;
			return l;
		} catch (IOException e) {
			throw new SearchLibException(e);
		} catch (InstantiationException e) {
			throw new SearchLibException(e);
		} catch (IllegalAccessException e) {
			throw new SearchLibException(e);
		} catch (ClassNotFoundException e) {
			throw new SearchLibException(e);
		} finally {
			close(indexWriter);
		}
	}

	@Override
	public int deleteDocument(Schema schema, String field, String value)
			throws SearchLibException {
		SchemaField schemaField = field != null ? schema.getFieldList().get(
				field) : schema.getFieldList().getUniqueField();
		if (schemaField == null)
			return 0;
		lock.rl.lock();
		try {
			return deleteDocumentNoLock(schemaField.getName(), value);
		} finally {
			lock.rl.unlock();
		}
	}

	private int deleteDocumentsNoLock(Schema schema, Term[] terms)
			throws SearchLibException {
		IndexWriter indexWriter = null;
		try {
			int l = indexSingle.getStatistics().getNumDeletedDocs();
			indexWriter = open();
			indexWriter.deleteDocuments(terms);
			indexWriter = close(indexWriter);
			if (terms.length > 0)
				indexSingle.reload();
			l = indexSingle.getStatistics().getNumDeletedDocs() - l;
			return l;
		} catch (IOException e) {
			throw new SearchLibException(e);
		} catch (InstantiationException e) {
			throw new SearchLibException(e);
		} catch (IllegalAccessException e) {
			throw new SearchLibException(e);
		} catch (ClassNotFoundException e) {
			throw new SearchLibException(e);
		} finally {
			close(indexWriter);
		}
	}

	@Override
	public int deleteDocuments(Schema schema, String field,
			Collection<String> values) throws SearchLibException {
		SchemaField schemaField = field != null ? schema.getFieldList().get(
				field) : schema.getFieldList().getUniqueField();
		if (schemaField == null)
			return 0;
		int countNonNullValues = 0;
		for (String value : values)
			if (value != null)
				countNonNullValues++;
		if (countNonNullValues == 0)
			return 0;
		Term[] terms = new Term[countNonNullValues];
		int i = 0;
		for (String value : values)
			if (value != null)
				terms[i++] = new Term(field, value);
		lock.rl.lock();
		try {
			return deleteDocumentsNoLock(schema, terms);
		} finally {
			lock.rl.unlock();
		}
	}

	private int deleteDocumentsNoLock(SearchRequest query)
			throws SearchLibException {
		IndexWriter indexWriter = null;
		try {
			int l = indexSingle.getStatistics().getNumDeletedDocs();
			indexWriter = open();
			indexWriter.deleteDocuments(query.getQuery());
			indexWriter = close(indexWriter);
			indexSingle.reload();
			l = indexSingle.getStatistics().getNumDeletedDocs() - l;
			return l;
		} catch (IOException e) {
			throw new SearchLibException(e);
		} catch (InstantiationException e) {
			throw new SearchLibException(e);
		} catch (IllegalAccessException e) {
			throw new SearchLibException(e);
		} catch (ClassNotFoundException e) {
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
	public int deleteDocuments(SearchRequest query) throws SearchLibException {
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
			indexSingle.reload();
		} catch (CorruptIndexException e) {
			throw new SearchLibException(e);
		} catch (LockObtainFailedException e) {
			throw new SearchLibException(e);
		} catch (IOException e) {
			throw new SearchLibException(e);
		} catch (InstantiationException e) {
			throw new SearchLibException(e);
		} catch (IllegalAccessException e) {
			throw new SearchLibException(e);
		} catch (ClassNotFoundException e) {
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

}
