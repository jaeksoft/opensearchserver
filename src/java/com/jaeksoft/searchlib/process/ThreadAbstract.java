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
import java.util.Date;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.jaeksoft.searchlib.config.Config;

public abstract class ThreadAbstract implements Runnable {

	private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock(true);
	protected final Lock r = rwl.readLock();
	protected final Lock w = rwl.writeLock();

	private Config config;

	private ThreadMasterAbstract threadMaster;

	private String info;

	private volatile boolean abort;

	private Thread thread;

	private volatile boolean running;

	private Date startTime;

	private long idleTime;

	private Exception exception;

	protected ThreadAbstract(Config config, ThreadMasterAbstract threadMaster) {
		this.config = config;
		this.threadMaster = threadMaster;
		exception = null;
		abort = false;
		thread = null;
		info = null;
		running = false;
		idleTime = 0;
	}

	public Config getConfig() {
		r.lock();
		try {
			return config;
		} finally {
			r.unlock();
		}
	}

	public String getInfo() {
		r.lock();
		try {
			return info;
		} finally {
			r.unlock();
		}
	}

	protected void setInfo(String info) {
		w.lock();
		try {
			this.info = info;
		} finally {
			w.unlock();
		}
	}

	public boolean isAborted() {
		r.lock();
		try {
			return abort;
		} finally {
			r.unlock();
		}
	}

	public void abort() {
		w.lock();
		try {
			abort = true;
		} finally {
			w.unlock();
		}
	}

	public Exception getException() {
		r.lock();
		try {
			return exception;
		} finally {
			r.unlock();
		}
	}

	protected void setException(Exception e) {
		w.lock();
		try {
			this.exception = e;
		} finally {
			w.unlock();
		}
	}

	public boolean isAborting() {
		return isRunning() && isAborted();

	}

	public void waitForEnd() {
		while (isRunning())
			sleepMs(100);
	}

	protected void sleepMs(long ms) {
		try {
			Thread.sleep(ms);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	protected void sleepSec(int sec) {
		if (sec == 0)
			return;
		long finalTime = System.currentTimeMillis() + sec * 1000;
		while (System.currentTimeMillis() < finalTime)
			sleepMs(500);
	}

	protected void idle() {
		w.lock();
		try {
			idleTime = System.currentTimeMillis();
		} finally {
			w.unlock();
		}
	}

	protected boolean isIdleTimeExhausted(int seconds) {
		r.lock();
		try {
			if (seconds == 0)
				return false;
			long timeElapsed = (System.currentTimeMillis() - idleTime) / 1000;
			return timeElapsed > seconds;
		} finally {
			r.unlock();
		}

	}

	public String getCurrentMethod() {
		r.lock();
		try {
			if (thread == null)
				return "No thread";
			StackTraceElement[] ste = thread.getStackTrace();
			if (ste == null)
				return "No stack";
			if (ste.length == 0)
				return "Empty stack";
			StackTraceElement element = ste[0];
			for (StackTraceElement e : ste) {
				if (e.getClassName().contains("jaeksoft")) {
					element = e;
					break;
				}
			}
			return element.getClassName() + '.' + element.getMethodName()
					+ " (" + element.getLineNumber() + ")";
		} finally {
			r.unlock();
		}
	}

	public abstract void runner() throws Exception;

	public abstract void release();

	@Override
	final public void run() {
		w.lock();
		try {
			startTime = new Date();
			idleTime = System.currentTimeMillis();
			abort = false;
			thread = Thread.currentThread();
		} finally {
			w.unlock();
		}
		try {
			runner();
		} catch (Exception e) {
			setException(e);
			setInfo(e.getMessage());
		}
		if (threadMaster != null) {
			threadMaster.remove(this);
			synchronized (threadMaster) {
				threadMaster.notify();
			}
		}
		w.lock();
		try {
			thread = null;
			running = false;
		} finally {
			w.unlock();
		}
		release();
	}

	public boolean isRunning() {
		r.lock();
		try {
			return running;
		} finally {
			r.unlock();
		}
	}

	public State getThreadState() {
		r.lock();
		try {
			if (thread == null)
				return null;
			return thread.getState();
		} finally {
			r.unlock();
		}
	}

	public String getThreadStatus() {
		r.lock();
		try {
			StringBuffer sb = new StringBuffer();
			sb.append(this.hashCode());
			if (thread == null)
				return sb.toString();
			sb.append('/');
			sb.append(thread.hashCode());
			sb.append(' ');
			sb.append(thread.getState().toString());
			return sb.toString();
		} finally {
			r.unlock();
		}
	}

	final protected void execute() {
		w.lock();
		try {
			running = true;
			config.getThreadPool().execute(this);
		} finally {
			w.unlock();
		}
	}

	protected ThreadMasterAbstract getThreadMaster() {
		r.lock();
		try {
			return threadMaster;
		} finally {
			r.unlock();
		}
	}

	/**
	 * @return the startTime
	 */
	public Date getStartTime() {
		r.lock();
		try {
			return startTime;
		} finally {
			r.unlock();
		}
	}

	public long returnElapsedTime() {
		r.lock();
		try {
			return System.currentTimeMillis() - startTime.getTime();
		} finally {
			r.unlock();
		}
	}

}
