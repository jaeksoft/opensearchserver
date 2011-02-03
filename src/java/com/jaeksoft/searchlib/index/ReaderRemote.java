/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2008-2011 Emmanuel Keller / Jaeksoft
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
import org.apache.lucene.search.Query;
import org.apache.lucene.search.similar.MoreLikeThis;
import org.apache.lucene.store.LockObtainFailedException;

import com.jaeksoft.searchlib.SearchLibException;
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

	@Override
	public void reload() throws SearchLibException {
		try {
			ActionServlet.reload(uri);
		} catch (URISyntaxException e) {
			throw new SearchLibException(e);
		}
	}

	@Override
	public void swap(long version, boolean deleteOld) throws SearchLibException {
		try {
			ActionServlet.swap(uri, null, version, deleteOld);
		} catch (URISyntaxException e) {
			throw new SearchLibException(e);
		}
	}

	public void xmlInfo(PrintWriter writer) {
		// TODO Auto-generated method stub

	}

	@Override
	public Result search(SearchRequest searchRequest) throws SearchLibException {
		try {
			return SearchServlet.search(uri, searchRequest, null);
		} catch (IOException e) {
			throw new SearchLibException(e);
		} catch (URISyntaxException e) {
			throw new SearchLibException(e);
		}
	}

	@Override
	public ResultDocuments documents(DocumentsRequest documentRequest)
			throws SearchLibException {
		try {
			return DocumentsServlet.documents(uri, documentRequest);
		} catch (IOException e) {
			throw new SearchLibException(e);
		} catch (URISyntaxException e) {
			throw new SearchLibException(e);
		} catch (ClassNotFoundException e) {
			throw new SearchLibException(e);
		}
	}

	public boolean deleteDocument(int docId) throws StaleReaderException,
			CorruptIndexException, LockObtainFailedException, IOException,
			URISyntaxException, HttpException, SearchLibException {
		return DeleteServlet.deleteDocument(uri, null, docId);
	}

	public int deleteDocuments(Collection<Integer> docIds)
			throws StaleReaderException, CorruptIndexException,
			LockObtainFailedException, IOException, URISyntaxException {
		return DeleteServlet.deleteDocuments(uri, null, docIds);
	}

	@Override
	public boolean sameIndex(ReaderInterface reader) {
		if (reader == this)
			return true;
		return reader.sameIndex(this);
	}

	@Override
	public IndexStatistics getStatistics() {
		// TODO Auto-generated method stub
		throw new RuntimeException("Not yet implemented");
	}

	@Override
	public int getDocFreq(Term term) {
		// TODO Auto-generated method stub
		throw new RuntimeException("Not yet implemented");
	}

	@Override
	public TermFreqVector getTermFreqVector(int docId, String field) {
		throw new RuntimeException("Not yet implemented");
	}

	@Override
	public long getVersion() {
		// TODO Auto-generated method stub
		throw new RuntimeException("Not yet implemented");
	}

	@Override
	public void push(URI dest) {
		// TODO Auto-generated method stub
		throw new RuntimeException("Not yet implemented");
	}

	@Override
	public TermEnum getTermEnum() {
		throw new RuntimeException("Not yet implemented");
	}

	@Override
	public TermEnum getTermEnum(String field, String term) {
		throw new RuntimeException("Not yet implemented");
	}

	@Override
	public Collection<?> getFieldNames() {
		throw new RuntimeException("Not yet implemented");
	}

	@Override
	public String explain(SearchRequest searchRequest, int docId) {
		throw new RuntimeException("Not yet implemented");
	}

	@Override
	public Query rewrite(Query query) {
		throw new RuntimeException("Not yet implemented");
	}

	@Override
	public MoreLikeThis getMoreLikeThis() throws SearchLibException {
		throw new RuntimeException("Not yet implemented");
	}

}
