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
import java.util.List;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.store.LockObtainFailedException;
import org.w3c.dom.Node;

import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.request.Request;
import com.jaeksoft.searchlib.result.DocumentResult;
import com.jaeksoft.searchlib.result.Result;
import com.jaeksoft.searchlib.schema.Schema;
import com.jaeksoft.searchlib.util.XPathParser;

public class IndexLocal extends IndexAbstract {

	private ReaderLocal readerLocal = null;
	private WriterLocal writerLocal = null;
	private ReaderRemote readerRemote = null;
	private WriterRemote writerRemote = null;

	public IndexLocal(File homeDir, XPathParser xpp, Node node,
			boolean createIfNotExists) throws IOException {
		super(node);
		readerRemote = ReaderRemote.fromXmlConfig(getName(), xpp, node);
		readerLocal = ReaderLocal.fromXmlConfig(homeDir, getName(), xpp, node,
				createIfNotExists);
		writerRemote = WriterRemote.fromXmlConfig(getName(), xpp, node);
		if (readerLocal != null) {
			writerLocal = WriterLocal.fromXmlConfig(getName(), xpp, node);
			if (writerLocal != null)
				writerLocal.setDirectory(readerLocal.getDirectory());
		}
	}

	public void xmlInfo(PrintWriter writer, HashSet<String> classDetail) {
		r.lock();
		try {
			if (readerLocal != null)
				readerLocal.xmlInfo(writer, classDetail);
			if (readerRemote != null)
				readerRemote.xmlInfo(writer, classDetail);
			if (writerLocal != null)
				writerLocal.xmlInfo(writer, classDetail);
			if (writerRemote != null)
				writerRemote.xmlInfo(writer, classDetail);
		} finally {
			r.unlock();
		}
	}

	public void optimize(String indexName, boolean forceLocal)
			throws CorruptIndexException, LockObtainFailedException,
			IOException {
		r.lock();
		try {
			if (writerLocal != null)
				writerLocal.optimize(indexName, forceLocal);
			if (writerRemote != null)
				writerRemote.optimize(indexName, forceLocal);
		} finally {
			r.unlock();
		}
	}

	public void deleteDocuments(Schema schema, String uniqueField,
			boolean forceLocal) throws CorruptIndexException,
			LockObtainFailedException, IOException {
		r.lock();
		try {
			if (writerLocal != null)
				writerLocal.deleteDocuments(schema, uniqueField, forceLocal);
			if (forceLocal)
				return;
			if (writerRemote != null)
				writerRemote.deleteDocuments(schema, uniqueField, forceLocal);
		} finally {
			r.unlock();
		}
	}

	public void deleteDocuments(String indexName, Schema schema,
			String uniqueField, boolean forceLocal)
			throws CorruptIndexException, LockObtainFailedException,
			IOException {
		r.lock();
		try {
			if (writerLocal != null)
				writerLocal.deleteDocuments(indexName, schema, uniqueField,
						forceLocal);
			if (forceLocal)
				return;
			if (writerRemote != null)
				writerRemote.deleteDocuments(indexName, schema, uniqueField,
						forceLocal);
		} finally {
			r.unlock();
		}
	}

	public void updateDocument(Schema schema, IndexDocument document,
			boolean forceLocal) throws NoSuchAlgorithmException, IOException {
		r.lock();
		try {
			if (writerLocal != null)
				writerLocal.updateDocument(schema, document, forceLocal);
			if (forceLocal)
				return;
			if (writerRemote != null)
				writerRemote.updateDocument(schema, document, forceLocal);
		} finally {
			r.unlock();
		}
	}

	public void updateDocument(String indexName, Schema schema,
			IndexDocument document, boolean forceLocal)
			throws NoSuchAlgorithmException, IOException {
		r.lock();
		try {
			if (writerLocal != null)
				writerLocal.updateDocument(indexName, schema, document,
						forceLocal);
			if (forceLocal)
				return;
			if (writerRemote != null)
				writerRemote.updateDocument(indexName, schema, document,
						forceLocal);
		} finally {
			r.unlock();
		}
	}

	public void updateDocuments(Schema schema,
			List<? extends IndexDocument> documents, boolean forceLocal)
			throws NoSuchAlgorithmException, IOException {
		r.lock();
		try {
			if (writerLocal != null)
				writerLocal.updateDocuments(schema, documents, forceLocal);
			if (forceLocal)
				return;
			if (writerRemote != null)
				writerRemote.updateDocuments(schema, documents, forceLocal);
		} finally {
			r.unlock();
		}
	}

	public void updateDocuments(String indexName, Schema schema,
			List<? extends IndexDocument> documents, boolean forceLocal)
			throws NoSuchAlgorithmException, IOException {
		r.lock();
		try {
			if (writerLocal != null)
				writerLocal.updateDocuments(indexName, schema, documents,
						forceLocal);
			if (forceLocal)
				return;
			if (writerRemote != null)
				writerRemote.updateDocuments(indexName, schema, documents,
						forceLocal);
		} finally {
			r.unlock();
		}
	}

	public DocumentResult documents(Request request)
			throws CorruptIndexException, IOException, ParseException {
		r.lock();
		try {
			if (request.getForceLocal() && readerLocal != null)
				return readerLocal.documents(request);
			if (readerRemote != null)
				return readerRemote.documents(request);
			return null;
		} finally {
			r.unlock();
		}
	}

	public void reload(String indexName, boolean deleteOld) throws IOException {
		w.lock();
		try {
			if (readerLocal != null)
				readerLocal.reload(indexName, deleteOld);
			if (writerLocal != null && readerLocal != null)
				writerLocal.setDirectory(readerLocal.getDirectory());
			if (readerRemote != null)
				readerRemote.reload(indexName, deleteOld);
		} finally {
			w.unlock();
		}
	}

	public Result<?> search(Request request) throws IOException,
			ParseException, SyntaxError {
		r.lock();
		try {
			if (request.getForceLocal() || readerLocal != null)
				return readerLocal.search(request);
			if (readerRemote != null)
				return readerRemote.search(request);
			return null;
		} finally {
			r.unlock();
		}
	}

	public boolean sameIndex(ReaderInterface reader) {
		r.lock();
		try {
			if (reader == this)
				return true;
			if (reader == this.readerLocal)
				return true;
			return false;
		} finally {
			r.unlock();
		}
	}

	public IndexStatistics getStatistics() {
		r.lock();
		try {
			if (readerLocal != null)
				return readerLocal.getStatistics();
			if (readerRemote != null)
				return readerRemote.getStatistics();
			return null;
		} finally {
			r.unlock();
		}
	}

	@Override
	public IndexLocal get(String name) {
		return this;
	}

	public int getDocFreq(Term term) throws IOException {
		r.lock();
		try {
			if (readerLocal != null)
				return readerLocal.getDocFreq(term);
			if (readerRemote != null)
				return readerRemote.getDocFreq(term);
			return 0;
		} finally {
			r.unlock();
		}
	}

}
