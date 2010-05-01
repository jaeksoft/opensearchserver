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

package com.jaeksoft.searchlib.request;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutorService;

import org.apache.lucene.queryParser.ParseException;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.index.IndexAbstract;
import com.jaeksoft.searchlib.index.IndexGroup;
import com.jaeksoft.searchlib.result.ResultDocuments;

public class DocumentsGroup extends AbstractGroupRequest<DocumentsThread> {

	private DocumentsRequest documentsRequest;

	private ResultDocuments resultDocuments;

	public DocumentsGroup(IndexGroup indexGroup,
			DocumentsRequest documentsRequest, ExecutorService threadPool)
			throws IOException, URISyntaxException, ParseException,
			SyntaxError, ClassNotFoundException, InterruptedException,
			SearchLibException, IllegalAccessException, InstantiationException {
		super(indexGroup, threadPool, 60);
		this.documentsRequest = documentsRequest;
		DocumentRequest[] requestedDocuments = documentsRequest
				.getRequestedDocuments();
		if (requestedDocuments == null)
			return;
		resultDocuments = new ResultDocuments(requestedDocuments.length);
		run();
	}

	@Override
	protected boolean complete(DocumentsThread thread) {
		return true;
	}

	@Override
	protected void complete() throws IOException, URISyntaxException,
			ParseException, SyntaxError {
	}

	@Override
	protected DocumentsThread getNewThread(IndexAbstract index)
			throws ParseException, SyntaxError, IOException {
		return new DocumentsThread(resultDocuments, index, documentsRequest);
	}

	public ResultDocuments getDocuments() {
		return resultDocuments;
	}

}
