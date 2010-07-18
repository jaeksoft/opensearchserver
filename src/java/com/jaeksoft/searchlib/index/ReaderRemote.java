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

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;

import org.apache.http.HttpException;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.StaleReaderException;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.index.TermFreqVector;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.LockObtainFailedException;

import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.request.DocumentsRequest;
import com.jaeksoft.searchlib.request.SearchRequest;
import com.jaeksoft.searchlib.result.Result;
import com.jaeksoft.searchlib.result.ResultDocuments;
import com.jaeksoft.searchlib.web.ActionServlet;
import com.jaeksoft.searchlib.web.DeleteServlet;
import com.jaeksoft.searchlib.web.DocumentsServlet;
import com.jaeksoft.searchlib.web.SearchServlet;

public class ReaderRemote extends ReaderAbstract implements ReaderInterface {

	private URI uri;

	protected ReaderRemote(IndexConfig indexConfig) {
		this.uri = indexConfig.getRemoteUri();
	}

	@Override
	public void close() {
	}

	// TODO Implementation
	public void reload() throws IOException, URISyntaxException, HttpException {
		ActionServlet.reload(uri);
	}

	// TODO Propagate version information
	public void swap(long version, boolean deleteOld) throws IOException,
			URISyntaxException, HttpException {
		ActionServlet.swap(uri, null, version, deleteOld);
	}

	public void xmlInfo(PrintWriter writer) {
		// TODO Auto-generated method stub

	}

	public Result search(SearchRequest searchRequest) throws IOException,
			URISyntaxException {
		return SearchServlet.search(uri, searchRequest, null);
	}

	public ResultDocuments documents(DocumentsRequest documentRequest)
			throws IOException, URISyntaxException, ClassNotFoundException {
		return DocumentsServlet.documents(uri, documentRequest);
	}

	public boolean deleteDocument(int docId) throws StaleReaderException,
			CorruptIndexException, LockObtainFailedException, IOException,
			URISyntaxException, HttpException {
		return DeleteServlet.deleteDocument(uri, null, docId);
	}

	public int deleteDocuments(Collection<Integer> docIds)
			throws StaleReaderException, CorruptIndexException,
			LockObtainFailedException, IOException, URISyntaxException {
		return DeleteServlet.deleteDocuments(uri, null, docIds);
	}

	public boolean sameIndex(ReaderInterface reader) {
		if (reader == this)
			return true;
		return reader.sameIndex(this);
	}

	public IndexStatistics getStatistics() {
		// TODO Auto-generated method stub
		throw new RuntimeException("Not yet implemented");
	}

	public int getDocFreq(Term term) throws IOException {
		// TODO Auto-generated method stub
		throw new RuntimeException("Not yet implemented");
	}

	public TermFreqVector getTermFreqVector(int docId, String field)
			throws IOException {
		throw new RuntimeException("Not yet implemented");
	}

	public long getVersion() {
		// TODO Auto-generated method stub
		throw new RuntimeException("Not yet implemented");
	}

	public void push(URI dest) throws URISyntaxException, IOException {
		// TODO Auto-generated method stub
		throw new RuntimeException("Not yet implemented");
	}

	@Override
	public TermEnum getTermEnum() throws IOException {
		throw new RuntimeException("Not yet implemented");
	}

	@Override
	public TermEnum getTermEnum(String field, String term) throws IOException {
		throw new RuntimeException("Not yet implemented");
	}

	@Override
	public Collection<?> getFieldNames() {
		throw new RuntimeException("Not yet implemented");
	}

	@Override
	public String explain(SearchRequest searchRequest, int docId)
			throws IOException, ParseException, SyntaxError {
		throw new RuntimeException("Not yet implemented");
	}

	@Override
	public Query rewrite(Query query) throws IOException {
		throw new RuntimeException("Not yet implemented");
	}

}
