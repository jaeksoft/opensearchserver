/**   
 * License Agreement for Jaeksoft SearchLib Community
 *
 * Copyright (C) 2008 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.result;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.lucene.queryParser.ParseException;

import com.jaeksoft.searchlib.cache.DocumentCache;
import com.jaeksoft.searchlib.index.ReaderInterface;
import com.jaeksoft.searchlib.index.ReaderLocal;
import com.jaeksoft.searchlib.request.Request;

public class DocumentsGroup {

	private class Item {

		private DocumentsThread docsThread;
		private String key;

		private Item(DocumentsThread docsThread, ReaderInterface reader,
				int docId) {
			this.docsThread = docsThread;
			this.key = DocumentCache.getKey(request.getName(),
					reader.getName(), docId);
		}
	}

	private HashMap<ReaderInterface, DocumentsThread> docsThreads;
	private ArrayList<Item> docIds;
	private Request request;

	public DocumentsGroup(Request request) {
		docsThreads = new HashMap<ReaderInterface, DocumentsThread>();
		docIds = new ArrayList<Item>();
		this.request = request;
	}

	protected void add(ResultScoreDoc resultScoreDoc) throws IOException {
		ReaderLocal reader = resultScoreDoc.resultSearch.getReader();
		DocumentsThread docsThread = docsThreads.get(reader);
		if (docsThread == null) {
			docsThread = new DocumentsThread(reader, request);
			docsThreads.put(reader, docsThread);
		}
		docsThread.add(resultScoreDoc);
		docIds.add(new Item(docsThread, reader, resultScoreDoc.doc));
	}

	public DocumentResult documents() throws IOException, ParseException {
		for (DocumentsThread docsThread : docsThreads.values())
			docsThread.start();
		for (DocumentsThread docsThread : docsThreads.values()) {
			docsThread.waitForCompletion();
			docsThread.exception();
		}
		DocumentResult documentResult = new DocumentResult();
		for (Item item : docIds)
			documentResult.add(item.docsThread.getDocumentResult()
					.get(item.key));
		return documentResult;
	}

}
