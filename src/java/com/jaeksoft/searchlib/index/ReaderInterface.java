/**   
 * License Agreement for Jaeksoft SearchLib Community
 *
 * Copyright (C) 2008-2009 Emmanuel Keller / Jaeksoft
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

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.StaleReaderException;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermFreqVector;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.store.LockObtainFailedException;

import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.request.DocumentsRequest;
import com.jaeksoft.searchlib.request.SearchRequest;
import com.jaeksoft.searchlib.result.Result;
import com.jaeksoft.searchlib.result.ResultDocuments;
import com.jaeksoft.searchlib.util.XmlInfo;

public interface ReaderInterface extends XmlInfo {

	public abstract boolean sameIndex(ReaderInterface reader);

	public int getDocFreq(Term term) throws IOException;

	public TermFreqVector getTermFreqVector(int docId, String field)
			throws IOException;

	public Result search(SearchRequest searchRequest) throws IOException,
			URISyntaxException, ParseException, SyntaxError,
			ClassNotFoundException;

	public ResultDocuments documents(DocumentsRequest documentsRequest)
			throws IOException, ParseException, SyntaxError,
			URISyntaxException, ClassNotFoundException;

	public boolean deleteDocument(int docId) throws StaleReaderException,
			CorruptIndexException, LockObtainFailedException, IOException,
			URISyntaxException;

	public boolean deleteDocument(String indexName, int docId)
			throws StaleReaderException, CorruptIndexException,
			LockObtainFailedException, IOException, URISyntaxException;

	public int deleteDocuments(Collection<Integer> docIds)
			throws StaleReaderException, CorruptIndexException,
			LockObtainFailedException, IOException, URISyntaxException;

	public int deleteDocuments(String indexName, Collection<Integer> docIds)
			throws StaleReaderException, CorruptIndexException,
			LockObtainFailedException, IOException, URISyntaxException;

	public String getName();

	public IndexStatistics getStatistics() throws IOException;

	public void reload() throws IOException, URISyntaxException;

	public void swap(long version, boolean deleteOld) throws IOException,
			URISyntaxException;

	public void push(URI dest) throws URISyntaxException, IOException;

	public long getVersion();
}
