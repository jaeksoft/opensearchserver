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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.lucene.analysis.PerFieldAnalyzerWrapper;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.LockObtainFailedException;

import com.jaeksoft.searchlib.schema.Schema;
import com.jaeksoft.searchlib.schema.SchemaField;

public class WriterLocal extends WriterAbstract {

	private ReentrantLock l = new ReentrantLock();

	private IndexWriter indexWriter;

	private ReaderLocal readerLocal;

	private WriterLocal(String indexName, String keyField,
			String keyMd5Pattern, ReaderLocal readerLocal) throws IOException {
		super(indexName, keyField, keyMd5Pattern);
		this.readerLocal = readerLocal;
		this.indexWriter = null;
	}

	private void close() {
		l.lock();
		try {
			if (l.getQueueLength() > 0)
				return;
			if (indexWriter != null) {
				try {
					indexWriter.close();
					indexWriter = null;
				} catch (CorruptIndexException e) {
					e.printStackTrace();
					indexWriter = null;
				} catch (IOException e) {
					e.printStackTrace();
					indexWriter = null;
				}
			}
		} finally {
			l.unlock();
		}
	}

	private void open() throws CorruptIndexException,
			LockObtainFailedException, IOException {
		l.lock();
		try {
			if (indexWriter != null)
				return;
			indexWriter = openIndexWriter(readerLocal.getDirectory(), false);
			indexWriter.setMaxMergeDocs(1000000);
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
			directory = FSDirectory.getDirectory(dataDir);
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

	protected static void receiveIndexFile(File rootDir, Long version,
			String fileName, InputStream is) throws IOException {
		File dataDir = new File(rootDir, version.toString());
		if (!dataDir.exists())
			dataDir.mkdirs();
		File targetFile = new File(dataDir, fileName);
		if (!targetFile.exists())
			targetFile.createNewFile();
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(targetFile);
			int len;
			byte[] buffer = new byte[131072];
			while ((len = is.read(buffer)) != -1)
				fos.write(buffer, 0, len);
		} catch (IOException e) {
			throw e;
		} finally {
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public void addDocument(Document document) throws CorruptIndexException,
			LockObtainFailedException, IOException {
		l.lock();
		try {
			open();
			indexWriter.addDocument(document);
			close();
		} finally {
			l.unlock();
		}
	}

	private void updateDoc(Schema schema, IndexDocument document)
			throws CorruptIndexException, IOException, NoSuchAlgorithmException {
		if (!acceptDocument(document))
			return;
		String field = schema.getFieldList().getUniqueField().getName();
		Document doc = getLuceneDocument(schema, document);
		Term updateTerm = new Term(field, doc.get(field));
		PerFieldAnalyzerWrapper pfa = schema.getQueryPerFieldAnalyzer(document
				.getLang());
		indexWriter.updateDocument(updateTerm, doc, pfa);
	}

	public void updateDocument(Schema schema, IndexDocument document)
			throws NoSuchAlgorithmException, IOException {
		l.lock();
		try {
			open();
			updateDoc(schema, document);
			close();
		} finally {
			l.unlock();
		}
	}

	public void updateDocuments(Schema schema,
			List<? extends IndexDocument> documents)
			throws NoSuchAlgorithmException, IOException {
		l.lock();
		try {
			open();
			for (IndexDocument document : documents)
				updateDoc(schema, document);
			close();
		} finally {
			l.unlock();
		}

	}

	private static Document getLuceneDocument(Schema schema,
			IndexDocument document) {
		org.apache.lucene.document.Document doc = new Document();
		for (FieldContent fieldContent : document.getFields())
			for (String value : fieldContent.getValues()) {
				SchemaField field = schema.getFieldList().get(
						fieldContent.getField());
				if (field != null)
					doc.add(field.getLuceneField(value));
			}
		return doc;
	}

	public void optimize(String indexName) throws CorruptIndexException,
			LockObtainFailedException, IOException {
		l.lock();
		try {
			if (!acceptNameOrEmpty(indexName))
				return;
			open();
			indexWriter.optimize(10, true);
			close();
		} finally {
			l.unlock();
		}
	}

	public void deleteDocuments(Schema schema, String uniqueField)
			throws CorruptIndexException, LockObtainFailedException,
			IOException {
		l.lock();
		try {
			open();
			indexWriter.deleteDocuments(new Term(schema.getFieldList()
					.getUniqueField().getName(), uniqueField));
			close();
		} finally {
			l.unlock();
		}
	}

	public void deleteDocuments(Schema schema, List<String> uniqueFields)
			throws CorruptIndexException, LockObtainFailedException,
			IOException {
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
		} finally {
			l.unlock();
		}
	}

	public static WriterLocal fromConfig(IndexConfig indexConfig,
			ReaderLocal reader) throws IOException {
		if (indexConfig.getName() == null)
			return null;
		return new WriterLocal(indexConfig.getName(),
				indexConfig.getKeyField(), indexConfig.getKeyMd5RegExp(),
				reader);
	}

	public void xmlInfo(PrintWriter writer, HashSet<String> classDetail) {
		// TODO Auto-generated method stub

	}

}
