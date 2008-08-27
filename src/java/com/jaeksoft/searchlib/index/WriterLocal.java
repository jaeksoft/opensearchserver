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
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.lucene.analysis.PerFieldAnalyzerWrapper;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.LockObtainFailedException;
import org.w3c.dom.Node;

import com.jaeksoft.searchlib.schema.Schema;
import com.jaeksoft.searchlib.schema.SchemaField;
import com.jaeksoft.searchlib.util.XPathParser;

public class WriterLocal extends WriterAbstract {

	private ReentrantLock l = new ReentrantLock();

	private IndexWriter indexWriter;

	private Directory directory;

	private WriterLocal(String indexName) throws IOException {
		super(indexName);
		this.directory = null;
		this.indexWriter = null;
	}

	protected void setDirectory(Directory directory) {
		l.lock();
		try {
			this.directory = directory;
			close();
		} finally {
			l.unlock();
		}
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
			indexWriter = openIndexWriter(directory, false);
		} finally {
			l.unlock();
		}
	}

	private static IndexWriter openIndexWriter(Directory directory,
			boolean create) throws CorruptIndexException,
			LockObtainFailedException, IOException {
		return new IndexWriter(directory, false, null, create);
	}

	protected static void create(File dataDir) throws IOException {
		Directory directory = null;
		IndexWriter indexWriter = null;
		try {
			dataDir.mkdirs();
			directory = FSDirectory.getDirectory(dataDir);
			indexWriter = openIndexWriter(directory, true);
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

	public void updateDocument(Schema schema, IndexDocument document,
			boolean forceLocal) throws NoSuchAlgorithmException, IOException {
		l.lock();
		try {
			if (!acceptDocument(document))
				return;
			open();
			String field = schema.getFieldList().getUniqueField().getName();
			Document doc = getLuceneDocument(schema, document);
			Term updateTerm = new Term(field, doc.get(field));
			PerFieldAnalyzerWrapper pfa = schema
					.getQueryPerFieldAnalyzer(document.getLang());
			indexWriter.updateDocument(updateTerm, doc, pfa);
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

	public void optimize(String indexName, boolean forceLocal)
			throws CorruptIndexException, LockObtainFailedException,
			IOException {
		l.lock();
		try {
			if (!acceptName(indexName))
				return;
			open();
			indexWriter.optimize();
			close();
		} finally {
			l.unlock();
		}
	}

	public void deleteDocuments(Schema schema, String uniqueField,
			boolean bForceLocal) throws CorruptIndexException,
			LockObtainFailedException, IOException {
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

	public static WriterLocal fromXmlConfig(String indexName, XPathParser xpp,
			Node node) throws IOException {
		if (indexName == null)
			return null;
		return new WriterLocal(indexName);
	}

	public void xmlInfo(PrintWriter writer, HashSet<String> classDetail) {
		// TODO Auto-generated method stub

	}

}
