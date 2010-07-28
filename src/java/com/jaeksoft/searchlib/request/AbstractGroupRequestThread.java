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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.lucene.queryParser.ParseException;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.function.expression.SyntaxError;

public abstract class AbstractGroupRequestThread implements Runnable {

	final Lock lock = new ReentrantLock(true);
	final Condition notCompletion = lock.newCondition();
	private boolean completion;

	private IOException ioException;
	private URISyntaxException uriSyntaxException;
	private ParseException parseException;
	private SyntaxError syntaxError;
	private ClassNotFoundException classNotFound;
	private InterruptedException interruptedException;
	private SearchLibException searchLibException;
	private InstantiationException instantiationException;
	private IllegalAccessException illegalAccessException;

	protected AbstractGroupRequestThread() {
		ioException = null;
		uriSyntaxException = null;
		parseException = null;
		syntaxError = null;
		interruptedException = null;
		searchLibException = null;
		instantiationException = null;
		illegalAccessException = null;
		completion = false;
	}

	final protected void waitForCompletion(int timeOut)
			throws InterruptedException {
		lock.lock();
		try {
			while (!completion)
				if (!notCompletion.await(timeOut, TimeUnit.SECONDS))
					throw new InterruptedException("Running time out");
		} finally {
			lock.unlock();
		}
	}

	public abstract void runner() throws IOException, URISyntaxException,
			ParseException, SyntaxError, ClassNotFoundException,
			InterruptedException, SearchLibException, InstantiationException,
			IllegalAccessException;

	@Override
	final public void run() {
		lock.lock();
		try {
			runner();
			completion = true;
			notCompletion.signal();
		} catch (IOException e) {
			this.ioException = e;
		} catch (URISyntaxException e) {
			this.uriSyntaxException = e;
		} catch (ParseException e) {
			this.parseException = e;
		} catch (SyntaxError e) {
			this.syntaxError = e;
		} catch (ClassNotFoundException e) {
			this.classNotFound = e;
		} catch (InterruptedException e) {
			this.interruptedException = e;
		} catch (SearchLibException e) {
			this.searchLibException = e;
		} catch (InstantiationException e) {
			this.instantiationException = e;
		} catch (IllegalAccessException e) {
			this.illegalAccessException = e;
		} finally {
			lock.unlock();
		}
	}

	final public void start(ExecutorService threadPool) {
		lock.lock();
		try {
			threadPool.execute(this);
		} finally {
			lock.unlock();
		}
	}

	final public void exception() throws IOException, URISyntaxException,
			ParseException, SyntaxError, ClassNotFoundException,
			InterruptedException, SearchLibException, IllegalAccessException,
			InstantiationException {
		if (ioException != null)
			throw ioException;
		if (parseException != null)
			throw parseException;
		if (uriSyntaxException != null)
			throw uriSyntaxException;
		if (syntaxError != null)
			throw syntaxError;
		if (classNotFound != null)
			throw classNotFound;
		if (interruptedException != null)
			throw interruptedException;
		if (searchLibException != null)
			throw searchLibException;
		if (illegalAccessException != null)
			throw illegalAccessException;
		if (instantiationException != null)
			throw instantiationException;
	}
}
