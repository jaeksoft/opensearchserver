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
import java.util.concurrent.ExecutorService;

import org.apache.lucene.queryParser.ParseException;

import com.jaeksoft.searchlib.function.expression.SyntaxError;

public abstract class AbstractGroupRequestThread implements Runnable {

	private volatile boolean running;

	private IOException ioException;
	private URISyntaxException uriSyntaxException;
	private ParseException parseException;
	private SyntaxError syntaxError;
	private ClassNotFoundException classNotFound;

	protected AbstractGroupRequestThread() {
		running = false;
		ioException = null;
		uriSyntaxException = null;
		parseException = null;
		syntaxError = null;
	}

	final public void waitForCompletion() {
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

	public abstract void runner() throws IOException, URISyntaxException,
			ParseException, SyntaxError, ClassNotFoundException;

	public abstract boolean done();

	final public void run() {
		try {
			runner();
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
		} finally {
			running = false;
			synchronized (this) {
				notifyAll();
			}
		}
	}

	final public void start(ExecutorService threadPool) {
		running = true;
		threadPool.execute(this);
	}

	final public void exception() throws IOException, URISyntaxException,
			ParseException, SyntaxError, ClassNotFoundException {
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
	}
}
