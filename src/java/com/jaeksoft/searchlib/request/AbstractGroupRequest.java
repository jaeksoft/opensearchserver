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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.lucene.queryParser.ParseException;

import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.index.IndexAbstract;
import com.jaeksoft.searchlib.index.IndexGroup;

public abstract class AbstractGroupRequest<T extends AbstractGroupRequestThread> {

	protected IndexGroup indexGroup;

	private ExecutorService threadPool;

	private int timeOut;

	final private Lock lock = new ReentrantLock(true);

	protected AbstractGroupRequest(IndexGroup indexGroup,
			ExecutorService treadPool, int timeOut) {
		this.indexGroup = indexGroup;
		this.threadPool = treadPool;
		this.timeOut = timeOut;
	}

	protected abstract T getNewThread(IndexAbstract index)
			throws ParseException, SyntaxError, IOException;

	final private void run(List<T> threads) throws IOException,
			URISyntaxException, ParseException, SyntaxError,
			ClassNotFoundException, InterruptedException {
		for (T thread : threads)
			thread.start(threadPool);
		Iterator<T> iterator = threads.iterator();
		while (iterator.hasNext()) {
			T thread = iterator.next();
			thread.waitForCompletion(timeOut);
			thread.exception();
			if (complete(thread))
				iterator.remove();
		}
		complete();
	}

	/**
	 * Called when all thread execution completed
	 */
	protected abstract void complete() throws IOException, URISyntaxException,
			ParseException, SyntaxError;

	/**
	 * Call each time a thread execution complete
	 * 
	 * @param thread
	 */
	protected abstract boolean complete(T thread);

	final private List<T> initThreads() throws ParseException, SyntaxError,
			IOException {
		List<T> threads = new ArrayList<T>(indexGroup.size());

		for (IndexAbstract index : indexGroup.getIndices())
			threads.add(getNewThread(index));
		return threads;
	}

	final protected void loop(int maxIteration) throws IOException,
			URISyntaxException, ParseException, SyntaxError,
			ClassNotFoundException, InterruptedException {
		lock.lock();
		try {

			List<T> threads = initThreads();
			while (threads.size() > 0) {
				if (maxIteration-- == 0)
					throw new InterruptedException("Maximum iteration reachs ("
							+ maxIteration + ')');
				run(threads);
			}

		} finally {
			lock.unlock();
		}
	}

	final protected void run() throws ParseException, SyntaxError, IOException,
			URISyntaxException, ClassNotFoundException, InterruptedException {
		lock.lock();
		try {
			List<T> threads = initThreads();
			run(threads);
		} finally {
			lock.unlock();
		}
	}

}
