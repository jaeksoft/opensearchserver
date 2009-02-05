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

import org.apache.lucene.queryParser.ParseException;

import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.index.ReaderInterface;
import com.jaeksoft.searchlib.request.Request;
import com.jaeksoft.searchlib.util.ThreadUtils;

public class DocumentsThread implements Runnable {

	private ReaderInterface reader;
	private Request request;
	private boolean running;
	private IOException ioException;
	private ParseException parseException;
	private SyntaxError syntaxError;
	private DocumentResult documentResult;

	public DocumentsThread(ReaderInterface reader, Request request)
			throws IOException {
		this.running = false;
		this.reader = reader;
		this.request = request.clone();
		this.ioException = null;
		this.parseException = null;
		this.syntaxError = null;
	}

	public void add(ResultScoreDoc resultScoreDoc) {
		request.addDocId(resultScoreDoc.resultSearch.getReader(),
				resultScoreDoc.doc);
	}

	public void run() {
		try {
			documentResult = reader.documents(request);
		} catch (IOException e) {
			this.ioException = e;
			e.printStackTrace();
		} catch (ParseException e) {
			this.parseException = e;
			e.printStackTrace();
		} catch (SyntaxError e) {
			this.syntaxError = e;
			e.printStackTrace();
		} finally {
			running = false;
			synchronized (this) {
				notifyAll();
			}
		}
	}

	public void waitForCompletion() {
		while (running) {
			try {
				synchronized (this) {
					wait(1000);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public void exception() throws IOException, ParseException, SyntaxError {
		if (this.ioException != null)
			throw this.ioException;
		if (this.parseException != null)
			throw this.parseException;
		if (this.syntaxError != null)
			throw this.syntaxError;
	}

	public void start() {
		documentResult = null;
		running = true;
		ThreadUtils.pool.execute(this);
	}

	public DocumentResult getDocumentResult() {
		return documentResult;
	}

}
