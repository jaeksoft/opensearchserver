/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2010 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.process;

import java.lang.Thread.State;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.jaeksoft.searchlib.config.Config;

public abstract class ThreadMasterAbstract extends ThreadAbstract {

	private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock(true);
	protected final Lock r = rwl.readLock();
	protected final Lock w = rwl.writeLock();

	private final LinkedHashSet<ThreadAbstract> threads;

	private ThreadAbstract[] threadArray;

	protected ThreadMasterAbstract(Config config) {
		super(config, null);
		threadArray = null;
		threads = new LinkedHashSet<ThreadAbstract>();
	}

	public int getThreadsCount() {
		r.lock();
		try {
			return threads.size();
		} finally {
			r.unlock();
		}
	}

	protected void add(ThreadAbstract thread) {
		w.lock();
		try {
			threads.add(thread);
			threadArray = null;
			thread.execute();
		} finally {
			w.unlock();
		}
	}

	public void remove(ThreadAbstract thread) {
		w.lock();
		try {
			threads.remove(thread);
			threadArray = null;
		} finally {
			w.unlock();
		}
	}

	public ThreadAbstract[] getThreads() {
		r.lock();
		try {
			if (threadArray != null)
				return threadArray;
			threadArray = new ThreadAbstract[threads.size()];
			return threads.toArray(threadArray);
		} finally {
			r.unlock();
		}
	}

	@Override
	public void abort() {
		r.lock();
		try {
			for (ThreadAbstract thread : threads)
				thread.abort();
			super.abort();
		} finally {
			r.unlock();
		}
	}

	protected void waitForChild(int maxIdleTime) {
		while (getThreadsCount() > 0) {
			try {
				synchronized (this) {
					wait(5000);
				}
				// Remove terminated thread
				w.lock();
				try {
					synchronized (threads) {
						boolean remove = false;
						Iterator<ThreadAbstract> it = threads.iterator();
						while (it.hasNext()) {
							ThreadAbstract thread = it.next();
							if (thread.getThreadState() == State.TERMINATED) {
								it.remove();
								remove = true;
							} else if (thread.isIdleTimeExhausted(maxIdleTime)) {
								thread.abort();
								it.remove();
								remove = true;
							}
						}
						if (remove)
							threadArray = null;
					}
				} finally {
					w.unlock();
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			sleepSec(1);
		}
	}

	@Override
	public void runner() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void release() {
		// TODO Auto-generated method stub

	}

}
