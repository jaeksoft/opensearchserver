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

package com.jaeksoft.searchlib.request;

import java.io.IOException;
import java.net.URISyntaxException;

import org.apache.lucene.queryParser.ParseException;

import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.index.IndexAbstract;
import com.jaeksoft.searchlib.index.ReaderInterface;
import com.jaeksoft.searchlib.result.ResultDocument;

public class DocumentsThread extends AbstractGroupRequestThread {

	private DocumentsRequest documentsRequest;

	private ReaderInterface reader;

	private ResultDocument[] groupResultDocuments;

	public DocumentsThread(ResultDocument[] groupResultDocuments,
			IndexAbstract index, DocumentsRequest documentsRequest)
			throws ParseException, SyntaxError, IOException {
		reader = index;
		this.groupResultDocuments = groupResultDocuments;
		this.documentsRequest = new DocumentsRequest(documentsRequest, index
				.getName());
	}

	@Override
	public boolean done() {
		return true;
	}

	@Override
	public void runner() throws IOException, URISyntaxException,
			ParseException, SyntaxError {
		ResultDocument[] documents = reader.documents(documentsRequest);
		int i = 0;
		for (DocumentRequest documentRequest : documentsRequest
				.getRequestedDocuments())
			groupResultDocuments[documentRequest.pos] = documents[i++];
	}

}
