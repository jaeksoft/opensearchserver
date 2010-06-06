/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2008-2010 Emmanuel Keller / Jaeksoft
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
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.StaleReaderException;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.index.TermFreqVector;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.store.LockObtainFailedException;

import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.index.osse.OsseLibrary;
import com.jaeksoft.searchlib.request.DocumentsRequest;
import com.jaeksoft.searchlib.request.SearchRequest;
import com.jaeksoft.searchlib.result.Result;
import com.jaeksoft.searchlib.result.ResultDocuments;
import com.sun.jna.Pointer;

public class ReaderNativeOSSE extends ReaderAbstract {

	private Pointer index;

	protected ReaderNativeOSSE(File configDir, IndexConfig indexConfig) {
		super(configDir.getName());
		index = OsseLibrary.INSTANCE.index_new();
	}

	public void close() {
		OsseLibrary.INSTANCE.index_delete(index);
	}

	protected Pointer getIndex() {
		return index;
	}

	@Override
	public boolean deleteDocument(int docId) throws StaleReaderException,
			CorruptIndexException, LockObtainFailedException, IOException,
			URISyntaxException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int deleteDocuments(Collection<Integer> docIds)
			throws StaleReaderException, CorruptIndexException,
			LockObtainFailedException, IOException, URISyntaxException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public ResultDocuments documents(DocumentsRequest documentsRequest)
			throws IOException, ParseException, SyntaxError,
			URISyntaxException, ClassNotFoundException, InterruptedException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getDocFreq(Term term) throws IOException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public IndexStatistics getStatistics() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TermFreqVector getTermFreqVector(int docId, String field)
			throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getVersion() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void push(URI dest) throws URISyntaxException, IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void reload() throws IOException, URISyntaxException {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean sameIndex(ReaderInterface reader) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Result search(SearchRequest searchRequest) throws IOException,
			URISyntaxException, ParseException, SyntaxError,
			ClassNotFoundException, InterruptedException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void swap(long version, boolean deleteOld) throws IOException,
			URISyntaxException {
		// TODO Auto-generated method stub

	}

	@Override
	public TermEnum getTermEnum() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TermEnum getTermEnum(String field, String term) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<?> getFieldNames() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String explain(SearchRequest searchRequest, int docId)
			throws IOException, ParseException, SyntaxError {
		// TODO Auto-generated method stub
		return null;
	}

}
