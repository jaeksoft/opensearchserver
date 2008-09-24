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

import org.apache.lucene.queryParser.ParseException;

import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.request.Request;
import com.jaeksoft.searchlib.result.ResultGroup;
import com.jaeksoft.searchlib.result.ResultSearch;

public class SearchThread implements Runnable {

	private ResultGroup resultGroup;
	private ResultSearch resultSearch;
	private ReaderInterface reader;
	private Request request;
	private Thread thread;
	private int step;
	private float rawScoreGoal;
	private int currentFetchPos;
	private IOException ioException;
	private ParseException parseException;
	private SyntaxError syntaxError;
	private int fetchCount;

	public SearchThread(ReaderInterface reader, Request request,
			ResultGroup resultGroup) throws IOException {
		this.thread = null;
		this.reader = reader;
		this.resultSearch = null;
		this.resultGroup = resultGroup;
		this.request = request;
		this.currentFetchPos = 0;
		this.ioException = null;
		this.parseException = null;
		this.syntaxError = null;
	}

	public void run() {
		try {
			if (resultSearch == null) {
				resultSearch = (ResultSearch) reader.search(request);
				resultGroup.addResult(resultSearch);
			} else {
				if (resultSearch.getFetchedDoc().length > 0)
					if (resultSearch
							.getScore(this.resultSearch.getFetchedDoc().length - 1) < rawScoreGoal)
						return;
			}
			resultSearch.getDocSetHits().getHits(
					this.currentFetchPos + this.step);
			fetchCount = resultSearch.getFetchedDoc().length - currentFetchPos;
			if (fetchCount == 0)
				return;
			resultGroup
					.populate(resultSearch, currentFetchPos, this.fetchCount);
			currentFetchPos += fetchCount;
		} catch (IOException e) {
			this.ioException = e;
		} catch (ParseException e) {
			this.parseException = e;
		} catch (SyntaxError e) {
			this.syntaxError = e;
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

	public void search(int step, float rawScoreGoal) {
		this.step = step;
		this.fetchCount = 0;
		this.rawScoreGoal = rawScoreGoal;
		this.thread = new Thread(this);
		this.thread.start();
	}

	public int getFetchCount() {
		return this.fetchCount;
	}

	public void exception() throws IOException, ParseException, SyntaxError {
		if (this.ioException != null)
			throw this.ioException;
		if (this.parseException != null)
			throw this.parseException;
		if (this.syntaxError != null)
			throw this.syntaxError;
	}

}
