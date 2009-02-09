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

package com.jaeksoft.searchlib.index;

import java.io.IOException;
import java.net.URISyntaxException;

import org.apache.lucene.queryParser.ParseException;

import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.request.Request;
import com.jaeksoft.searchlib.result.ResultGroup;
import com.jaeksoft.searchlib.result.ResultSingle;
import com.jaeksoft.searchlib.util.ThreadUtils;

public class SearchThread implements Runnable {

	private ResultGroup resultGroup;
	private ResultSingle resultSingle;
	private ReaderInterface reader;
	private Request request;
	private boolean running;
	private int currentFetchPos;
	private IOException ioException;
	private URISyntaxException uriSyntaxException;
	private ParseException parseException;
	private SyntaxError syntaxError;
	private int fetchCount;

	public SearchThread(ReaderInterface reader, Request request,
			ResultGroup resultGroup) throws IOException {
		this.running = false;
		this.reader = reader;
		this.resultSingle = null;
		this.resultGroup = resultGroup;
		this.request = request;
		this.currentFetchPos = 0;
		this.ioException = null;
		this.uriSyntaxException = null;
		this.parseException = null;
		this.syntaxError = null;
	}

	public void run() {
		try {
			if (resultSingle == null) {
				resultSingle = (ResultSingle) reader.search(request);
				resultGroup.addResult(resultSingle);
			}
			int nextFetchPos = currentFetchPos + fetchCount;
			resultSingle.loadDocs(nextFetchPos);
			int docLength = resultSingle.getDocLength();
			if (nextFetchPos > docLength)
				nextFetchPos = docLength;
			fetchCount = nextFetchPos - currentFetchPos;
			if (fetchCount == 0)
				return;
			resultGroup.populate(resultSingle, currentFetchPos, fetchCount);
			currentFetchPos = nextFetchPos;
		} catch (IOException e) {
			this.ioException = e;
		} catch (URISyntaxException e) {
			this.uriSyntaxException = e;
		} catch (ParseException e) {
			this.parseException = e;
		} catch (SyntaxError e) {
			this.syntaxError = e;
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
					wait(5000);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public void searchNextStep(int step) {
		fetchCount = step;
		running = true;
		ThreadUtils.pool.execute(this);
	}

	public int getFetchCount() {
		return fetchCount;
	}

	public void exception() throws IOException, URISyntaxException,
			ParseException, SyntaxError {
		if (ioException != null)
			throw ioException;
		if (parseException != null)
			throw parseException;
		if (uriSyntaxException != null)
			throw uriSyntaxException;
		if (syntaxError != null)
			throw syntaxError;
	}

}
