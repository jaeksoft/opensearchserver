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

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.queryParser.ParseException;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.Logging;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.index.IndexDocument;
import com.jaeksoft.searchlib.process.ThreadAbstract;
import com.jaeksoft.searchlib.request.SearchRequest;
import com.jaeksoft.searchlib.util.StringUtils;

public class AutoCompletionBuildThread extends ThreadAbstract {

	private Client sourceClient;
	private Client autoCompClient;
	private String fieldName;
	private TermEnum termEnum;

	protected AutoCompletionBuildThread(Client sourceClient,
			Client autoCompClient) {
		super(sourceClient, null);
		this.sourceClient = sourceClient;
		this.autoCompClient = autoCompClient;
		this.fieldName = null;
		this.termEnum = null;
	}

	public String getStatus() {
		State state = getThreadState();
		if (state == null)
			return "STOPPED";
		return state.toString();
	}

	public int getIndexNumDocs() throws IOException {
		return autoCompClient.getIndex().getStatistics().getNumDocs();
	}

	final private void indexBuffer(List<IndexDocument> buffer)
			throws SearchLibException, NoSuchAlgorithmException, IOException,
			URISyntaxException, InstantiationException, IllegalAccessException,
			ClassNotFoundException {
		if (buffer.size() == 0)
			return;
		autoCompClient.updateDocuments(buffer);
		buffer.clear();
	}

	final private void truncateIndex() throws CorruptIndexException,
			SearchLibException, IOException, InstantiationException,
			IllegalAccessException, ClassNotFoundException, ParseException,
			SyntaxError, URISyntaxException, InterruptedException {
		SearchRequest searchRequest = autoCompClient.getNewSearchRequest();
		searchRequest.setQueryString("*:*");
		autoCompClient.deleteDocuments(searchRequest);
	}

	@Override
	public void runner() throws Exception {
		truncateIndex();
		if (fieldName == null)
			return;
		termEnum = sourceClient.getIndex().getTermEnum(fieldName, "");
		Term term = null;
		List<IndexDocument> buffer = new ArrayList<IndexDocument>();
		while ((term = termEnum.term()) != null) {
			System.out.println(term);
			if (!fieldName.equals(term.field()))
				break;
			IndexDocument indexDocument = new IndexDocument();
			indexDocument.addString("term", StringUtils.removeTag(term.text()));
			indexDocument.addString("freq",
					StringUtils.leftPad(termEnum.docFreq(), 9));
			buffer.add(indexDocument);
			if (buffer.size() == 50)
				indexBuffer(buffer);
			if (!termEnum.next())
				break;
		}
		indexBuffer(buffer);
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

	public void init(String fieldName) {
		this.fieldName = fieldName;

	}

}
