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

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;

import org.apache.lucene.analysis.PerFieldAnalyzerWrapper;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.LockObtainFailedException;

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

	private IndexWriter indexWriter;

	private ReaderLocal readerLocal;

	private String similarityClass;

	private int maxNumSegments;

	protected WriterLocal(IndexConfig indexConfig, ReaderLocal readerLocal)
			throws IOException {
		super(indexConfig);
		this.readerLocal = readerLocal;
		this.indexWriter = null;
		this.similarityClass = indexConfig.getSimilarityClass();
		this.maxNumSegments = indexConfig.getMaxNumSegments();
	}

	private void unlock() {
		try {
			Directory dir = readerLocal.getDirectory();
			if (!IndexWriter.isLocked(dir))
				return;
			IndexWriter.unlock(dir);
		} catch (IOException e) {
			Logging.error(e.getMessage(), e);
		}
	}

	private void close() {
		lock.rl.lock();
		try {
			if (indexWriter != null) {
				try {
					indexWriter.close();
				} catch (IOException e) {
					Logging.error(e.getMessage(), e);
				} finally {
					unlock();
				}
				indexWriter = null;
			}
		} finally {
			lock.rl.unlock();
		}
	}

	private void open() throws IOException, InstantiationException,
			IllegalAccessException, ClassNotFoundException {
		lock.rl.lock();
		try {
			if (indexWriter != null)
				return;
			indexWriter = openIndexWriter(readerLocal.getDirectory(), false);
			indexWriter.setMaxMergeDocs(1000000);
			if (similarityClass != null) {
				Similarity similarity = (Similarity) Class.forName(
						similarityClass).newInstance();
				indexWriter.setSimilarity(similarity);
			}
		} finally {
			lock.rl.unlock();
		}
	}

	private static IndexWriter openIndexWriter(Directory directory,
			boolean create) throws IOException {
		return new IndexWriter(directory, null, create,
				IndexWriter.MaxFieldLength.UNLIMITED);
	}

	private final static SimpleDateFormat dateDirFormat = new SimpleDateFormat(
			"yyyyMMddHHmmss");

	protected static File createIndex(File rootDir) throws IOException {

		File dataDir = new File(rootDir, dateDirFormat.format(new Date()));

		Directory directory = null;
		IndexWriter indexWriter = null;
		try {
			dataDir.mkdirs();
			directory = FSDirectory.open(dataDir);
			indexWriter = openIndexWriter(directory, true);
			return dataDir;
		} finally {
			if (indexWriter != null) {
				try {
					indexWriter.close();
				} catch (IOException e) {
					Logging.error(e.getMessage(), e);
				}
			}
			if (directory != null) {
				try {
					directory.close();
				} catch (IOException e) {
					Logging.error(e.getMessage(), e);
				}
			}
		}
	}

	@Deprecated
	public void addDocument(Document document) throws IOException,
			InstantiationException, IllegalAccessException,
			ClassNotFoundException {
		lock.rl.lock();
		try {
			open();
			indexWriter.addDocument(document);
			close();
		} finally {
			lock.rl.unlock();
		}
	}

	private boolean updateDoc(Schema schema, IndexDocument document)
			throws IOException, NoSuchAlgorithmException, SearchLibException {
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
		try {
			open();
			boolean updated = updateDoc(schema, document);
			close();
			if (updated)
				readerLocal.reload();
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
			close();
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
		try {
			int count = 0;
			open();
			for (IndexDocument document : documents)
				if (updateDoc(schema, document))
					count++;
			close();
			if (count > 0)
				readerLocal.reload();
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
			close();
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
		try {
			open();
			optimizing = true;
			indexWriter.optimize(maxNumSegments, true);
			close();
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
			close();
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

	private boolean deleteDocumentNoLock(String field, String value)
			throws SearchLibException {
		try {
			open();
			indexWriter.deleteDocuments(new Term(field, value));
			close();
			readerLocal.reload();
			return true;
		} catch (IOException e) {
			throw new SearchLibException(e);
		} catch (InstantiationException e) {
			throw new SearchLibException(e);
		} catch (IllegalAccessException e) {
			throw new SearchLibException(e);
		} catch (ClassNotFoundException e) {
			throw new SearchLibException(e);
		} finally {
			close();
		}
	}

	@Override
	public boolean deleteDocument(Schema schema, String uniqueField)
			throws SearchLibException {
		SchemaField uniqueSchemaField = schema.getFieldList().getUniqueField();
		if (uniqueSchemaField == null)
			return false;
		lock.rl.lock();
		try {
			return deleteDocumentNoLock(uniqueSchemaField.getName(),
					uniqueField);
		} finally {
			lock.rl.unlock();
		}
	}

	private int deleteDocumentsNoLock(Schema schema, Term[] terms)
			throws SearchLibException {
		try {
			open();
			indexWriter.deleteDocuments(terms);
			close();
			if (terms.length > 0)
				readerLocal.reload();
			return terms.length;
		} catch (IOException e) {
			throw new SearchLibException(e);
		} catch (InstantiationException e) {
			throw new SearchLibException(e);
		} catch (IllegalAccessException e) {
			throw new SearchLibException(e);
		} catch (ClassNotFoundException e) {
			throw new SearchLibException(e);
		} finally {
			close();
		}
	}

	@Override
	public int deleteDocuments(Schema schema, Collection<String> uniqueFields)
			throws SearchLibException {
		SchemaField uniqueSchemaField = schema.getFieldList().getUniqueField();
		if (uniqueSchemaField == null)
			return 0;
		String uniqueField = uniqueSchemaField.getName();
		Term[] terms = new Term[uniqueFields.size()];
		int i = 0;
		for (String value : uniqueFields)
			terms[i++] = new Term(uniqueField, value);
		lock.rl.lock();
		try {
			return deleteDocumentsNoLock(schema, terms);
		} finally {
			lock.rl.unlock();
		}
	}

	private int deleteDocumentsNoLock(SearchRequest query)
			throws SearchLibException {
		try {
			open();
			indexWriter.deleteDocuments(query.getQuery());
			close();
			readerLocal.reload();
			return 0;
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
			close();
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
		try {
			open();
			indexWriter.deleteAll();
			close();
			readerLocal.reload();
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
			close();
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
