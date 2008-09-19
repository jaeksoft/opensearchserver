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

import com.jaeksoft.searchlib.index.ReaderInterface;
import com.jaeksoft.searchlib.request.Request;

public class DocumentsThread implements Runnable {

	private ReaderInterface reader;
	private Request request;
	private Thread thread;
	private IOException e;
	private DocumentResult documentResult;

	public DocumentsThread(ReaderInterface reader, Request request)
			throws IOException {
		this.thread = null;
		this.reader = reader;
		this.request = request.clone();
		this.request.setReader(reader);
		this.e = null;
	}

	public void addDocId(ReaderInterface reader, int docId) {
		request.addDocId(reader, docId);
	}

	public void run() {
		try {
			documentResult = reader.documents(request);
		} catch (IOException e) {
			this.e = e;
			e.printStackTrace();
		}
	}

	public void waitForCompletion() {
		synchronized (this.thread) {
			while (this.thread.isAlive()) {
				try {
					this.thread.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public void exception() throws IOException {
		if (this.e != null)
			throw this.e;
	}

	public void start() {
		documentResult = null;
		thread = new Thread(this);
		thread.start();
	}

	public DocumentResult getDocumentResult() {
		return documentResult;
	}

}
