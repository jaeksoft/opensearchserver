/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2010 Emmanuel Keller / Jaeksoft
 * 
 * http://www.open-search-server.com
 * 
 * This file is part of OpenSearchServer.
 *
 * OpenSearchServer is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * OpenSearchServer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with OpenSearchServer. 
 *  If not, see <http://www.gnu.org/licenses/>.
 **/

package com.jaeksoft.searchlib.process;

import java.lang.Thread.State;
import java.util.Iterator;
import java.util.LinkedHashSet;

import com.jaeksoft.searchlib.Logging;
import com.jaeksoft.searchlib.config.Config;
import com.jaeksoft.searchlib.util.ReadWriteLock;

public abstract class ThreadMasterAbstract extends ThreadAbstract {

	final private ReadWriteLock rwl = new ReadWriteLock();

	private final LinkedHashSet<ThreadAbstract> threads;

	private volatile ThreadAbstract[] threadArray;

	protected ThreadMasterAbstract(Config config) {
		super(config, null);
		threadArray = null;
		threads = new LinkedHashSet<ThreadAbstract>();
	}

	public int getThreadsCount() {
		rwl.r.lock();
		try {
			return threads.size();
		} finally {
			rwl.r.unlock();
		}
	}

	protected void add(ThreadAbstract thread) {
		rwl.w.lock();
		try {
			threads.add(thread);
			threadArray = null;
			thread.execute();
		} finally {
			rwl.w.unlock();
		}
	}

	public void remove(ThreadAbstract thread) {
		rwl.w.lock();
		try {
			threads.remove(thread);
			threadArray = null;
		} finally {
			rwl.w.unlock();
		}
	}

	public ThreadAbstract[] getThreads() {
		rwl.r.lock();
		try {
			if (threadArray != null)
				return threadArray;
			threadArray = new ThreadAbstract[threads.size()];
			return threads.toArray(threadArray);
		} finally {
			rwl.r.unlock();
		}
	}

	@Override
	public void abort() {
		rwl.r.lock();
		try {
			for (ThreadAbstract thread : threads)
				thread.abort();
			super.abort();
		} finally {
			rwl.r.unlock();
		}
	}

	protected void waitForChild(int maxIdleTime) {
		while (getThreadsCount() > 0) {
			try {
				synchronized (this) {
					wait(5000);
				}
				// Remove terminated thread
				rwl.w.lock();
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
					rwl.w.unlock();
				}
			} catch (InterruptedException e) {
				Logging.warn(e.getMessage(), e);
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
