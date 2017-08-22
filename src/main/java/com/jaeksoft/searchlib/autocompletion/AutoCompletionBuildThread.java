/*
 * License Agreement for OpenSearchServer
 * <p>
 * Copyright (C) 2012-2017 Emmanuel Keller / Jaeksoft
 * <p>
 * http://www.open-search-server.com
 * <p>
 * This file is part of OpenSearchServer.
 * <p>
 * OpenSearchServer is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * OpenSearchServer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with OpenSearchServer.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package com.jaeksoft.searchlib.autocompletion;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.Logging;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.index.IndexDocument;
import com.jaeksoft.searchlib.process.ThreadAbstract;
import com.jaeksoft.searchlib.request.AbstractRequest;
import com.jaeksoft.searchlib.request.AbstractSearchRequest;
import com.jaeksoft.searchlib.result.AbstractResultSearch;
import com.jaeksoft.searchlib.result.collector.DocIdInterface;
import com.jaeksoft.searchlib.util.InfoCallback;
import com.qwazr.utils.FunctionUtils;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.index.TermEnum;
import org.roaringbitmap.RoaringBitmap;

import java.io.IOException;
import java.lang.Thread.State;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class AutoCompletionBuildThread extends ThreadAbstract<AutoCompletionBuildThread> {

	private volatile Client sourceClient;
	private volatile Client autoCompClient;
	private volatile String searchRequest;
	private volatile String[] fieldNames;
	private volatile TermEnum termEnum;
	private volatile int bufferSize;

	protected AutoCompletionBuildThread(Client sourceClient, Client autoCompClient, InfoCallback infoCallBack) {
		super(sourceClient, null, null, infoCallBack);
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
		return autoCompClient.getStatistics().getNumDocs();
	}

	final private int indexBuffer(int docCount, List<IndexDocument> buffer) throws IOException, SearchLibException {
		if (buffer.size() == 0)
			return docCount;
		docCount += autoCompClient.updateDocuments(buffer);
		buffer.clear();
		if (infoCallback != null)
			infoCallback.setInfo(docCount + " term(s) indexed");
		return docCount;
	}

	private int indexTerm(String term, Integer freq, List<IndexDocument> buffer, int docCount)
			throws IOException, SearchLibException {
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

	private class TermConsumer implements FunctionUtils.ConsumerEx2<TermEnum, IOException, SearchLibException> {

		private int docCount;
		private final String fieldName;
		private final List<IndexDocument> buffer;

		private TermConsumer(int docCount, String fieldName, List<IndexDocument> buffer) {
			this.docCount = docCount;
			this.fieldName = fieldName;
			this.buffer = buffer;
		}

		@Override
		public void accept(TermEnum termEnum) throws IOException, SearchLibException {
			Term term;
			while ((term = termEnum.term()) != null) {
				if (!fieldName.equals(term.field()))
					break;
				if (isAborted())
					break;
				docCount = indexTerm(term.text(), termEnum.docFreq(), buffer, docCount);
				termEnum.next();
			}
		}
	}

	private int buildTermEnum(List<IndexDocument> buffer, int docCount)
			throws SearchLibException, NoSuchAlgorithmException, IOException, URISyntaxException,
			InstantiationException, IllegalAccessException, ClassNotFoundException {
		if (fieldNames == null)
			return docCount;
		for (String fieldName : fieldNames) {
			final TermConsumer termConsumer = new TermConsumer(docCount, fieldName, buffer);
			sourceClient.termEnum(new Term(fieldName, ""), termConsumer);
			docCount = termConsumer.docCount;
		}
		return docCount;
	}

	private class SearchTermConsumer implements FunctionUtils.ConsumerEx2<TermEnum, IOException, SearchLibException> {

		private final List<IndexDocument> buffer;
		private int docCount;
		private final RoaringBitmap bitSet;
		private final String fieldName;

		private SearchTermConsumer(List<IndexDocument> buffer, int docCount, final RoaringBitmap bitSet,
				final String fieldName) {
			this.buffer = buffer;
			this.docCount = docCount;
			this.bitSet = bitSet;
			this.fieldName = fieldName;
		}

		@Override
		public void accept(TermEnum termEnum) throws IOException, SearchLibException {
			Term term;
			while ((term = termEnum.term()) != null) {
				if (isAborted())
					break;
				if (!fieldName.equals(term.field()))
					break;
				final TermDocsConsumer termDocsConsumer = new TermDocsConsumer();
				sourceClient.getIndex().termDocs(term, termDocsConsumer);
				if (termDocsConsumer.add)
					docCount = indexTerm(term.text(), termEnum.docFreq(), buffer, docCount);
				termEnum.next();
			}
		}

		private class TermDocsConsumer implements FunctionUtils.ConsumerEx<TermDocs, IOException> {

			private boolean add = false;

			@Override
			public void accept(TermDocs termDocs) throws IOException {
				while (termDocs.next() && !add)
					add = bitSet.contains(termDocs.doc());
			}
		}
	}

	private int buildSearchRequest(List<IndexDocument> buffer, int docCount)
			throws SearchLibException, IOException, NoSuchAlgorithmException, URISyntaxException,
			InstantiationException, IllegalAccessException, ClassNotFoundException {
		if (fieldNames == null)
			return docCount;
		AbstractRequest request = sourceClient.getNewRequest(searchRequest);
		if (request == null)
			throw new SearchLibException("Request not found " + searchRequest);
		if (!(request instanceof AbstractSearchRequest))
			throw new SearchLibException("The request " + searchRequest + " is not a Search request ");
		AbstractSearchRequest searchRequest = (AbstractSearchRequest) request;
		searchRequest.setRows(0);
		AbstractResultSearch<?> result = (AbstractResultSearch<?>) sourceClient.request(request);
		if (result == null)
			return docCount;
		DocIdInterface docIds = result.getDocs();
		if (docIds == null)
			return docCount;
		RoaringBitmap bitSet = docIds.getBitSet();
		if (bitSet == null || bitSet.isEmpty())
			return docCount;
		for (String fieldName : fieldNames) {
			final SearchTermConsumer termEnumConsumer = new SearchTermConsumer(buffer, docCount, bitSet, fieldName);
			sourceClient.termEnum(new Term(fieldName, ""), termEnumConsumer);
			docCount = termEnumConsumer.docCount;
			if (isAborted())
				break;
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

	public void init(Collection<String> fieldNames, String searchRequest, int bufferSize) {
		this.fieldNames = fieldNames.toArray(new String[fieldNames.size()]);
		this.searchRequest = searchRequest;
		this.bufferSize = bufferSize;
	}

}
