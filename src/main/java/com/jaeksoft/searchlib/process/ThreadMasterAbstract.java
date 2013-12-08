/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2010-2013 Emmanuel Keller / Jaeksoft
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
import java.util.TreeMap;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.Logging;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.config.Config;
import com.jaeksoft.searchlib.util.InfoCallback;
import com.jaeksoft.searchlib.util.ReadWriteLock;
import com.jaeksoft.searchlib.util.ThreadUtils;
import com.jaeksoft.searchlib.util.Variables;

public abstract class ThreadMasterAbstract<M extends ThreadMasterAbstract<M, T>, T extends ThreadAbstract<T>>
		extends ThreadAbstract<M> {

	final private ReadWriteLock rwl = new ReadWriteLock();

	private final LinkedHashSet<T> threads;

	private final TreeMap<ThreadItem<?, T>, T> threadMap;

	private volatile T[] threadArray;

	protected ThreadMasterAbstract(Config config) {
		super(config, null, null);
		threadArray = null;
		threads = new LinkedHashSet<T>();
		threadMap = new TreeMap<ThreadItem<?, T>, T>();
	}

	public int getThreadsCount() {
		rwl.r.lock();
		try {
			return threads.size();
		} finally {
			rwl.r.unlock();
		}
	}

	protected void add(T thread) {
		rwl.w.lock();
		try {
			threads.add(thread);
			threadArray = null;
			thread.execute();
		} finally {
			rwl.w.unlock();
		}
	}

	public boolean isThread(ThreadItem<?, T> crawlItem) {
		rwl.r.lock();
		try {
			return threadMap.containsKey(crawlItem);
		} finally {
			rwl.r.unlock();
		}
	}

	public T getCrawlThread(ThreadItem<?, T> crawlItem) {
		rwl.r.lock();
		try {
			return threadMap.get(crawlItem);
		} finally {
			rwl.r.unlock();
		}
	}

	protected void remove(T thread) {
		rwl.w.lock();
		try {
			ThreadItem<?, T> uti = thread.getThreadItem();
			if (uti != null)
				threadMap.remove(uti);
			threads.remove(thread);
			threadArray = null;
		} finally {
			rwl.w.unlock();
		}
	}

	protected abstract T[] getNewArray(int size);

	public T[] getThreads() {
		rwl.r.lock();
		try {
			if (threadArray != null)
				return threadArray;
			threadArray = getNewArray(threads.size());
			return threads.toArray(threadArray);
		} finally {
			rwl.r.unlock();
		}
	}

	@Override
	public void abort() {
		rwl.r.lock();
		try {
			super.abort();
			for (T thread : threads)
				thread.abort();
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
						Iterator<T> it = threads.iterator();
						while (it.hasNext()) {
							T thread = it.next();
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
			ThreadUtils.sleepMs(1000);
		}
	}

	public String getChildProcessInfo() {
		rwl.r.lock();
		try {
			StringBuilder sb = new StringBuilder();
			int l = threadMap.size();
			switch (l) {
			case 0:
				sb.append("Not running");
				break;
			case 1:
				sb.append("1 process");
				break;
			default:
				sb.append(l);
				sb.append(" processes");
				break;
			}
			return sb.toString();
		} finally {
			rwl.r.unlock();
		}
	}

	public T execute(Client client, ThreadItem<?, T> threadItem,
			boolean bWaitForCompletion, Variables variables,
			InfoCallback infoCallback) throws InterruptedException,
			SearchLibException {
		T crawlThread = null;
		rwl.w.lock();
		try {
			if (threadItem != null) {
				if (threadMap.containsKey(threadItem)) {
					throw new SearchLibException("The job "
							+ threadItem.toString() + " is already running");
				}
			}
			crawlThread = getNewThread(client, threadItem, variables,
					infoCallback);
			if (threadItem != null) {
				threadMap.put(threadItem, crawlThread);
				threadItem.setLastThread(crawlThread);
			}
			add(crawlThread);
		} finally {
			rwl.w.unlock();
		}
		if (crawlThread == null)
			return null;
		crawlThread.waitForStart(600);
		if (bWaitForCompletion)
			crawlThread.waitForEnd(0);
		return crawlThread;
	}

	protected T getNewThread(Client client, ThreadItem<?, T> threadItem,
			Variables variables, InfoCallback infoCallback)
			throws SearchLibException {
		throw new SearchLibException("Not implemented");
	}

	@Override
	public void runner() throws Exception {
		throw new SearchLibException("Not implemented");
	}

	@Override
	public void release() {
	}

}
