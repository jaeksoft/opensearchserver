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
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;

import org.apache.http.HttpException;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.StaleReaderException;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermFreqVector;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.store.LockObtainFailedException;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.request.DocumentsRequest;
import com.jaeksoft.searchlib.request.SearchRequest;
import com.jaeksoft.searchlib.result.Result;
import com.jaeksoft.searchlib.result.ResultDocuments;

public interface ReaderInterface {

	public abstract boolean sameIndex(ReaderInterface reader);

	public void close();

	public int getDocFreq(Term term) throws IOException;

	public TermFreqVector getTermFreqVector(int docId, String field)
			throws IOException;

	public Result search(SearchRequest searchRequest) throws IOException,
			URISyntaxException, ParseException, SyntaxError,
			ClassNotFoundException, InterruptedException, SearchLibException,
			InstantiationException, IllegalAccessException;

	public ResultDocuments documents(DocumentsRequest documentsRequest)
			throws IOException, ParseException, SyntaxError,
			URISyntaxException, ClassNotFoundException, InterruptedException,
			SearchLibException, IllegalAccessException, InstantiationException;

	public boolean deleteDocument(int docId) throws StaleReaderException,
			CorruptIndexException, LockObtainFailedException, IOException,
			URISyntaxException, HttpException;

	public boolean deleteDocument(String indexName, int docId)
			throws StaleReaderException, CorruptIndexException,
			LockObtainFailedException, IOException, URISyntaxException,
			HttpException;

	public int deleteDocuments(Collection<Integer> docIds)
			throws StaleReaderException, CorruptIndexException,
			LockObtainFailedException, IOException, URISyntaxException;

	public int deleteDocuments(String indexName, Collection<Integer> docIds)
			throws StaleReaderException, CorruptIndexException,
			LockObtainFailedException, IOException, URISyntaxException;

	public String getName();

	public IndexStatistics getStatistics() throws IOException;

	public void reload() throws IOException, URISyntaxException,
			InstantiationException, IllegalAccessException,
			ClassNotFoundException, HttpException;

	public void swap(long version, boolean deleteOld) throws IOException,
			URISyntaxException, HttpException;

	public void push(URI dest) throws URISyntaxException, IOException;

	public long getVersion();
}
