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
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.lucene.analysis.PerFieldAnalyzerWrapper;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.LockObtainFailedException;

import com.jaeksoft.searchlib.analysis.Analyzer;
import com.jaeksoft.searchlib.analysis.LanguageEnum;
import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.request.SearchRequest;
import com.jaeksoft.searchlib.schema.Field;
import com.jaeksoft.searchlib.schema.Schema;
import com.jaeksoft.searchlib.schema.SchemaField;

public class WriterLocal extends WriterAbstract {

	private final ReentrantLock l = new ReentrantLock(true);

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
			e.printStackTrace();
		}
	}

	private void close() {
		l.lock();
		try {
			if (indexWriter != null) {
				try {
					indexWriter.close();
				} catch (CorruptIndexException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					unlock();
				}
				indexWriter = null;
			}
		} finally {
			l.unlock();
		}
	}

	private void open() throws CorruptIndexException,
			LockObtainFailedException, IOException, InstantiationException,
			IllegalAccessException, ClassNotFoundException {
		l.lock();
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
			l.unlock();
		}
	}

	private static IndexWriter openIndexWriter(Directory directory,
			boolean create) throws CorruptIndexException,
			LockObtainFailedException, IOException {
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
		} catch (IOException e) {
			throw e;
		} finally {
			if (indexWriter != null) {
				try {
					indexWriter.close();
				} catch (CorruptIndexException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (directory != null) {
				try {
					directory.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public void addDocument(Document document) throws CorruptIndexException,
			LockObtainFailedException, IOException, InstantiationException,
			IllegalAccessException, ClassNotFoundException {
		l.lock();
		try {
			open();
			indexWriter.addDocument(document);
			close();
		} finally {
			l.unlock();
		}
	}

	private boolean updateDoc(Schema schema, IndexDocument document)
			throws CorruptIndexException, IOException, NoSuchAlgorithmException {
		if (!acceptDocument(document))
			return false;
		Document doc = getLuceneDocument(schema, document);
		PerFieldAnalyzerWrapper pfa = schema.getQueryPerFieldAnalyzer(document
				.getLang());

		Field uniqueField = schema.getFieldList().getUniqueField();
		if (uniqueField != null) {
			String uniqueFieldName = uniqueField.getName();
			indexWriter.updateDocument(
					new Term(uniqueFieldName, doc.get(uniqueFieldName)), doc,
					pfa);
		} else
			indexWriter.addDocument(doc, pfa);
		return true;
	}

	@Override
	public boolean updateDocument(Schema schema, IndexDocument document)
			throws NoSuchAlgorithmException, IOException,
			InstantiationException, IllegalAccessException,
			ClassNotFoundException {
		l.lock();
		try {
			open();
			boolean updated = updateDoc(schema, document);
			close();
			if (updated)
				readerLocal.reload();
			return updated;
		} finally {
			l.unlock();
		}
	}

	@Override
	public int updateDocuments(Schema schema,
			Collection<IndexDocument> documents)
			throws NoSuchAlgorithmException, IOException,
			InstantiationException, IllegalAccessException,
			ClassNotFoundException {
		l.lock();
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
		} finally {
			l.unlock();
		}

	}

	private static Document getLuceneDocument(Schema schema,
			IndexDocument document) throws IOException {
		schema.getQueryPerFieldAnalyzer(document.getLang());
		org.apache.lucene.document.Document doc = new Document();
		LanguageEnum lang = document.getLang();
		for (FieldContent fieldContent : document) {
			String fieldName = fieldContent.getField();
			SchemaField field = schema.getFieldList().get(fieldName);
			if (field != null) {
				Analyzer analyzer = schema.getAnalyzer(field, lang);
				for (String value : fieldContent.getValues()) {
					if (value == null)
						continue;
					if (analyzer != null)
						if (!analyzer.isAnyToken(fieldName, value))
							continue;
					doc.add(field.getLuceneField(value));
				}
			}
		}
		return doc;
	}

	@Override
	public void optimize() throws CorruptIndexException,
			LockObtainFailedException, IOException, InstantiationException,
			IllegalAccessException, ClassNotFoundException {
		l.lock();
		try {
			open();
			indexWriter.optimize(maxNumSegments, true);
			close();
		} finally {
			l.unlock();
		}
	}

	@Override
	public boolean deleteDocument(Schema schema, String uniqueField)
			throws CorruptIndexException, LockObtainFailedException,
			IOException, InstantiationException, IllegalAccessException,
			ClassNotFoundException {
		l.lock();
		try {
			open();
			indexWriter.deleteDocuments(new Term(schema.getFieldList()
					.getUniqueField().getName(), uniqueField));
			close();
			readerLocal.reload();
			return true;
		} finally {
			l.unlock();
		}
	}

	@Override
	public int deleteDocuments(Schema schema, Collection<String> uniqueFields)
			throws CorruptIndexException, LockObtainFailedException,
			IOException, InstantiationException, IllegalAccessException,
			ClassNotFoundException {
		String uniqueField = schema.getFieldList().getUniqueField().getName();
		Term[] terms = new Term[uniqueFields.size()];
		int i = 0;
		for (String value : uniqueFields)
			terms[i++] = new Term(uniqueField, value);
		l.lock();
		try {
			open();
			indexWriter.deleteDocuments(terms);
			close();
			if (terms.length > 0)
				readerLocal.reload();
			return terms.length;
		} finally {
			l.unlock();
		}
	}

	@Override
	public int deleteDocuments(SearchRequest query)
			throws CorruptIndexException, IOException, InstantiationException,
			IllegalAccessException, ClassNotFoundException, ParseException,
			SyntaxError {
		l.lock();
		try {
			open();
			indexWriter.deleteDocuments(query.getQuery());
			close();
			readerLocal.reload();
			return 0;
		} finally {
			l.unlock();
		}
	}

	public void xmlInfo(PrintWriter writer) {
		// TODO Auto-generated method stub

	}

}
