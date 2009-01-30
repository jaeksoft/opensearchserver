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
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.store.LockObtainFailedException;

import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.request.Request;
import com.jaeksoft.searchlib.result.DocumentResult;
import com.jaeksoft.searchlib.result.Result;
import com.jaeksoft.searchlib.schema.Schema;

public class IndexLocal extends IndexAbstract {

	private ReaderLocal readerLocal = null;
	private WriterLocal writerLocal = null;
	private ReaderRemote readerRemote = null;
	private WriterRemote writerRemote = null;

	private volatile boolean online;

	private volatile boolean readonly;

	final private static Logger logger = Logger.getLogger(IndexLocal.class
			.getCanonicalName());

	public IndexLocal(File homeDir, IndexConfig indexConfig,
			boolean createIfNotExists) throws IOException {
		super(indexConfig);
		online = true;
		readonly = false;
		readerRemote = ReaderRemote.fromConfig(indexConfig);
		readerLocal = ReaderLocal.fromConfig(homeDir, indexConfig,
				createIfNotExists);
		writerRemote = WriterRemote.fromConfig(indexConfig);
		if (readerLocal != null) {
			writerLocal = WriterLocal.fromConfig(indexConfig);
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
		if (!online)
			throw new IOException("Index is offline");
		if (readonly)
			throw new IOException("Index is read only");
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
		if (!online)
			throw new IOException("Index is offline");
		if (readonly)
			throw new IOException("Index is read only");
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
		if (!online)
			throw new IOException("Index is offline");
		if (readonly)
			throw new IOException("Index is read only");
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

	public void deleteDocuments(Schema schema, List<String> uniqueFields,
			boolean forceLocal) throws CorruptIndexException,
			LockObtainFailedException, IOException {
		if (!online)
			throw new IOException("Index is offline");
		if (readonly)
			throw new IOException("Index is read only");
		r.lock();
		try {
			if (writerLocal != null)
				writerLocal.deleteDocuments(schema, uniqueFields, forceLocal);
			if (forceLocal)
				return;
			if (writerRemote != null)
				writerRemote.deleteDocuments(schema, uniqueFields, forceLocal);
		} finally {
			r.unlock();
		}
	}

	public void deleteDocuments(String indexName, Schema schema,
			List<String> uniqueFields, boolean forceLocal)
			throws CorruptIndexException, LockObtainFailedException,
			IOException {
		if (!online)
			throw new IOException("Index is offline");
		if (readonly)
			throw new IOException("Index is read only");
		r.lock();
		try {
			if (writerLocal != null)
				writerLocal.deleteDocuments(indexName, schema, uniqueFields,
						forceLocal);
			if (forceLocal)
				return;
			if (writerRemote != null)
				writerRemote.deleteDocuments(indexName, schema, uniqueFields,
						forceLocal);
		} finally {
			r.unlock();
		}
	}

	public void updateDocument(Schema schema, IndexDocument document,
			boolean forceLocal) throws NoSuchAlgorithmException, IOException {
		if (!online)
			throw new IOException("Index is offline");
		if (readonly)
			throw new IOException("Index is read only");
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
		if (!online)
			throw new IOException("Index is offline");
		if (readonly)
			throw new IOException("Index is read only");
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
		if (!online)
			throw new IOException("Index is offline");
		if (readonly)
			throw new IOException("Index is read only");
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
		if (!online)
			throw new IOException("Index is offline");
		if (readonly)
			throw new IOException("Index is read only");
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
		if (!online)
			throw new IOException("Index is offline");
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

	public void reload(String indexName) throws IOException {
		if (!online)
			throw new IOException("Index is offline");
		if (readonly)
			throw new IOException("Index is read only");
		if (!acceptNameOrEmpty(indexName))
			return;
		w.lock();
		try {
			if (readerLocal != null)
				readerLocal.reload();
			if (writerLocal != null && readerLocal != null)
				writerLocal.setDirectory(readerLocal.getDirectory());
			if (readerRemote != null)
				readerRemote.reload();
		} finally {
			w.unlock();
		}
	}

	public void swap(String indexName, long version, boolean deleteOld)
			throws IOException {
		if (!online)
			throw new IOException("Index is offline");
		if (readonly)
			throw new IOException("Index is read only");
		if (!acceptNameOrEmpty(indexName))
			return;
		w.lock();
		try {
			if (logger.isLoggable(Level.INFO))
				logger.info("Reload " + indexName + " " + deleteOld);
			if (readerLocal != null)
				readerLocal.swap(version, deleteOld);
			if (writerLocal != null && readerLocal != null)
				writerLocal.setDirectory(readerLocal.getDirectory());
			if (readerRemote != null)
				readerRemote.swap(version, deleteOld);
		} finally {
			w.unlock();
		}
	}

	public Result search(Request request) throws IOException, ParseException,
			SyntaxError {
		if (!online)
			throw new IOException("Index is offline");
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

	public IndexStatistics getStatistics() throws IOException {
		if (!online)
			throw new IOException("Index is offline");
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
		if (!online)
			throw new IOException("Index is offline");
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

	public void push(String indexName, URI dest) throws URISyntaxException,
			IOException {
		if (readerLocal == null)
			return;
		if (!acceptOnlyRightName(indexName))
			return;
		boolean oldReadOnly;
		w.lock();
		try {
			oldReadOnly = readonly;
			readonly = true;
		} finally {
			w.unlock();
		}
		r.lock();
		try {
			readerLocal.push(dest);
		} finally {
			r.unlock();
		}
		readonly = oldReadOnly;
	}

	public void receive(String indexName, long version, String fileName,
			InputStream inputStream) throws IOException {
		if (readerLocal == null)
			return;
		if (!acceptOnlyRightName(indexName))
			return;
		WriterLocal.receiveIndexFile(readerLocal.getRootDir(), version,
				fileName, inputStream);
	}

	@Override
	public boolean isOnline(String indexName) {
		return online;
	}

	@Override
	public boolean isReadOnly(String indexName) {
		return readonly;
	}

	@Override
	public void setOnline(String indexName, boolean v) {
		if (!acceptNameOrEmpty(indexName))
			return;
		online = v;
	}

	@Override
	public void setReadOnly(String indexName, boolean v) {
		if (!acceptNameOrEmpty(indexName))
			return;
		readonly = v;
	}

	public long getVersion(String indexName) {
		if (!online)
			return 0;
		if (!acceptNameOrEmpty(indexName))
			return 0;
		if (readerLocal == null)
			return 0;
		return readerLocal.getVersion();
	}
}
