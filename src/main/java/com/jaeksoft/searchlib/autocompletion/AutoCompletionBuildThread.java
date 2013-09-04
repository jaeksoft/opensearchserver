/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2012-2013 Emmanuel Keller / Jaeksoft
 * 
 * http://www.open-search-server.com
 * 
 * This file is part of OpenSearchServer.
 *
 * OpenSearchServer is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * OpenSearchServer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with OpenSearchServer. 
 *  If not, see <http://www.gnu.org/licenses/>.
 **/

package com.jaeksoft.searchlib.autocompletion;

import java.io.IOException;
import java.lang.Thread.State;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.util.OpenBitSet;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.Logging;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.index.IndexDocument;
import com.jaeksoft.searchlib.index.IndexStatistics;
import com.jaeksoft.searchlib.process.ThreadAbstract;
import com.jaeksoft.searchlib.request.AbstractRequest;
import com.jaeksoft.searchlib.request.AbstractSearchRequest;
import com.jaeksoft.searchlib.result.AbstractResultSearch;
import com.jaeksoft.searchlib.result.collector.DocIdInterface;
import com.jaeksoft.searchlib.util.InfoCallback;

public class AutoCompletionBuildThread extends
		ThreadAbstract<AutoCompletionBuildThread> {

	private volatile Client sourceClient;
	private volatile Client autoCompClient;
	private volatile String searchRequest;
	private volatile String[] fieldNames;
	private volatile TermEnum termEnum;
	private volatile InfoCallback infoCallBack;
	private volatile int bufferSize;

	protected AutoCompletionBuildThread(Client sourceClient,
			Client autoCompClient) {
		super(sourceClient, null, null);
		this.sourceClient = sourceClient;
		this.autoCompClient = autoCompClient;
		this.fieldNames = null;
		this.searchRequest = null;
		this.termEnum = null;
		this.bufferSize = 50;
	}

	public String getStatus() {
		State state = getThreadState();
		if (state == null)
			return "STOPPED";
		return state.toString();
	}

	public int getIndexNumDocs() throws IOException, SearchLibException {
		IndexStatistics stats = autoCompClient.getStatistics();
		if (stats == null)
			return 0;
		return stats.getNumDocs();
	}

	final private int indexBuffer(int docCount, List<IndexDocument> buffer)
			throws SearchLibException, NoSuchAlgorithmException, IOException,
			URISyntaxException, InstantiationException, IllegalAccessException,
			ClassNotFoundException {
		if (buffer.size() == 0)
			return docCount;
		docCount += autoCompClient.updateDocuments(buffer);
		buffer.clear();
		if (infoCallBack != null)
			infoCallBack.setInfo(docCount + " term(s) indexed");
		return docCount;
	}

	private int indexTerm(String term, Integer freq,
			List<IndexDocument> buffer, int docCount)
			throws NoSuchAlgorithmException, SearchLibException, IOException,
			URISyntaxException, InstantiationException, IllegalAccessException,
			ClassNotFoundException {
		IndexDocument indexDocument = new IndexDocument();
		indexDocument.addString("term", term);
		indexDocument.addString("cluster", term);
		if (freq != null)
			indexDocument.addString("freq", Integer.toString(freq));
		buffer.add(indexDocument);
		if (buffer.size() == bufferSize)
			docCount = indexBuffer(docCount, buffer);
		return docCount;
	}

	private int buildTermEnum(List<IndexDocument> buffer, int docCount)
			throws SearchLibException, NoSuchAlgorithmException, IOException,
			URISyntaxException, InstantiationException, IllegalAccessException,
			ClassNotFoundException {
		if (fieldNames == null)
			return docCount;
		for (String fieldName : fieldNames) {
			termEnum = sourceClient.getTermEnum(new Term(fieldName, ""));
			Term term = null;
			while ((term = termEnum.term()) != null) {
				if (!fieldName.equals(term.field()))
					break;
				docCount = indexTerm(term.text(), termEnum.docFreq(), buffer,
						docCount);
				termEnum.next();
			}
			termEnum.close();
		}
		return docCount;
	}

	private int buildSearchRequest(List<IndexDocument> buffer, int docCount)
			throws SearchLibException, IOException, NoSuchAlgorithmException,
			URISyntaxException, InstantiationException, IllegalAccessException,
			ClassNotFoundException {
		if (fieldNames == null)
			return docCount;
		AbstractRequest request = sourceClient.getNewRequest(searchRequest);
		if (request == null)
			throw new SearchLibException("Request not found " + searchRequest);
		if (!(request instanceof AbstractSearchRequest))
			throw new SearchLibException("The request " + searchRequest
					+ " is not a Search request ");
		AbstractSearchRequest searchRequest = (AbstractSearchRequest) request;
		searchRequest.setRows(0);
		AbstractResultSearch result = (AbstractResultSearch) sourceClient
				.request(request);
		if (result == null)
			return docCount;
		DocIdInterface docIds = result.getDocs();
		if (docIds == null)
			return docCount;
		OpenBitSet openBitSet = docIds.getBitSet();
		if (openBitSet == null || openBitSet.size() == 0)
			return docCount;
		for (String fieldName : fieldNames) {
			termEnum = sourceClient.getTermEnum(new Term(fieldName, ""));
			Term term = null;
			while ((term = termEnum.term()) != null) {
				if (!fieldName.equals(term.field()))
					break;
				TermDocs termDocs = sourceClient.getIndex().getTermDocs(term);
				boolean add = false;
				while (termDocs.next() && !add)
					add = openBitSet.fastGet(termDocs.doc());
				if (add)
					docCount = indexTerm(term.text(), termEnum.docFreq(),
							buffer, docCount);
				termEnum.next();
			}
			termEnum.close();
		}
		return docCount;
	}

	@Override
	public void runner() throws Exception {
		autoCompClient.deleteAll();
		List<IndexDocument> buffer = new ArrayList<IndexDocument>();
		int docCount = 0;
		if (searchRequest != null && searchRequest.length() > 0)
			docCount = buildSearchRequest(buffer, docCount);
		else
			docCount = buildTermEnum(buffer, docCount);
		docCount = indexBuffer(docCount, buffer);
		autoCompClient.optimize();
	}

	@Override
	public void release() {
		if (termEnum != null) {
			try {
				termEnum.close();
			} catch (IOException e) {
				Logging.warn(e);
			}
			termEnum = null;
		}
	}

	public void init(Collection<String> fieldNames, String searchRequest,
			int bufferSize, InfoCallback infoCallBack) {
		this.fieldNames = fieldNames.toArray(new String[fieldNames.size()]);
		this.searchRequest = searchRequest;
		this.infoCallBack = infoCallBack;
		this.bufferSize = bufferSize;
	}

}
