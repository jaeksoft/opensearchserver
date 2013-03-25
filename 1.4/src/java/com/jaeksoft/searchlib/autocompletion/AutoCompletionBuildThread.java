/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2012 Emmanuel Keller / Jaeksoft
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
import java.util.List;

import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermEnum;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.Logging;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.index.IndexDocument;
import com.jaeksoft.searchlib.process.ThreadAbstract;
import com.jaeksoft.searchlib.util.InfoCallback;

public class AutoCompletionBuildThread extends
		ThreadAbstract<AutoCompletionBuildThread> {

	private volatile Client sourceClient;
	private volatile Client autoCompClient;
	private volatile String fieldName;
	private volatile TermEnum termEnum;
	private volatile InfoCallback infoCallBack;
	private volatile int bufferSize;

	protected AutoCompletionBuildThread(Client sourceClient,
			Client autoCompClient) {
		super(sourceClient, null, null);
		this.sourceClient = sourceClient;
		this.autoCompClient = autoCompClient;
		this.fieldName = null;
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

	final private int indexBuffer(int docCount, List<IndexDocument> buffer)
			throws SearchLibException, NoSuchAlgorithmException, IOException,
			URISyntaxException, InstantiationException, IllegalAccessException,
			ClassNotFoundException {
		if (buffer.size() == 0)
			return docCount;
		docCount += autoCompClient.updateDocuments(buffer);
		buffer.clear();
		if (infoCallBack != null) {
			infoCallBack.setInfo(docCount + " term(s) indexed");

		}
		return docCount;
	}

	@Override
	public void runner() throws Exception {
		autoCompClient.deleteAll();
		if (fieldName == null)
			return;
		termEnum = sourceClient.getTermEnum(fieldName, "");
		Term term = null;
		List<IndexDocument> buffer = new ArrayList<IndexDocument>();
		int docCount = 0;
		while ((term = termEnum.term()) != null) {
			if (!fieldName.equals(term.field()))
				break;
			IndexDocument indexDocument = new IndexDocument();
			String t = term.text();
			indexDocument.addString("term", t);
			indexDocument.addString("cluster", t);
			indexDocument.addString("freq",
					Integer.toString(termEnum.docFreq()));
			buffer.add(indexDocument);
			if (buffer.size() == bufferSize)
				docCount = indexBuffer(docCount, buffer);
			if (!termEnum.next())
				break;
		}
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

	public void init(String fieldName, int bufferSize, InfoCallback infoCallBack) {
		this.fieldName = fieldName;
		this.infoCallBack = infoCallBack;
		this.bufferSize = bufferSize;
	}

}
